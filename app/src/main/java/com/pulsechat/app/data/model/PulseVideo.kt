package com.pulsechat.app.data.model

data class PulseVideo(
    val id: String,
    val title: String,
    val channel: String,
    val channelId: String,
    val thumbnailUrl: String,
    val views: String,
    /** Direct MP4 URL for custom ExoPlayer. */
    val streamUrl: String,
    val isShort: Boolean = false
)

data class PulseChannel(
    val id: String,
    val name: String,
    val description: String,
    val subscriberCount: String,
    val avatarColor: Long = 0xFFB14EFF
)

object PulseFeed {
    val channels: List<PulseChannel> = listOf(
        PulseChannel(
            id = "blender",
            name = "Blender Foundation",
            description = "Open movies and 3D animation from Blender.",
            subscriberCount = "2.1M subscribers",
            avatarColor = 0xFFB14EFF
        ),
        PulseChannel(
            id = "google",
            name = "Google Samples",
            description = "Sample media for developers and demos.",
            subscriberCount = "890K subscribers",
            avatarColor = 0xFF5B8DEF
        )
    )

    val homeVideos: List<PulseVideo> = listOf(
        PulseVideo(
            id = "bbb",
            title = "Big Buck Bunny",
            channel = "Blender Foundation",
            channelId = "blender",
            thumbnailUrl = "https://i.ytimg.com/vi/aqz-KE-bpKQ/hqdefault.jpg",
            views = "12M views",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ),
        PulseVideo(
            id = "elephants",
            title = "Elephant Dream",
            channel = "Blender Foundation",
            channelId = "blender",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
            views = "4.2M views",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        ),
        PulseVideo(
            id = "sintel",
            title = "Sintel",
            channel = "Blender Foundation",
            channelId = "blender",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
            views = "8.5M views",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        ),
        PulseVideo(
            id = "tears",
            title = "Tears of Steel",
            channel = "Blender Foundation",
            channelId = "blender",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
            views = "3.1M views",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        ),
        PulseVideo(
            id = "subaru",
            title = "Subaru Outback On Street",
            channel = "Google Samples",
            channelId = "google",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/SubaruOutbackOnStreetAndDirt.jpg",
            views = "520K views",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
        ),
        PulseVideo(
            id = "forbigger",
            title = "For Bigger Blazes",
            channel = "Google Samples",
            channelId = "google",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg",
            views = "310K views",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        ),
        PulseVideo(
            id = "forbiggerescapes",
            title = "For Bigger Escapes",
            channel = "Google Samples",
            channelId = "google",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg",
            views = "280K views",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
        ),
        PulseVideo(
            id = "forbiggerfun",
            title = "For Bigger Fun",
            channel = "Google Samples",
            channelId = "google",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerFun.jpg",
            views = "190K views",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
        )
    )

    val shorts: List<PulseVideo> = homeVideos.map {
        it.copy(isShort = true, id = "short_${it.id}")
    }

    fun videosForChannel(channelId: String): List<PulseVideo> =
        homeVideos.filter { it.channelId == channelId }

    fun search(query: String): List<PulseVideo> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return homeVideos
        return homeVideos.filter {
            it.title.lowercase().contains(q) ||
                it.channel.lowercase().contains(q) ||
                it.views.lowercase().contains(q)
        }
    }

    fun channelById(id: String): PulseChannel? = channels.find { it.id == id }
}
