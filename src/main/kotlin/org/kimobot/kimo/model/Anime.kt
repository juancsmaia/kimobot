package org.kimobot.kimo.model

class Anime(
  var id: Int? = null,
  var name: String? = null,
  var episodes: Int? = null,
  var watched: Int = 0,
  var url: String? = null,
  var rouletteIdx: Int = 0,
  var rouletteId: Int? = null
)