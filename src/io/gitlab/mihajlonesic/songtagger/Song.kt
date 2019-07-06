package io.gitlab.mihajlonesic.songtagger

class Song(var album: String, var title: String, var artist: String, var trackNumber: String, var year: String, var genre: String) {
    constructor() : this("", "", "", "", "", "")

    override fun toString(): String {
        return "Song(album='$album', title='$title', artist='$artist', trackNumber='$trackNumber', year='$year', genre='$genre')"
    }

}