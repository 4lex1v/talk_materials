package universe.errors

import akka.http.scaladsl.server._
import Directives._
import akka.http.scaladsl.model.StatusCodes
import universe.internal

trait Errors extends internal.Scaffolding with App {

  /**
   * Custom Exceptions
   */
  final case class CustomError(value: String) extends Throwable(value)
  final case class SomeOtherError(value: String) extends Throwable(value)

  /**
   * Custom Rejections
   */
  final case class CustomRejection(value: String) extends Rejection


}

object Example1 extends Errors {

  val customHandler: ExceptionHandler = {
    ExceptionHandler {
      case ex: CustomError => complete("There's an error in your code!")
    }
  }

  implicit val exceptionHandler: ExceptionHandler =  customHandler.withFallback {
    ExceptionHandler {
      case SomeOtherError(v) => complete(s"Another error :( $v")
    }
  }

  /**
   * Exceptions thrown during route execution bubble up through the route structure
   * to the defined exception handler.
   */
  runServer {
    path("errors") {
      get {
        complete(throw new CustomError("Here comes the BOOM!"))
      }
    }
  }
}

object Exampled2 extends Errors {
  implicit val customRejections: RejectionHandler = {
    RejectionHandler.newBuilder()
      .handle {
        case CustomRejection(value) =>
          complete(StatusCodes.BadRequest, s"The reason: $value")
      }
      .result()
  }

  runServer {
    path("rejections") {
      get {
        reject(CustomRejection("Rejected!"))
      }
    }
  }

}