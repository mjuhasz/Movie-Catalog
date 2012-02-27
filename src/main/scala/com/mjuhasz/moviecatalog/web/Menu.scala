package com.mjuhasz.moviecatalog.web

import org.scalatra.{Route, UrlGeneratorSupport}
import scala.xml.Text

trait Menu { this: MovieCatalogRoutes with UrlGeneratorSupport =>
  case class Entry(title: String, route: Option[Route] = None, children: Seq[Entry] = Nil) {
    def toXhtml(movieTitle: String) = route match {
      case Some(route) => <a href={url(route, "title" -> movieTitle)}>{ title }</a>
      case None => Text(title)
    }
  }

  private[this] implicit def routeToSomeRoute(route: Route): Option[Route] = Some(route)
  
  def menuEntries = Seq(
    Entry("Overview", movieTemperatureRoute),
    Entry("Technical Info", movieTechnicalRoute)
  )
}
