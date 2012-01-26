package com.mjuhasz.moviecatalog.config

import java.io.File
import org.apache.log4j.PropertyConfigurator
import grizzled.slf4j.Logger
import org.clapper.argot._
import org.clapper.argot.ArgotConverters._

object Main {
  def main(args: Array[String]): Unit = Options.parse(args) match {
    case Left(error) =>
      Console.err.println(error)
      sys.exit(1)
    case Right(options) =>
      configureLogging()
      run(options)
  }

  private def configureLogging() {
    val configFile = new File("config/log4j.properties")
    PropertyConfigurator.configureAndWatch(configFile.toString);
  }

  private def run(options: Options) {
    val logger = Logger[this.type]

    import org.eclipse.jetty.server.Server
    import org.eclipse.jetty.server.handler.HandlerCollection
    import org.eclipse.jetty.webapp.WebAppContext

    val server = new Server(options.httpPort)
    val context = new WebAppContext
    context.setContextPath("/")
    context.setResourceBase("src/main/webapp/")

    val handlers = new HandlerCollection()
    handlers.addHandler(context)
    server.setHandler(handlers)

    sys.addShutdownHook({
      server.stop()
      logger.info("Terminating...")
    })
    server.start()
    logger.info("Welcome to the Movie Catalog! Available on port %d. Hit CTRL+C to terminate."
        .format(options.httpPort))
    server.join()
  }
}

private[config] object Options {
  val DEFAULT_HTTP_PORT = 8080

  def parse(args: Array[String]): Either[String, Options] = try {
    Right(new Options(args))
  } catch {
    case e: ArgotUsageException => Left(e.getMessage())
  }
}

private[config] class Options(args: Array[String]) {
  private val parser = new ArgotParser(programName = "movie-catalog")
  private val httpPortOption = parser.option[Int](List("p", "port"), "PORT", "The port the movie-catalog listens on for HTTP requests. Default: " + Options.DEFAULT_HTTP_PORT)
  def httpPort: Int = httpPortOption.value.getOrElse(Options.DEFAULT_HTTP_PORT)
  parser.parse(args)
}
