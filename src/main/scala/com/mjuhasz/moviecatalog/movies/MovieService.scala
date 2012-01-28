package com.mjuhasz.moviecatalog.movies

import java.util.concurrent.Future
import java.net.URLEncoder
import org.apache.commons.lang3.concurrent.ConcurrentUtils

case class SearchResult[A](
  pageNumber: Int,
  pageSize: Int,
  pageCount: Int,
  totalSize: Int,
  results: List[A])

case class MovieSearchResult(title: String, title_hu: String, apiUrl: String)

case class MovieInformation(
  title: String,
  title_hu: String,
  duration: String)

trait MovieService {
  def search(query: String): Future[SearchResult[MovieSearchResult]]
  def find(title: String): Future[Option[MovieInformation]]
}

class MockMovieService extends MovieService {

  private def encode(s: String) = URLEncoder.encode(s, "UTF-8")

  private def movies = Seq(
    MovieInformation(
      title = "Fool's Gold",
      title_hu = "Bolondok aranya",
      duration = "107"),
    MovieInformation(
      title = "The Golden Compass",
      title_hu = "Az arany iránytű",
      duration = "108"))

  override def search(query: String) = {
    val matches = movies.filter(_.title.toLowerCase.contains(query.toLowerCase)).map { movie =>
      MovieSearchResult(movie.title, movie.title_hu, "/movies/" + encode(movie.title) + ".json")
    }
    ConcurrentUtils.constantFuture(SearchResult(1, 20, (matches.size + 19) / 20, matches.size, matches.toList))
  }

  override def find(title: String) = ConcurrentUtils.constantFuture(movies.find(_.title.toLowerCase == title.toLowerCase))
}