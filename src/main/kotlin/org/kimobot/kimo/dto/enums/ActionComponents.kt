package org.kimobot.kimo.dto.enums

enum class ActionComponents(val id: String, val tag: String) {
  TOKEN("token", "Token"),
  ANIME_ID("anime", "Anime ID"),
  ANIME_IDX("animeIdx", "ID"),
  ANIME_WATCHED("watched", "Watched"),
  ANIME_EDIT("edit", "Edit Watched"),
  ANIME_REMOVE("remove","Remove Anime"),
  ROULETTE_NAME("name", "Roulette Name");

  companion object {
    infix fun getComponent(id: String): ActionComponents? =
      ActionComponents.values().firstOrNull { it.id == id }
  }
}