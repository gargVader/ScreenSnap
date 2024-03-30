package com.screensnap.core.datastore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatastoreModule {

    @Binds
    @Singleton
    abstract fun bindScreenSnapDatastore(screenSnapDatastoreImpl: com.screensnap.core.datastore.ScreenSnapDatastoreImpl):
            com.screensnap.core.datastore.ScreenSnapDatastore

}