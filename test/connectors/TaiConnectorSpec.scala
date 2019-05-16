/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import models.{Employment, EmploymentExpense}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaiConnectorSpec extends SpecBase with WireMockHelper with MockitoSugar with GuiceOneAppPerSuite with ScalaFutures with IntegrationPatience {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        conf = "microservice.services.tai.port" -> server.port
      )
      .build()

  private lazy val taiConnector: TaiConnector = app.injector.instanceOf[TaiConnector]

  "taiEmployments" must {
    "return a Sequence of Employments on success" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/employments/years/$taxYear"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(validEmploymentJson.toString)
          )
      )
      val result: Future[Seq[Employment]] = taiConnector.getEmployments("2016", fakeNino)

      whenReady(result) {
        result =>
          result mustBe taiEmployment
      }
    }

    "return an Exception on failure" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/employments/years/$taxYear"))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
          )
      )
      val result: Future[Seq[Employment]] = taiConnector.getEmployments("2016", fakeNino)

      whenReady(result.failed) {
        result =>
          result mustBe an[Exception]
      }
    }

    "getProfessionalSubscriptionAmount" must {
      "return seq employeeExpense when available" in {
        server.stubFor(
          get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withBody(validProfessionalSubscriptionAmountJson.toString)
            )
        )

        val result = taiConnector.getProfessionalSubscriptionAmount(fakeNino, taxYear)

        whenReady(result) {
          result =>
            result mustBe Seq(EmploymentExpense(240))

        }
      }
    }
  }

  val validProfessionalSubscriptionAmountJson: JsValue = Json.parse(
    """
      |[
      |    {
      |        "nino": "AB216913",
      |        "type": 57,
      |        "grossAmount": 240,
      |        "source": 26
      |    }
      |]
      |""".stripMargin)

}
