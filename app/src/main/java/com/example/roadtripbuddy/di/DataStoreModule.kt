package com.example.roadtripbuddy.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.roadtripbuddy.data.Trips
import com.example.roadtripbuddy.data.TripsSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.tripsDataStore by dataStore(
    fileName   = "trips.pb",
    serializer = TripsSerializer
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides @Singleton
    fun provideTripsStore(
        @ApplicationContext context: Context
    ): DataStore<Trips> = context.tripsDataStore
}
