package com.ecalar.listaviva.data.catalog

data class CatalogCategory(
    val name: String,
    val icon: String, // Nombre del Material Icon (lo mapearemos en UI)
    val subcategories: List<CatalogSubcategory>
)

data class CatalogSubcategory(
    val name: String,
    val products: List<String>
)

val catalogData = listOf(
    CatalogCategory(
        name = "Lácteos",
        icon = "EggAlt",
        subcategories = listOf(
            CatalogSubcategory("Leche", listOf("Leche entera", "Leche semidesnatada", "Leche desnatada", "Leche sin lactosa", "Leche de avena", "Leche de soja")),
            CatalogSubcategory("Yogures", listOf("Yogur natural", "Yogur griego", "Yogur de sabores", "Yogur líquido")),
            CatalogSubcategory("Quesos", listOf("Queso fresco", "Queso curado", "Queso semicurado", "Queso rallado", "Queso en lonchas", "Mozzarella", "Queso crema")),
            CatalogSubcategory("Mantequilla y margarina", listOf("Mantequilla", "Margarina")),
            CatalogSubcategory("Nata y crema", listOf("Nata para cocinar", "Nata para montar"))
        )
    ),
    CatalogCategory(
        name = "Carnes",
        icon = "Restaurant",
        subcategories = listOf(
            CatalogSubcategory("Pollo", listOf("Pechuga de pollo", "Muslos de pollo", "Alas de pollo", "Pollo entero")),
            CatalogSubcategory("Cerdo", listOf("Filetes de cerdo", "Chuletas de cerdo", "Lomo de cerdo", "Costillas de cerdo", "Panceta")),
            CatalogSubcategory("Ternera", listOf("Filetes de ternera", "Carne picada de ternera", "Chuletón de ternera")),
            CatalogSubcategory("Cordero", listOf("Pierna de cordero", "Chuletas de cordero")),
            CatalogSubcategory("Embutidos", listOf("Jamón serrano", "Jamón cocido", "Chorizo", "Salami", "Salchichón", "Mortadela", "Pavo en lonchas")),
            CatalogSubcategory("Salchichas", listOf("Salchichas Frankfurt", "Salchichas frescas"))
        )
    ),
    CatalogCategory(
        name = "Pescados",
        icon = "SetMeal",
        subcategories = listOf(
            CatalogSubcategory("Pescado fresco", listOf("Merluza", "Salmón", "Dorada", "Lubina", "Trucha", "Atún fresco", "Sardinas", "Boquerones")),
            CatalogSubcategory("Pescado congelado", listOf("Filetes de merluza congelados", "Salmón congelado", "Varitas de merluza")),
            CatalogSubcategory("Marisco", listOf("Gambas", "Langostinos", "Mejillones", "Almejas", "Calamares", "Pulpo")),
            CatalogSubcategory("Conservas", listOf("Atún en lata", "Sardinas en lata", "Mejillones en escabeche", "Berberechos"))
        )
    ),
    CatalogCategory(
        name = "Pastas y Arroces",
        icon = "RamenDining",
        subcategories = listOf(
            CatalogSubcategory("Pasta", listOf("Spaghetti", "Macarrones", "Fusilli", "Penne", "Tallarines", "Lasaña", "Canelones")),
            CatalogSubcategory("Arroz", listOf("Arroz redondo", "Arroz largo", "Arroz basmati", "Arroz integral")),
            CatalogSubcategory("Fideos", listOf("Fideos finos", "Fideos gruesos", "Noodles"))
        )
    ),
    CatalogCategory(
        name = "Verduras y Hortalizas",
        icon = "Eco",
        subcategories = listOf(
            CatalogSubcategory("Verduras frescas", listOf("Tomates", "Lechuga", "Cebolla", "Pimiento verde", "Pimiento rojo", "Zanahorias", "Calabacín", "Berenjena", "Pepino", "Ajo", "Champiñones", "Setas")),
            CatalogSubcategory("Patatas", listOf("Patatas blancas", "Patatas rojas")),
            CatalogSubcategory("Verduras congeladas", listOf("Guisantes congelados", "Judías verdes congeladas", "Espinacas congeladas", "Menestra congelada"))
        )
    ),
    CatalogCategory(
        name = "Frutas",
        icon = "Nutrition",
        subcategories = listOf(
            CatalogSubcategory("Frutas frescas", listOf("Manzanas", "Plátanos", "Naranjas", "Peras", "Fresas", "Uvas", "Melocotones", "Kiwi", "Sandía", "Melón", "Piña", "Aguacate", "Limones")),
            CatalogSubcategory("Frutos secos", listOf("Almendras", "Nueces", "Avellanas", "Pistachos", "Cacahuetes", "Pipas"))
        )
    ),
    CatalogCategory(
        name = "Bebidas",
        icon = "LocalDrink",
        subcategories = listOf(
            CatalogSubcategory("Refrescos", listOf("Coca-Cola", "Fanta", "Sprite", "Tónica")),
            CatalogSubcategory("Zumos", listOf("Zumo de naranja", "Zumo de piña", "Zumo de melocotón")),
            CatalogSubcategory("Agua", listOf("Agua mineral", "Agua con gas")),
            CatalogSubcategory("Cerveza", listOf("Cerveza rubia", "Cerveza tostada", "Cerveza sin alcohol")),
            CatalogSubcategory("Vino", listOf("Vino tinto", "Vino blanco", "Vino rosado", "Cava")),
            CatalogSubcategory("Café e infusiones", listOf("Café molido", "Café en cápsulas", "Té", "Manzanilla", "Infusiones"))
        )
    ),
    CatalogCategory(
        name = "Limpieza",
        icon = "Mop",
        subcategories = listOf(
            CatalogSubcategory("Cocina", listOf("Lavavajillas", "Detergente lavaplatos", "Bayetas", "Estropajos")),
            CatalogSubcategory("Baño", listOf("Papel higiénico", "Gel de baño", "Champú", "Acondicionador", "Pasta de dientes")),
            CatalogSubcategory("Hogar", listOf("Detergente ropa", "Suavizante", "Limpiador multiusos", "Limpiacristales", "Lejía", "Amoníaco", "Bolsas de basura"))
        )
    ),
    CatalogCategory(
        name = "Bazar",
        icon = "Store",
        subcategories = listOf(
            CatalogSubcategory("Herramientas", listOf("Destornillador", "Martillo", "Llave inglesa", "Cinta métrica")),
            CatalogSubcategory("Pilas", listOf("Pilas AA", "Pilas AAA", "Pilas de botón")),
            CatalogSubcategory("Oficina", listOf("Folios", "Bolígrafos", "Cinta adhesiva", "Tijeras"))
        )
    ),
    CatalogCategory(
        name = "Otros",
        icon = "MoreHoriz",
        subcategories = listOf(
            CatalogSubcategory("Varios", listOf())
        )
    )
)

// Lista plana de todos los productos para búsqueda rápida
val allProducts: List<String> = catalogData
    .flatMap { category ->
        category.subcategories.flatMap { it.products }
    }
    .distinct()
    .sorted()
