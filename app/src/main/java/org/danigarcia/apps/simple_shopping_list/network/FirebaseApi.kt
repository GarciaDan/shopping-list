package org.danigarcia.apps.simple_shopping_list.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Url

/**
 * API REST de Firebase Realtime Database.
 *
 * Se usa @Url porque el endpoint (y el parámetro ?auth=) se configura
 * en tiempo de ejecución desde la pantalla de ajustes.
 *
 * Estrategia: GET del árbol completo -> modificación local -> PUT del
 * árbol completo (tal y como exige el flujo de negocio de la app).
 */
interface FirebaseApi {

    /** Devuelve el mapa completo { pushId: item, ... } (o null si está vacío). */
    @GET
    suspend fun fetchAll(@Url url: String): Response<JsonObject?>

    /** Reemplaza el árbol completo con el mapa modificado. */
    @PUT
    suspend fun putAll(@Url url: String, @Body body: JsonObject): Response<JsonObject>
}
