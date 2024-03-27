package com.screensnap.app.di

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.view.WindowManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindScreenSnapDatastore(screenSnapDatastoreImpl: com.screensnap.app.data.ScreenSnapDatastoreImpl):
            com.screensnap.app.data.ScreenSnapDatastore

    companion object {
        @Provides
        @Singleton
        fun provideMediaProjectionManager(app: Application): MediaProjectionManager {
            return app.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        }

        @Provides
        @Singleton
        fun provideNotificationManager(app: Application): NotificationManager {
            return app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        @Provides
        @Singleton
        fun provideWindowManager(app: Application): WindowManager {
            return app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
    }
}