package org.kimobot.kimo.dto.enums

enum class Modals(val id: String, val tag: String) {
  TOKEN("token", "Token"),
  ANIME_ADD("add","Add"),
  ANIME_REMOVE("remove", "Remove"),
  ANIME_EDIT("edit","Edit"),
  INFO_ANIME_ADD("infoAdd", "Add to Roulette")
  ;

  companion object {
    infix fun getModal(id: String): Modals? =
      Modals.values().firstOrNull { it.id == id }
  }
}