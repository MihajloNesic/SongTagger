package io.gitlab.mihajlonesic.songtagger

class Song(val album: String, var title: String, var artist: String) {
    constructor() : this("", "", "")

}