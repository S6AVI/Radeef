package com.saleem.radeef.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Singleton
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.saleem.radeef.util.FirebaseStorageConstants
//import com.saleem.radeef.data.relations.RadeefDatabase
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

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
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): StorageReference {
        return FirebaseStorage.getInstance().getReference(FirebaseStorageConstants.ROOT_DIRECTORY)
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