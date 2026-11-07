package com.example.skin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import coil.compose.rememberAsyncImagePainter
import com.example.skin.navidrome.AlbumID3
import com.example.skin.navidrome.ArtistID3
import com.example.skin.navidrome.Genre
import com.example.skin.navidrome.MusicPlayerManager
import com.example.skin.navidrome.NavidromeClientManager
import com.example.skin.navidrome.PlayerState
import com.example.skin.navidrome.Playlist
import com.example.skin.navidrome.Song
import com.example.skin.ui.theme.SkinTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.math.log10

// Warm Minimalist Blue Palette
val Primary = Color(0xFF3B82F6) // Warm Blue
val Background = Color(0xFF0F172A) // Deep Slate / Warm Dark
val Surface = Color(0xFF1E293B) // Slate Surface
val TextPrimary = Color.White
val TextSecondary = Color(0xFF94A3B8) // Slate Gray

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkinTheme {
                GamerApp()
            }
        }
    }
}

fun getLocalAudioFiles(context: Context): List<Song> {
    val songList = mutableListOf<Song>()
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA
    )

    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
    val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

    try {
        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val title = cursor.getString(titleCol) ?: "Unknown"
                val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                val album = cursor.getString(albumCol) ?: "Unknown Album"
                val durationMs = cursor.getLong(durationCol)
                val path = cursor.getString(dataCol) ?: ""

                val suffix = when {
                    path.endsWith(".flac", true) -> "flac"
                    path.endsWith(".wav", true) -> "wav"
                    path.endsWith(".aac", true) -> "aac"
                    path.endsWith(".ogg", true) -> "ogg"
                    else -> "mp3"
                }

                if (path.isNotEmpty()) {
                    songList.add(
                        Song(
                            id = path,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = "local_album",
                            duration = (durationMs / 1000).toInt(),
                            track = 1,
                            year = 2024,
                            genre = "Device",
                            size = 0L,
                            contentType = "audio/*",
                            suffix = suffix,
                            coverArt = null,
                            created = null
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {}
    return songList
}

fun downloadSongToFolder(context: Context, song: Song, targetFolder: String, serverUrl: String, username: String, password: String, onComplete: (Boolean, String) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val destDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), targetFolder)
            if (!destDir.exists()) destDir.mkdirs()

            if (song.id.startsWith("/")) {
                val srcFile = File(song.id)
                val destFile = File(destDir, srcFile.name)
                srcFile.copyTo(destFile, overwrite = true)
                withContext(Dispatchers.Main) {
                    onComplete(true, destFile.absolutePath)
                }
                return@launch
            }

            val streamUrl = NavidromeClientManager.getStreamUrl(serverUrl, song.id, username, password)
            val client = OkHttpClient()
            val request = Request.Builder().url(streamUrl).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body
                if (body != null) {
                    val fileName = "${song.artist ?: "Unknown"} - ${song.title}.${song.suffix ?: "mp3"}"
                    val destFile = File(destDir, fileName)
                    destFile.outputStream().use { output ->
                        body.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        onComplete(true, destFile.absolutePath)
                    }
                } else {
                    withContext(Dispatchers.Main) { onComplete(false, "") }
                }
            } else {
                withContext(Dispatchers.Main) { onComplete(false, "") }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onComplete(false, "") }
        }
    }
}

fun downloadAllSongsToFolder(context: Context, songs: List<Song>, targetFolder: String, serverUrl: String, username: String, password: String, onComplete: (Int) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        var count = 0
        for (song in songs) {
            try {
                val destDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), targetFolder)
                if (!destDir.exists()) destDir.mkdirs()

                if (song.id.startsWith("/")) {
                    val srcFile = File(song.id)
                    val destFile = File(destDir, srcFile.name)
                    if (srcFile.exists()) {
                        srcFile.copyTo(destFile, overwrite = true)
                        count++
                    }
                } else {
                    val streamUrl = NavidromeClientManager.getStreamUrl(serverUrl, song.id, username, password)
                    val client = OkHttpClient()
                    val request = Request.Builder().url(streamUrl).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body
                        if (body != null) {
                            val fileName = "${song.artist ?: "Unknown"} - ${song.title}.${song.suffix ?: "mp3"}"
                            val destFile = File(destDir, fileName)
                            destFile.outputStream().use { output ->
                                body.byteStream().use { input ->
                                    input.copyTo(output)
                                }
                            }
                            count++
                        }
                    }
                }
            } catch (e: Exception) {}
        }
        withContext(Dispatchers.Main) {
            onComplete(count)
        }
    }
}

@Composable
fun GamerApp() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("skin_navidrome_prefs", Context.MODE_PRIVATE) }

    var currentScreen by remember { mutableStateOf("start") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var serverUrl by remember { mutableStateOf(sharedPrefs.getString("server_url", "https://demo.navidrome.org") ?: "https://demo.navidrome.org") }
    var username by remember { mutableStateOf(sharedPrefs.getString("username", "demo") ?: "demo") }
    var password by remember { mutableStateOf(sharedPrefs.getString("password", "demo") ?: "demo") }

    var albums by remember { mutableStateOf<List<AlbumID3>>(emptyList()) }
    var artists by remember { mutableStateOf<List<ArtistID3>>(emptyList()) }
    var localSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<Song>>(emptyList()) }
    var starredSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var historySongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var genres by remember { mutableStateOf<List<Genre>>(emptyList()) }
    var genreSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var selectedGenreName by remember { mutableStateOf("") }
    var initialLibraryTab by remember { mutableStateOf("albums") }

    var selectedAlbumSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var selectedAlbumTitle by remember { mutableStateOf("") }
    var selectedAlbumCover by remember { mutableStateOf<String?>(null) }
    var selectedArtistAlbums by remember { mutableStateOf<List<AlbumID3>>(emptyList()) }
    var selectedArtistName by remember { mutableStateOf("") }

    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var isLoadingLibrary by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("") }

    val musicPlayerManager = remember { MusicPlayerManager(context) }
    val playerState by musicPlayerManager.playerState.collectAsState()

    LaunchedEffect(Unit) {
        localSongs = getLocalAudioFiles(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            musicPlayerManager.release()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            currentScreen = "hub"
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "start" -> {
                    FirstScreen(
                        onActivate = {
                            if (hasPermission) {
                                currentScreen = "hub"
                            } else {
                                launcher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    )
                }
                "hub" -> {
                    HubScreen(
                        onSelectMeter = { currentScreen = "meter" },
                        onSelectNavidrome = { currentScreen = "navidrome_config" },
                        onSelectGenresDirect = {
                            initialLibraryTab = "genres"
                            currentScreen = "navidrome_config"
                        },
                        onBack = { currentScreen = "start" }
                    )
                }
                "meter" -> {
                    SecondScreen(onBack = { currentScreen = "hub" })
                }
                "navidrome_config" -> {
                    NavidromeConfigScreen(
                        serverUrl = serverUrl,
                        username = username,
                        password = password,
                        connectionStatus = connectionStatus,
                        onUrlChange = { serverUrl = it },
                        onUserChange = { username = it },
                        onPassChange = { password = it },
                        onTestAndConnect = {
                            sharedPrefs.edit()
                                .putString("server_url", serverUrl)
                                .putString("username", username)
                                .putString("password", password)
                                .apply()

                            isLoadingLibrary = true
                            connectionStatus = "Connecting..."
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val api = NavidromeClientManager.getApi(serverUrl)
                                    val (salt, token) = NavidromeClientManager.generateSaltAndToken(password)
                                    val pingResp = api.ping(u = username, t = token, s = salt)
                                    if (pingResp.`subsonic-response`.status == "ok") {
                                        val albumsResp = api.getAlbumList2(type = "newest", u = username, t = token, s = salt)
                                        albums = albumsResp.`subsonic-response`.albumList2?.album ?: emptyList()
                                        
                                        val artistsResp = api.getArtists(u = username, t = token, s = salt)
                                        val indexList = artistsResp.`subsonic-response`.artists?.index ?: emptyList()
                                        artists = indexList.flatMap { it.artist ?: emptyList() }

                                        val playlistsResp = api.getPlaylists(u = username, t = token, s = salt)
                                        playlists = playlistsResp.`subsonic-response`.playlists?.playlist ?: emptyList()

                                        val genresResp = api.getGenres(u = username, t = token, s = salt)
                                        genres = genresResp.`subsonic-response`.genres?.genre ?: emptyList()

                                        val starredResp = api.getStarred2(u = username, t = token, s = salt)
                                        starredSongs = starredResp.`subsonic-response`.starred2?.song ?: emptyList()

                                        connectionStatus = "Connected! Loaded ${albums.size} albums, ${localSongs.size} local device songs."
                                        isLoadingLibrary = false
                                        withContext(Dispatchers.Main) {
                                            currentScreen = "navidrome_library"
                                        }
                                    } else {
                                        connectionStatus = "Error: ${pingResp.`subsonic-response`.error?.message ?: "Unknown"}"
                                        isLoadingLibrary = false
                                    }
                                } catch (e: Exception) {
                                    connectionStatus = "Connection Failed: ${e.localizedMessage}"
                                    isLoadingLibrary = false
                                }
                            }
                        },
                        onBack = { currentScreen = "hub" }
                    )
                }
                "navidrome_library" -> {
                    NavidromeLibraryScreen(
                        albums = albums,
                        artists = artists,
                        localSongs = localSongs,
                        playlists = playlists,
                        genres = genres,
                        starredSongs = starredSongs,
                        historySongs = historySongs,
                        initialTab = initialLibraryTab,
                        isLoading = isLoadingLibrary,
                        serverUrl = serverUrl,
                        username = username,
                        password = password,
                        playerState = playerState,
                        onAlbumClick = { album ->
                            isLoadingLibrary = true
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val api = NavidromeClientManager.getApi(serverUrl)
                                    val (salt, token) = NavidromeClientManager.generateSaltAndToken(password)
                                    val albumResp = api.getAlbum(album.id, u = username, t = token, s = salt)
                                    selectedAlbumSongs = albumResp.`subsonic-response`.album?.song ?: emptyList()
                                    selectedAlbumTitle = album.name
                                    selectedAlbumCover = album.coverArt
                                    isLoadingLibrary = false
                                    withContext(Dispatchers.Main) {
                                        currentScreen = "navidrome_album_detail"
                                    }
                                } catch (e: Exception) {
                                    isLoadingLibrary = false
                                }
                            }
                        },
                        onArtistClick = { artist ->
                            isLoadingLibrary = true
                            selectedArtistName = artist.name
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val api = NavidromeClientManager.getApi(serverUrl)
                                    val (salt, token) = NavidromeClientManager.generateSaltAndToken(password)
                                    val artistResp = api.getArtist(artist.id, u = username, t = token, s = salt)
                                    selectedArtistAlbums = artistResp.`subsonic-response`.artist?.album ?: emptyList()
                                    isLoadingLibrary = false
                                    withContext(Dispatchers.Main) {
                                        currentScreen = "navidrome_artist_detail"
                                    }
                                } catch (e: Exception) {
                                    isLoadingLibrary = false
                                }
                            }
                        },
                        onGenreClick = { genreName ->
                            isLoadingLibrary = true
                            selectedGenreName = genreName
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val api = NavidromeClientManager.getApi(serverUrl)
                                    val (salt, token) = NavidromeClientManager.generateSaltAndToken(password)
                                    val songsResp = api.getSongsByGenre(genre = genreName, u = username, t = token, s = salt)
                                    genreSongs = songsResp.`subsonic-response`.songsByGenre?.song ?: emptyList()
                                    isLoadingLibrary = false
                                    withContext(Dispatchers.Main) {
                                        currentScreen = "navidrome_genre_detail"
                                    }
                                } catch (e: Exception) {
                                    isLoadingLibrary = false
                                }
                            }
                        },
                        onSearch = { query ->
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val api = NavidromeClientManager.getApi(serverUrl)
                                    val (salt, token) = NavidromeClientManager.generateSaltAndToken(password)
                                    val searchResp = api.search3(query = query, u = username, t = token, s = salt)
                                    searchResults = searchResp.`subsonic-response`.searchResult3?.song ?: emptyList()
                                } catch (e: Exception) {}
                            }
                        },
                        searchResults = searchResults,
                        onSortByDateAdded = {
                            albums = albums.sortedByDescending { it.created ?: "" }
                        },
                        onSortByName = {
                            albums = albums.sortedBy { it.name }
                        },
                        onSongClick = { _, queue, index ->
                            musicPlayerManager.playQueue(queue, index, serverUrl, username, password)
                            currentScreen = "navidrome_player"
                        },
                        onOpenPlayer = { currentScreen = "navidrome_player" },
                        onPlayPause = { musicPlayerManager.togglePlayPause() },
                        onBack = { currentScreen = "navidrome_config" }
                    )
                }
                "navidrome_album_detail" -> {
                    NavidromeAlbumDetailScreen(
                        albumTitle = selectedAlbumTitle,
                        coverArtId = selectedAlbumCover,
                        songs = selectedAlbumSongs,
                        serverUrl = serverUrl,
                        username = username,
                        password = password,
                        playerState = playerState,
                        onSongClick = { _, queue, index ->
                            musicPlayerManager.playQueue(queue, index, serverUrl, username, password)
                            currentScreen = "navidrome_player"
                        },
                        onOpenPlayer = { currentScreen = "navidrome_player" },
                        onBack = { currentScreen = "navidrome_library" }
                    )
                }
                "navidrome_artist_detail" -> {
                    NavidromeArtistAlbumsScreen(
                        artistName = selectedArtistName,
                        albums = selectedArtistAlbums,
                        serverUrl = serverUrl,
                        username = username,
                        password = password,
                        playerState = playerState,
                        onAlbumClick = { album ->
                            isLoadingLibrary = true
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val api = NavidromeClientManager.getApi(serverUrl)
                                    val (salt, token) = NavidromeClientManager.generateSaltAndToken(password)
                                    val albumResp = api.getAlbum(album.id, u = username, t = token, s = salt)
                                    selectedAlbumSongs = albumResp.`subsonic-response`.album?.song ?: emptyList()
                                    selectedAlbumTitle = album.name
                                    selectedAlbumCover = album.coverArt
                                    isLoadingLibrary = false
                                    withContext(Dispatchers.Main) {
                                        currentScreen = "navidrome_album_detail"
                                    }
                                } catch (e: Exception) {
                                    isLoadingLibrary = false
                                }
                            }
                        },
                        onOpenPlayer = { currentScreen = "navidrome_player" },
                        onBack = { currentScreen = "navidrome_library" }
                    )
                }
                "navidrome_genre_detail" -> {
                    NavidromeAlbumDetailScreen(
                        albumTitle = "Genre: $selectedGenreName",
                        coverArtId = null,
                        songs = genreSongs,
                        serverUrl = serverUrl,
                        username = username,
                        password = password,
                        playerState = playerState,
                        onSongClick = { _, queue, index ->
                            musicPlayerManager.playQueue(queue, index, serverUrl, username, password)
                            currentScreen = "navidrome_player"
                        },
                        onOpenPlayer = { currentScreen = "navidrome_player" },
                        onBack = { currentScreen = "navidrome_library" }
                    )
                }
                "navidrome_player" -> {
                    NavidromePlayerScreen(
                        playerState = playerState,
                        serverUrl = serverUrl,
                        username = username,
                        password = password,
                        musicPlayerManager = musicPlayerManager,
                        onPlayPause = { musicPlayerManager.togglePlayPause() },
                        onNext = { musicPlayerManager.skipNext() },
                        onPrevious = { musicPlayerManager.skipPrevious() },
                        onSeek = { musicPlayerManager.seekTo(it) },
                        onVolume = { musicPlayerManager.setVolume(it) },
                        onShuffle = { musicPlayerManager.toggleShuffle() },
                        onRepeat = { musicPlayerManager.toggleRepeatMode() },
                        onBack = { currentScreen = "navidrome_library" }
                    )
                }
            }
        }
    }
}

@Composable
fun FirstScreen(onActivate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SKIN HI-FI",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "JMSJ TECHNOLOGY // ULTIMATE MUSIC SYSTEM",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
            }

            MasterPowerButton(onClick = onActivate)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 40.dp, bottom = 60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("INPUT: FLAC • MP3 • WAV", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("AUDIO ENGINE: ACTIVE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "ULTIMATE v2.5",
                    color = Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Composable
fun MasterPowerButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val infiniteTransition = rememberInfiniteTransition(label = "blue_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "led_breathing"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(220.dp)
                .background(Surface, CircleShape)
                .border(2.dp, Primary.copy(alpha = glowAlpha), CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POWER",
                    color = Primary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .shadow(6.dp, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "TAP TO START",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun HubScreen(onSelectMeter: () -> Unit, onSelectNavidrome: () -> Unit, onSelectGenresDirect: () -> Unit, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBack) {
                    Text("←", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "ULTIMATE AUDIO CONSOLE",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SELECT OPERATING MODE",
                fontSize = 12.sp,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onSelectMeter,
                colors = ButtonDefaults.buttonColors(containerColor = Surface),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Primary, Primary))),
                modifier = Modifier.fillMaxWidth(0.85f).height(56.dp)
            ) {
                Text(text = "ANALOG AUDIO METER", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSelectNavidrome,
                colors = ButtonDefaults.buttonColors(containerColor = Surface),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Primary, Primary))),
                modifier = Modifier.fillMaxWidth(0.85f).height(56.dp)
            ) {
                Text(text = "NAVIDROME HI-FI PLAYER", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSelectGenresDirect,
                colors = ButtonDefaults.buttonColors(containerColor = Surface),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Primary, Primary))),
                modifier = Modifier.fillMaxWidth(0.85f).height(56.dp)
            ) {
                Text(text = "MUSIC GENRES", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "SUPPORTS FLAC • MP3 • WAV • AAC • OGG",
                color = TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun UnifiedLedBar(value: Float) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "unified_led_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFF090D16))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val numLeds = 80
            val spacing = 4f
            val ledWidth = (size.width - (numLeds - 1) * spacing) / numLeds
            
            for (i in 0 until numLeds) {
                val percent = i.toFloat() / (numLeds - 1)
                val isOn = animatedValue >= percent * 100f
                val x = i * (ledWidth + spacing)
                
                val ledColor = when {
                    percent < 0.6f -> if (isOn) Color(0xFF3B82F6) else Color(0xFF1E3A8A)
                    percent < 0.85f -> if (isOn) Color(0xFF60A5FA) else Color(0xFF1E293B)
                    else -> if (isOn) Color(0xFF93C5FD) else Color(0xFF0F172A)
                }
                
                drawRect(
                    color = ledColor,
                    topLeft = Offset(x, 0f),
                    size = Size(ledWidth, size.height),
                    style = Fill
                )
            }
        }
    }
}

@Composable
fun SecondScreen(onBack: () -> Unit) {
    var dbLevel by remember { mutableFloatStateOf(0f) }
    var sensitivity by remember { mutableFloatStateOf(1.5f) }
    var isSliderVisible by remember { mutableStateOf(false) }
    var lastInteraction by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    @SuppressLint("MissingPermission")
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val bufferSize = AudioRecord.getMinBufferSize(
                54100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                54100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            val buffer = ShortArray(bufferSize)
            audioRecord.startRecording()

            try {
                while (isActive) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val amplitude = Math.sqrt(sum / read)
                        val db = if (amplitude > 0) 20 * log10(amplitude / 32768.0) + 60 else 0.0
                        val targetLevel = (db.toFloat() * sensitivity * 1.5f).coerceIn(0f, 100f)
                        
                        if (targetLevel > dbLevel) {
                            dbLevel = targetLevel
                        } else {
                            dbLevel = dbLevel * 0.85f + targetLevel * 0.15f
                        }
                    }
                    delay(10)
                }
            } finally {
                audioRecord.stop()
                audioRecord.release()
            }
        }
    }

    LaunchedEffect(lastInteraction) {
        if (lastInteraction > 0) {
            isSliderVisible = true
            delay(1500)
            isSliderVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                lastInteraction = System.currentTimeMillis()
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBack) {
                    Text("←", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            UnifiedLedBar(value = dbLevel)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    VintageNeedleMeter(value = dbLevel, label = "L_NODE")
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    VintageNeedleMeter(value = dbLevel * 0.98f, label = "R_NODE")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        AnimatedVisibility(
            visible = isSliderVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SENSITIVITY",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "GAIN: ${(sensitivity).toString().take(4)}",
                        color = Color.White.copy(0.7f),
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = sensitivity,
                    onValueChange = { 
                        sensitivity = it
                        lastInteraction = System.currentTimeMillis()
                    },
                    valueRange = 0f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(0.1f)
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

@Composable
fun VintageNeedleMeter(value: Float, label: String) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "vu_animation"
    )
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val componentWidth = size.width
            val componentHeight = size.height
            val radius = (minOf(componentWidth, componentHeight) * 0.85f) / 2
            val center = Offset(componentWidth / 2, componentHeight * 0.7f)
            
            val arcWidth = radius * 2.64f
            val arcHeight = radius * 2.64f
            val arcTopLeft = Offset(center.x - arcWidth / 2, center.y - radius * 1.56f)

            drawRect(color = Color(0xFF0F172A))

            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
                    center = center,
                    radius = radius * 1.8f
                ),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = arcTopLeft,
                size = Size(arcWidth, arcHeight),
                style = Fill
            )

            drawArc(
                color = Color.Black,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = Size(arcWidth, arcHeight),
                style = Stroke(width = 4f)
            )

            val scaleStart = 205f
            val scaleSweep = 130f
            val vuValues = listOf("20", "10", "7", "5", "3", "2", "1", "0", "3", "5")
            val vuAngles = listOf(0f, 0.15f, 0.25f, 0.35f, 0.45f, 0.55f, 0.65f, 0.75f, 0.88f, 1f)
            
            drawArc(
                color = Color(0xFF3B82F6).copy(0.8f),
                startAngle = scaleStart + (scaleSweep * 0.75f),
                sweepAngle = scaleSweep * 0.25f,
                useCenter = false,
                topLeft = Offset(center.x - (arcWidth * 0.85f) / 2, center.y - radius * 1.38f),
                size = Size(arcWidth * 0.85f, arcHeight * 0.85f),
                style = Stroke(width = 8f)
            )

            for (i in vuValues.indices) {
                val angle = scaleStart + (vuAngles[i] * scaleSweep)
                val rad = Math.toRadians(angle.toDouble())
                val isBlue = i >= 7
                
                val outerR = radius * 1.32f
                val tickLen = radius * 0.18f
                val outerX = center.x + Math.cos(rad).toFloat() * outerR
                val outerY = center.y + Math.sin(rad).toFloat() * outerR
                val innerX = center.x + Math.cos(rad).toFloat() * (outerR - tickLen)
                val innerY = center.y + Math.sin(rad).toFloat() * (outerR - tickLen)

                drawLine(
                    color = if (isBlue) Color(0xFF3B82F6) else Color.Black,
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = 3f
                )

                val textLayoutResult = textMeasurer.measure(
                    text = vuValues[i],
                    style = TextStyle(fontSize = (radius * 0.045f).sp, fontWeight = FontWeight.Bold, color = if (isBlue) Color(0xFF3B82F6) else Color.Black)
                )
                val textR = outerR + radius * 0.22f
                val textX = center.x + Math.cos(rad).toFloat() * textR - textLayoutResult.size.width / 2
                val textY = center.y + Math.sin(rad).toFloat() * textR - textLayoutResult.size.height / 2
                drawText(textLayoutResult, topLeft = Offset(textX, textY))
            }

            val percentValues = listOf("0", "20", "40", "60", "80", "100%")
            val percentR = radius * 1.02f
            for (i in percentValues.indices) {
                val angle = scaleStart + (i * (scaleSweep / 5f))
                val rad = Math.toRadians(angle.toDouble())
                
                val outerX = center.x + Math.cos(rad).toFloat() * percentR
                val outerY = center.y + Math.sin(rad).toFloat() * percentR
                
                val textLayoutResult = textMeasurer.measure(
                    text = percentValues[i],
                    style = TextStyle(fontSize = (radius * 0.045f).sp, fontWeight = FontWeight.Medium, color = Color.Black.copy(0.7f))
                )
                val textX = outerX - textLayoutResult.size.width / 2
                val textY = outerY - textLayoutResult.size.height / 2
                drawText(textLayoutResult, topLeft = Offset(textX, textY))
            }

            val vuTextLayout = textMeasurer.measure(
                text = "VU",
                style = TextStyle(fontSize = (radius * 0.2f).sp, fontWeight = FontWeight.Black, color = Color.Black.copy(0.6f))
            )
            drawText(vuTextLayout, topLeft = Offset(center.x - vuTextLayout.size.width / 2, center.y - radius * 0.72f))

            val baseWidth = radius * 1.2f
            drawRect(
                color = Color.Black,
                topLeft = Offset(center.x - baseWidth / 2, center.y - 10f),
                size = Size(baseWidth, 30f)
            )
            val screwOffset = baseWidth * 0.4f
            drawCircle(color = Color.LightGray, radius = 8f, center = Offset(center.x - screwOffset, center.y + 5f))
            drawCircle(color = Color.DarkGray, radius = 3f, center = Offset(center.x - screwOffset, center.y + 5f), style = Stroke(2f))
            drawCircle(color = Color.LightGray, radius = 8f, center = Offset(center.x + screwOffset, center.y + 5f))
            drawCircle(color = Color.DarkGray, radius = 3f, center = Offset(center.x + screwOffset, center.y + 5f), style = Stroke(2f))

            val rotationAngle = scaleStart + (animatedValue * (scaleSweep / 100f))
            rotate(degrees = rotationAngle - 268f, pivot = center) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.2f),
                    start = center,
                    end = Offset(center.x, center.y - radius * 1.56f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            rotate(degrees = rotationAngle - 270f, pivot = center) {
                drawLine(
                    color = Color(0xFF3B82F6),
                    start = center,
                    end = Offset(center.x, center.y - radius * 1.62f),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
            }

            drawCircle(color = Color.Black, radius = 15f, center = center)
            drawCircle(color = Color.DarkGray, radius = 6f, center = center)
        }
        
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }
}

@Composable
fun NavidromeConfigScreen(
    serverUrl: String,
    username: String,
    password: String,
    connectionStatus: String,
    onUrlChange: (String) -> Unit,
    onUserChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onTestAndConnect: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NAVIDROME SERVER SETUP",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SAVED & SECURED FOR FUTURE CONNECTIONS",
                fontSize = 10.sp,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = serverUrl,
                onValueChange = onUrlChange,
                label = { Text("Server URL (e.g. http://192.168.1.58:4533)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = onUserChange,
                label = { Text("Username") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPassChange,
                label = { Text("Password") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (connectionStatus.isNotEmpty()) {
                Text(
                    text = connectionStatus,
                    color = if (connectionStatus.contains("Connected")) Primary else Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onTestAndConnect,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("SAVE & CONNECT", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Surface),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("BACK TO HUB", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NavidromeLibraryScreen(
    albums: List<AlbumID3>,
    artists: List<ArtistID3>,
    localSongs: List<Song>,
    playlists: List<Playlist>,
    genres: List<Genre>,
    starredSongs: List<Song>,
    historySongs: List<Song>,
    initialTab: String = "albums",
    isLoading: Boolean,
    serverUrl: String,
    username: String,
    password: String,
    playerState: PlayerState,
    onAlbumClick: (AlbumID3) -> Unit,
    onArtistClick: (ArtistID3) -> Unit,
    onGenreClick: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<Song>,
    onSortByDateAdded: () -> Unit,
    onSortByName: () -> Unit,
    onSongClick: (Song, List<Song>, Int) -> Unit,
    onOpenPlayer: () -> Unit,
    onPlayPause: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(initialTab) }
    var showBulkDownloadDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("←", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "HI-FI LIBRARY",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Row {
                    Button(
                        onClick = { showBulkDownloadDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("⬇ Descarga", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (playerState.currentSong != null) {
                        Button(
                            onClick = onOpenPlayer,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("PLAYER", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.length >= 2) {
                        onSearch(it)
                        selectedTab = "search"
                    }
                },
                placeholder = { Text("Search FLAC, MP3, WAV...", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    "albums" to "Albums",
                    "artists" to "Artists",
                    "device" to "Device",
                    "genres" to "Genres",
                    "favorites" to "Favs"
                )
                items(tabs) { (key, label) ->
                    Button(
                        onClick = { selectedTab = key },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == key) Primary else Surface),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (selectedTab == key) Color.Black else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else {
                    when (selectedTab) {
                        "albums" -> {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "SORT BY:", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Row {
                                        Button(onClick = onSortByDateAdded, colors = ButtonDefaults.buttonColors(containerColor = Surface), modifier = Modifier.padding(end = 4.dp)) {
                                            Text("Date Added", color = TextPrimary, fontSize = 10.sp)
                                        }
                                        Button(onClick = onSortByName, colors = ButtonDefaults.buttonColors(containerColor = Surface)) {
                                            Text("Name", color = TextPrimary, fontSize = 10.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                    items(albums) { album ->
                                        val coverUrl = NavidromeClientManager.getCoverArtUrl(serverUrl, album.coverArt, album.id, username, password)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onAlbumClick(album) }
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (coverUrl.isNotEmpty()) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(coverUrl),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Surface),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("🎵", color = Primary)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(album.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(album.artist ?: "Unknown", color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "artists" -> {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(artists) { artist ->
                                    val coverUrl = NavidromeClientManager.getCoverArtUrl(serverUrl, artist.coverArt, artist.id, username, password)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onArtistClick(artist) }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (coverUrl.isNotEmpty()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(coverUrl),
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp).clip(CircleShape)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.size(48.dp).clip(CircleShape).background(Surface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🎤", color = Primary)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(artist.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${artist.albumCount ?: 0} albums", color = TextSecondary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                        "device" -> {
                            Column {
                                Text(text = "DEVICE LOCAL AUDIO (${localSongs.size})", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                    items(localSongs) { song ->
                                        val formatBadge = song.suffix?.uppercase() ?: "MP3"
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSongClick(song, localSongs, localSongs.indexOf(song)) }
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Surface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🎵", color = Primary)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(song.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("${song.artist} • ${song.album}", color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(Primary, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(formatBadge, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "search" -> {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(searchResults) { song ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { onSongClick(song, searchResults, searchResults.indexOf(song)) }.padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(song.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text("${song.artist} • ${song.album}", color = TextSecondary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                        "genres" -> {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(genres) { genre ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onGenreClick(genre.value) }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(genre.value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text("${genre.songCount} songs →", color = Primary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        "favorites" -> {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(starredSongs) { song ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { onSongClick(song, starredSongs, starredSongs.indexOf(song)) }.padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⭐ ", fontSize = 14.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(song.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text(song.artist ?: "Unknown", color = TextSecondary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (playerState.currentSong != null) {
                Spacer(modifier = Modifier.height(4.dp))
                MiniPlayerBar(
                    playerState = playerState,
                    serverUrl = serverUrl,
                    username = username,
                    password = password,
                    onPlayPause = onPlayPause,
                    onClick = onOpenPlayer
                )
            }
        }
    }

    if (showBulkDownloadDialog) {
        DownloadFolderDialog(
            songTitle = "Toda la biblioteca (${localSongs.size + starredSongs.size} pistas)",
            onDismiss = { showBulkDownloadDialog = false },
            onConfirmDownload = { folder ->
                showBulkDownloadDialog = false
                val allSongsToDownload = (localSongs + starredSongs).distinctBy { it.id }
                Toast.makeText(context, "Iniciando descarga masiva de ${allSongsToDownload.size} pistas...", Toast.LENGTH_SHORT).show()
                downloadAllSongsToFolder(context, allSongsToDownload, folder, serverUrl, username, password) { count ->
                    Toast.makeText(context, "¡Descarga masiva completada! $count archivos guardados en: $folder", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

@Composable
fun MiniPlayerBar(
    playerState: PlayerState,
    serverUrl: String,
    username: String,
    password: String,
    onPlayPause: () -> Unit,
    onClick: () -> Unit
) {
    val song = playerState.currentSong ?: return
    val coverUrl = NavidromeClientManager.getCoverArtUrl(serverUrl, song.coverArt, song.albumId, username, password)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Surface, RoundedCornerShape(12.dp))
            .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (coverUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(coverUrl),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(Background), contentAlignment = Alignment.Center) {
                        Text("🎵", fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist ?: "Unknown", color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = onPlayPause) {
                Text(if (playerState.isPlaying) "||" else "▶", fontSize = 18.sp, color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NavidromeAlbumDetailScreen(
    albumTitle: String,
    coverArtId: String?,
    songs: List<Song>,
    serverUrl: String,
    username: String,
    password: String,
    playerState: PlayerState,
    onSongClick: (Song, List<Song>, Int) -> Unit,
    onOpenPlayer: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDownloadDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("←", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = albumTitle,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Row {
                    Button(
                        onClick = { showDownloadDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("⬇ Descarga", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    if (playerState.currentSong != null) {
                        Button(
                            onClick = onOpenPlayer,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("PLAYER", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(songs) { song ->
                    val formatBadge = song.suffix?.uppercase() ?: "MP3"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSongClick(song, songs, songs.indexOf(song)) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${song.track ?: 1}",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.width(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = if (playerState.currentSong?.id == song.id) Primary else TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (formatBadge) {
                                                "FLAC" -> Color(0xFF3B82F6)
                                                "WAV" -> Color(0xFF60A5FA)
                                                "AAC" -> Color(0xFF93C5FD)
                                                "OGG" -> Color(0xFF1E40AF)
                                                else -> Color(0xFF2563EB)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = formatBadge,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${(song.duration ?: 0) / 60}:${String.format("%02d", (song.duration ?: 0) % 60)}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDownloadDialog) {
        DownloadFolderDialog(
            songTitle = "Álbum completo ($albumTitle - ${songs.size} pistas)",
            onDismiss = { showDownloadDialog = false },
            onConfirmDownload = { folder ->
                showDownloadDialog = false
                Toast.makeText(context, "Iniciando descarga masiva de ${songs.size} pistas...", Toast.LENGTH_SHORT).show()
                downloadAllSongsToFolder(context, songs, folder, serverUrl, username, password) { count ->
                    Toast.makeText(context, "¡Descarga masiva completada! $count archivos guardados en: $folder", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

@Composable
fun NavidromeArtistAlbumsScreen(
    artistName: String,
    albums: List<AlbumID3>,
    serverUrl: String,
    username: String,
    password: String,
    playerState: PlayerState,
    onAlbumClick: (AlbumID3) -> Unit,
    onOpenPlayer: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("←", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = artistName,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                if (playerState.currentSong != null) {
                    Button(
                        onClick = onOpenPlayer,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("PLAYER", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(albums) { album ->
                    val coverUrl = NavidromeClientManager.getCoverArtUrl(serverUrl, album.coverArt, album.id, username, password)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumClick(album) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (coverUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(coverUrl),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎵", color = Primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(album.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${album.songCount ?: 0} songs • ${album.year ?: ""}", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadFolderDialog(
    songTitle: String,
    onDismiss: () -> Unit,
    onConfirmDownload: (String) -> Unit
) {
    var folderPath by remember { mutableStateOf("Music/SkinMusic") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Descargar Audio", color = TextPrimary) },
        text = {
            Column {
                Text("Pista: $songTitle", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = folderPath,
                    onValueChange = { folderPath = it },
                    label = { Text("Carpeta de destino") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmDownload(folderPath) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Descargar", color = Color.Black)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Surface)
            ) {
                Text("Cancelar", color = TextPrimary)
            }
        },
        containerColor = Surface
    )
}

@Composable
fun NavidromePlayerScreen(
    playerState: PlayerState,
    serverUrl: String,
    username: String,
    password: String,
    musicPlayerManager: MusicPlayerManager,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onVolume: (Float) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val song = playerState.currentSong
    val coverUrl = NavidromeClientManager.getCoverArtUrl(serverUrl, song?.coverArt, song?.albumId, username, password)

    var showLyrics by remember { mutableStateOf(false) }
    var showEqualizer by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showBulkDownloadDialog by remember { mutableStateOf(false) }

    var bassBoostVal by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) {
                        onPrevious()
                    } else if (dragAmount < -50) {
                        onNext()
                    }
                }
            }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: ⬇ Reproduciendo ⋮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("⬇", color = TextPrimary, fontSize = 20.sp)
                }
                Text(
                    text = "Reproduciendo",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { }) {
                    Text("⋮", color = TextPrimary, fontSize = 20.sp)
                }
            }

            // Album Image (Large card, 260dp height, rounded 20dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Surface)
            ) {
                if (coverUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(coverUrl),
                        contentDescription = "Album",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("🎵", fontSize = 64.sp)
                }
            }

            // Song Info (Title 24px, Artist gray)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = song?.title ?: "Cyber Dreams",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song?.artist ?: "Synthwave Artist",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress Bar & Time
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Slider(
                    value = playerState.currentPosition.toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..(playerState.duration.toFloat().coerceAtLeast(1f)),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = Primary,
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.height(12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val posSec = (playerState.currentPosition / 1000).toInt()
                    val durSec = (playerState.duration / 1000).toInt()
                    Text(text = "${posSec / 60}:${String.format("%02d", posSec % 60)}", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "${durSec / 60}:${String.format("%02d", durSec % 60)}", color = TextSecondary, fontSize = 12.sp)
                }
            }

            // Controls: Shuffle (⇄), Previous, Play/Pause (Clean text symbols), Next, Repeat (⟲)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffle) {
                    Text(
                        text = "⇄",
                        fontSize = 24.sp,
                        color = if (playerState.isShuffleEnabled) Primary else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onPrevious) {
                    Text(text = "⏮", fontSize = 28.sp, color = TextPrimary)
                }

                Button(
                    onClick = onPlayPause,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.size(70.dp).clip(CircleShape),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (playerState.isPlaying) "||" else "▶",
                        fontSize = 28.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onNext) {
                    Text(text = "⏭", fontSize = 28.sp, color = TextPrimary)
                }

                IconButton(onClick = onRepeat) {
                    val repeatSymbol = when (playerState.repeatMode) {
                        Player.REPEAT_MODE_ONE -> "⟳1"
                        Player.REPEAT_MODE_ALL -> "⟲"
                        else -> "⟲"
                    }
                    Text(
                        text = repeatSymbol,
                        fontSize = 20.sp,
                        color = if (playerState.repeatMode != Player.REPEAT_MODE_OFF) Primary else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom Icons: Heart, Music (Lyrics), Document (Queue / Download), Volume (Equalizer)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "❤", fontSize = 22.sp, color = TextSecondary, modifier = Modifier.clickable { })
                Text(text = "🎵", fontSize = 22.sp, color = Primary, modifier = Modifier.clickable { showLyrics = !showLyrics })
                Text(text = "📃", fontSize = 22.sp, color = Primary, modifier = Modifier.clickable { showQueue = !showQueue })
                Text(text = "🔊", fontSize = 22.sp, color = Primary, modifier = Modifier.clickable { showEqualizer = !showEqualizer })
            }

            // Overlays: Queue (with download buttons), Lyrics, Equalizer
            if (showLyrics) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Surface, RoundedCornerShape(12.dp))
                        .border(1.dp, Primary, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("♪ Synchronized LRC Karaoke ♪", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "[00:12] Playing ${song?.title ?: "Track"}\n[00:25] Enjoying Minimalist Blue Hi-Fi...",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (showEqualizer) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Surface, RoundedCornerShape(12.dp))
                        .border(1.dp, Primary, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("BAND EQUALIZER & AUDIO FX", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(onClick = { musicPlayerManager.setEqualizerPreset("Rock") }, colors = ButtonDefaults.buttonColors(containerColor = Background)) { Text("Rock", fontSize = 9.sp, color = TextPrimary) }
                            Button(onClick = { musicPlayerManager.setEqualizerPreset("Pop") }, colors = ButtonDefaults.buttonColors(containerColor = Background)) { Text("Pop", fontSize = 9.sp, color = TextPrimary) }
                            Button(onClick = { musicPlayerManager.setEqualizerPreset("Jazz") }, colors = ButtonDefaults.buttonColors(containerColor = Background)) { Text("Jazz", fontSize = 9.sp, color = TextPrimary) }
                            Button(onClick = { musicPlayerManager.setEqualizerPreset("Classical") }, colors = ButtonDefaults.buttonColors(containerColor = Background)) { Text("Classic", fontSize = 9.sp, color = TextPrimary) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Bass:", color = TextSecondary, fontSize = 10.sp)
                            Slider(
                                value = bassBoostVal,
                                onValueChange = {
                                    bassBoostVal = it
                                    musicPlayerManager.setBassBoost((it * 1000).toInt().toShort())
                                },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f).height(24.dp)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Surface, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("QUEUE & DOWNLOAD", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row {
                                if (song != null) {
                                    Button(
                                        onClick = { showDownloadDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("⬇ Pista", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Button(
                                    onClick = { showBulkDownloadDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("⬇ Masivo", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val queue = if (playerState.queue.isNotEmpty()) playerState.queue else listOf(
                            Song("1", "Midnight City", "M83", "Album", "1", 225, 1, 2011, "Electronic", 0, "mp3", "mp3", null, null),
                            Song("2", "Blinding Lights", "The Weeknd", "Album", "2", 242, 2, 2020, "Pop", 0, "mp3", "mp3", null, null),
                            Song("3", "Take On Me", "A-ha", "Album", "3", 232, 3, 1985, "Pop", 0, "mp3", "mp3", null, null)
                        )

                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(queue) { qSong ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = qSong.title,
                                            color = if (qSong.id == song?.id) Primary else TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = qSong.artist ?: "Unknown",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    val dur = qSong.duration ?: 0
                                    Text(
                                        text = "${dur / 60}:${String.format("%02d", dur % 60)}",
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDownloadDialog && song != null) {
        DownloadFolderDialog(
            songTitle = song.title,
            onDismiss = { showDownloadDialog = false },
            onConfirmDownload = { folder ->
                showDownloadDialog = false
                downloadSongToFolder(context, song, folder, serverUrl, username, password) { success, path ->
                    if (success) {
                        Toast.makeText(context, "Descargado en: $path", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error al descargar audio", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (showBulkDownloadDialog) {
        DownloadFolderDialog(
            songTitle = "Cola de reproducción completa (${playerState.queue.size} pistas)",
            onDismiss = { showBulkDownloadDialog = false },
            onConfirmDownload = { folder ->
                showBulkDownloadDialog = false
                val songsToDownload = if (playerState.queue.isNotEmpty()) playerState.queue else emptyList()
                Toast.makeText(context, "Iniciando descarga masiva de ${songsToDownload.size} pistas...", Toast.LENGTH_SHORT).show()
                downloadAllSongsToFolder(context, songsToDownload, folder, serverUrl, username, password) { count ->
                    Toast.makeText(context, "¡Descarga masiva completada! $count archivos guardados en: $folder", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}
