package org.kimobot.kimo.util

import org.apache.commons.dbcp.BasicDataSource
import java.io.IOException
import java.lang.management.ManagementFactory
import java.sql.Connection
import java.sql.SQLException
import java.util.*

class DatabaseConnection {

  companion object {
    val fonteDeDados: BasicDataSource = BasicDataSource()

    init {
      val profile = profileArgument
      val properties = String.format("application.properties", profile)
      try {
        DatabaseConnection::class.java.classLoader.getResourceAsStream(properties).use { rs ->
          val prop = Properties()
          prop.load(rs)
          fonteDeDados.driverClassName = prop.getProperty("db.classPath")
          fonteDeDados.url = prop.getProperty("db.url")
          fonteDeDados.username = prop.getProperty("db.username")
          fonteDeDados.password = prop.getProperty("db.password")
          fonteDeDados.maxActive = 10
          fonteDeDados.maxIdle = 5
          fonteDeDados.minIdle = 2
          fonteDeDados.initialSize = 10
        }
      } catch (e: IOException) {
        throw RuntimeException("Error loading file")
      }
    }

    @Throws(SQLException::class)
    fun getConn(): Connection {
      return fonteDeDados.connection
    }

    private val profileArgument: String?
      get() {
        val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
        val arguments = runtimeMXBean.inputArguments
        for (argument in arguments) {
          if (argument == "-Dprofile=local") {
            val parts = argument.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return parts[parts.size - 1]
          }
        }
        return null
      }

  }


}