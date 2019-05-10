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
import com.github.tomakehurst.wiremock.client.WireMock._
import models._
import org.joda.time.LocalDate
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http._
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProfessionalBodiesConnectorSpec extends SpecBase with MockitoSugar with WireMockHelper with GuiceOneAppPerSuite with ScalaFutures with IntegrationPatience {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        conf = "microservice.services.professional-bodies.port" -> server.port
      )
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val professionalBodiesConnector: ProfessionalBodiesConnector = app.injector.instanceOf[ProfessionalBodiesConnector]

  "getProfessionalBodies" must {
    "return the professional bodies on success" in {
      server.stubFor(
        get(urlEqualTo(s"/professionalBodies"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(professionalBodies.toString)
          )
      )

      val result: Future[HttpResponse] = professionalBodiesConnector.getProfessionalBodies()

      whenReady(result) {
        res =>
          res mustBe a[HttpResponse]
          res.status mustBe 200
          res.body mustBe professionalBodies.toString
      }
    }

    lazy val professionalBodies: JsValue = Json.parse(
      s"""
         |[
         |  {"name":"subscriptions", "synonyms": []}
         |]
    """.stripMargin)
  }
}
