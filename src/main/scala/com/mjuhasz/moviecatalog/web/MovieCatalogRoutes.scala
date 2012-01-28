package com.mjuhasz.moviecatalog.web

import org.scalatra.Route

trait MovieCatalogRoutes {
  def movieIndexRoute: Route
  def movieTemperatureRoute: Route
}