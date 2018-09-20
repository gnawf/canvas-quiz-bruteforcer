package com.gnawf.okhttp

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CookieJar : CookieJar {

  private val cookies: MutableList<Cookie> = ArrayList()

  fun add(cookie: Cookie) {
    synchronized(cookies) {
      cookies.add(cookie)
    }
  }

  override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
    synchronized(this.cookies) {
      for (cookie in cookies) {
        // Remove traces of the old cookie, simple check
        this.cookies.removeAll { candidate ->
          cookie.name() == candidate.name()
            && cookie.domain() == candidate.domain()
        }
        // Add the cookie again
        this.cookies.add(cookie)
      }
    }
  }

  override fun loadForRequest(url: HttpUrl?): MutableList<Cookie> {
    url ?: return arrayListOf()

    synchronized(cookies) {
      val cookies = ArrayList<Cookie>()

      val iterator = this.cookies.iterator()

      while (iterator.hasNext()) {
        val cookie = iterator.next()

        // Handle expired cookies
        if (cookie.expiresAt() <= System.currentTimeMillis()) {
          iterator.remove()
          continue
        }

        if (cookie.matches(url)) {
          cookies.add(cookie)
        }
      }

      return cookies
    }
  }

}
