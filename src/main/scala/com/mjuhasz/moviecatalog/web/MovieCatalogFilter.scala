package com.mjuhasz.moviecatalog.web

import com.mjuhasz.moviecatalog.movies.MovieService
import com.mjuhasz.moviecatalog.movies.MovieInformation
import org.springframework.web.context.support.WebApplicationContextUtils
import org.scalatra.{UrlGeneratorSupport, Route, UrlSupport, ScalatraFilter}
import net.liftweb.json.Serialization.write
import net.liftweb.json.{NoTypeHints, Serialization}
import xml.{Xhtml, NodeSeq}

case class JsonResponse[A](content: A)

class MovieCatalogFilter extends ScalatraFilter with UrlSupport with UrlGeneratorSupport with MovieCatalogRoutes {

  implicit val formats = Serialization.formats(NoTypeHints)

  override  def contextPath = servletContext.getContextPath()

  override def contentTypeInferrer = {
    case _: JsonResponse[_] => "application/json"
    case _: NodeSeq => "text/html"
    case any => super.contentTypeInferrer(any)
  }

  override def renderPipeline = {
    case json: JsonResponse[_] => write(json)
    case xhtml: NodeSeq => Xhtml.toXhtml(xhtml)
    case any => super.renderPipeline(any)
  }

  protected def movieService: MovieService = {
    WebApplicationContextUtils.getWebApplicationContext(servletContext).getBean("movieService", classOf[MovieService])
  }

  get("/") {
    redirect(url(movieIndexRoute))
  }

  get("/movies.json") {
    val query = params.get("q").getOrElse("")
    JsonResponse(movieService.search(query).results)
  }

  get("/movies/:title.json") {
    JsonResponse(movieService.find(params("title")))
  }

  val movieIndexRoute: Route = get("/movies.html") {
    val query = params.get("q").getOrElse("")
    val movies = movieService.search(query)
    if (movies.results.isEmpty) {
      status(404)
      withSidebarLayout("Movie Catalog", None, Some(query), None, <div class="container"><h1>No movie matches '{ query }'</h1></div>)
    } else if (movies.results.size == 1 && movies.results.head.title == query) {
      redirect(url(movieTemperatureRoute, "title" -> movies.results.head.title))
    } else {
      withSidebarLayout("Movie Catalog", None, None, None,
        <div class="container">
          <h2>Use the search bar to find information for a specific movie</h2>
          <h2>Matching movies ({ movies.results.size } out of { movies.totalSize }):</h2>
          <ul>{
            for (movie <- movies.results) yield {
              <li><a href={ url(movieTemperatureRoute, "title" -> movie.title) }>{ movie.title }</a> - { movie.title_hu }</li>
            }
            }</ul>
        </div>)
    }
  }

  val movieTemperatureRoute: Route = get("/movies/:title.html") {
    renderWithMovieInformation(params("title"), movieTemperatureRoute) { info =>
      <div class="content">
        <h1>{ info.title }</h1>
        <h3>{ info.title_hu }</h3>
        <table>
          <tr><td>Audio:</td><td><strong>{ info.audio }</strong></td></tr>
          <tr><td>Subtitle</td><td><strong>{ info.subtitle }</strong></td></tr>
          <tr><td>Runtime</td><td><strong>{ info.runtime } min</strong></td></tr>
          <tr><td>Storage</td><td><strong>{ info.storage }{ if (info.storage_hu != info.storage) " (" + info.storage_hu + ")" }</strong></td></tr>
          <tr><td>Source</td><td><strong>{ info.source }</strong></td></tr>
        </table>
      </div>
    }
  }

  private def renderWithMovieInformation(title: String, route: Route)(body: MovieInformation => NodeSeq): NodeSeq = {
    try {
      movieService.find(title) match {
        case None =>
          status(404)
          withSidebarLayout("Movie Catalog", None, Some(title), None, <div class="container"><h1>Movie '{ title }' not found</h1></div>)
        case Some(info) =>
          withSidebarLayout("Movie Catalog", Some(info.title), None, Some(route), body(info))
      }
    } catch {
      case e =>
        e.printStackTrace()
        status(500)
        withSidebarLayout("Movie Catalog", None, Some(title), None,
          <div class="container">
            <h1>Error accessing movie '{ title }'</h1>
            <pre>{ e }</pre>
          </div>)
    }
  }

  private def withSidebarLayout(pageTitle: String, title: Option[String], query: Option[String], selected: Option[Route], body: NodeSeq) = {
    withBasicLayout(pageTitle,
      <form id="movieSearch" action={ url(movieIndexRoute) } method="GET">
          <input type="text" name="q" value={ query.getOrElse("") } placeholder="Search for a movie..." class="span12" autofocus=""/>
      </form>,
      <div class="container-fluid">
        { NodeSeq.Empty //title.map(renderMenu(_, selected)).getOrElse(NodeSeq.Empty) } //TODO: menu
        }
        { body }
      </div>)
  }

  private def withBasicLayout(pageTitle: String, topbar: NodeSeq, body: NodeSeq) = {
    <html lang="en">
      <head>
        <meta charset="utf-8"/>
        <title>{ pageTitle }</title>
        <link href={ url("/css/custom-theme/jquery-ui-1.8.16.custom.css") } rel="stylesheet"/>
        <link href={ url("/css/moviecatalog.css") } rel="stylesheet"/>
        <script type="text/javascript" src={ url("/js/jquery-1.6.4.min.js") }></script>
        <script type="text/javascript" src={ url("/js/jquery-ui-1.8.16.custom.min.js") }></script>
        <script type="text/javascript" src={ url("/js/moviecatalog.js") }></script>
      </head>
      <body>
        <div class="topbar">
          <div class="topbar-inner">
            <div class="container-fluid">
              <a class="brand" href="#">Movie Catalog</a>
              { topbar }
            </div>
          </div>
        </div>
        { body }
      </body>
    </html>
  }
}
