package org.danigarcia.apps.simple_shopping_list.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de un elemento de la lista tal y como se almacena en Firebase.
 * Firebase Realtime Database responde con un mapa { pushId: item, ... },
 * por eso cada item viaja acompañado de su clave [id].
 */
data class ShoppingItem(
    val id: String,
    @SerializedName("created") val created: String,
    @SerializedName("updated") val updated: String,
    @SerializedName("description") val description: String,
    @SerializedName("owner") val owner: String,
    @SerializedName("checked") val checked: Boolean
)
