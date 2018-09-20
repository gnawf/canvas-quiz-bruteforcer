package com.gnawf

import com.gnawf.canvas.QuizSolver
import okhttp3.HttpUrl

fun main(vararg args: String) {
  val url = HttpUrl.Builder()
    .scheme("https")
    .host("canvas.auckland.ac.nz")
    .addPathSegment("courses")
    .addPathSegment(args[0])
    .addPathSegment("quizzes")
    .addPathSegment(args[1])
    .build()

  QuizSolver(url).run()
}
