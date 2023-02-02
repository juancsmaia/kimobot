package org.kimobot.kimo.util

import org.apache.commons.dbcp.BasicDataSource
import java.io.IOException
import java.lang.management.ManagementFactory
import java.sql.Connection
import java.sql.SQLException
import java.util.*

class DataBaseConnection {

  companion object {
    val dataSource: BasicDataSource = BasicDataSource()

    init {
      val profile = profileArgument
      val properties = String.format("application.properties", profile)
      try {
        DataBaseConnection::class.java.classLoader.getResourceAsStream(properties).use { rs ->
          val prop = Properties()
          prop.load(rs)
          dataSource.driverClassName = prop.getProperty("db.classPath")
          dataSource.url = prop.getProperty("db.url")
          dataSource.username = prop.getProperty("db.username")
          dataSource.password = prop.getProperty("db.password")
          dataSource.maxActive = 10
          dataSource.maxIdle = 5
          dataSource.minIdle = 2
          dataSource.initialSize = 10
        }
      } catch (e: IOException) {
        throw RuntimeException("Error loading file")
      }
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