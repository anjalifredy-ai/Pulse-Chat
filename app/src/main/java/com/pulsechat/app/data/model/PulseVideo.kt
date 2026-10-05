package com.pulsechat.app.data.model

data class PulseVideo(
    val id: String,
    val title: String,
    val channel: String,
    val thumbnailUrl: String,
    val views: String,
    /** Direct MP4 URL for custom ExoPlayer (no YouTube chrome). */
    val streamUrl: String,
    val isShort: Boolean = false
)

object PulseFeed {
    /** Google sample + open movies — play reliably in ExoPlayer. */
    val homeVideos: List<PulseVideo> = listOf(
        PulseVideo(
            id = "bbb",
            title = "Big Buck Bunny",
            channel = "Blender Foundation",
            thumbnailUrl = "https://i.ytimg.com/vi/aqz-KE-bpKQ/hqdefault.jpg",
            views = "Open movie",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ),
        PulseVideo(
            id = "elephants",
            title = "Elephant Dream",
            channel = "Blender Foundation",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
            views = "Open movie",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        ),
        PulseVideo(
            id = "sintel",
            title = "Sintel",
            channel = "Blender Foundation",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
            views = "Open movie",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        ),
        PulseVideo(
            id = "tears",
            title = "Tears of Steel",
            channel = "Blender Foundation",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
            views = "Open movie",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        ),
        PulseVideo(
            id = "subaru",
            title = "Subaru Outback On Street",
            channel = "Google Samples",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/SubaruOutbackOnStreetAndDirt.jpg",
            views = "Sample",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
        ),
        PulseVideo(
            id = "forbigger",
            title = "For Bigger Blazes",
            channel = "Google Samples",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg",
            views = "Sample",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        )
    )

    val shorts: List<PulseVideo> = homeVideos.map {
        it.copy(isShort = true, id = "short_${it.id}")
    }
}
