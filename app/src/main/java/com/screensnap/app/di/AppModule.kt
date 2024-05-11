package com.screensnap.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    //    companion object {
//        @Provides
//        @Singleton
//        fun provideMediaProjectionManager(app: Application): MediaProjectionManager {
//            return app.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
//        }
//
//        @Provides
//        @Singleton
//        fun provideNotificationManager(app: Application): NotificationManager {
//            return app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        }
//
//        @Provides
//        @Singleton
//        fun provideWindowManager(app: Application): WindowManager {
//            return app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        }
//    }
}