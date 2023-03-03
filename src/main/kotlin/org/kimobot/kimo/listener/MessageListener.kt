package org.kimobot.kimo.listener

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.kimobot.kimo.dao.AnimeDAO
import org.kimobot.kimo.dao.RouletteDAO
import org.kimobot.kimo.dto.enums.AniListType
import org.kimobot.kimo.dto.enums.Buttons
import org.kimobot.kimo.dto.enums.Command
import org.kimobot.kimo.dto.enums.RouletteStatus
import org.kimobot.kimo.model.Roulette
import org.kimobot.kimo.service.AniListApi
import org.kimobot.kimo.service.MessageSender
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MessageListener : ListenerAdapter() {

  private val log = LoggerFactory.getLogger(javaClass) as Logger
  private val aniListApi = AniListApi()
  private val messageSender = MessageSender()
  private val rouletteDAO = RouletteDAO()
  private val animeDAO = AnimeDAO()

  override fun onMessageReceived(event: MessageReceivedEvent) {

    if (event.author.isBot) {
      return
    }

    val message = event.message.contentRaw
    if (!message.startsWith("!")) {
      return
    }

    val crudeCommand = message.replace("!", "").split(" ")
    val command = Command.pegarComando(crudeCommand[0]) ?: return

    when (command) {
      Command.HELP -> help(event)
      Command.INFO -> info(event, crudeCommand[1])
      Command.CLEAR -> clearMessages(event)
      Command.NEW_ROULETTE -> newRoulette(event, crudeCommand[1])
      Command.ROULETTE -> getRouletteData(event, crudeCommand[1])
      Command.CLOSE -> closeRoulette(event, crudeCommand[1])
      else -> {}
    }

  }

  private fun closeRoulette(event: MessageReceivedEvent, name: String) {
    val guildId = event.guild.id
    rouletteDAO.closeRoulette(name, guildId)
    event.message.reply("Roleta [$name] foi encerrada e não aceitará mais atualizações.").queue()
  }

  private fun newRoulette(event: MessageReceivedEvent, name: String) {

    val guildId = event.guild.id
    val roulette = rouletteDAO.getByNameAndGuildId(name, guildId)
    if (roulette != null) {
      if (roulette.active == RouletteStatus.ACTIVE.name) {
        messageSender.sendMessage(
          event, "Uma Roleta com o nome [${name}] já existe e está ativa. Use o comando *!roul $name*"
        )
        return
      }
    }

    val newRoulette = Roulette(name = name, guildId = guildId, active = RouletteStatus.ACTIVE.name)
    val rouletteId: Int = rouletteDAO.createRoulette(newRoulette)
    log.info("Roulette created with ID: [$rouletteId]")

    event.message.reply("Roleta criada. Utilize [*!roul $name*] para gerenciar a roleta.").queue()
  }

  private fun getRouletteData(event: MessageReceivedEvent, name: String) {

    val guildId = event.guild.id

    val roulette = rouletteDAO.getByNameAndGuildId(name, guildId)
    if (roulette == null) {
      messageSender.sendMessage(event, "Roleta [$name] não encontrada.")
      return
    }

    val animes = animeDAO.getAllByRoulette(roulette.id)

    messageSender.sendMessage(event, animes, name, roulette.id)
  }

  private fun help(event: MessageReceivedEvent) {
    val eb = EmbedBuilder()
    eb.setTitle("Comandos").addField(
      MessageEmbed.Field(
        "!${Command.NEW_ROULETTE.tag}",
        "Comando para criar uma nova roleta. Evite nomes com espaços\n!newroul <nome>\nExemplo: !newroul Roleta",
        false
      )
    ).addField(
      MessageEmbed.Field(
        "!${Command.ROULETTE.tag}",
        "Comando para ver/buscar os dados de uma roleta existente\n!roul <nome>\nExemplo: !roul Roleta",
        false
      )
    ).addField(
      MessageEmbed.Field(
        "!${Command.CLOSE.tag}",
        "Comando utilizado para encerrar uma roleta.\nA Roleta encerrada não receberá novas atualizações ou interações, porém poderá ser consultada através do comando *!roul*\n!close <nome>\nExemplo: !close Roleta",
        false
      )
    ).addField(
      MessageEmbed.Field(
        "!${Command.INFO.tag}",
        "Comando para buscar informações de animes no Anilist.\nVocê receberá a lista de anime que correspondam com a pesquisa em 1 por página para que possa adicionar a roleta.\n!info <busca>\nExemplo: !info naruto\n",
        false
      )
    )

    event.message.replyEmbeds(eb.build()).queue()
  }

  private fun registration(event: MessageReceivedEvent) {
    val url = "https://anilist.co/api/v2/oauth/authorize?client_id=11086&response_type=token"
    val message = "[Clique aqui para autorizar o KimoBot]($url)"

    val eb = EmbedBuilder()
    eb.setTitle("Autenticação")
    eb.addField(
      MessageEmbed.Field(
        "",
        "Para se autenticar clique no Link abaixo. Você será redirecionado para o site Anilist e receberá um Token",
        true
      )
    ).addField(MessageEmbed.Field("", message, false)).addField(
      MessageEmbed.Field(
        "", "Clique no Botão abaixo para enviar o token de autenticação.", true
      )
    ).addField(
      MessageEmbed.Field(
        "",
        "Você pode revogar esta autorização diretamente no Anilist em Perfil -> Settings -> Apps -> Revoke App",
        false
      )
    ).addField(
      MessageEmbed.Field(
        "", "Esta mensagem será excluída assim que o token for recebido.", false
      )
    )

    event.message.replyEmbeds(eb.build()).setActionRow(Button.primary(Buttons.TOKEN.id, Buttons.TOKEN.tag)).queue()
  }

  private fun info(event: MessageReceivedEvent, search: String) {

    val aniListDTO = aniListApi.findAnimeSearch(search, null)

    messageSender.sendAniListMessageInfo(event, search, aniListDTO)
  }

  private fun clearMessages(event: MessageReceivedEvent) {
    if (!event.member!!.isOwner) {
      return
    }
    val args = event.message.contentRaw.split(" ".toRegex())
    if (args.size != 2) {
      event.message.reply("Faltou a quantidade.").queue()
      return
    }
    val amount = args[1].toInt() + 1
    val messages = event.channel.history.retrievePast(amount).complete()
    event.guildChannel.deleteMessages(messages).queue()
  }

}