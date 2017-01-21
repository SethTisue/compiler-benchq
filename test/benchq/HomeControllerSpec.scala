package benchq

import controllers.HomeController
import org.scalatestplus.play._
import play.api._
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec {
  lazy val components = {
    val env = Environment.simple()
    val context = ApplicationLoader.createContext(env)
    new BenchQComponents(context)
  }
  implicit lazy val app = components.application

  "HomeController GET" should {
    "render the index page from a new instance of controller" in {
      val controller = new HomeController(components.toolDb)
      val home = controller.index().apply(FakeRequest())

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }

    "render the index page from the application" in {
      val controller = components.homeController
      val home = controller.index().apply(FakeRequest())

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }

    "render the index page from the router" in {
      // Need to specify Host header to get through AllowedHostsFilter
      val request = FakeRequest(GET, "/").withHeaders("Host" -> "localhost")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }
  }
}
