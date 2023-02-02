package org.kimobot.kimo.listener

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.kimobot.kimo.dto.enums.AniListStatus

class MenuListener : ListenerAdapter() {

  override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
    if (event.componentId == "choose-status") {
      val status = AniListStatus.getStatus(event.values[0])
    }
  }
}