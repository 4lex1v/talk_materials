load.ivy("org.scalaz" %% "scalaz-core" % "7.2.0")
load.ivy("org.scalaz" %% "scalaz-concurrent" % "7.2.0")


case class Employee(name: String, age: Int, lang: String)
val map = Map("Alex" -> Employee("Aleksandr", 25, "Scala"))
def findByName(id: String): Future[Employee] = Future.async(cb => cb(map(id)))
findByName("Alex").map(_.lang).runAsync { lang =>
  println(s"The language is: $lang ")
}
