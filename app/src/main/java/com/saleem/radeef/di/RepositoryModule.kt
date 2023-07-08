package com.saleem.radeef.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.repository.AuthRepository
import com.saleem.radeef.data.repository.AuthRepositoryImpl
import com.saleem.radeef.data.repository.RideRepository
import com.saleem.radeef.data.repository.RideRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRideRepository(
        database: FirebaseFirestore
    ): RideRepository {
        return RideRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth
    ): AuthRepository {
        return AuthRepositoryImpl(database, auth)
    }
}