package org.kimobot.kimo.dto.enums

enum class Command(val tag: String) {
  REGISTRATION("register"),
  INFO("info"),
  CLEAR("clear");

  companion object {
    infix fun pegarComando(nome: String): Command? =
      Command.values().firstOrNull { it.tag == nome }
  }

}