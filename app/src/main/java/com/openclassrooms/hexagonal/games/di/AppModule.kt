package com.openclassrooms.hexagonal.games.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.hexagonal.games.data.service.PostApi
import com.openclassrooms.hexagonal.games.data.service.PostFirestoreApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * This class acts as a Dagger Hilt module, responsible for providing dependencies to other parts of the application.
 * It's installed in the SingletonComponent, ensuring that dependencies provided by this module are created only once
 * and remain available throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
  /**
   * Provides a Singleton instance of PostApi backed by Firestore.
   *
   * @return A Singleton instance of PostFirestoreApi.
   */
  @Provides
  @Singleton
  fun providePostApi(
    firestore: FirebaseFirestore
  ): PostApi = PostFirestoreApi(firestore)
  
  @Provides
  @Singleton
  fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

  @Provides
  @Singleton
  fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
