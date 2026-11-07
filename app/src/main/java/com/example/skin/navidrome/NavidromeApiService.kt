package com.example.skin.navidrome

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.security.MessageDigest
import java.util.UUID

interface NavidromeApi {
    @GET("rest/ping.view")
    suspend fun ping(
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/getArtists.view")
    suspend fun getArtists(
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/getArtist.view")
    suspend fun getArtist(
        @Query("id") artistId: String,
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/getAlbumList2.view")
    suspend fun getAlbumList2(
        @Query("type") type: String = "newest",
        @Query("size") size: Int = 50,
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/getAlbum.view")
    suspend fun getAlbum(
        @Query("id") albumId: String,
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/search3.view")
    suspend fun search3(
        @Query("query") query: String,
        @Query("artistCount") artistCount: Int = 20,
        @Query("albumCount") albumCount: Int = 20,
        @Query("songCount") songCount: Int = 50,
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/getPlaylists.view")
    suspend fun getPlaylists(
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/getGenres.view")
    suspend fun getGenres(
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/getSongsByGenre.view")
    suspend fun getSongsByGenre(
        @Query("genre") genre: String,
        @Query("count") count: Int = 50,
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/getStarred2.view")
    suspend fun getStarred2(
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/star.view")
    suspend fun star(
        @Query("id") id: String?,
        @Query("albumId") albumId: String?,
        @Query("artistId") artistId: String?,
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper

    @GET("rest/unstar.view")
    suspend fun unstar(
        @Query("id") id: String?,
        @Query("albumId") albumId: String?,
        @Query("artistId") artistId: String?,
        @Query("u") u: String,
        @Query("t") t: String,
        @Query("s") s: String,
        @Query("v") version: String = "1.16.1",
        @Query("c") client: String = "Skin",
        @Query("f") format: String = "json"
    ): SubsonicResponseWrapper
}

object NavidromeClientManager {
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String = ""

    fun getApi(baseUrl: String): NavidromeApi {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (retrofit == null || currentBaseUrl != normalizedUrl) {
            currentBaseUrl = normalizedUrl
            val client = OkHttpClient.Builder().build()
            retrofit = Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(NavidromeApi::class.java)
    }

    fun generateSaltAndToken(password: String): Pair<String, String> {
        val salt = UUID.randomUUID().toString().take(6)
        val input = password + salt
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        val token = bytes.joinToString("") { "%02x".format(it) }
        return Pair(salt, token)
    }

    fun getStreamUrl(baseUrl: String, songId: String, user: String, pass: String): String {
        val (salt, token) = generateSaltAndToken(pass)
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return "${normalizedUrl}rest/stream.view?id=$songId&u=$user&t=$token&s=$salt&v=1.16.1&c=Skin"
    }

    fun getCoverArtUrl(baseUrl: String, coverArtId: String?, albumId: String?, user: String, pass: String): String {
        val artId = when {
            !coverArtId.isNullOrEmpty() -> coverArtId
            !albumId.isNullOrEmpty() -> albumId
            else -> return ""
        }
        val (salt, token) = generateSaltAndToken(pass)
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return "${normalizedUrl}rest/getCoverArt.view?id=$artId&u=$user&t=$token&s=$salt&v=1.16.1&c=Skin"
    }
}
