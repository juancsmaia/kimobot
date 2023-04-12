package org.kimobot.kimo

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import org.flywaydb.core.Flyway
import org.kimobot.kimo.listener.ButtonListener
import org.kimobot.kimo.listener.MenuListener
import org.kimobot.kimo.listener.MessageListener
import org.kimobot.kimo.listener.ModalListener
import org.kimobot.kimo.util.DataBaseConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
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

      try {
        KimoBotApplication::class.java.classLoader.getResourceAsStream("application.properties").use { rs ->
          val prop = Properties()
          prop.load(rs)
          val token = prop.getProperty("discord.token")

          val jda = JDABuilder.createDefault(token)
            .addEventListeners(MessageListener(), ButtonListener(), ModalListener(), MenuListener())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .setActivity(Activity.playing("!help"))
            .build()

          jda.awaitReady()
          log.info("Bot Ready!")
        }
      } catch (e: Exception) {
        log.error(e.message)
      }
    }

    private fun runFlyway() {
      val flyway = Flyway.configure().dataSource(DataBaseConnection.dataSource).load()
      flyway.migrate()
    }

  }

}