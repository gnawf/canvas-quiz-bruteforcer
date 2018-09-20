package com.gnawf

import com.gnawf.canvas.canvasIntegration
import com.gnawf.okhttp.CookieJar
import okhttp3.Cookie
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val okHttpClient: OkHttpClient = OkHttpClient.Builder()
  .cookieJar(CookieJar().apply {
    // Authenticate canvas
    add(Cookie.Builder()
      .hostOnlyDomain("canvas.auckland.ac.nz")
      .name("canvas_session")
      .value(Environment.token)
      .path("/")
      .expiresAt(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365))
      .secure()
      .httpOnly()
      .build())
  })
  .canvasIntegration()
  .build()
