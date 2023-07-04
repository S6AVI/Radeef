package com.saleem.radeef

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun login(phone: String): MyResource<FirebaseUser>
    fun logout()
}