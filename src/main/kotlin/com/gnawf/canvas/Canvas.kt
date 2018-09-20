package com.gnawf.canvas

import com.gnawf.Environment
import com.gnawf.extensions.decodeName
import com.gnawf.extensions.decodeValue
import okhttp3.Cookie
import okhttp3.OkHttpClient

object Canvas {

  var csrfToken: String = ""

}

fun OkHttpClient.Builder.canvasIntegration(): OkHttpClient.Builder {
  return addInterceptor scope@{ chain ->
    val request = chain.request()

    val url = request.url()

    if (url.host() != "canvas.auckland.ac.nz") {
      return@scope chain.proceed(request)
    }

    val authenticated = request.newBuilder().apply {
      if (Canvas.csrfToken != "") {
        header("X-CSRF-Token", Canvas.csrfToken)
      }
      header("User-Agent", Environment.userAgent)
    }.build()

    val response = chain.proceed(authenticated)

    // Parse the cookies
    val host = response.request().url()
    response.headers("Set-Cookie").forEach { header ->
      val cookie = Cookie.parse(host, header) ?: return@forEach

      // Store the CSRF token
      when (cookie.decodeName()) {
        "_csrf_token" -> Canvas.csrfToken = cookie.decodeValue()
      }
    }

    return@scope response
  }
}
