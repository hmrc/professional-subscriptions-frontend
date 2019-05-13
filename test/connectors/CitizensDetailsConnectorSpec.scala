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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HttpResponse
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CitizensDetailsConnectorSpec extends SpecBase with WireMockHelper with ScalaFutures with IntegrationPatience {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        conf = "microservice.services.citizen-details.port" -> server.port
      )
      .build()

  private lazy val citizenDetailsConnector: CitizenDetailsConnector = app.injector.instanceOf[CitizenDetailsConnector]


  "getAddress" must {
    "return 200 and an address on success" in {
      server.stubFor(
        get(urlEqualTo(s"/citizen-details/$fakeNino/designatory-details"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(validAddressJson.toString)
          )
      )

      val result: Future[HttpResponse] = citizenDetailsConnector.getAddress(fakeNino)

      whenReady(result) {
        result =>
          result.body mustBe validAddressJson.toString
      }
    }

    "return 500 on failure" in {
      server.stubFor(
        get(urlEqualTo(s"/citizen-details/$fakeNino/designatory-details"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = citizenDetailsConnector.getAddress(fakeNino)

      whenReady(result.failed) {
        result =>
          result mustBe an[Exception]
      }
    }

    "return 404 when record not found" in {
      server.stubFor(
        get(urlEqualTo(s"/citizen-details/$fakeNino/designatory-details"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = citizenDetailsConnector.getAddress(fakeNino)

      whenReady(result.failed) {
        result =>
          result mustBe an[Exception]
      }
    }

    "return 423 when record is hidden" in {
      server.stubFor(
        get(urlEqualTo(s"/citizen-details/$fakeNino/designatory-details"))
          .willReturn(
            aResponse()
              .withStatus(LOCKED)
          )
      )

      val result: Future[HttpResponse] = citizenDetailsConnector.getAddress(fakeNino)

      whenReady(result.failed) {
        result =>
          result mustBe an[Exception]
      }
    }
  }
}
