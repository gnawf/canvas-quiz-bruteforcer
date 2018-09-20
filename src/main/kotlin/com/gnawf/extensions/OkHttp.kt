package com.gnawf.extensions

import okhttp3.Cookie
import java.net.URLDecoder

fun Cookie.decodeName(): String {
  return URLDecoder.decode(name(), "utf-8")
}

fun Cookie.decodeValue(): String {
  return URLDecoder.decode(value(), "utf-8")
}
