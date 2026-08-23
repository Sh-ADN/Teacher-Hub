package com.abutorab.teacher.hub.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun getWebClientId(context: Context): String {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) {
            context.getString(resId)
        } else {
            "YOUR_WEB_CLIENT_ID" // Placeholder, requires google-services.json
        }
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        val credentialManager = CredentialManager.create(context)
        
        val hashedNonce = UUID.randomUUID().toString()
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getWebClientId(context))
            .setNonce(hashedNonce)
            .build()
            
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
            
        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                
                if (authResult.user != null) {
                    Result.success(authResult.user!!)
                } else {
                    Result.failure(Exception("Firebase Auth returned null user"))
                }
            } else {
                Result.failure(Exception("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "GetCredentialException", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthManager", "General Exception", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context) {
        auth.signOut()
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthManager", "Error clearing credential state", e)
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
