package com.mjuhasz.moviecatalog.web

import org.scalatra.ScalatraFilter
import compat.Platform


class MovieCatalogFilter extends ScalatraFilter {

  get("/") {
    redirect("/index.html")
  }

  get("/index.html") {
    <html>
      <head>
        <title>Movie Catalog</title>
      </head>
      <body>
        <p>Sample page generated at epoch time { Platform.currentTime }</p>
      </body>
    </html>
  }
}