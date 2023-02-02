package org.kimobot.kimo.listener

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.kimobot.kimo.dao.UserDAO
import org.kimobot.kimo.dto.enums.Modals
import org.slf4j.LoggerFactory

class ModalListener : ListenerAdapter() {

  private val log = LoggerFactory.getLogger(javaClass)
  private val userDao = UserDAO()

  override fun onModalInteraction(event: ModalInteractionEvent) {
    when (Modals.getModal(event.modalId)) {
      Modals.TOKEN -> saveUserAndToken(event)
      null -> {}
    }
  }

  private fun saveUserAndToken(event: ModalInteractionEvent) {
    event.deferEdit().queue()
    val token = event.interaction.getValue(Modals.TOKEN.id)!!.asString
    val idUser = event.interaction.user.id
    userDao.newAuthenticatedUser(idUser, token)
    event.hook.deleteOriginal().queue()
  }
}