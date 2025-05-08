package com.example.roadtripbuddy.data


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object  FireBaseModule{
    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth=
        FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFireStore():FirebaseFirestore=
        FirebaseFirestore.getInstance()
}