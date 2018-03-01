package universe.server

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import universe.internal

trait Server extends internal.Scaffolding with App {

  /** Entry point into Akka Http module */
  val http = Http()

}

object Example1 extends Server {

  /**
   * [[akka.http.scaladsl.HttpExt.bind()]] allows us to handle incoming
   * connections on specific `interface` and `port`, which in its response
   * gives us a potentially infinite steam of incoming connections
   *
   * Additional settings can be found here:
   *   @see http://doc.akka.io/docs/akka/2.4.3/scala/http/configuration.html
   *
   */
  val incomingConnections = Http().bind(interface, port)

  incomingConnections.runForeach { newConnection =>
    log.info(s"New connection: ${newConnection.localAddress}")
  }

  started()
}

object Example2 extends Server {
  http.bind(interface, port).runForeach { newConnection =>

    val handler: (HttpRequest) => HttpResponse = {
      case HttpRequest(method, uri, headers, entity, protocol) =>

        entity.dataBytes.runForeach {
          data => log.info(data.decodeString("utf-8"))
        }

        HttpResponse(entity = HttpEntity("Processed!"))
    }

    newConnection.handleWithSyncHandler(handler)
  }

  started()
}

object Example3 extends Server {

  /**
   * Convenient way to combine `bind` with connection
   * handling function.
   */
  http.bindAndHandleSync({
    case request: HttpRequest =>
      request.entity.dataBytes.runForeach(data => log.info(data.decodeString("utf-8")))
      HttpResponse(entity = HttpEntity("Processed!"))
  }, interface, port)

  // handler :: HttpRequest => Future[HttpResponse]
  // Http().bindAndHandleAsync(handler, interface, port)

  started()

}

/**
 * Instead of explicit function for processing Request/Response
 * we can use flexible Routing DSL that would be implicitly converted
 * in Streaming Flow[...]
 */
object Example4 extends Server {
  import akka.http.scaladsl.server.{Directives, Route}

  /** More on Routing DSL in [[universe.routing.Routing]] */
  val route: Route = Directives.complete("Coming next... Routing DSL!")

  /**
   * Implicitly converts [[Route]] into [[Flow[HttpRequest, HttpResponse, Any]]
   * Converters :: [[Route.handlerFlow()]] and [[Route.asyncHandler()]]
   * Old Spray "route compiler" [[spray.routing.HttpServiceBase.runRoute()]]
   */
  http.bindAndHandle(route, interface, port)

  started()
}

/**
 * Take away:
 *   1. Http().bind(...) // Source[IncomingConnection, Future[Binding]]
 *   1. Http().bindAndHandle(...) // Converts Route into Akka stream Flow[...]
 *   1. Http().bindAndHandleAsync(...) // Explicit function
 *   1. Http().bindAndHandleSync(...) // Synchronous version
 */