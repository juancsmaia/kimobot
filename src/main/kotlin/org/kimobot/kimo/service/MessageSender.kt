package org.kimobot.kimo.service

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.kimobot.kimo.dto.AniListDTO
import org.kimobot.kimo.dto.enums.Buttons
import java.awt.Color

class MessageSender {

  fun sendAniListMessageInfo(event: MessageReceivedEvent, tipo: String, busca: String, dto: AniListDTO) {
    val me = mountAniListMessageInfo(tipo, busca, dto)

    event
      .message
      .replyEmbeds(me)
      .setActionRow(Button.primary(Buttons.NEXT.id, Buttons.NEXT.tag), Button.success(Buttons.ADD.id, Buttons.ADD.tag))
      .queue()
  }

  fun mountAniListMessageInfo(tipo: String, busca: String, dto: AniListDTO): MessageEmbed {
    val eb = EmbedBuilder()

    val pageInfo = dto.data!!.page!!.pageInfo

    eb.setTitle("Page ${pageInfo!!.currentPage} / ${pageInfo.lastPage}")

    val medias = dto.data!!.page!!.media
    medias?.iterator()?.forEach {
      val titulo = it!!.title!!.romaji
      val url = "https://anilist.co/anime/${it.id}/"
      val generos = it.genres!!.joinToString(", ")

      eb.setThumbnail(it.coverImage!!.large)
      eb.addField(MessageEmbed.Field("Title", titulo, false))

      if (it.episodes == null) {
        val capitulos = if (it.chapters == null) "?" else it.chapters.toString()
        val volumes = if (it.volumes == null) "?" else it.volumes.toString()
        eb.addField(MessageEmbed.Field("Chapters", capitulos, false))
        eb.addField(MessageEmbed.Field("Volumes", volumes, false))
      } else {
        eb.addField(MessageEmbed.Field("Episodes", it.episodes.toString(), false))
      }

      eb.addField(MessageEmbed.Field("Status", it.status, false))
      eb.addField(MessageEmbed.Field("Genres", generos, false))

      eb.addField(MessageEmbed.Field("", String.format("[AniList](%s)", url), false))
      eb.setFooter("Data: Type: [$tipo], Search: [$busca], Id [${it.id}]")
    }

    return eb.build()
  }

}