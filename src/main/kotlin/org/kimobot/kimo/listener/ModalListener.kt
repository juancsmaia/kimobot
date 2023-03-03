package org.kimobot.kimo.listener

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.kimobot.kimo.dao.AnimeDAO
import org.kimobot.kimo.dao.RouletteDAO
import org.kimobot.kimo.dao.UserDAO
import org.kimobot.kimo.dto.enums.ActionComponents
import org.kimobot.kimo.dto.enums.Modals
import org.kimobot.kimo.dto.enums.RouletteStatus
import org.kimobot.kimo.model.Anime
import org.kimobot.kimo.service.AniListApi
import org.kimobot.kimo.service.MessageSender
import org.kimobot.kimo.util.MessageUtil
import org.slf4j.LoggerFactory

class ModalListener : ListenerAdapter() {

  private val log = LoggerFactory.getLogger(javaClass)
  private val aniListApi = AniListApi()
  private val userDao = UserDAO()
  private val animeDAO = AnimeDAO()
  private val messageSender = MessageSender()
  private val rouletteDAO = RouletteDAO()

  override fun onModalInteraction(event: ModalInteractionEvent) {
    when (event.modalId) {
      Modals.TOKEN.id -> saveUserAndToken(event)
      Modals.ANIME_ADD.id -> addAnimeToRoulette(event)
      Modals.ANIME_EDIT.id -> editAnimeFromRoulette(event)
      Modals.ANIME_REMOVE.id -> removeAnimeFromRoulette(event)
      Modals.INFO_ANIME_ADD.id -> infoAnimeAddToRoulette(event)
      else -> {}
    }
  }

  private fun infoAnimeAddToRoulette(event: ModalInteractionEvent) {
    event.deferEdit().queue()

    val rouletteName = event.interaction.getValue(ActionComponents.ROULETTE_NAME.id)!!.asString
    val guildId = event.guild!!.id

    val roulette = rouletteDAO.getByNameAndGuildId(rouletteName, guildId)
    if (roulette == null) {
      event.hook.retrieveOriginal().complete().reply("Roleta [$rouletteName] não encontrada.").queue()
      return
    }

    if (roulette.active != RouletteStatus.ACTIVE.name) {
      messageSender.sendMessage(event, MessageUtil.ROULETTE_INACTIVE)
      return
    }

    val metaData = MessageUtil.getMetaData(event)
    val animeId = metaData!![2].toString()

    saveAnimesToRoulette(listOf(animeId), roulette.id!!)

    event.hook.retrieveOriginal().complete().reply("Anime adicionado à roleta.").queue()
  }

  private fun removeAnimeFromRoulette(event: ModalInteractionEvent) {
    event.deferEdit().queue()

    val message = event.hook.retrieveOriginal().complete()
    val messageId = message.id

    val title = message.embeds[0].title!!.split("-")
    val rouletteId = Integer.parseInt(title[0].trim())
    val rouletteName = title[1].trim()

    val animeIdx = event.interaction.getValue(ActionComponents.ANIME_ID.id)!!.asString.split(",")

    for (idx in animeIdx) {
      animeDAO.removeByRouletteAndIdx(rouletteId, idx)
    }

    val animes: List<Anime> = animeDAO.getAllByRoulette(rouletteId)
    animeDAO.updateRouletteIdx(animes)

    val me = messageSender.mountRouletteMessage(animes, rouletteName, rouletteId)
    event.hook.editMessageEmbedsById(messageId, me).queue()
  }

  private fun editAnimeFromRoulette(event: ModalInteractionEvent) {
    event.deferEdit().queue()
    val message = event.hook.retrieveOriginal().complete()
    val messageId = message.id

    val title = message.embeds[0].title!!.split("-")
    val rouletteId = Integer.parseInt(title[0].trim())
    val rouletteName = title[1].trim()

    val idx = event.interaction.getValue(ActionComponents.ANIME_IDX.id)!!.asString
    val watched = event.interaction.getValue(ActionComponents.ANIME_WATCHED.id)!!.asString

    animeDAO.updateAnime(rouletteId, Integer.parseInt(idx), Integer.parseInt(watched))

    val animes: List<Anime> = animeDAO.getAllByRoulette(rouletteId)

    val me = messageSender.mountRouletteMessage(animes, rouletteName, rouletteId)
    event.hook.editMessageEmbedsById(messageId, me).queue()

  }

  private fun addAnimeToRoulette(event: ModalInteractionEvent) {
    event.deferEdit().queue()
    val message = event.hook.retrieveOriginal().complete()
    val messageId = message.id

    val title = message.embeds[0].title!!.split("-")
    val rouletteId = Integer.parseInt(title[0].trim())
    val rouletteName = title[1].trim()

    val animeIds =
      event.interaction.getValue(ActionComponents.ANIME_ID.id)!!.asString.split(",")

    saveAnimesToRoulette(animeIds, rouletteId)
    val animes: List<Anime> = animeDAO.getAllByRoulette(rouletteId)

    val me: MessageEmbed = messageSender.mountRouletteMessage(animes, rouletteName, rouletteId)
    event.hook.editMessageEmbedsById(messageId, me).queue()
  }

  private fun saveAnimesToRoulette(animeIds: List<String>, rouletteId: Int) {
    log.debug("Collecting anime data for [{}] animes", animeIds.size)
    var rouletteIdx: Int = animeDAO.countByRouletteId(rouletteId)
    rouletteIdx++
    for (animeId in animeIds) {
      log.debug("Collecting Anime ID [{}]", animeId)

      val anilistDTO = aniListApi.getAnimeById(animeId)
      val title = anilistDTO.data!!.media!!.title!!.romaji
      val episodes = anilistDTO.data!!.media!!.episodes
      val url = "https://anilist.co/anime/${anilistDTO.data!!.media!!.id}"

      log.debug("Successfully collected anime [{}]", title)

      val anime =
        Anime(name = title, url = url, episodes = episodes, rouletteIdx = rouletteIdx++, rouletteId = rouletteId)

      animeDAO.saveAnime(anime)
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