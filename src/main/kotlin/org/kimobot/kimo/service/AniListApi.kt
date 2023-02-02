package org.kimobot.kimo.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.json.JSONObject
import org.kimobot.kimo.dto.AniListDTO
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class AniListApi {

  companion object {
    private const val URL = "https://graphql.anilist.co"
  }

  internal val objectMapper: ObjectMapper = ObjectMapper()

  fun getAnime(busca: String, tipo: String, page: Int?): AniListDTO {

    val query = ("query (\$type: MediaType, \$page: Int, \$perPage: Int, \$search: String) { "
        + "    Page (page: \$page, perPage: \$perPage) { "
        + "        pageInfo {"
        + "            total "
        + "            currentPage "
        + "            lastPage "
        + "            hasNextPage "
        + "            perPage "
        + "        } "
        + "        media (search: \$search, type: \$type, sort: SEARCH_MATCH) { "
        + "            id "
        + "            status "
        + "            episodes "
        + "            chapters "
        + "            volumes "
        + "            genres "
        + "            title { "
        + "                romaji "
        + "            }"
        + "        }"
        + "    }"
        + "}")

    val variables: MutableMap<String, Any> = HashMap()
    variables["search"] = busca
    variables["type"] = tipo
    variables["page"] = page ?: 1
    variables["perPage"] = 2

    val params: MutableMap<String?, Any?> = HashMap()
    params["variables"] = variables
    params["query"] = query

    return try {
      val client = HttpClientBuilder.create().build()
      val httpPost = HttpPost(URL)
      httpPost.addHeader("Accept", "application/json")
      httpPost.addHeader("Content-Type", "application/json")
      val jsonObject = JSONObject(params)
      val entity = StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON)
      httpPost.entity = entity
      val response = client.execute(httpPost)
      val bufReader = BufferedReader(InputStreamReader(response.entity.content))
      convertResponse(bufReader)
    } catch (e: Exception) {
      throw RuntimeException("AniListApi Exception", e)
    }
  }

  @Throws(IOException::class)
  private fun convertResponse(bufferedReader: BufferedReader): AniListDTO {
    val builder = StringBuilder()
    var line: String?
    while (bufferedReader.readLine().also { line = it } != null) {
      builder.append(line)
      builder.append(System.lineSeparator())
    }
    return objectMapper.readValue(builder.toString(), AniListDTO::class.java)
  }

}