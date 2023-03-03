package org.kimobot.kimo.service

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.kimobot.kimo.dto.AniListDTO
import org.kimobot.kimo.dto.enums.Buttons
import org.kimobot.kimo.model.Anime

class MessageSender {

  fun sendAniListMessageInfo(event: MessageReceivedEvent, tipo: String, busca: String, dto: AniListDTO) {
    val me = mountAniListMessageInfo(tipo, busca, dto)

    event
      .message
      .replyEmbeds(me)
      .setActionRow(
        Button.primary(Buttons.NEXT.id, Buttons.NEXT.tag),
        Button.success(Buttons.ADD_TO.id, Buttons.ADD_TO.tag)
      )
      .queue()
  }

  fun mountAniListMessageInfo(type: String, search: String, dto: AniListDTO): MessageEmbed {
    val eb = EmbedBuilder()

    val pageInfo = dto.data!!.page!!.pageInfo

    // "Page ${pageInfo!!.currentPage} / ${pageInfo.lastPage}"

    val medias = dto.data!!.page!!.media
    medias!!.iterator().forEach {
      val title = it!!.title!!.romaji
      val url = "https://anilist.co/anime/${it.id}/"
      val genres = it.genres!!.joinToString(", ")
      eb.setTitle(title, url)
      eb.setThumbnail(it.coverImage!!.large)
      eb.setDescription(
        it.description!!
          .replace("<b>", "**")
          .replace("</b>", "**")
          .replace("<br>", "\n")
          .replace("</br>", "")
          .replace("<i>", "*")
          .replace("</i>", "*")
      ).addField(MessageEmbed.Field("", "", false))

      eb.addField(MessageEmbed.Field("Format", it.format, true))

      if (it.episodes == null) {
        val chapters = if (it.chapters == null) "?" else it.chapters.toString()
        val volumes = if (it.volumes == null) "?" else it.volumes.toString()
        eb.addField(MessageEmbed.Field("Chapters", chapters, true))
        eb.addField(MessageEmbed.Field("Volumes", volumes, true))
      } else {
        eb.addField(MessageEmbed.Field("Episodes", it.episodes.toString(), true))
      }

      eb.addField(MessageEmbed.Field("Status", it.status, true))
      eb.addField(MessageEmbed.Field("Genres", genres, false))
      eb.addField(
        MessageEmbed.Field(
          "", "Página: ${pageInfo!!.currentPage} de ${pageInfo.lastPage}", false
        )
      )

      eb.setFooter("Data: Type: [$type], Search: [$search], ID [${it.id}]")
    }

    return eb.build()
  }

  fun mountRouletteMessage(animes: List<Anime>, name: String, id: Int): MessageEmbed {
    val eb = EmbedBuilder()
    eb.setTitle("$id - $name")
    val sb = StringBuilder()
    animes.forEach {
      val data =
        "${it.rouletteIdx} - [${it.name}](${it.url}) (${it.watched} / ${it.episodes})"
      sb.append(data).append(System.lineSeparator()).append(System.lineSeparator())
    }
    eb.addField(MessageEmbed.Field("", sb.toString(), true))
    return eb.build()
  }

  fun sendMessage(event: MessageReceivedEvent, animes: List<Anime>, name: String?, id: Int?) {
    val me = mountRouletteMessage(animes, name!!, id!!)
    event
      .message
      .replyEmbeds(me)
      .setActionRow(
        Button.primary(Buttons.ADD.id, Buttons.ADD.tag),
        Button.primary(Buttons.EDIT.id, Buttons.EDIT.tag),
        Button.primary(Buttons.REMOVE.id, Buttons.REMOVE.tag),
        Button.success(Buttons.ROLL.id, Buttons.ROLL.tag)
      )
      .queue()
  }

  fun sendMessage(event: MessageReceivedEvent, message: String) {
    event.message.reply(message).queue()
  }

  fun sendMessage(event: ButtonInteractionEvent, message: String) {
    event.message.reply(message).queue()
  }

  fun sendRollResult(event: ButtonInteractionEvent, sortedAnime: Anime) {
    val mb =
      MessageCreateBuilder().addContent(":mega:")
        .addContent(" Drafted: [${sortedAnime.name}](${sortedAnime.url})")

    event.reply(mb.build()).queue()
  }

}