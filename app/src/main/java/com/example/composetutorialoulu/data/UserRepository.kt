// data/UserRepository.kt
package com.example.composetutorialoulu.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserRepository(private val context: Context) {
    private val usernameKey = stringPreferencesKey("username")
    private val profileImageUriKey = stringPreferencesKey("profile_image_uri")
    private val cameraImageUriKey = stringPreferencesKey("camera_image_uri")

    val usernameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[usernameKey] ?: ""
    }

    val profileImageUriFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[profileImageUriKey] ?: ""
    }

    val cameraImageUriFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[cameraImageUriKey] ?: ""
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[usernameKey] = username
        }
    }

    private suspend fun saveProfileImageUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[profileImageUriKey] = uri
        }
    }

    private suspend fun saveCameraImageUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[cameraImageUriKey] = uri
        }
    }

    suspend fun saveProfileImage(uri: Uri) {
        val file = saveImageToInternalStorage(uri, "profile_image.jpg")
        saveProfileImageUri(file.absolutePath)
    }

    suspend fun saveCameraImage(uri: Uri){
        val file = saveImageToInternalStorage(uri, "camera_image.jpg")
        saveCameraImageUri(file.absolutePath)
    }

    private fun saveImageToInternalStorage(uri: Uri, fileName: String): File {
        val file = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}
