plugins {
  application
  kotlin("jvm") version "1.2.61"
}

repositories {
  jcenter()
}

dependencies {
  compile(group = "org.jetbrains.kotlin", name = "kotlin-stdlib")
  compile(group = "org.jsoup", name = "jsoup", version = "1.11.3")
  compile(group = "com.google.code.gson", name = "gson", version = "2.8.5")
  compile(group = "com.squareup.okhttp3", name = "okhttp", version = "3.11.0")
}
