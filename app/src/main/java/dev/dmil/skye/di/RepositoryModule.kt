package dev.dmil.skye.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.dmil.skye.data.repository.SavedCityRepositoryImpl
import dev.dmil.skye.data.repository.SettingsRepositoryImpl
import dev.dmil.skye.data.repository.WeatherCacheRepositoryImpl
import dev.dmil.skye.data.repository.WeatherRepositoryImpl
import dev.dmil.skye.domain.repository.SavedCityRepository
import dev.dmil.skye.domain.repository.SettingsRepository
import dev.dmil.skye.domain.repository.WeatherCacheRepository
import dev.dmil.skye.domain.repository.WeatherRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindSavedCityRepository(impl: SavedCityRepositoryImpl): SavedCityRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindWeatherCacheRepository(impl: WeatherCacheRepositoryImpl): WeatherCacheRepository

}