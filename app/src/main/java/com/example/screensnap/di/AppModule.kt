package com.example.screensnap.di

import android.app.Application
import android.content.Context
import android.media.projection.MediaProjectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideMediaProjectionManager(app: Application): MediaProjectionManager {
        return app.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

}