package com.saleem.radeef.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.maps.GeoApiContext
import com.saleem.radeef.R
import com.saleem.radeef.driver.repo.CarsApi
import com.saleem.radeef.util.FirebaseStorageConstants
import com.saleem.radeef.util.GOOGLE_MAPS_KEY
//import com.saleem.radeef.data.relations.RadeefDatabase
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        //auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        return auth
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): StorageReference {
        return FirebaseStorage.getInstance().getReference(FirebaseStorageConstants.ROOT_DIRECTORY)
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(CarsApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    @Provides
    @Singleton
    fun provideCarsApi(retrofit: Retrofit): CarsApi =
        retrofit.create(CarsApi::class.java)

    @Provides
    fun provideGeoApiContext(): GeoApiContext {
        val apiKey = GOOGLE_MAPS_KEY
        return GeoApiContext.Builder()
                .apiKey(apiKey)
                .build()
    }


//    @Provides
//    @Singleton
//    fun provideDatabase(
//        app: Application
//    ) = Room.databaseBuilder(
//        app,
//        RadeefDatabase::class.java,
//        "radeef_database"
//    )
//        .fallbackToDestructiveMigration()
//        .build()
//
//    @Provides
//    fun provideRadeefDao(db: RadeefDatabase) = db.radeefDao()


    //@ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}