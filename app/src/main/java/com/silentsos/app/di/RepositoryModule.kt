package com.silentsos.app.di

import com.silentsos.app.data.repository.AuthRepositoryImpl
import com.silentsos.app.data.repository.ContactRepositoryImpl
import com.silentsos.app.data.repository.LocationRepositoryImpl
import com.silentsos.app.data.repository.SOSRepositoryImpl
import com.silentsos.app.domain.repository.AuthRepository
import com.silentsos.app.domain.repository.ContactRepository
import com.silentsos.app.domain.repository.LocationRepository
import com.silentsos.app.domain.repository.SOSRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindSOSRepository(impl: SOSRepositoryImpl): SOSRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
