package org.kimobot.kimo.model

import com.fasterxml.jackson.annotation.JsonProperty

class Roulette(
  var id: Int? = null,
  var name: String,
  var active: Boolean = false,
  @JsonProperty("guild_id") var guildId: String? = null
)