package org.kimobot.kimo.util

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class MessageUtil {

  companion object {

    const val ROULETTE_INACTIVE = "A Roleta está inativa e não aceitá alterações."

    fun getMetaData(event: ButtonInteractionEvent): List<String?>? {
      val embed = event.message.embeds[0]
      val footer = embed.footer
      val parser = Parser()
      return parser.parseBetween(footer!!.text)
    }

    fun getMetaData(event: ModalInteractionEvent): List<String?>? {
      val embed = event.hook.retrieveOriginal().complete().embeds[0]
      val footer = embed.footer
      val parser = Parser()
      return parser.parseBetween(footer!!.text)
    }
  }

}