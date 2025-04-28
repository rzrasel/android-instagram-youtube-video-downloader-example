package com.rzrasel.instagramvideodownload.instagramdownloader.domain.state

sealed class DownloadState {
    data class Success(val message: String) : DownloadState()
    data class Error(val message: String, val type: ErrorType) : DownloadState()

    enum class ErrorType {
        InvalidUrl,
        PrivateAccount,
        NoVideoContent,
        NetworkError,
        BlobUrl,
        StorageError,
        Unknown;

        companion object {
            fun fromException(e: Exception): ErrorType = when (e) {
                is java.net.SocketTimeoutException -> NetworkError
                is java.net.UnknownHostException -> NetworkError
                else -> Unknown
            }
        }
    }
}
