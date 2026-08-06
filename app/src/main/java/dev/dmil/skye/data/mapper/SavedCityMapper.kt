package dev.dmil.skye.data.mapper

import dev.dmil.skye.data.local.SavedCityEntity
import dev.dmil.skye.domain.model.SavedCity

fun SavedCityEntity.toSavedCity(): SavedCity {
    return SavedCity(
        id = this.id,
        name = this.name,
        state = this.state,
        countryCode = this.countryCode,
        lat = this.lat,
        lon = this.lon,
        tag = this.tag
    )
}

fun SavedCity.toSavedCityEntity(): SavedCityEntity {
    return SavedCityEntity(
        id = this.id,
        name = this.name,
        state = this.state,
        countryCode = this.countryCode,
        lat = this.lat,
        lon = this.lon,
        tag = this.tag
    )
}