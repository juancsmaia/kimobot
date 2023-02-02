@file:JvmName("Main")

package org.kimobot.kimo

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import org.flywaydb.core.Flyway
import org.kimobot.kimo.listener.ButtonListener
import org.kimobot.kimo.listener.MenuListener
import org.kimobot.kimo.listener.MessageListener
import org.kimobot.kimo.listener.ModalListener
import org.kimobot.kimo.util.DatabaseConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("Main") as Logger

class KimoBotApplication {

  companion object {

    @JvmStatic
    fun main(array: Array<String>) {
      runFlyway()
      botInit()
    }

    private fun botInit() {
      RestAction.setDefaultTimeout(10, TimeUnit.SECONDS)

      val token = "OTc5NDQxNTA2NzkwMjc3MjAy.G4zY_T.KOlvMWqgSkcKpLJ5QcaxeHPTvAy_7gRqeYtM8s"

      val jda = JDABuilder.createDefault(token)
        .addEventListeners(MessageListener(), ButtonListener(), ModalListener(), MenuListener())
        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
        .build()

      jda.awaitReady()
      log.debug("Bot Ready!")
    }

    private fun runFlyway() {
      val flyway = Flyway.configure().dataSource(DatabaseConnection.fonteDeDados).load()
      flyway.migrate()
    }

  }

}