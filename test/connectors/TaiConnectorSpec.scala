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
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HttpResponse, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

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
      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYearInt)

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
      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYearInt)

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
      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYearInt)

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
      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYearInt)

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

      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYearInt)

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

      val result: Future[Seq[Employment]] = taiConnector.getEmployments(fakeNino, taxYearInt)

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

      val result = taiConnector.getTaxCodeRecords(fakeNino, taxYearInt)

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

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecords(fakeNino, taxYearInt)

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

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecords(fakeNino, taxYearInt)

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

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecords(fakeNino, taxYearInt)

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

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecords(fakeNino, taxYearInt)

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

      val result: Future[Seq[TaxCodeRecord]] = taiConnector.getTaxCodeRecords(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe Seq.empty
      }
    }
  }

  "isYearAvailable" must {
    "return a true on success" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYearInt/summary"))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      val result: Future[Boolean] = taiConnector.isYearAvailable(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe true
      }
    }

    "return false on NOT_FOUND response" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYearInt/summary"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result: Future[Boolean] = taiConnector.isYearAvailable(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe false
      }
    }

    "return false on INTERNAL_SERVER_ERROR response" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYearInt/summary"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result: Future[Boolean] = taiConnector.isYearAvailable(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe false
      }
    }

    "return false on UNAUTHORISED response" in {
      server.stubFor(
        get(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYearInt/summary"))
          .willReturn(
            aResponse()
              .withStatus(UNAUTHORIZED)
          )
      )

      val result: Future[Boolean] = taiConnector.isYearAvailable(fakeNino, taxYearInt)

      whenReady(result) {
        result =>
          result mustBe false
      }
    }
      }

  "updateProfessionalSubscriptionAmount" must {
    "returns a successful future on a NO_CONTENT response" in {
      server.stubFor(
        post(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
          )
      )

      val result: Future[Unit] = taiConnector.updateProfessionalSubscriptionAmount(fakeNino, taxYearInt, 1, 100)

      whenReady(result) {_ => succeed}
    }

    "returns a failed future on a INTERNAL_SERVER_ERROR response" in {
      server.stubFor(
        post(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result: Future[Unit] = taiConnector.updateProfessionalSubscriptionAmount(fakeNino, taxYearInt, 1, 100)
      whenReady(result.failed) {ex =>
        ex mustBe an[Upstream5xxResponse]
      }
    }

    "returns a failed future on a NOT_FOUND response" in {
      server.stubFor(
        post(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result: Future[Unit] = taiConnector.updateProfessionalSubscriptionAmount(fakeNino, taxYearInt, 1, 100)
      whenReady(result.failed) {ex =>
        ex mustBe an[NotFoundException]
      }
    }

    "returns a failed future on a UNAUTHORIZED response" in {
      server.stubFor(
        post(urlEqualTo(s"/tai/$fakeNino/tax-account/$taxYear/expenses/employee-expenses/57"))
          .willReturn(
            aResponse()
              .withStatus(UNAUTHORIZED)
          )
      )

      val result: Future[Unit] = taiConnector.updateProfessionalSubscriptionAmount(fakeNino, taxYearInt, 1, 100)
      whenReady(result.failed) {ex =>
        ex mustBe an[Upstream4xxResponse]
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

  val validTaxCodeJson: JsValue = Json.parse(
    """
      |{
      |  "data" : [ {
      |    "componentType" : "EmploymentIncome",
      |    "employmentId" : 1,
      |    "amount" : 1100,
      |    "description" : "EmploymentIncome",
      |    "taxCode" : "1150L",
      |    "name" : "Employer1",
      |    "basisOperation" : "Week1Month1BasisOperation",
      |    "status" : "Live",
      |    "inYearAdjustment" : 0
      |  }, {
      |    "componentType" : "EmploymentIncome",
      |    "employmentId" : 2,
      |    "amount" : 0,
      |    "description" : "EmploymentIncome",
      |    "taxCode" : "1100L",
      |    "name" : "Employer2",
      |    "basisOperation" : "OtherBasisOperation",
      |    "status" : "PotentiallyCeased",
      |    "inYearAdjustment" : 321.12
      |  }, {
      |    "componentType" : "EmploymentIncome",
      |    "employmentId" : 3,
      |    "amount" : 0,
      |    "description" : "EmploymentIncome",
      |    "taxCode" : "830L",
      |    "name" : "Employer3",
      |    "basisOperation" : "OtherBasisOperation",
      |    "status" : "Ceased",
      |    "inYearAdjustment" : 400.00
      |  }  ],
      |  "links" : [ ]
      |}
    """.stripMargin
  )
}
