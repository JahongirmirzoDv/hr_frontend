package uz.mobiledv.hr_frontend.data.storage

import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json
import uz.mobiledv.hr_frontend.data.remote.LoginResponse
import kotlin.text.get

class AuthStorage {

    private val userKey = "hr_auth_user"

    /**
     * Saves the login response to local storage after serializing it to a JSON string.
     */
    fun saveUser(loginResponse: LoginResponse) {
        val jsonString = Json.Default.encodeToString(loginResponse)
        localStorage.setItem(userKey,jsonString)
    }

    /**
     * Retrieves the user from local storage, deserializes it, and returns it.
     * Returns null if no user data is found.
     */
    fun getUser(): LoginResponse? {
        val jsonString = localStorage.getItem(userKey) ?: return null
        return try {
            Json.Default.decodeFromString<LoginResponse>(jsonString)
        } catch (e: Exception) {
            // If deserialization fails (e.g., data format changed), clear the invalid data.
            println("Failed to deserialize user from storage: ${e.message}")
            clearUser()
            null
        }
    }

    /**
     * Clears user data from local storage.
     */
    fun clearUser() {
        localStorage.removeItem(userKey)
    }
}