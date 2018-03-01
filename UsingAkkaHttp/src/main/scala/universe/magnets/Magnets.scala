package universe.magnets

import universe.internal
import akka.http.scaladsl.server._, Directives._
import scala.concurrent.Future

trait Magnets extends internal.Scaffolding with App

/**
 * The Magnet Pattern was a solution created by the Spray team
 * to overcome various issues with function overloading and type
 * erasure
 */
object TheProblem extends Magnets {

  object env {
    def test(list: List[Int]): Unit = ()
//    def test(list: List[String]): Unit = ()

    def test(num: Int): Unit = ()
  }

  def use[A](f: A => Unit) = ()

//  use(env.test _)

}

object Scrathpad extends Magnets {

  val route = {
    onSuccess(Future.successful((10, ""))) {
      (n, s) => _.complete("")
    }
  }

  runServer(route)

}