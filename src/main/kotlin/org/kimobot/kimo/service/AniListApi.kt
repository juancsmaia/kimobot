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

  private val objectMapper: ObjectMapper = ObjectMapper()

  fun findAnimeSearch(search: String, page: Int?): AniListDTO {

    val query = ("{ "
        + "    Page (page: ${page ?: 1}, perPage: 1) { "
        + "        pageInfo {"
        + "            total "
        + "            currentPage "
        + "            lastPage "
        + "            hasNextPage "
        + "            perPage "
        + "        } "
        + "        media (search: \"$search\", type: ANIME, sort: SEARCH_MATCH) { "
        + "            coverImage { "
        + "                 medium "
        + "                 large "
        + "                 color "
        + "               }"
        + "            id "
        + "            description "
        + "            status "
        + "            episodes "
        + "            chapters "
        + "            volumes "
        + "            genres "
        + "            format "
        + "            title { "
        + "                romaji "
        + "            }"
        + "        }"
        + "    }"
        + "}")

    val params: MutableMap<String, Any> = HashMap()
    params["query"] = query

    return sendRequest(params, null)
  }

  fun getAnimeById(id: String): AniListDTO {
    val query = ("{ "
        + "Media(id: $id) {"
        + "    id "
        + "    episodes "
        + "    title {"
        + "      romaji "
        + "    }"
        + "  }"
        + "}")

    val params: MutableMap<String, Any> = HashMap()
    params["query"] = query

    return sendRequest(params, null)
  }

  fun addAnimeToList(id: Int, status: String, accessToken: String): AniListDTO {
    val mutation = ("mutation (\$mediaId: Int, \$status: MediaListStatus) { "
        + "    SaveMediaListEntry (mediaId: \$mediaId, status: \$status) { "
        + "        id "
        + "        status "
        + "    } "
        + "}")

    val variables: MutableMap<String, Any> = HashMap()
    variables["mediaId"] = id
    variables["status"] = status

    val params: MutableMap<String, Any> = HashMap()
    params["query"] = mutation
    params["variables"] = variables

    return sendRequest(params, accessToken)
  }

  private fun sendRequest(params: MutableMap<String, Any>, accessToken: String?): AniListDTO {
    return try {
      val client = HttpClientBuilder.create().build()
      val httpPost = HttpPost(URL)
      httpPost.addHeader("Accept", "application/json")
      httpPost.addHeader("Content-Type", "application/json")
      if (accessToken != null) {
        httpPost.addHeader("Authorization", accessToken)
      }
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