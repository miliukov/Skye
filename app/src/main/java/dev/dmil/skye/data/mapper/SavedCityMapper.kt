package dev.dmil.skye.data.mapper

import dev.dmil.skye.data.local.SavedCityEntity
import dev.dmil.skye.domain.model.SavedCity

fun SavedCityEntity.toSavedCity(): SavedCity {
    return SavedCity(
        name = this.name,
        state = this.state,
        countryCode = this.countryCode,
        tag = this.tag
    )
}

fun SavedCity.toSavedCityEntity(): SavedCityEntity {
    return SavedCityEntity(
        name = this.name,
        state = this.state,
        countryCode = this.countryCode,
        tag = this.tag
    )
}