package com.ecalar.listaviva.domain.model

import com.google.gson.annotations.SerializedName

data class OpenFoodFactsResponse(
    @SerializedName("status") val status: Int, // 1 si se encuentra, 0 si no
    @SerializedName("product") val product: OffProduct?
)

data class OffProduct(
    @SerializedName("image_front_url") val imageFrontUrl: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("quantity") val quantity: String?,
    @SerializedName("categories") val categories: String?
)