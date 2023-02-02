package org.kimobot.kimo.listener

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.kimobot.kimo.dto.AniListDTO
import org.kimobot.kimo.dto.enums.*
import org.kimobot.kimo.service.AniListApi
import org.kimobot.kimo.service.MessageSender
import org.kimobot.kimo.util.Parser

class ButtonListener : ListenerAdapter() {

  private val messageSender = MessageSender()
  private val aniListApi = AniListApi()

  override fun onButtonInteraction(event: ButtonInteractionEvent) {
    when (Buttons.getButton(event.componentId)) {
      Buttons.NEXT -> nextPage(event)
      Buttons.BACK -> backPage(event)
      Buttons.TOKEN -> receiveToken(event)
      Buttons.ADD -> addToList(event)
      else -> {}
    }

  }

  private fun addToList(event: ButtonInteractionEvent) {
    val statusInput =
      TextInput.create(ActionComponents.STATUS_INPUT.id, ActionComponents.STATUS_INPUT.tag, TextInputStyle.SHORT)
        .setPlaceholder("current, planning, completed, dropped, paused, repeat")
        .build()

    val modal = Modal.create(Modals.ADD.id, Modals.ADD.tag).addActionRow(statusInput).build()
    event.replyModal(modal).queue()
  }

  private fun backPage(event: ButtonInteractionEvent) {
    val tipoBusca = getMetaData(event) as List<String>
    val tipo = tipoBusca[0]
    val busca = tipoBusca[1]
    val pagina: Int = getPage(event, false)
    mountPage(event, busca, tipo, pagina)
  }

  private fun nextPage(event: ButtonInteractionEvent) {
    val tipoBusca = getMetaData(event) as List<String>
    val tipo = tipoBusca[0]
    val busca = tipoBusca[1]
    val pagina: Int = getPage(event, true)
    mountPage(event, busca, tipo, pagina)
  }

  private fun getMetaData(event: ButtonInteractionEvent): List<String?>? {
    val embed = event.message.embeds[0]
    val footer = embed.footer
    val parser = Parser()
    return parser.parseBetween(footer!!.text)
  }

  private fun getPage(event: ButtonInteractionEvent, next: Boolean): Int {
    val embed = event.message.embeds[0]
    val title = embed.title!!
    val actual: Int = title.split(" ")[1].toInt()
    return if (next) {
      actual + 1
    } else actual - 1
  }

  private fun mountPage(
    event: ButtonInteractionEvent, busca: String, tipo: String, pagina: Int
  ) {

    val type = AniListType.getType(tipo.lowercase())

    event.deferEdit().queue()
    val aniListDTO: AniListDTO = aniListApi.getAnime(busca, tipo, pagina)
    if (aniListDTO.data!!.page!!.media!!.isEmpty()) {
      return
    }

    val me: MessageEmbed = messageSender.mountAniListMessageInfo(type!!.name, busca, aniListDTO)
    val pageInfo = aniListDTO.data!!.page!!.pageInfo
    val botoes = if (pagina > 1) {
      if (pageInfo!!.hasNextPage!!) {
        listOf(
          Button.primary(Buttons.BACK.id, Buttons.BACK.tag),
          Button.primary(Buttons.NEXT.id, Buttons.NEXT.tag),
          Button.success(Buttons.ADD.id, Buttons.ADD.tag)
        )
      } else {
        listOf(Button.primary(Buttons.BACK.id, Buttons.BACK.tag), Button.success(Buttons.ADD.id, Buttons.ADD.tag))
      }
    } else {
      listOf(Button.primary(Buttons.NEXT.id, Buttons.NEXT.tag), Button.success(Buttons.ADD.id, Buttons.ADD.tag))
    }

    event.hook.editOriginalEmbeds(me).setActionRow(botoes).queue()
  }

  private fun receiveToken(event: ButtonInteractionEvent) {
    val tokenInput = TextInput.create(ActionComponents.TOKEN.id, ActionComponents.TOKEN.tag, TextInputStyle.PARAGRAPH)
      .setPlaceholder("Token de Autorização")
      .build()
    val modal = Modal.create(Modals.TOKEN.id, Modals.TOKEN.tag).addActionRow(tokenInput).build()
    event.replyModal(modal).queue()
  }
}