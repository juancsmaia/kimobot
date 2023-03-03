package org.kimobot.kimo.dao

import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanHandler
import org.apache.commons.dbutils.handlers.BeanListHandler
import org.apache.commons.dbutils.handlers.ScalarHandler
import org.kimobot.kimo.model.Anime
import org.kimobot.kimo.util.DataBaseConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException

class AnimeDAO {

  private val log = LoggerFactory.getLogger(javaClass) as Logger

  private val qr = QueryRunner(DataBaseConnection.dataSource)

  private val COLUMNS =
    "id, name, url, episodes, watched, roulette_index AS rouletteIdx, roulette_id AS rouletteId"


  fun saveAnime(anime: Anime): Anime {
    return try {
      val sql = java.lang.String.format(
        "INSERT INTO anime (name, roulette_index, episodes, url, watched, roulette_id) values ('%s',%s,%s,'%s',%s,%d)",
        anime.name,
        anime.rouletteIdx,
        anime.episodes,
        anime.url,
        anime.watched,
        anime.rouletteId
      )
      qr.insert(sql, BeanHandler(Anime::class.java))
    } catch (e: SQLException) {
      val msgError = "Error saving new anime to roulette"
      throw SQLException(msgError, e)
    }
  }

  fun getAllByRoulette(idRoulette: Int?): List<Anime> {
    return try {
      val sql = "SELECT $COLUMNS FROM anime WHERE roulette_id = ?"
      qr.query(sql, BeanListHandler(Anime::class.java), idRoulette)
    } catch (e: SQLException) {
      val msgError = "Error getting anime by roulette id"
      throw SQLException(msgError, e)
    }
  }

  fun countByRouletteId(rouletteId: Int?): Int {
    return try {
      val sql = "SELECT COUNT(*) FROM anime WHERE roulette_id = ?"
      qr.query(sql, ScalarHandler(), rouletteId)
    } catch (e: SQLException) {
      val msgError = "Error counting animes for roulette"
      throw SQLException(msgError, e)
    }
  }

  fun getByRouletteAndRouletteIdx(rouletteId: Int, idx: Int): Anime {
    return try {
      val sql = "SELECT $COLUMNS FROM anime WHERE roulette_id = ? AND roulette_index = ?"
      qr.query(sql, BeanHandler(Anime::class.java), rouletteId, idx)
    } catch (e: SQLException) {
      val msgError = "Error getting sorted anime"
      throw SQLException(msgError, e)
    }
  }

  fun updateAnime(rouletteId: Int, idx: Int, watched: Int) {
    try {
      val sql = "UPDATE anime SET watched = ? WHERE roulette_id = ? AND roulette_index = ?"
      qr.update(sql, watched, rouletteId, idx)
    } catch (e: SQLException) {
      val msgError = "Error updating anime watched episodes"
      log.info(msgError, e)
      throw RuntimeException(msgError)
    }
  }

  fun removeByRouletteAndIdx(rouletteId: Int, idx: String) {
    try {
      val sql = "DELETE FROM anime WHERE roulette_id = ? AND roulette_index = ?"
      qr.update(sql, rouletteId, idx)
    } catch (e: SQLException) {
      val msgError = "Error deleting anime from roulette"
      log.info(msgError, e)
      throw RuntimeException(msgError)
    }
  }

  fun updateRouletteIdx(animes: List<Anime>) {
    try {
      for (i in animes.indices) {
        val anime: Anime = animes[i]
        val sql = "UPDATE anime SET roulette_index = ? WHERE id = ?"
        qr.update(sql, i + 1, anime.id)
      }
    } catch (e: SQLException) {
      val msgError = "Error updating roulette index"
      log.info(msgError, e)
      throw RuntimeException(msgError)
    }
  }

}