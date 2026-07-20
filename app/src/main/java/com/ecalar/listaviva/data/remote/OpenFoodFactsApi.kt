package com.ecalar.listaviva.data.remote

import com.ecalar.listaviva.domain.model.OpenFoodFactsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    // La URL base es "https://world.openfoodfacts.org/"
    // Aquí le decimos qué ruta exacta debe consultar pasándole el código de barras
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsResponse
}