package org.kimobot.kimo.listener

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.kimobot.kimo.dto.enums.AniListType
import org.kimobot.kimo.dto.enums.Buttons
import org.kimobot.kimo.dto.enums.Command
import org.kimobot.kimo.service.AniListApi
import org.kimobot.kimo.service.MessageSender
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MessageListener : ListenerAdapter() {

  private val log = LoggerFactory.getLogger(javaClass) as Logger
  private val aniListApi = AniListApi()
  private val messageSender = MessageSender()

  override fun onMessageReceived(event: MessageReceivedEvent) {

    if (event.author.isBot) {
      return
    }

    val mensagem = event.message.contentRaw
    if (!mensagem.startsWith("!")) {
      return
    }

    log.debug("Comando recebido $mensagem")
    val comandoCru = mensagem.replace("!", "").split(" ")
    val command = Command.pegarComando(comandoCru[0]) ?: return

    when (command) {
      Command.REGISTRATION -> cadastro(event)
      Command.INFO -> info(event, comandoCru)
      Command.CLEAR -> clearMessages(event)
    }

  }

  private fun cadastro(event: MessageReceivedEvent) {
    val url = "https://anilist.co/api/v2/oauth/authorize?client_id=11086&response_type=token"
    val menssagem = String.format("[Clique aqui para autorizar o KimoBot](%s)", url)

    val eb = EmbedBuilder()
    eb.setTitle("Autenticação")
    eb.addField(
      MessageEmbed.Field(
        "",
        "Para se autenticar clique no Link abaixo. Você será redirecionado para o site Anilist e receberá um Token",
        true
      )
    )
      .addField(MessageEmbed.Field("", menssagem, false))
      .addField(
        MessageEmbed.Field(
          "", "Clique no Botão abaixo para enviar o token de autenticação.", true
        )
      )
      .addField(
        MessageEmbed.Field(
          "",
          "Você pode revogar esta autorização diretamente no Anilist em Perfil -> Settings -> Apps -> Revoke App",
          false
        )
      )
      .addField(
        MessageEmbed.Field(
          "", "Esta mensagem será excluída assim que o token for recebido.", false
        )
      )

    event
      .message
      .replyEmbeds(eb.build())
      .setActionRow(Button.primary(Buttons.TOKEN.id, Buttons.TOKEN.tag))
      .queue()
  }

  private fun info(event: MessageReceivedEvent, comandoCru: List<String>) {

    val tipo = comandoCru[1].lowercase()
    val aniListType = AniListType.getType(tipo)
    if (aniListType == null) {
      event.message.reply("Tipo Inválido")
      return
    }

    val sb = StringBuilder()
    for (i in 2 until comandoCru.size) {
      sb.append(comandoCru[i]).append(" ")
    }

    val busca = sb.toString().trim()
    val aniListDTO = aniListApi.getAnime(busca, aniListType.name, null)

    messageSender.sendAniListMessageInfo(event, aniListType.name, busca, aniListDTO)
  }

  private fun clearMessages(event: MessageReceivedEvent) {
    if (!event.member!!.isOwner) {
      return
    }
    val args = event.message.contentRaw.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
      .toTypedArray()
    if (args.size != 2) {
      event.message.reply("Faltou a quantidade.").queue()
      return
    }
    val amount = args[1].toInt() + 1
    val messages = event.channel.history.retrievePast(amount).complete()
    event.guildChannel.deleteMessages(messages).queue()
  }

}