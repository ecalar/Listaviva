package com.ecalar.listaviva.ui.despensa

import android.content.Context
import com.ecalar.listaviva.domain.model.ItemLista
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.data.remote.OpenFoodFactsApi
import com.ecalar.listaviva.domain.model.EstadoProducto
import com.ecalar.listaviva.domain.model.ListaCompra
import com.ecalar.listaviva.domain.model.ProductoCatalogo
import com.ecalar.listaviva.domain.model.ProductoDespensa
import com.ecalar.listaviva.domain.repository.CatalogoRepository
import com.ecalar.listaviva.domain.repository.DespensaRepository
import com.ecalar.listaviva.domain.repository.ListaCompraRepository
import com.ecalar.listaviva.domain.repository.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

sealed class DespensaState {
    object Loading : DespensaState()
    data class Success(
        val productosTotales: List<ProductoDespensa>,
        val productosFiltrados: List<ProductoDespensa>,
        val categorias: List<String>
    ) : DespensaState()
    data class Error(val message: String) : DespensaState()
}

@HiltViewModel
class DespensaViewModel @Inject constructor(
    private val despensaRepository: DespensaRepository,
    private val listaCompraRepository: ListaCompraRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val catalogoRepository: CatalogoRepository,
    private val firestore: FirebaseFirestore,
    private val api: OpenFoodFactsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<DespensaState>(DespensaState.Loading)
    val uiState: StateFlow<DespensaState> = _uiState.asStateFlow()

    private val _listasDisponibles = MutableStateFlow<List<ListaCompra>>(emptyList())
    val listasDisponibles: StateFlow<List<ListaCompra>> = _listasDisponibles.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow("Todos")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    init {
        cargarDatos()
    }



    private fun cargarDatos() {
        val familiaId = preferencesRepository.getFamiliaId()
        if (familiaId == null) {
            _uiState.value = DespensaState.Error("No se encontró el grupo familiar")
            return
        }

        viewModelScope.launch {
            despensaRepository.getProductosDespensa(familiaId).collect { result ->
                result.onSuccess { productos ->
                    actualizarEstadoConFiltro(productos, _categoriaSeleccionada.value)
                }.onFailure { e ->
                    _uiState.value = DespensaState.Error(e.message ?: "Error al sincronizar")
                }
            }
        }

        viewModelScope.launch {
            listaCompraRepository.getListas(familiaId).collect { result ->
                result.onSuccess { listas -> _listasDisponibles.value = listas }
            }
        }
    }

    fun seleccionarCategoria(categoria: String) {
        _categoriaSeleccionada.value = categoria
        val currentState = _uiState.value
        if (currentState is DespensaState.Success) {
            actualizarEstadoConFiltro(currentState.productosTotales, categoria)
        }
    }

    private fun actualizarEstadoConFiltro(productos: List<ProductoDespensa>, categoria: String) {
        val categoriasDinamicas =
            listOf("Todos") + productos.map { it.categoria }.distinct().filter { it.isNotBlank() }
                .sorted()

        val filtrados = if (categoria == "Todos") {
            productos
        } else {
            productos.filter { it.categoria.equals(categoria, ignoreCase = true) }
        }

        _uiState.value = DespensaState.Success(
            productosTotales = productos,
            productosFiltrados = filtrados,
            categorias = categoriasDinamicas
        )
    }

    fun cambiarEstadoProducto(productoId: String, nuevoEstado: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch {
            despensaRepository.updateEstadoProducto(familiaId, productoId, nuevoEstado)
        }
    }

    fun agotarYAñadirALista(producto: ProductoDespensa, listaId: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val alias = preferencesRepository.getAlias() ?: "Desconocido"

        viewModelScope.launch {
            despensaRepository.actualizarStock(
                familiaId,
                producto.id,
                cantidadActual = 0,
                cantidadReferencia = producto.cantidadReferencia,
                estado = EstadoProducto.AGOTADO.name.lowercase()
            )

            val cantidadAComprar = 1

            val nuevoItem = ItemLista(
                nombre = producto.nombre,
                despensaProductoId = producto.id,
                cantidad = producto.formato,
                cantidadAComprar = cantidadAComprar,
                comprado = false,
                aliasAñadidoPor = alias,
                fechaAñadido = Date(),
                imageUrl = producto.imageUrl
            )
            listaCompraRepository.addItemToLista(familiaId, listaId, nuevoItem)
        }
    }

    fun comprobarEInicializarCatalogo(context: Context) {
        viewModelScope.launch {
            catalogoRepository.inicializarCatalogo(context)
        }
    }

    fun eliminarProducto(productoId: String) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        viewModelScope.launch {
            despensaRepository.deleteProducto(familiaId, productoId)
        }
    }

    private fun calcularEstado(actual: Int, referencia: Int): String {
        if (referencia <= 0 || actual <= 0) return EstadoProducto.AGOTADO.name.lowercase()
        val porcentaje = (actual.toFloat() / referencia.toFloat()) * 100

        return when {
            porcentaje >= 60f -> EstadoProducto.COMPLETO.name.lowercase()
            porcentaje >= 30f -> EstadoProducto.MITAD.name.lowercase()
            else -> EstadoProducto.CASI_AGOTADO.name.lowercase()
        }
    }

    fun cambiarCantidad(producto: ProductoDespensa, incremento: Int) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return

        val nuevaCantidad = (producto.cantidadActual + incremento).coerceAtLeast(0)


        val nuevaReferencia = if (incremento > 0) {
            nuevaCantidad
        } else {
            producto.cantidadReferencia
        }

        val nuevoEstado = calcularEstado(nuevaCantidad, nuevaReferencia)

        viewModelScope.launch {
            despensaRepository.actualizarStock(
                familiaId = familiaId,
                productoId = producto.id,
                cantidadActual = nuevaCantidad,
                cantidadReferencia = nuevaReferencia,
                estado = nuevoEstado
            )
        }
    }

    private fun añadirProductoADespensa(productoCatalogo: ProductoCatalogo) {
        val familiaId = preferencesRepository.getFamiliaId() ?: return
        val alias = preferencesRepository.getAlias() ?: "Desconocido"

        // Creamos el objeto listo para tu despensa
        val nuevoProducto = ProductoDespensa(
            id = "",
            nombre = productoCatalogo.nombre,
            categoria = productoCatalogo.categoria,
            formato = "1 ud",
            cantidadActual = 1,
            cantidadReferencia = 1,
            estado = EstadoProducto.COMPLETO.name.lowercase(),
            añadidoPor = alias,
            imageUrl = productoCatalogo.imageUrl
        )

        viewModelScope.launch {
            despensaRepository.addProducto(familiaId, nuevoProducto)
        }
    }

    // Función auxiliar para buscar en Firestore
    private suspend fun buscarProductoEnCatalogoGlobal(codigo: String): ProductoCatalogo? {
        return try {
            val querySnapshot = firestore.collection("productosCatalogo")
                .whereEqualTo("codigoBarras", codigo)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) null
            else querySnapshot.documents.first().toObject(ProductoCatalogo::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun procesarCodigoBarras(
        codigo: String,
        onSuccess: (String) -> Unit,
        onProductoNoEncontrado: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. ¿Está en nuestro catálogo local unificado?
                val productoEncontrado = buscarProductoEnCatalogoGlobal(codigo)
                if (productoEncontrado != null) {
                    añadirProductoADespensa(productoEncontrado)
                    onSuccess(productoEncontrado.nombre)
                    return@launch
                }

                // 2. ¿Está en Open Food Facts? (CON TIMEOUT DE 5 SEGUNDOS)
                val respuesta = withTimeoutOrNull(5000L) {
                    api.getProductByBarcode(codigo)
                }

                // 3. Procesamos si hay respuesta y todo fue bien
                if (respuesta != null && respuesta.status == 1 && respuesta.product != null) {

                    // A) Extraemos nombre y marca
                    val nombreCrudo = respuesta.product.productName ?: "Producto"
                    val marca = respuesta.product.brands ?: ""
                    val nombreCompleto = if (marca.isNotBlank()) "$nombreCrudo $marca" else nombreCrudo

                    // B) Sanitizamos (Title Case y límite de 40 caracteres)
                    val nombreLimpio = nombreCompleto.toTitleCase().take(40)

                    // C) Protegemos nuestras categorías
                    val categoriaLimpia = mapearCategoria(respuesta.product.categories)

                    // D) Construimos el modelo CONFIABLE
                    val nuevoProductoCatalogo = ProductoCatalogo(
                        id = codigo,
                        nombre = nombreLimpio,
                        categoria = categoriaLimpia,
                        codigoBarras = codigo,
                        imageUrl = respuesta.product.imageUrl ?: "",
                        verificado = true
                    )

                    // Guardamos y añadimos
                    firestore.collection("productosCatalogo").document(codigo).set(nuevoProductoCatalogo)
                    añadirProductoADespensa(nuevoProductoCatalogo)

                    onSuccess(nombreLimpio)
                } else {
                    // Si ha tardado más de 5 segundos o la API no lo tiene
                    onProductoNoEncontrado(codigo)
                }
            } catch (e: Exception) {
                // Caída de red o error inesperado
                onProductoNoEncontrado(codigo)
            }
        }
    }

    // Función privada para "limpiar" la categoría de la API y adaptarla a nuestro catálogo
    private fun mapearCategoria(categoriaApi: String?): String {
        if (categoriaApi == null) return "Otros"

        val cat = categoriaApi.lowercase()

        return when {
            // 1. Bebidas
            cat.contains("bebida") || cat.contains("beverage") || cat.contains("drink") || cat.contains("agua") || cat.contains("water") || cat.contains("zumo") || cat.contains("juice") || cat.contains("refresco") || cat.contains("soda") || cat.contains("cerveza") || cat.contains("beer") || cat.contains("vino") || cat.contains("wine") -> "Bebidas"

            // 2. Lácteos y Huevos (Ojo, el queso lo mandamos a Charcutería según tu catálogo, así que aquí va leche, yogur y huevos)
            cat.contains("leche") || cat.contains("milk") || cat.contains("yogur") || cat.contains("yogurt") || cat.contains("huevo") || cat.contains("egg") || cat.contains("mantequilla") || cat.contains("butter") || cat.contains("dairy") || cat.contains("lácteo") -> "Lácteos y Huevos"

            // 3. Charcutería y Quesos (Según tu catálogo, los quesos van aquí)
            cat.contains("queso") || cat.contains("cheese") || cat.contains("jamón") || cat.contains("ham") || cat.contains("embutido") || cat.contains("chorizo") || cat.contains("salchichón") || cat.contains("bacon") || cat.contains("charcuterie") -> "Charcutería y Quesos"

            // 4. Carnicería
            cat.contains("carne") || cat.contains("meat") || cat.contains("pollo") || cat.contains("chicken") || cat.contains("ternera") || cat.contains("beef") || cat.contains("cerdo") || cat.contains("pork") || cat.contains("pavo") || cat.contains("turkey") -> "Carnicería"

            // 5. Pescadería
            cat.contains("pescado") || cat.contains("fish") || cat.contains("salmón") || cat.contains("salmon") || cat.contains("marisco") || cat.contains("seafood") || cat.contains("atún fresco") -> "Pescadería"

            // 6. Frutería y Verdulería
            cat.contains("fruta") || cat.contains("fruit") || cat.contains("verdura") || cat.contains("vegetable") || cat.contains("plant-based") || cat.contains("ensalada") || cat.contains("salad") -> "Frutería y Verdulería"

            // 7. Panadería y Bollería
            cat.contains("pan") || cat.contains("bread") || cat.contains("bollería") || cat.contains("pastry") || cat.contains("croissant") -> "Panadería y Bollería"

            // 8. Desayuno y Dulces
            cat.contains("galleta") || cat.contains("biscuit") || cat.contains("cookie") || cat.contains("cereal") || cat.contains("desayuno") || cat.contains("breakfast") || cat.contains("café") || cat.contains("coffee") || cat.contains("té") || cat.contains("tea") || cat.contains("mermelada") || cat.contains("jam") || cat.contains("cacao") || cat.contains("chocolate") || cat.contains("dulce") || cat.contains("sweet") -> "Desayuno y Dulces"

            // 9. Snacks y Aperitivos
            cat.contains("snack") || cat.contains("patatas fritas") || cat.contains("chips") || cat.contains("crisp") || cat.contains("fruto seco") || cat.contains("nut") || cat.contains("pipas") -> "Snacks y Aperitivos"

            // 10. Congelados
            cat.contains("congelado") || cat.contains("frozen") || cat.contains("ice cream") || cat.contains("helado") -> "Congelados"

            // 11. Conservas
            cat.contains("conserva") || cat.contains("canned") || cat.contains("lata") -> "Conservas"

            // 12. Salsas y Condimentos
            cat.contains("salsa") || cat.contains("sauce") || cat.contains("condiment") || cat.contains("kétchup") || cat.contains("mayonesa") || cat.contains("mayo") || cat.contains("mostaza") || cat.contains("mustard") || cat.contains("dressing") -> "Salsas y Condimentos"

            // 13. Platos Preparados
            cat.contains("plato preparado") || cat.contains("ready meal") || cat.contains("meal") || cat.contains("pizza") || cat.contains("hummus") || cat.contains("guacamole") -> "Platos Preparados"

            // 14. Sopas y Cremas
            cat.contains("sopa") || cat.contains("soup") || cat.contains("crema") || cat.contains("broth") || cat.contains("caldo") || cat.contains("gazpacho") -> "Sopas y Cremas"

            // 15. Especias y Hierbas
            cat.contains("especia") || cat.contains("spice") || cat.contains("hierba") || cat.contains("herb") || cat.contains("pimienta") || cat.contains("pepper") || cat.contains("sal") || cat.contains("salt") || cat.contains("orégano") -> "Especias y Hierbas"

            // 16. Repostería
            cat.contains("repostería") || cat.contains("baking") || cat.contains("levadura") || cat.contains("yeast") || cat.contains("colorante") -> "Repostería"

            // 17. Higiene Personal
            cat.contains("higiene") || cat.contains("hygiene") || cat.contains("champú") || cat.contains("shampoo") || cat.contains("gel") || cat.contains("jabón") || cat.contains("soap") || cat.contains("desodorante") || cat.contains("deodorant") || cat.contains("cosmetic") || cat.contains("dental") -> "Higiene Personal"

            // 18. Limpieza del Hogar
            cat.contains("limpieza") || cat.contains("cleaning") || cat.contains("detergente") || cat.contains("detergent") || cat.contains("suavizante") || cat.contains("lejía") -> "Limpieza del Hogar"

            // 19. Hogar y Ferretería
            cat.contains("hogar") || cat.contains("home") || cat.contains("ferretería") || cat.contains("hardware") || cat.contains("pila") || cat.contains("battery") -> "Hogar y Ferretería"

            // 20. Mascotas
            cat.contains("mascota") || cat.contains("pet") || cat.contains("perro") || cat.contains("dog") || cat.contains("gato") || cat.contains("cat") || cat.contains("pienso") -> "Mascotas"

            // 21. Bebés
            cat.contains("bebé") || cat.contains("baby") || cat.contains("infant") || cat.contains("pañal") || cat.contains("diaper") || cat.contains("potito") -> "Bebés"

            // 22. Despensa (Filtro base para esenciales que no cayeron en lo anterior)
            cat.contains("aceite") || cat.contains("oil") || cat.contains("vinagre") || cat.contains("vinegar") || cat.contains("arroz") || cat.contains("rice") || cat.contains("pasta") || cat.contains("harina") || cat.contains("flour") || cat.contains("azúcar") || cat.contains("sugar") || cat.contains("legumbre") || cat.contains("lenteja") || cat.contains("garbanzo") || cat.contains("grocery") -> "Despensa"

            // Si la API nos devuelve algo súper raro que no contemplamos
            else -> "Otros"
        }
    }

    private fun String.toTitleCase(): String {
        return this.lowercase().split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
    }
}
