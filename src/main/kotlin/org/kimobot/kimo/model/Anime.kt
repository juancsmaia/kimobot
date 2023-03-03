package org.kimobot.kimo.model

import com.fasterxml.jackson.annotation.JsonProperty

class Anime(
  var id: Int? = null,
  var name: String? = null,
  var episodes: Int? = null,
  var watched: Int = 0,
  var url: String? = null,
  @JsonProperty("roulette_index") var rouletteIdx: Int = 0,
  @JsonProperty("roulette_id") var rouletteId: Int? = null
)