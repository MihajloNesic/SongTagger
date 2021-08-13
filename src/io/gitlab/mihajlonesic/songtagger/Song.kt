package io.gitlab.mihajlonesic.songtagger

class Song(var album: String, var title: String, var artist: String, var trackNumber: String, var year: String, var genre: String) {
    constructor() : this("", "", "", "", "", "")

    override fun toString(): String {
        return """
            {
                "title": "$title",
                "artist": "$artist",
                "album": "$album",
                "trackNumber": $trackNumber,
                "year": "$year",
                "genre": "$genre"
            }
        """.trimIndent()
    }

}