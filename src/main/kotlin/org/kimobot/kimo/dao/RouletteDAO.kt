package org.kimobot.kimo.dao

import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanHandler
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import org.kimobot.kimo.dto.enums.RouletteStatus
import org.kimobot.kimo.model.Roulette
import org.kimobot.kimo.util.DataBaseConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.sql.SQLException

class RouletteDAO {

  private val log = LoggerFactory.getLogger(javaClass) as Logger

  private val qr = QueryRunner(DataBaseConnection.dataSource)
  private val COLUMNS = "id, name, active, guild_id AS guildId"

  fun getByNameAndGuildId(name: String, guildId: String): Roulette? {
    return try {
      val sql = "SELECT $COLUMNS FROM roulette WHERE name = ? and guild_id = ?"
      qr.query(sql, BeanHandler(Roulette::class.java), name, guildId)
    } catch (e: SQLException) {
      val errorMsg = "Error getting roulette NAME [${name}]"
      throw SQLException(errorMsg, e)
    }
  }

  fun createRoulette(roulette: Roulette): Int {
    return try {
      val sql = "INSERT INTO roulette (name, active, guild_id) VALUES (?, ?, ?)"
      qr.insert(sql, ScalarHandler(), roulette.name, roulette.active, roulette.guildId)
    } catch (e: SQLException) {
      val errorMsg = "Error creating new roulette"
      throw SQLException(errorMsg, e)
    }
  }

  fun closeRoulette(name: String, guildId: String) {
    try {
      val sql = "UPDATE roulette SET active = '${RouletteStatus.INACTIVE.name}' WHERE name = ? AND guild_id = ?"
      qr.update(sql, name, guildId)
    } catch (e: SQLException) {
      val errorMsg = "Error closing roulette [$name] for guild [$guildId]"
      throw SQLException(errorMsg, e)
    }
  }

  fun getRouletteIsActive(name: String, guildId: String): Roulette {
    return try {
      val sql = "SELECT $COLUMNS FROM roulette WHERE name = ? AND guild_id = ?"
      qr.query(sql, BeanHandler(Roulette::class.java), name, guildId)
    } catch (e: SQLException) {
      val errorMsg = "Error getting roulette NAME [${name}]"
      throw SQLException(errorMsg, e)
    }
  }

}