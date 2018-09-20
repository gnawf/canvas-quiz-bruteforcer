package com.gnawf.canvas

data class Quiz constructor(
  val id: Int,
  val courseId: Int,
  val attempt: Int,
  val questions: List<Question>,
  val token: String
)
