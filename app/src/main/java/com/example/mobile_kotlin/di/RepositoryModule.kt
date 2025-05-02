package com.example.mobile_kotlin.di

import com.example.mobile_kotlin.data.remote.FirebaseAuthService
import com.example.mobile_kotlin.data.remote.FirestoreService
import com.example.mobile_kotlin.data.repository.ActorsRepository
import com.example.mobile_kotlin.data.repository.AuthRepository
import com.example.mobile_kotlin.data.repository.ReviewsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authService: FirebaseAuthService,
        firestoreService: FirestoreService
    ): AuthRepository = AuthRepository(authService, firestoreService)

    @Provides
    @Singleton
    fun provideActorsRepository(
        firestoreService: FirestoreService,
        authRepo: AuthRepository
    ): ActorsRepository = ActorsRepository(firestoreService, authRepo)

    @Provides
    @Singleton
    fun provideReviewsRepository(
        firestoreService: FirestoreService
    ): ReviewsRepository = ReviewsRepository(firestoreService)
}