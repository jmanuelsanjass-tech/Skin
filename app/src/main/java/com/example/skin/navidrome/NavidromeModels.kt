package com.example.skin.navidrome

data class SubsonicResponseWrapper(
    val `subsonic-response`: SubsonicResponse
)

data class SubsonicResponse(
    val status: String,
    val version: String,
    val error: SubsonicError?,
    val artists: ArtistsID3?,
    val artist: ArtistID3?,
    val albumList2: AlbumList2?,
    val album: AlbumDetail?,
    val searchResult3: SearchResult3?,
    val playlists: PlaylistsResponse?,
    val playlist: PlaylistDetail?,
    val genres: GenresResponse?,
    val starred2: Starred2?,
    val songsByGenre: SongsByGenre?
)

data class SubsonicError(
    val code: Int,
    val message: String
)

data class ArtistsID3(
    val index: List<IndexID3>?
)

data class IndexID3(
    val name: String,
    val artist: List<ArtistID3>?
)

data class ArtistID3(
    val id: String,
    val name: String,
    val albumCount: Int?,
    val coverArt: String?,
    val album: List<AlbumID3>?
)

data class AlbumList2(
    val album: List<AlbumID3>?
)

data class AlbumID3(
    val id: String,
    val name: String,
    val artist: String?,
    val artistId: String?,
    val coverArt: String?,
    val songCount: Int?,
    val duration: Int?,
    val year: Int?,
    val genre: String?,
    val created: String?
)

data class AlbumDetail(
    val id: String,
    val name: String,
    val artist: String?,
    val artistId: String?,
    val coverArt: String?,
    val songCount: Int?,
    val duration: Int?,
    val song: List<Song>?
)

data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumId: String?,
    val duration: Int?,
    val track: Int?,
    val year: Int?,
    val genre: String?,
    val size: Long?,
    val contentType: String?,
    val suffix: String?,
    val coverArt: String?,
    val created: String?
)

data class SearchResult3(
    val artist: List<ArtistID3>?,
    val album: List<AlbumID3>?,
    val song: List<Song>?
)

data class PlaylistsResponse(
    val playlist: List<Playlist>?
)

data class Playlist(
    val id: String,
    val name: String,
    val comment: String?,
    val songCount: Int?,
    val duration: Int?,
    val owner: String?,
    val public: Boolean?
)

data class PlaylistDetail(
    val id: String,
    val name: String,
    val song: List<Song>?
)

data class GenresResponse(
    val genre: List<Genre>?
)

data class Genre(
    val value: String,
    val songCount: Int,
    val albumCount: Int
)

data class Starred2(
    val artist: List<ArtistID3>?,
    val album: List<AlbumID3>?,
    val song: List<Song>?
)

data class SongsByGenre(
    val song: List<Song>?
)
