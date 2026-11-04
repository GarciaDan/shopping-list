package org.danigarcia.apps.simple_shopping_list.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.danigarcia.apps.simple_shopping_list.model.ShoppingItem
import org.danigarcia.apps.simple_shopping_list.network.FirebaseApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Repositorio que encapsula las operaciones GET/PUT contra Firebase
 * y la conversión entre el mapa JSON remoto y la lista de [ShoppingItem].
 */
class ShoppingListRepository {

    private val api: FirebaseApi = Retrofit.Builder()
        .baseUrl("https://localhost/") // placeholder; siempre se usa @Url absoluto
        .client(
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()
        .create(FirebaseApi::class.java)

    companion object {
        private val ISO_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun nowIso(): String = ISO_FORMAT.format(Date())
    }

    /** Construye la URL final: endpoint (+ .json si falta) + ?auth=... */
    private fun buildUrl(settings: AppSettings): String {
        var url = settings.endpoint.trim().trimEnd('/')
        if (!url.endsWith(".json")) url = "$url.json"
        return if (settings.auth.isNotBlank()) "$url?auth=${settings.auth}" else url
    }

    /** GET del árbol completo y conversión a lista de items. */
    suspend fun fetchItems(settings: AppSettings): List<ShoppingItem> {
        val response = api.fetchAll(buildUrl(settings))
        if (!response.isSuccessful) {
            throw IllegalStateException("HTTP ${response.code()}: ${response.message()}")
        }
        val root = response.body() ?: return emptyList()
        return root.entrySet().mapNotNull { (id, value) ->
            val obj = value.asJsonObject
            ShoppingItem(
                id = id,
                created = obj.get("created")?.asString ?: "",
                updated = obj.get("updated")?.asString ?: "",
                description = obj.get("description")?.asString ?: "",
                owner = obj.get("owner")?.asString ?: "",
                checked = obj.get("checked")?.asBoolean ?: false
            )
        }
    }

    /** PUT del árbol completo a partir de la lista de items. */
    suspend fun pushItems(settings: AppSettings, items: List<ShoppingItem>) {
        val root = JsonObject()
        items.forEach { item ->
            val obj = JsonObject()
            obj.addProperty("created", item.created)
            obj.addProperty("updated", item.updated)
            obj.addProperty("description", item.description)
            obj.addProperty("owner", item.owner)
            obj.addProperty("checked", item.checked)
            root.add(item.id, obj)
        }
        val response = api.putAll(buildUrl(settings), root)
        if (!response.isSuccessful) {
            throw IllegalStateException("HTTP ${response.code()}: ${response.message()}")
        }
    }
}

/**
 * Regla de ordenación crítica:
 *  1) Bloque superior: checked == false, orden descendente por [ShoppingItem.created] (los más nuevos arriba).
 *  2) Bloque inferior: checked == true, orden descendente por [ShoppingItem.updated] (los marcados más recientemente arriba).
 */
fun List<ShoppingItem>.sortedForDisplay(): List<ShoppingItem> =
    filterNot { it.checked }.sortedByDescending { it.created } +
        filter { it.checked }.sortedByDescending { it.updated }
