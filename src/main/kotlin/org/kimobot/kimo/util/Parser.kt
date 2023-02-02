package org.kimobot.kimo.util

import java.util.regex.Pattern

class Parser {

  fun parseBetween(toParse: String?): List<String>? {
    val regex = "\\[(.*?)\\]"
    val pattern = Pattern.compile(regex, Pattern.MULTILINE)
    val matcher = pattern.matcher(toParse)
    val matchs: MutableList<String> = ArrayList()
    while (matcher.find()) {
      matchs.add(matcher.group(1))
    }
    return matchs
  }

}