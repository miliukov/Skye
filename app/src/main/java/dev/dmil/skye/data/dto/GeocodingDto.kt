package dev.dmil.skye.data.dto

import com.google.gson.annotations.SerializedName

data class GeocodingDto(
    @SerializedName("name")
    val city: String,
    val lat: Double,
    val lon: Double,
    val state: String? = null,
    @SerializedName("country")
    val countryCode: String,
    @SerializedName("local_names")
    val localNames: Map<String, String>? = null
)