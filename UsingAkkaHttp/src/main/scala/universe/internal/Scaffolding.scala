package universe.internal

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRefFactory, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import akka.stream.ActorMaterializer

import scala.concurrent.duration._

trait Scaffolding {

  implicit val system = ActorSystem("UsingAkkaHttp")
  implicit val materializer = ActorMaterializer()

  val config = system.settings.config.getConfig("config")
  val log = system.log

  val minute = 1.minute

  val interface = config.getString("host")
  val port      = config.getInt("port")

  def runServer(route: Route)(implicit
    rh: RejectionHandler = RejectionHandler.default,
    eh: ExceptionHandler = null) = {

    Http().bindAndHandle(route, interface, port)

    started()
  }

  def runSprayServer(sprayRoute: spray.routing.Route) = {
    import akka.actor.ActorDSL._

    akka.io.IO(spray.can.Http) ! spray.can.Http.Bind(
      actor(new Actor with spray.routing.HttpService {
        implicit val actorRefFactory: ActorRefFactory = context
        implicit val _ = spray.routing.RoutingSettings.default(context)
        override def receive: Receive = runRoute(sprayRoute)
      }), interface, port
    )
  }

  def started() = log.info(s"Server started @ $interface:$port")

}
