package org.kimobot.kimo.dto.enums

enum class Modals(val id: String, val tag: String) {
  TOKEN("token", "Token"),
  ADD("add","Add");

  companion object {
    infix fun getModal(id: String): Modals? =
      Modals.values().firstOrNull { it.id == id }
  }
}