/*
 * Copyright 2012 Miklos Juhasz (mjuhasz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
