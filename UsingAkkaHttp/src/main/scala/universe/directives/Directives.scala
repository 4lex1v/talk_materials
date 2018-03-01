package universe.directives

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import spray.json.DefaultJsonProtocol._
import universe.internal

/**
 * Directives are the primitives using which we can build vast
 * Routing structures.
 *
 * List of predefined Directives:
 *   http://doc.akka.io/docs/akka/2.4.3/scala/http/routing-dsl/directives/alphabetically.html
 */
trait Directives extends internal.Scaffolding with App {
  case object BadCalculation extends Rejection
}

object TheBasics extends Directives {

  object Step1 {
    // RequestContext => Future[RouteResult]
    val route: Route = { ctx =>
      if (ctx.request.method == HttpMethods.GET) {
        ctx.complete("Done!")
      } else {
        ctx.reject(MethodRejection(HttpMethods.GET))
      }
    }
  }

  object Step2 {
    /**
     * [[server.Directives.get()]]
     * [[server.Directives.method()]]
     *
     * General form
     * {{{
     *   name(arguments).tapply { extractions =>
     *     ... // inner route
     *   }
     * }}}
     */
    val option1 = get.tapply { _ => ctx =>
      ctx.complete("Done!")
    }
  }

  object Step3 {

    /** [[Directive]] */
    val refined: Route = get.apply { ctx => ctx.complete("Done!") }

  }

  object Step4 {
    /**
     * On [[StandardRoute]] read this:
     * https://groups.google.com/forum/#!msg/spray-user/hMZndG4IYRs/8qWw4volu8sJ
     */
    val result: Route = get { complete("Done!") }
  }

  object Abstraction {

    // Directive[L: Tuple]
    // type Directive0 = Directive[Unit]
    // type Directive1[A] = Directive[Tuple1[A]]
    // Directive[(A, B, ...)]
    /**
     * [[server.util.Tuple]]
     * More on Phantom types :: http://ktoso.github.io/scala-types-of-types/
     */

  }

  /**
   * Summing up:
   *  1. Directive is a building block to construct a Route
   *  2. Uses one Route to product another Route
   *  3. Perceive it as a sequence of nested functions through which
   *     you are passing the incoming Request to get a Result
   */
}

/**
 * A note on Directives0
 */
object OldSprayBehavior extends Directives {
  import spray.routing._
  import Directives._

  /**
   * The following "compiles" to the corresponding code:
   * {{{
   *   val route: Route = {
   *     println("Hello!")
   *     ctx => ctx.complete("Done!")
   *   }
   * }}}
   * This lead to a bug when some logic inbetween was executed during
   * "the compilation" rather on "per request" bases.
   */
  val route: Route = get {
    println("Hello!")
    complete("Done!")
  }

  runSprayServer(route)
}

object ConsistentBehavior extends Directives {

  val route: Route = get {
    println("Hello!")
    complete("Done!")
  }

  runServer(route)

}

/**
 * The basics.
 * 1. Filter request
 * 2. Extract request data
 * 3. Transform request / response
 * 4. Complete request
 */
object Example1 extends Directives {

  runServer {
    get { // Filtering
      // Filtering + Extraction
      path("directives" / IntNumber) { left =>
        // Extraction
        parameter('right.as[Int]) { right =>
          // Transformation
          respondWithHeader(RawHeader("X-Custom-Header", "4lex1v")) {
            complete { // Completion
              val result = left + right
              Map("result" -> result)
            }
          }
        }
      }
    }
  }

}

/**
 * Composition and Type safety
 */
object Example2 extends Directives {

  object Original {
    val route = {
      pathPrefix("routing") {
        path("route1") {
          get {
            complete("GET /routing/route1")
          }
        } ~
        path("route2") {
          post {
            complete("POST /routing/route2")
          } ~
            get {
              complete("GET /routing/route2")
            }
        }
      }
    }
  }

  object Composition {
    val getOrPost = get & post
    val _path = path("route1") | path("route2")

    val route = (_path & getOrPost) {
      extract(_.request.uri.path) { uri =>
        extractMethod { m =>
          complete(s"${m.name()} ${uri.toString()}")
        }
      }
    }
  }

  runServer(Original.route)

  object Typesafe {

    val route = path("directives" / IntNumber) | parameter('number.as[Int])
//    val route = path("directives" / IntNumber) | parameter('number.as[String])
//    val route = path("directives" / IntNumber) | path("directives" / DoubleNumber)
//    val route = path("directives" / IntNumber) | get

//      val route = parameter('name.as[String]) | parameter('age.as[Int])

  }

  /**
   * Take aways:
   *   1. `And` / & operator aggregates all extractions in a typesafe manner
   *   2. `Or` / | operator requires both directives to be of the same type
   */

}

/**
 * Custom directives
 */
object Example3 extends Directives {

  /**
   * http://www.cakesolutions.net/teamblogs/binding-directives-in-spray
   * But doesn't work for Akka HTTP (possibly)
   */
  object ExtendingExisting {

    /**
     * [[Directive.tmap()]] and [[Directive.tflatMap()]]
     * [[akka.http.scaladsl.server.Directive.SingleValueModifiers]]
     */

    // Build a composite Directive that extracts two Integer from incomming request
    val numbers: Directive[(Int, Int)] = path("directive" / IntNumber) & parameter('right.as[Int])

    // Using old Directive we can build a new one atop of it
    val sum: Directive1[Int] = numbers.tmap { case (left, right) => left + right }

    val calculator: Directive1[String] = {
      sum.flatMap {
        case 0 => reject(BadCalculation)
        case x => provide(x.toString)
      }
    }

  }

  runServer(ExtendingExisting.calculator(complete(_)))

  object FromScratch {

    val customGet: Directive0 = Directive { innerRoute => ctx =>
      if (ctx.request.method == HttpMethods.GET) {
        innerRoute()(ctx)
      } else {
        ctx.reject()
      }
    }

  }



}
