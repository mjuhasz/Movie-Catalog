package com.mjuhasz.moviecatalog.movies

import java.net.URLEncoder

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
  audio: String,
  subtitle: String,
  runtime: String,
  storage_a: String,
  storage_b: String,
  source: String,
  size: String,
  resolution: String, 
  aspect_ratio: String,
  framerate: String)

trait MovieService {
  def search(query: String): SearchResult[MovieSearchResult]
  def find(title: String): Option[MovieInformation]
}

class MockMovieService extends MovieService {

  private def encode(s: String) = URLEncoder.encode(s, "UTF-8")

  private def movies = Seq(
    MovieInformation(
      title = "Fool's Gold",
      title_hu = "Bolondok aranya",
      audio = "5.1 HU, EN",
      subtitle = "HU, EN",
      runtime = "107 min",
      storage_a = "M",
      storage_b = "D",
      source = "dvd",
      size = "4.4 GiB",
      resolution = "720 x 576",
      aspect_ratio = "4:3",
      framerate = "25 fps"),
    MovieInformation(
      title = "The Golden Compass",
      title_hu = "Az arany iránytű",
      audio = "2.0 HU",
      subtitle = "HU",
      runtime = "108 min",
      storage_a = "O",
      storage_b = "G",
      source = "blu-ray",
      size = "4.4 GiB",
      resolution = "720 x 576",
      aspect_ratio = "4:3",
      framerate = "25 fps"))

  override def search(query: String) = {
    val matches = movies.filter(_.title.toLowerCase.contains(query.toLowerCase)).map { movie =>
      MovieSearchResult(movie.title, movie.title_hu, "/movies/" + encode(movie.title) + ".json")
    }
    SearchResult(1, 20, (matches.size + 19) / 20, matches.size, matches.toList)
  }

  override def find(title: String) = movies.find(_.title.toLowerCase == title.toLowerCase)
}

class DBMovieService(val dbFilePath: String) extends MovieService {
  import java.sql._

  private def encode(s: String) = URLEncoder.encode(s, "UTF-8")

  override def search(query: String) = {

    val matches = getMovies().filter(_.title.toLowerCase.contains(query.toLowerCase)).map { movie =>
      MovieSearchResult(movie.title, movie.title_hu, "/movies/" + encode(movie.title) + ".json")
    }
    SearchResult(1, 20, (matches.size + 19) / 20, matches.size, matches.toList)
  }

  override def find(title: String) = getMovies().find(_.title.toLowerCase == title.toLowerCase)

  def getMovies() = {
    import com.mjuhasz.moviecatalog.movies.MovieDAO._

    Class.forName("org.sqlite.JDBC");
    using(DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)) { connection =>
      val movies = queryEach(connection, "SELECT movie.title, movie.title_hu, mediainfo.audio_lng, mediainfo.subtitle_lng, mediainfo.runtime, movie.source, movie.storage_a, movie.storage_b, mediainfo.size, mediainfo.resolution, mediainfo.aspect_ratio, mediainfo.framerate FROM movie JOIN mediainfo ON movie.title=mediainfo.title") {rs =>
        MovieInformation(rs.getString("title"), rs.getString("title_hu"), rs.getString("audio_lng"), rs.getString("subtitle_lng"), rs.getString("runtime"), rs.getString("storage_a"), rs.getString("storage_b"), rs.getString("source"), rs.getString("size"), rs.getString("resolution"), rs.getString("aspect_ratio"), rs.getString("framerate"))
      }
      movies
    }
  }
}

object MovieDAO {
  def using[A <: {def close(): Unit}, B](closeable: A)(getB: A => B): B =
    try {
      getB(closeable)
    } finally {
      closeable.close()
    }

  import scala.collection.mutable.ListBuffer

  def bmap[T](test: => Boolean)(block: => T): List[T] = {
    val ret = new ListBuffer[T]
    while(test) ret += block
    ret.toList
  }

  import java.sql._

  /** Executes the SQL and processes the result set using the specified function. */
  def query[B](connection: Connection, sql: String)(process: ResultSet => B): B =
      using (connection.createStatement) { statement =>
        using (statement.executeQuery(sql)) { results =>
          process(results)
        }
    }

  /** Executes the SQL and uses the process function to convert each row into a T. */
  def queryEach[T](connection: Connection, sql: String)(process: ResultSet => T): List[T] =
    query(connection, sql) { results =>
      bmap(results.next) {
        process(results)
      }
    }
}
