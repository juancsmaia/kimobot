package org.kimobot.kimo.dto.enums

enum class Buttons(val id: String, val tag: String) {

  NEXT("next", "->"),
  BACK("back", "<-"),
  TOKEN("token", "Enviar Token"),
  ADD_TO("add_to", "Add"),
  ADD("add", "Add"),
  EDIT("edit", "Editar"),
  ROLL("roll", "Sortear"),
  REMOVE("remove", "Remover"),
  ;

  companion object {
    infix fun getButton(id: String): Buttons? =
      Buttons.values().firstOrNull { it.id == id }
  }

}