package org.kimobot.kimo.listener

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.kimobot.kimo.dao.UserDAO
import org.kimobot.kimo.dto.enums.ActionComponents
import org.kimobot.kimo.dto.enums.AniListStatus
import org.kimobot.kimo.dto.enums.Modals
import org.kimobot.kimo.service.AniListApi
import org.kimobot.kimo.util.Parser
import org.slf4j.LoggerFactory

class ModalListener : ListenerAdapter() {

  private val log = LoggerFactory.getLogger(javaClass)
  private val userDao = UserDAO()
  private val aniListApi = AniListApi()

  override fun onModalInteraction(event: ModalInteractionEvent) {
    when (Modals.getModal(event.modalId)) {
      Modals.TOKEN -> saveUserAndToken(event)
      Modals.ADD -> updateAniList(event)
      null -> {}
    }
  }

  private fun updateAniList(event: ModalInteractionEvent) {
    event.deferEdit().queue()

    val originalMessage = event.hook.retrieveOriginal().complete()
    val embed = originalMessage.embeds.firstOrNull()
    val parser = Parser()
    val footerData = parser.parseBetween(embed!!.footer!!.text) as List<String>
    val id = footerData[2].toInt()

    val idStatus = event.interaction.getValue(ActionComponents.STATUS_INPUT.id)!!.asString
    val aniListStatus = AniListStatus.getStatus(idStatus)
    if (aniListStatus == null) {
      event.reply("Status inválido").queue()
      return
    }

    val idUser = event.user.id
    val accessToken = userDao.getUserTokenById(idUser)

    aniListApi.addAnimeToList(id, aniListStatus.name, accessToken)
  }

  private fun saveUserAndToken(event: ModalInteractionEvent) {
    event.deferEdit().queue()
    val token = event.interaction.getValue(Modals.TOKEN.id)!!.asString
    val idUser = event.interaction.user.id
    userDao.newAuthenticatedUser(idUser, token)
    event.hook.deleteOriginal().queue()
  }
}