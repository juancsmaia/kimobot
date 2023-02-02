package org.kimobot.kimo.dao

import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.BeanHandler
import org.kimobot.kimo.model.User
import org.kimobot.kimo.util.DatabaseConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.sql.SQLException

class UserDAO {

  private val log = LoggerFactory.getLogger(javaClass) as Logger

  private val qr = QueryRunner(DatabaseConnection.fonteDeDados)

  fun newAuthenticatedUser(idUser: String, token: String): User {
    try {

      val insert = "INSERT INTO user_token (id_user, token) VALUES ($idUser, ?)"

      return qr.insert(insert, BeanHandler(User::class.java), token)
    } catch (e: SQLException) {
      val error = "Error saving new user"
      log.error(error, e)
      throw RuntimeException(error, e)
    }
  }

}