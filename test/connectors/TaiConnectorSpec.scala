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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, post, urlEqualTo}
import models.TaxCodeStatus._
import models.{Employment, EmploymentExpense, TaxCodeRecord}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Logger}
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
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

  "getEmployments" must {
    "return a sequence of Employments on OK" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/employments/years/$taxYear"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(validEmploymentJson.toString)
          )
      )
      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYear)

      whenReady(result) {
        result =>
          result mustBe taiEmployment
      }
    }

    "return an empty sequence on INTERNAL_SERVER_ERROR" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/employments/years/$taxYear"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )
      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYear)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on NOT_FOUND" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/employments/years/$taxYear"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )
      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYear)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on UNAUTHORIZED" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/employments/years/$taxYear"))
          .willReturn(
            aResponse()
              .withStatus(UNAUTHORIZED)
          )
      )
      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYear)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on OK when empty array returned" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/employments/years/$taxYear"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(emptyEmploymentsSeqJson.toString)
          )
      )

      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYear)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on OK for Json parse error" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/employments/years/$taxYear"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(invalidEmploymentsJson.toString)
          )
      )

      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYear)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }
  }

  "getProfessionalSubscriptionAmount" must {
    "return a sequence of EmploymentExpense on OK" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(validProfessionalSubscriptionAmountJson.toString)
          )
      )

      val result = taiConnector.getProfessionalSubscriptionAmount(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq(EmploymentExpense(240))

      }
    }

    "return an empty sequence on INTERNAL_SERVER_ERROR" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = taiConnector.getProfessionalSubscriptionAmount(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on NOT_FOUND" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = taiConnector.getProfessionalSubscriptionAmount(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on UNAUTHORIZED" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(UNAUTHORIZED)
          )
      )

      val result = taiConnector.getProfessionalSubscriptionAmount(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on OK when empty array returned" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(emptyProfessionalSubscriptionAmountJson.toString)
          )
      )

      val result = taiConnector.getProfessionalSubscriptionAmount(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty

      }
    }

    "return an empty sequence on OK for Json parse error" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(invalidJson.toString)
          )
      )

      val result = taiConnector.getProfessionalSubscriptionAmount(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty

      }
    }
  }

  "getTaxCodeRecords" must {
    "return a sequence of TaxCodeRecord on  OK" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/income/tax-code-incomes"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(validTaxCodeRecordJson.toString)
          )
      )

      val result = taiConnector.getTaxCodeRecord(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq(
            TaxCodeRecord("1150L", Live),
            TaxCodeRecord("1100L", PotentiallyCeased),
            TaxCodeRecord("1100L", Ceased)
          )
      }
    }

    "return an empty sequence on INTERNAL_SERVER_ERROR" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/income/tax-code-incomes"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecord(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on NOT_FOUND" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/income/tax-code-incomes"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecord(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on UNAUTHORIZED" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/income/tax-code-incomes"))
          .willReturn(
            aResponse()
              .withStatus(UNAUTHORIZED)
          )
      )

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecord(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on OK when empty array returned" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/income/tax-code-incomes"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(emptySeqJson.toString)
          )
      )

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecord(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }

    "return an empty sequence on OK for Json parse error" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/income/tax-code-incomes"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(invalidJson.toString)
          )
      )

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecord(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }
  }

  "taiTaxAccountSummary" must {
    "return a 200 on success" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYearInt/summary"))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      val result: Future[HttpResponse] = taiConnector.taiTaxAccountSummary(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result.status mustBe OK
      }
    }
  }

  "updateProfessionalSubscriptionAmount" must {
    "return a 200 on success" in {
      server.stubFor(
        post(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYearInt/expenses/flat-rate-expenses"))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      val result: Future[HttpResponse] = taiConnector.updateProfessionalSubscriptionAmount(fakeNino, taxYearInt, 1, 100)

      whenReady(result) {
        result =>
          result.status mustBe OK
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

  val emptyProfessionalSubscriptionAmountJson: JsValue = Json.parse(
    """
      |[]
      |""".stripMargin)

  val invalidEmploymentsJson: JsValue = Json.parse(
    """{
      |  "data" : {
      |    "x": [{
      |      "name": "HMRC LongBenton",
      |      "startDate": "2018-06-27"
      |    }]
      |  }
      |}""".stripMargin)

  val emptyEmploymentsSeqJson: JsValue = Json.parse(
    """{
      |  "data" : {
      |    "employments": []
      |  }
      |}""".stripMargin)

  val emptySeqJson: JsValue = Json.parse(
    """{
      |  "data" : []
      |}""".stripMargin)

  val invalidJson: JsValue = Json.parse(
    """{
      |  "x" : []
      |}""".stripMargin)
}
