package universe.routing

import akka.http.scaladsl.model.HttpMethods
import universe.internal

trait Routing extends internal.Scaffolding with App {
  def answer(): String = {
    if (scala.util.Random.nextBoolean()) "Yes!"
    else "No :("
  }
}

object TheBasics extends Routing {
  import akka.http.scaladsl.server._

  /** [[Route]] and [[RequestContext]] */
  // type Route = RequestContext => Future[RouteResult]

  val complete: Route = ctx => ctx.complete("Completed!")
  val reject: Route = ctx => ctx.reject(MethodRejection(HttpMethods.GET))
  val fail: Route = ctx => ctx.fail(new Throwable("Here comes the Boom!"))

  /**
   * RouteResult:
   *   1. Completed
   *   2. Rejected
   */

}

/**
 * Basic Akka Http route example
 */
object Example1 extends Routing {
  import akka.http.scaladsl.server._, Directives._

  runServer {
    path("routing") {
      get {
        parameter('q) { q =>
          complete {
            s"""
               |Question: $q
               |Answer: ${answer()}
              """.stripMargin
          }
        }
      }
    }
  }

}

/**
 * Ol' Spray route. Not typesafe, unlike the Akka's
 * implementation
 */
object Example2 extends Routing {
  import spray.routing._, Directives._

  runSprayServer {
    path("routing") {
      get {
        parameter('q) { q =>
          complete {
            s"""
               |Question: $q
               |Answer: ${answer()}
              """.stripMargin
          }
        }
      }
    }
  }

}

/**
 * Routing structure, natural way to describe your REST API.
 */
object Example3 extends Routing {
  import akka.http.scaladsl.server._, Directives._

  val route2: Route = {
    path("route2") {
      post {
        complete("POST /routing/route2")
      } ~
      get {
        complete("GET /routing/route2")
      }
    }
  }

  /**
   * Request enters at the very root of the tree traversing all the branches
   * in a depth-first manner. In case of a rejection it tries the next branch.
   *
   * Not found handler [[RejectionHandler.default]]
   */
  runServer {
    pathPrefix("routing") {
      path("route1") {
        get {
          complete("GET /routing/route1")
        }
      } ~
      route2
    } ~
    complete("Unknown service...")
  }

}

/**
 * Take aways:
 *  1. Route = Context => Result
 *  2. Concatenation to compose Routes into Tree-like structures
 */