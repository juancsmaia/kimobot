package org.kimobot.kimo.listener

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.apache.commons.lang3.RandomUtils
import org.kimobot.kimo.dao.AnimeDAO
import org.kimobot.kimo.dao.RouletteDAO
import org.kimobot.kimo.dto.AniListDTO
import org.kimobot.kimo.dto.enums.*
import org.kimobot.kimo.model.Anime
import org.kimobot.kimo.service.AniListApi
import org.kimobot.kimo.service.MessageSender
import org.kimobot.kimo.util.MessageUtil
import org.kimobot.kimo.util.Parser
import java.math.BigInteger

class ButtonListener : ListenerAdapter() {

  private val messageSender = MessageSender()
  private val aniListApi = AniListApi()
  private val animeDAO = AnimeDAO()
  private val rouletteDAO = RouletteDAO()

  override fun onButtonInteraction(event: ButtonInteractionEvent) {
    when (Buttons.getButton(event.componentId)) {
      Buttons.NEXT -> nextPage(event)
      Buttons.BACK -> backPage(event)
      Buttons.TOKEN -> receiveToken(event)
      Buttons.ADD -> addToRoulette(event)
      Buttons.ADD_TO -> addFromInfoToRoulette(event)
      Buttons.ROLL -> doRollAction(event)
      Buttons.EDIT -> doEditAnimeAction(event)
      Buttons.REMOVE -> doRemoveAction(event)
      else -> {}
    }

  }

  private fun addToRoulette(event: ButtonInteractionEvent) {
    val animeIdInput =
      TextInput.create(ActionComponents.ANIME_ID.id, ActionComponents.ANIME_ID.tag, TextInputStyle.SHORT)
        .build()

    val modal = Modal.create(Modals.ANIME_ADD.id, Modals.ANIME_ADD.tag).addActionRow(animeIdInput).build()
    event.replyModal(modal).queue()
  }

  private fun addFromInfoToRoulette(event: ButtonInteractionEvent) {
    val rouletteInput =
      TextInput.create(ActionComponents.ROULETTE_NAME.id, ActionComponents.ROULETTE_NAME.tag, TextInputStyle.SHORT)
        .build()

    val modal =
      Modal.create(Modals.INFO_ANIME_ADD.id, Modals.INFO_ANIME_ADD.tag)
        .addActionRows(ActionRow.of(rouletteInput)).build()
    event.replyModal(modal).queue()
  }

  private fun backPage(event: ButtonInteractionEvent) {
    val tipoBusca = MessageUtil.getMetaData(event) as List<*>
    val tipo = tipoBusca[0].toString()
    val busca = tipoBusca[1].toString()
    val pagina: Int = getPage(event, false)
    mountPage(event, busca, tipo, pagina)
  }

  private fun nextPage(event: ButtonInteractionEvent) {
    val tipoBusca = MessageUtil.getMetaData(event) as List<*>
    val tipo = tipoBusca[0].toString()
    val busca = tipoBusca[1].toString()
    val pagina: Int = getPage(event, true)
    mountPage(event, busca, tipo, pagina)
  }

  private fun getPage(event: ButtonInteractionEvent, next: Boolean): Int {
    val fields = event.message.embeds[0].fields
    val pageField = fields[fields.size - 1].value
    val actual: Int = pageField!!.split(" ")[1].toInt()
    return if (next) {
      actual + 1
    } else actual - 1
  }

  private fun mountPage(
    event: ButtonInteractionEvent, busca: String, tipo: String, pagina: Int
  ) {

    val type = AniListType.getType(tipo.lowercase())

    event.deferEdit().queue()
    val aniListDTO: AniListDTO = aniListApi.findAnimeSearch(busca, tipo, pagina)
    if (aniListDTO.data!!.page!!.media!!.isEmpty()) {
      return
    }

    val me: MessageEmbed = messageSender.mountAniListMessageInfo(type!!.name, busca, aniListDTO)
    val pageInfo = aniListDTO.data!!.page!!.pageInfo
    val botoes: ArrayList<ActionComponent> = if (pagina > 1) {
      if (pageInfo!!.hasNextPage!!) {
        arrayListOf(
          Button.primary(Buttons.BACK.id, Buttons.BACK.tag),
          Button.primary(Buttons.NEXT.id, Buttons.NEXT.tag),
        )
      } else {
        arrayListOf(Button.primary(Buttons.BACK.id, Buttons.BACK.tag))
      }
    } else {
      arrayListOf(Button.primary(Buttons.NEXT.id, Buttons.NEXT.tag))
    }

    botoes.add(Button.success(Buttons.ADD_TO.id, Buttons.ADD_TO.tag))

    event.hook.editOriginalEmbeds(me).setActionRow(botoes).queue()
  }

  private fun receiveToken(event: ButtonInteractionEvent) {
    val tokenInput = TextInput.create(ActionComponents.TOKEN.id, ActionComponents.TOKEN.tag, TextInputStyle.PARAGRAPH)
      .setPlaceholder("Authorization Token")
      .build()
    val modal = Modal.create(Modals.TOKEN.id, Modals.TOKEN.tag).addActionRow(tokenInput).build()
    event.replyModal(modal).queue()
  }

  private fun doRollAction(event: ButtonInteractionEvent) {
    if (!validateActiveRoulette(event)) {
      messageSender.sendMessage(event, MessageUtil.ROULETTE_INACTIVE)
      return
    }

    val texts = getMessageTitle(event)
    val rouletteId = Integer.valueOf(texts[0])
    val animes: List<Anime> = animeDAO.getAllByRoulette(rouletteId)
    val min = animes.minBy { it.rouletteIdx }
    val max = animes.maxBy { it.rouletteIdx }
    val sorted = RandomUtils.nextInt(min.rouletteIdx, max.rouletteIdx + 1)
    val sortedAnime = animeDAO.getByRouletteAndRouletteIdx(rouletteId, sorted)
    messageSender.sendRollResult(event, sortedAnime)
  }

  private fun doEditAnimeAction(event: ButtonInteractionEvent) {
    val modal = editAnimeModal()
    event.replyModal(modal).queue()
  }

  private fun editAnimeModal(): Modal {
    val textAnimeIdx =
      TextInput.create(ActionComponents.ANIME_IDX.id, ActionComponents.ANIME_IDX.tag, TextInputStyle.SHORT)
        .setPlaceholder("Anime's Roulette ID")
        .build()

    val textAnimeWatched =
      TextInput.create(ActionComponents.ANIME_WATCHED.id, ActionComponents.ANIME_WATCHED.tag, TextInputStyle.SHORT)
        .setPlaceholder("Number of watched episodes")
        .build()

    return Modal.create(Modals.ANIME_EDIT.id, Modals.ANIME_EDIT.tag)
      .addActionRows(ActionRow.of(textAnimeIdx), ActionRow.of(textAnimeWatched))
      .build()
  }

  private fun createRemoveAnimeModal(): Modal {
    val textInput =
      TextInput.create(ActionComponents.ANIME_ID.id, ActionComponents.ANIME_ID.tag, TextInputStyle.SHORT)
        .setPlaceholder("Anime's Roulette ID")
        .build()

    return Modal.create(Modals.ANIME_REMOVE.id, Modals.ANIME_REMOVE.tag).addActionRow(textInput).build()
  }

  private fun doRemoveAction(event: ButtonInteractionEvent) {
    val modal = createRemoveAnimeModal()
    event.replyModal(modal).queue()
  }

  private fun getMessageTitle(event: ButtonInteractionEvent): List<String> {
    val content = event.message.embeds
    val maybeMessage = content.stream().findFirst()
    return maybeMessage.get().title!!.replace("-", "").split(" ")
  }

  private fun validateActiveRoulette(event: ButtonInteractionEvent): Boolean {
    val texts = getMessageTitle(event)
    val rouletteName = texts[2]
    val guildId = event.guild!!.id
    val active = rouletteDAO.getRouletteIsActive(rouletteName, guildId)
    return active != "0"
  }
}