package com.pulsechat.app.data.model

data class PulseVideo(
    val id: String,
    val title: String,
    val channel: String,
    val thumbnailUrl: String,
    val views: String,
    val isShort: Boolean = false
) {
    val watchUrl: String get() = "https://www.youtube.com/watch?v=$id"
    val shortUrl: String get() = "https://www.youtube.com/shorts/$id"
}

object PulseFeed {
    /**
     * Public videos that generally allow embedding.
     * Avoid restricted / music-label videos that show "Video unavailable" in WebView.
     */
    val homeVideos: List<PulseVideo> = listOf(
        PulseVideo(
            "aqz-KE-bpKQ",
            "Big Buck Bunny",
            "Blender Foundation",
            "https://i.ytimg.com/vi/aqz-KE-bpKQ/hqdefault.jpg",
            "Open movie"
        ),
        PulseVideo(
            "eRsGyueVLvQ",
            "Sintel — Open Movie",
            "Blender Foundation",
            "https://i.ytimg.com/vi/eRsGyueVLvQ/hqdefault.jpg",
            "Open movie"
        ),
        PulseVideo(
            "YE7VzlLtp-4",
            "Big Buck Bunny 1080p",
            "Blender",
            "https://i.ytimg.com/vi/YE7VzlLtp-4/hqdefault.jpg",
            "Open movie"
        ),
        PulseVideo(
            "ScMzIvxBSi4",
            "Nature Relaxation — 4K",
            "Nature",
            "https://i.ytimg.com/vi/ScMzIvxBSi4/hqdefault.jpg",
            "Relax"
        ),
        PulseVideo(
            "LXb3EKWsInQ",
            "Costa Rica in 4K",
            "Jacob + Katie Schwarz",
            "https://i.ytimg.com/vi/LXb3EKWsInQ/hqdefault.jpg",
            "Travel"
        ),
        PulseVideo(
            "dQw4w9WgXcQ",
            "Never Gonna Give You Up",
            "Rick Astley",
            "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            "Classic"
        ),
        PulseVideo(
            "jNQXAC9IVRw",
            "Me at the zoo",
            "jawed",
            "https://i.ytimg.com/vi/jNQXAC9IVRw/hqdefault.jpg",
            "First YouTube video"
        ),
        PulseVideo(
            "M7lc1UVf-VE",
            "YouTube Developers Live",
            "Google Developers",
            "https://i.ytimg.com/vi/M7lc1UVf-VE/hqdefault.jpg",
            "API demo"
        )
    )

    val shorts: List<PulseVideo> = listOf(
        PulseVideo("aqz-KE-bpKQ", "Big Buck Bunny", "Blender", "https://i.ytimg.com/vi/aqz-KE-bpKQ/hqdefault.jpg", "Open", true),
        PulseVideo("eRsGyueVLvQ", "Sintel", "Blender", "https://i.ytimg.com/vi/eRsGyueVLvQ/hqdefault.jpg", "Open", true),
        PulseVideo("YE7VzlLtp-4", "Bunny 1080p", "Blender", "https://i.ytimg.com/vi/YE7VzlLtp-4/hqdefault.jpg", "Open", true),
        PulseVideo("ScMzIvxBSi4", "Nature 4K", "Nature", "https://i.ytimg.com/vi/ScMzIvxBSi4/hqdefault.jpg", "Relax", true),
        PulseVideo("LXb3EKWsInQ", "Costa Rica", "Travel", "https://i.ytimg.com/vi/LXb3EKWsInQ/hqdefault.jpg", "Travel", true),
        PulseVideo("dQw4w9WgXcQ", "Rick Roll", "Rick Astley", "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", "Classic", true),
        PulseVideo("jNQXAC9IVRw", "Zoo", "jawed", "https://i.ytimg.com/vi/jNQXAC9IVRw/hqdefault.jpg", "First", true),
        PulseVideo("M7lc1UVf-VE", "YouTube API", "Google", "https://i.ytimg.com/vi/M7lc1UVf-VE/hqdefault.jpg", "Dev", true)
    )
}
