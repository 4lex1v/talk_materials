case class SuperString(name: String)

/**
 * Defining a typeclass
 */
trait Show[A] {
  def render(value: A): String
}

object Show {
  implicit def apply[A](s: A)(implicit show: Show[A]): Show[A] = {
    new Show[A] {
      override def render(value: A): String = show.render(value)
    }
  }

  implicit val showSuperString: Show[SuperString] = {
    new Show[SuperString] {
      override def render(value: SuperString): String =
        s"The content is ${value.name}"
    }
  }
}

def render[A](value: A)(implicit s: Show[A]) =
  s.render(value)

render(SuperString("A String"))

///////

implicit def s2SuperS(s: String): SuperString = SuperString(s)

val str: SuperString = "String"

def check(s: SuperString) = ()

check("String")

//

def conjure[A](s: Show[A]) = ()

conjure(SuperString(""))