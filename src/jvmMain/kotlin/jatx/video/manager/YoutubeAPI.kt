package jatx.video.manager

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.security.GeneralSecurityException


const val API_KEY = "AIzaSyDHlMYplQiPpVf_IvKEhSq_qcWHJqihva4"
const val CLIENT_SECRETS = "client_secret.json"
const val APPLICATION_NAME = "VideoManager"

object YoutubeAPI {
    private val SCOPES: Collection<String> = mutableListOf("https://www.googleapis.com/auth/youtube.readonly")
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()

    @Throws(IOException::class)
    fun authorize(httpTransport: NetHttpTransport?): Credential? {
        // Load client secrets.
        val inputStream: InputStream = File(CLIENT_SECRETS).inputStream()
        val clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))
        // Build flow and trigger user authorization request.
        val flow =
            GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
            )
                .build()
        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    fun getService(): YouTube? {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential = authorize(httpTransport)
        return YouTube.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    @Throws(GeneralSecurityException::class, IOException::class, GoogleJsonResponseException::class)
    fun tryFetchVideos(playlistNameToFetch: String): List<Pair<String, String>> {
        val result = arrayListOf<Pair<String, String>>()

        val youtubeService = getService()
        // Define and execute the API request
        val request = youtubeService!!.playlists()
            .list("snippet,contentDetails")
        val response = request.setMine(true).execute()
        response.items.forEach { playlist ->
            val playlistId = playlist.id
            val playlistTitle = playlist.snippet.title
            println("playlist: $playlistId; $playlistTitle")
            if (playlistTitle == playlistNameToFetch) {
                var nextPageToken: String? = null
                do {
                    val request2 = youtubeService.playlistItems().list("snippet,contentDetails")
                    val response2 = if (nextPageToken == null) {
                        request2.setPlaylistId(playlistId).setMaxResults(50L).execute()
                    } else {
                        request2.setPlaylistId(playlistId).setMaxResults(50L).setPageToken(nextPageToken).execute()
                    }
                    response2.items.forEach { playlistItem ->
                        val videoId = playlistItem.contentDetails.videoId
                        val videoTitle = playlistItem.snippet.title
                        println("video: $videoId; $videoTitle")
                        result.add(videoId to videoTitle)
                    }
                    nextPageToken = response2.nextPageToken
                    println(nextPageToken)
                } while (nextPageToken != null)
            }
        }

        return result
    }
}