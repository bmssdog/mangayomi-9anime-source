package com.bmssdog.nineanime

import eu.kanade.tachiyomi.animeextension.model.*
import eu.kanade.tachiyomi.animeextension.AnimeHttpSource
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup

class NineAnime : AnimeHttpSource() {

    override val name = "9anime"
    override val baseUrl = "https://9animetv.to"
    override val lang = "en"
    override val supportsLatest = true

    override fun popularAnimeRequest(page: Int): Request =
        GET("$baseUrl/home", headers)

    override fun popularAnimeParse(response: Response): AnimesPage {
        val doc = Jsoup.parse(response.body!!.string())
        val animeList = doc.select("div.film_list-wrap div.flw-item").map {
            Anime(
                title = it.select("h3.film-name a").text(),
                url = it.select("a").attr("href"),
                thumbnailUrl = it.select("img").attr("data-src")
            )
        }
        return AnimesPage(animeList, false)
    }

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/home", headers)

    override fun latestUpdatesParse(response: Response): AnimesPage =
        popularAnimeParse(response)

    override fun searchAnimeRequest(page: Int, query: String, filters: FilterList): Request =
        GET("$baseUrl/search?keyword=$query", headers)

    override fun searchAnimeParse(response: Response): AnimesPage {
        val doc = Jsoup.parse(response.body!!.string())
        val results = doc.select("div.film_list-wrap div.flw-item").map {
            Anime(it.select("h3.film-name a").text(),
                  it.select("a").attr("href"),
                  it.select("img").attr("data-src"))
        }
        return AnimesPage(results, false)
    }

    override fun episodeListParse(response: Response): List<Episode> {
        val doc = Jsoup.parse(response.body!!.string())
        return doc.select("ul.episodes li").mapIndexed { i, el ->
            Episode(i + 1, "Episode ${i + 1}", el.select("a").attr("href"))
        }
    }

    override fun videoListParse(response: Response): List<Video> {
        val doc = Jsoup.parse(response.body!!.string())
        return doc.select("iframe").map {
            Video(url = it.attr("src"), quality = "default", /* additional headers if needed */)
        }
    }

    override fun animeDetailsParse(response: Response): AnimeDetails {
        val doc = Jsoup.parse(response.body!!.string())
        return AnimeDetails(
            title = doc.select("h2.film-name").text(),
            description = doc.select("div.description-content").text(),
            genres = doc.select("div.film-genres a").map { it.text() },
            status = AnimeStatus.UNKNOWN
        )
    }
}
