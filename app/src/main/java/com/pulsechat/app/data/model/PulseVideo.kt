package com.pulsechat.app.data.model

data class PulseVideo(
    val id: String,
    val title: String,
    val channel: String,
    val thumbnailUrl: String,
    val views: String,
    val isShort: Boolean = false
) {
    val embedUrl: String
        get() = if (isShort) {
            "https://www.youtube.com/embed/$id?playsinline=1&autoplay=1&rel=0&modestbranding=1"
        } else {
            "https://www.youtube.com/embed/$id?playsinline=1&autoplay=1&rel=0&modestbranding=1&fs=1"
        }

    val watchUrl: String get() = "https://www.youtube.com/watch?v=$id"
}

object PulseFeed {
    /** Curated public YouTube videos — works without YouTube Data API key. */
    val homeVideos: List<PulseVideo> = listOf(
        PulseVideo("jNQXAC9IVRw", "Me at the zoo", "jawed", "https://i.ytimg.com/vi/jNQXAC9IVRw/hqdefault.jpg", "300M views"),
        PulseVideo("kJQP7kiw5Fk", "Despacito", "Luis Fonsi", "https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg", "8B views"),
        PulseVideo("9bZkp7q19f0", "Gangnam Style", "PSY", "https://i.ytimg.com/vi/9bZkp7q19f0/hqdefault.jpg", "5B views"),
        PulseVideo("OPf0YbXqDm0", "Uptown Funk", "Mark Ronson", "https://i.ytimg.com/vi/OPf0YbXqDm0/hqdefault.jpg", "5B views"),
        PulseVideo("RgKAFK5djSk", "See You Again", "Wiz Khalifa", "https://i.ytimg.com/vi/RgKAFK5djSk/hqdefault.jpg", "6B views"),
        PulseVideo("fJ9rUzIMcZQ", "Bohemian Rhapsody", "Queen", "https://i.ytimg.com/vi/fJ9rUzIMcZQ/hqdefault.jpg", "1.9B views"),
        PulseVideo("hTWKbfoikeg", "Smells Like Teen Spirit", "Nirvana", "https://i.ytimg.com/vi/hTWKbfoikeg/hqdefault.jpg", "1.8B views"),
        PulseVideo("YQHsXMglC9A", "Hello", "Adele", "https://i.ytimg.com/vi/YQHsXMglC9A/hqdefault.jpg", "3B views"),
        PulseVideo("CevxZvSJLk8", "Roar", "Katy Perry", "https://i.ytimg.com/vi/CevxZvSJLk8/hqdefault.jpg", "3.8B views"),
        PulseVideo("pRpeEdMmmQ0", "Waka Waka", "Shakira", "https://i.ytimg.com/vi/pRpeEdMmmQ0/hqdefault.jpg", "3.8B views")
    )

    val shorts: List<PulseVideo> = listOf(
        PulseVideo("jNQXAC9IVRw", "First YouTube video", "jawed", "https://i.ytimg.com/vi/jNQXAC9IVRw/hqdefault.jpg", "300M", true),
        PulseVideo("kJQP7kiw5Fk", "Despacito Short", "Luis Fonsi", "https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg", "8B", true),
        PulseVideo("9bZkp7q19f0", "Gangnam", "PSY", "https://i.ytimg.com/vi/9bZkp7q19f0/hqdefault.jpg", "5B", true),
        PulseVideo("OPf0YbXqDm0", "Uptown Funk", "Mark Ronson", "https://i.ytimg.com/vi/OPf0YbXqDm0/hqdefault.jpg", "5B", true),
        PulseVideo("RgKAFK5djSk", "See You Again", "Wiz Khalifa", "https://i.ytimg.com/vi/RgKAFK5djSk/hqdefault.jpg", "6B", true),
        PulseVideo("fJ9rUzIMcZQ", "Bohemian", "Queen", "https://i.ytimg.com/vi/fJ9rUzIMcZQ/hqdefault.jpg", "1.9B", true),
        PulseVideo("hTWKbfoikeg", "Nirvana", "Nirvana", "https://i.ytimg.com/vi/hTWKbfoikeg/hqdefault.jpg", "1.8B", true),
        PulseVideo("YQHsXMglC9A", "Hello", "Adele", "https://i.ytimg.com/vi/YQHsXMglC9A/hqdefault.jpg", "3B", true),
        PulseVideo("CevxZvSJLk8", "Roar", "Katy Perry", "https://i.ytimg.com/vi/CevxZvSJLk8/hqdefault.jpg", "3.8B", true),
        PulseVideo("pRpeEdMmmQ0", "Waka Waka", "Shakira", "https://i.ytimg.com/vi/pRpeEdMmmQ0/hqdefault.jpg", "3.8B", true)
    )
}
