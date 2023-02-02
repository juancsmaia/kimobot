package org.kimobot.kimo.dto.enums

enum class AniListStatus(val id: String, val tag: String) {
  CURRENT("current", "Watching/Reading"),
  PLANNING("planning", "Plan-To"),
  COMPLETED("completed", "Completed"),
  DROPPED("dropped", "Dropped"),
  PAUSED("paused", "On Hold"),
  REPEATING("repeat", "Repeating");

  companion object {
    fun getStatus(id: String): AniListStatus? =
      AniListStatus.values().firstOrNull { it.id == id }
  }

}