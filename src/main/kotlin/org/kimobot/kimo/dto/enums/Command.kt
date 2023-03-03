package org.kimobot.kimo.dto.enums

enum class Command(val tag: String) {
  HELP("help"),
  REGISTRATION("register"),
  INFO("info"),
  CLEAR("clear"),
  NEW_ROULETTE("newroul"),
  ROULETTE("roul"),
  CLOSE("close");

  companion object {
    infix fun pegarComando(nome: String): Command? =
      Command.values().firstOrNull { it.tag == nome }
  }

}