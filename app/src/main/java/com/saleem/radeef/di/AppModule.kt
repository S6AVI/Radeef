package com.saleem.radeef.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Singleton
import androidx.room.Room
import com.saleem.radeef.data.RadeefDatabase
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application
    ) = Room.databaseBuilder(
        app,
        RadeefDatabase::class.java,
        "radeef_database"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideRadeefDao(db:RadeefDatabase) = db.radeefDao()


    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope