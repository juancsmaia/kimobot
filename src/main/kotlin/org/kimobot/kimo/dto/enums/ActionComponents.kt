package org.kimobot.kimo.dto.enums

enum class ActionComponents(val id: String, val tag: String) {
  TOKEN("token", "Token");

  companion object {
    infix fun getComponent(id: String): ActionComponents? =
     ActionComponents.values().firstOrNull {it.id == id}
  }
}