package org.kimobot.kimo.dto

import com.fasterxml.jackson.annotation.JsonProperty
import javax.print.attribute.standard.Media

data class AniListDTO(var data: Data? = null) {

  class Data(
    @JsonProperty("Page") var page: Page? = null,
    @JsonProperty("Media") var media: Media? = null,
    @JsonProperty("SaveMediaListEntry") var savedMedia: SaveMedia? = null
  ) {

    class SaveMedia(var id: Int? = -1, var status: String? = null)

    class Page(var pageInfo: PageInfo? = null, var media: List<Media?>? = null) {

      class PageInfo(
        var total: Int? = -1,
        var currentPage: Int? = -1,
        var lastPage: Int? = -1,
        var hasNextPage: Boolean? = false,
        var perPage: Int? = null
      )

    }

    class Media(
      var id: Int? = -1,
      var description: String? = null,
      var status: String? = null,
      var episodes: Int? = -1,
      var chapters: Int? = -1,
      var volumes: Int? = -1,
      var genres: List<String>? = null,
      var title: Title? = null,
      var coverImage: CoverImage? = null,
      var format: String? = null
    ) {

      class CoverImage(var medium: String? = null, var large: String? = null, var color: String? = null)

      class Title(var romaji: String? = null)
    }
  }
}




