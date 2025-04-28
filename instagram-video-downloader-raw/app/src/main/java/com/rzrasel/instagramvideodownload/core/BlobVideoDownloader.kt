package com.rzrasel.instagramvideodownload.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.*
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import kotlin.coroutines.Continuation

class BlobVideoDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var currentContinuation: Continuation<ByteArray?>? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    suspend fun downloadBlobVideo(blobUrl: String): ByteArray? {
        return suspendCoroutine { continuation ->
            currentContinuation = continuation

            timeoutRunnable = Runnable {
                cleanup()
                continuation.resumeWithException(TimeoutException("Blob download timed out"))
            }
            handler.postDelayed(timeoutRunnable!!, Constants.BLOB_DOWNLOAD_TIMEOUT)

            handler.post {
                val webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            evaluateJavascript("""
                                (function() {
                                    var video = document.querySelector('video[src^="blob:"]');
                                    if (video) {
                                        var xhr = new XMLHttpRequest();
                                        xhr.open('GET', video.src, true);
                                        xhr.responseType = 'blob';
                                        xhr.onload = function(e) {
                                            if (this.status == 200) {
                                                var blob = this.response;
                                                var reader = new FileReader();
                                                reader.onload = function(event) {
                                                    var result = event.target.result;
                                                    Android.onBlobLoaded(JSON.stringify({
                                                        data: Array.from(new Uint8Array(result)),
                                                        type: blob.type
                                                    }));
                                                };
                                                reader.readAsArrayBuffer(blob);
                                            }
                                        };
                                        xhr.send();
                                    }
                                })();
                            """.trimIndent(), null)
                        }
                    }

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onBlobLoaded(data: String) {
                            try {
                                val json = JSONObject(data)
                                val byteArray = json.getJSONArray("data")
                                    .let { ByteArray(it.length()) { i -> it.getInt(i).toByte() } }
                                continuation.resume(byteArray)
                                cleanup()
                            } catch (e: Exception) {
                                continuation.resumeWithException(e)
                                cleanup()
                            }
                        }
                    }, "Android")
                }

                // Load a simple HTML page with the video
                webView.loadDataWithBaseURL(
                    "https://www.instagram.com/",
                    """
                    <html>
                        <body>
                            <video src="$blobUrl" autoplay playsinline></video>
                        </body>
                    </html>
                    """,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
    }

    private fun cleanup() {
        timeoutRunnable?.let { handler.removeCallbacks(it) }
        timeoutRunnable = null
        currentContinuation = null
    }
}