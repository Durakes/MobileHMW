// viewmodel/UserViewModel.kt
package com.example.composetutorialoulu.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composetutorialoulu.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _username = MutableStateFlow("Username")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _profileImageUri = MutableStateFlow("")
    val profileImageUri: StateFlow<String> = _profileImageUri.asStateFlow()

    init {
        viewModelScope.launch {
            repository.usernameFlow.collect { name ->
                _username.value = name.ifEmpty { "Username" }
            }
        }
        viewModelScope.launch {
            repository.profileImageUriFlow.collect { uri ->
                _profileImageUri.value = uri
            }
        }
    }

    fun saveUsername(username: String) {
        viewModelScope.launch {
            repository.saveUsername(username)
        }
    }

    fun saveProfileImageUri(uri: Uri) {
        viewModelScope.launch {
            repository.saveProfileImage(uri)
        }
    }
}
