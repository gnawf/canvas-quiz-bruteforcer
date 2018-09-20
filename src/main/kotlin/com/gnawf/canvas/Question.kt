package com.gnawf.canvas

data class Question constructor(
  val id: Int,
  val text: String,
  val selected: Int?,
  val answers: List<Int>
)
