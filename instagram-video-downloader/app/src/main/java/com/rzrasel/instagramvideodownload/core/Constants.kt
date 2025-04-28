package com.rzrasel.instagramvideodownload.core

object Constants {
    const val BASE_URL = "https://www.instagram.com/"
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    const val VIDEO_FILE_PREFIX = "instagram_"
    const val VIDEO_FILE_EXTENSION = ".mp4"
    const val MAX_VIDEO_SIZE_BYTES = 50 * 1024 * 1024
    const val BLOB_DOWNLOAD_TIMEOUT = 60_000L
    const val MAX_RETRIES = 3
    const val INITIAL_RETRY_DELAY = 1000L

    val INSTAGRAM_URL_REGEX = Regex(
        "^https?://(?:www\\.)?instagram\\.com/(?:p|reel|tv|reels)/([A-Za-z0-9_-]+)/?.*$",
        RegexOption.IGNORE_CASE
    )

    val VIDEO_URL_PATTERNS = listOf(
        Regex("""video_url":"(https?:\\?/\\?/[^"]+\.mp4[^"]*)"""),
        Regex("""video_url":"(https?:\\?/\\?/[^"]+)"""),
        Regex("""video_versions":\[.*?"url":"(https?:\\?/\\?/[^"]+)"""),
        Regex("""contentUrl":"(https?:\\?/\\?/[^"]+\.mp4[^"]*)"""),
        Regex("""video_url\\\\":\\\\"(https?:\\\\?/\\\\?/[^"]+)"""),
        Regex("""(https:\\?/\\?/[^"]+\.mp4)"""),
        Regex("""property="og:video" content="([^"]+)"""),
        Regex("""<meta property="og:video" content="([^"]+)""")
    )
}