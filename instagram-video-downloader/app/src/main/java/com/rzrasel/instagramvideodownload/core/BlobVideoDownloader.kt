package com.rzrasel.instagramvideodownload.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class BlobVideoDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var webView: WebView? = null
    private val handler = Handler(Looper.getMainLooper())

    suspend fun downloadBlobVideo(blobUrl: String): ByteArray? {
        return suspendCancellableCoroutine { continuation ->
            try {
                handler.post {
                    setupWebView(blobUrl, continuation)
                }

                // Set timeout
                handler.postDelayed({
                    continuation.cancel(TimeoutException("Blob download timed out after ${Constants.BLOB_DOWNLOAD_TIMEOUT / 1000} seconds"))
                }, Constants.BLOB_DOWNLOAD_TIMEOUT)

                continuation.invokeOnCancellation {
                    cleanup()
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
                cleanup()
            }
        }
    }

    private fun setupWebView(blobUrl: String, continuation: CancellableContinuation<ByteArray?>) {
        webView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                cacheMode = WebSettings.LOAD_NO_CACHE
                loadWithOverviewMode = true
                useWideViewPort = true
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    evaluateBlobExtractionScript()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    continuation.resumeWithException(Exception("WebView error: ${error?.description}"))
                    cleanup()
                }
            }

            addJavascriptInterface(BlobInterface(continuation), "Android")

            loadDataWithBaseURL(
                "https://www.instagram.com/",
                createHtmlContent(blobUrl),
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    private fun evaluateBlobExtractionScript() {
        webView?.evaluateJavascript("""
            (function() {
                try {
                    const video = document.querySelector('video');
                    if (!video) {
                        Android.onBlobError('No video element found');
                        return;
                    }
                    
                    const xhr = new XMLHttpRequest();
                    xhr.open('GET', video.src, true);
                    xhr.responseType = 'blob';
                    
                    xhr.onload = function() {
                        if (this.status !== 200) {
                            Android.onBlobError('Failed to fetch blob: ' + this.status);
                            return;
                        }
                        
                        const reader = new FileReader();
                        reader.onload = function() {
                            try {
                                const bytes = new Uint8Array(this.result);
                                Android.onBlobLoaded(Array.from(bytes));
                            } catch (e) {
                                Android.onBlobError('Error processing blob: ' + e.message);
                            }
                        };
                        reader.onerror = function() {
                            Android.onBlobError('FileReader error: ' + this.error);
                        };
                        reader.readAsArrayBuffer(this.response);
                    };
                    
                    xhr.onerror = function() {
                        Android.onBlobError('XHR error loading blob');
                    };
                    
                    xhr.send();
                } catch (e) {
                    Android.onBlobError('JavaScript error: ' + e.message);
                }
            })();
        """.trimIndent(), null)
    }

    private fun createHtmlContent(blobUrl: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { margin:0; padding:0; background:transparent; }
                    video { width:100%; height:auto; }
                </style>
            </head>
            <body>
                <video src="$blobUrl" autoplay playsinline controls></video>
            </body>
            </html>
        """.trimIndent()
    }

    private fun cleanup() {
        handler.removeCallbacksAndMessages(null)
        webView?.stopLoading()
        webView?.destroy()
        webView = null
    }

    private inner class BlobInterface(
        private val continuation: CancellableContinuation<ByteArray?>
    ) {
        @JavascriptInterface
        fun onBlobLoaded(data: List<Int>) {
            try {
                val byteArray = ByteArray(data.size) { i -> data[i].toByte() }
                continuation.resume(byteArray)
                cleanup()
            } catch (e: Exception) {
                continuation.resumeWithException(e)
                cleanup()
            }
        }

        @JavascriptInterface
        fun onBlobError(message: String) {
            continuation.resumeWithException(Exception(message))
            cleanup()
        }
    }
}