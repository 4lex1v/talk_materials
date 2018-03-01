package universe.marshalling

import akka.http.scaladsl.model.MessageEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.Materializer
import universe.internal

import scala.concurrent.{ExecutionContext, Future}

trait Marshalling extends internal.Scaffolding with App {
  import spray.json.DefaultJsonProtocol._

  final case class Meetup(language: String, day: Int)
  object Meetup {
    implicit val format = jsonFormat2(Meetup.apply)
  }
}

object Example1 extends Marshalling {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  runServer {
    path("marshalling") {
      get {
        complete(Meetup("Scala", 12))
      } ~
      post {
        entity(as[Meetup]) { meetup =>
          complete(s"The ${meetup.language} will be on ${meetup.day} of April")
        }
      }
    }
  }

}

object Example2 extends Marshalling {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import akka.http.scaladsl.marshalling.Marshal
  import akka.http.scaladsl.unmarshalling.Unmarshal
  import spray.json._

  import scala.concurrent.ExecutionContext.Implicits.global

  Marshal[Meetup](Meetup("Scala", 12)).to[MessageEntity]

  implicit val unm: Unmarshaller[JsValue, Meetup] = {
    Unmarshaller[JsValue, Meetup] { ctx => json =>
      Future.successful(Meetup.format.read(json))
    }
  }

  Unmarshal[JsValue](
    JsObject(
      "language" -> JsString("Scala"),
      "day" -> JsNumber(12)
    )
  ).to[Meetup]

}