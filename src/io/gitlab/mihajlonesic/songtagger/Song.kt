package io.gitlab.mihajlonesic.songtagger

class Song(var album: String, var title: String, var artist: String, var trackNumber: String, var year: String, var genre: String) {
    constructor() : this("", "", "", "", "", "")
    constructor(album: String, title: String, artist: String) : this(album, title, artist, "", "", "")
}