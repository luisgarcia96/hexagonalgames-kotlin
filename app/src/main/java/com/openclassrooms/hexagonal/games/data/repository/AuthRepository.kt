package com.openclassrooms.hexagonal.games.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository handling Firebase authentication tasks.
 */
@Singleton
class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth) {
  
  /**
   * Emits the current Firebase user whenever the auth state changes.
   */
  val authState: Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser).isSuccess }
    firebaseAuth.addAuthStateListener(listener)
    awaitClose { firebaseAuth.removeAuthStateListener(listener) }
  }
  
  val currentUser: FirebaseUser?
    get() = firebaseAuth.currentUser
  
  suspend fun signIn(email: String, password: String) {
    firebaseAuth.signInWithEmailAndPassword(email, password).await()
  }
  
  suspend fun signUp(email: String, password: String) {
    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
  }
  
  suspend fun sendPasswordReset(email: String) {
    firebaseAuth.sendPasswordResetEmail(email).await()
  }
  
  fun signOut() {
    firebaseAuth.signOut()
  }

  suspend fun deleteAccount() {
    val user = firebaseAuth.currentUser ?: error("No authenticated user")
    user.delete().await()
  }
}
