package com.saleem.radeef.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.saleem.radeef.data.repository.AuthRepository
import com.saleem.radeef.data.repository.AuthRepositoryImpl
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.data.repository.CloudRepositoryImpl
import com.saleem.radeef.data.repository.RideRepository
import com.saleem.radeef.data.repository.RideRepositoryImpl
import com.saleem.radeef.driver.repo.DriverRepository
import com.saleem.radeef.driver.repo.DriverRepositoryImpl
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
        database: FirebaseFirestore,
        auth: FirebaseAuth
    ): RideRepository {
        return RideRepositoryImpl(database, auth)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth
    ): AuthRepository {
        return AuthRepositoryImpl(database, auth)
    }

    @Provides
    @Singleton
    fun provideCloudRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth
    ): CloudRepository {
        return CloudRepositoryImpl(database, auth)
    }


    @Provides
    @Singleton
    fun provideDriverAuthRepository(
        database: FirebaseFirestore,
        auth: FirebaseAuth,
        storageReference: StorageReference
    ): DriverRepository {
        return DriverRepositoryImpl(database, auth, storageReference)
    }

}