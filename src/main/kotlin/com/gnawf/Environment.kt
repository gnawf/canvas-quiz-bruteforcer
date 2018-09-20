package com.gnawf

object Environment {

  val userId: String = System.getenv("USER_ID")

  val token: String = System.getenv("CANVAS_TOKEN")

  val userAgent: String = System.getenv("USER_AGENT")

}
