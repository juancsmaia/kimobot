package org.kimobot.kimo.dto.enums

enum class AniListType(val type: String) {
  ANIME("anime"), MANGA("manga");

  companion object {
    infix fun getType(nome: String): AniListType? =
      AniListType.values().firstOrNull { it.type == nome }

  }
}