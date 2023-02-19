package com.seeta.pekko.test

object PrintHelpers {
  def showInConsole(message: String, attentionGrabbing: Boolean = false): Unit = {
    if (attentionGrabbing) println("=" * 60)
    println(message)
    if (attentionGrabbing) println("=" * 60)
  }
}
