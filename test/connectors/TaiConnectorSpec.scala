package connectors

import base.SpecBase
import models.{Employment, TaiTaxYear, TaxYearSelection}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import utils.WireMockHelper

import scala.concurrent.Future

class TaiConnectorSpec extends WireMockHelper with MockitoSugar with GuiceOneAppPerSuite with ScalaFutures with SpecBase with IntegrationPatience {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.tai.port" -> server.port
    )
    .build()

  private lazy val taiConnector: TaiConnector = app.injector.instanceOf[TaiConnector]

  "taiEmployments" must {
    "return a taiEmployment on success" in {
      server.stubFor(
        get(urlEqualTo(s"/tai$fakeNino/employments/years/2016"))
          .willReturn(
            aResponse()
              .withstatus(OK)
              .withBody(validEmploymentJson.toString)
          )
      )
      val result: Future[Seq[Employment]] = taiConnector.taiEmployments(fakeNino, "2016")

      whenReady(result) {
        result =>
          result mustBe taiEmployment
      }
    }
  }

  val validEmploymentJson: JsValue = Json.parse(
    """{
      | "data" : {
      |   "employments" : [{
      |    "name": "HMRC Longbenton",
      |    "startDate": "2018-06-27"
      |    }]
      |  }
      |}""".stripMargin
  )
}
