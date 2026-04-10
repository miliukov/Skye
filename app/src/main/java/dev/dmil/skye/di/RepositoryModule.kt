package dev.dmil.skye.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.dmil.skye.data.repository.WeatherRepositoryImpl
import dev.dmil.skye.domain.repository.WeatherRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRepository(impl: WeatherRepositoryImpl): WeatherRepository

}