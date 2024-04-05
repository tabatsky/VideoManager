package jatx.video.manager

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.StoredCredential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore
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
    private val SCOPES: Collection<String> = mutableListOf("https://www.googleapis.com/auth/youtube")
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()

    @Throws(IOException::class)
    fun authorize(httpTransport: NetHttpTransport?): Credential? {
        // Load client secrets.
        val inputStream: InputStream = File(CLIENT_SECRETS).inputStream()
        val clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))
        val credentialStore = FileCredentialStore(File("youtube_api.json"), JSON_FACTORY)
        // Build flow and trigger user authorization request.
        val flow =
            GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
            )
                .setCredentialStore(credentialStore)
                .setAccessType("offline")
                .build()
        val credential = AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
        StoredCredential(credential)
        return credential
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    fun getService(): YouTube? {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential = authorize(httpTransport)
        return YouTube.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    fun fetchPlaylistNames(): List<String> {
        try {
            return tryFetchPlaylistNames()
        } catch (e: Exception) {
            e.printStackTrace()
            File("youtube_api.json").delete()
            return tryFetchPlaylistNames()
        }
    }

    private fun tryFetchPlaylistNames(): List<String> {
        getService()?.let { theYoutubeService ->
            // Define and execute the API request
            val request = theYoutubeService.playlists()
                .list("snippet,contentDetails")
            val response = request.setMine(true).execute()
            return response.items.map { it.snippet.title }
        } ?: return listOf()
    }

    @Throws(GeneralSecurityException::class, IOException::class, GoogleJsonResponseException::class)
    fun fetchPlaylistVideos(playlistNameToFetch: String): List<YoutubeVideo> {
        val result = arrayListOf<YoutubeVideo>()

        getService()?.let { theYoutubeService ->
            // Define and execute the API request
            val request = theYoutubeService.playlists()
                .list("snippet,contentDetails")
            val response = request.setMine(true).execute()
            response.items.forEach { playlist ->
                val playlistId = playlist.id
                val playlistTitle = playlist.snippet.title
                println("playlist: $playlistId; $playlistTitle")
                if (playlistTitle == playlistNameToFetch) {
                    var nextPageToken: String? = null
                    do {
                        val request2 = theYoutubeService.playlistItems().list("snippet,contentDetails")
                        val response2 = if (nextPageToken == null) {
                            request2.setPlaylistId(playlistId).setMaxResults(50L).execute()
                        } else {
                            request2.setPlaylistId(playlistId).setMaxResults(50L).setPageToken(nextPageToken).execute()
                        }
                        val videoIds = response2.items.map { playlistItem ->
                            playlistItem.contentDetails.videoId
                        }
                        val fetchedVideos = fetchVideos(videoIds)
                        result.addAll(fetchedVideos)
                        nextPageToken = response2.nextPageToken
                        println(nextPageToken)
                    } while (nextPageToken != null)
                }
            }
        }

        return result
    }

    @Throws(GeneralSecurityException::class, IOException::class, GoogleJsonResponseException::class)
    private fun fetchVideos(videoIds: List<String>): List<YoutubeVideo> {
        val result = arrayListOf<YoutubeVideo>()
        val videoIdsStr = videoIds.joinToString(",")

        getService()?.let { theYoutubeService ->
            var nextPageToken: String? = null
            do {
                val request = theYoutubeService.Videos().list("snippet,fileDetails")
                val response = request.setId(videoIdsStr).setMaxResults(50L).setPageToken(nextPageToken).execute()

                response.items.forEach { video ->
                    val videoId = video.id
                    val videoTitle = video.snippet.title.trim()
                    val videoFileName = video.fileDetails.fileName
                    println("video: $videoId; $videoFileName; $videoTitle")
                    val youtubeVideo = YoutubeVideo(
                        id = videoId,
                        fileName = videoFileName,
                        title = videoTitle
                    )
                    result.add(youtubeVideo)
                }

                nextPageToken = response.nextPageToken
            } while (nextPageToken != null)
        }

        return result
    }

    fun updateVideo(videoId: String, newTitle: String) {
        getService()?.let { theYoutubeService ->
            val request = theYoutubeService.Videos().list("snippet")
            val response = request.setId(videoId).execute()
            response.items.firstOrNull()?.let { video ->
                val title = video.snippet.title
                println("video: $videoId; $title")
                video.snippet.title = newTitle
                val request2 = theYoutubeService.Videos().update("snippet", video)
                val response2 = request2.execute()
                println(response2)
            }
        }
    }
}

data class YoutubeVideo(
    val id: String,
    val fileName: String,
    val title: String
)