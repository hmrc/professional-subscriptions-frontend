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

package services

import base.SpecBase
import connectors.ProfessionalBodiesConnector
import models.{ProfessionalBody, SubmissionValidationException}
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProfessionalBodiesServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val mockProfessionalBodiesConnector = mock[ProfessionalBodiesConnector]
  private val professionalBodiesService = new ProfessionalBodiesService(mockProfessionalBodiesConnector, Environment.simple(), frontendAppConfig)

  "ProfessionalBodiesService" must {
    "subscriptions" when {
      "must return a sequence of professional bodies" in {
        when(mockProfessionalBodiesConnector.getProfessionalBodies())
          .thenReturn(Future.successful(HttpResponse(200, Some(professionalBodiesJson))))

        val result = professionalBodiesService.subscriptions()

        whenReady(result) {
          _ mustBe Seq(ProfessionalBody("subscription", List(), None))
        }
      }

      "must return an exception when it fails to parse the professional bodies" in {
        when(mockProfessionalBodiesConnector.getProfessionalBodies())
          .thenReturn(Future.successful(HttpResponse(200, Some(invalidProfessionalBodiesJson))))

        val result = professionalBodiesService.subscriptions()

        val exception = intercept[Exception] {
          whenReady(result) {
            _ mustBe an[Exception]
          }
        }

        exception.getMessage must include("failed to get bodies")
      }
    }

    "professionalBodies" when {
      "must return a sequence of professional bodies" in {
        val result = professionalBodiesService.professionalBodies()

        whenReady(result) {
          result =>
            result mustBe a[Seq[_]]
            result.map(_ mustBe a[ProfessionalBody])
        }
      }

      "provided bad data return an exception as errors occur" in {
        val result = professionalBodiesService.professionalBodies("test-professional-bodies.json")

        whenReady(result.failed) {
          result =>
            result mustBe an[Exception]
            result.getMessage must include("failed to parse bodies")
        }
      }

      "no file must thrown an exception as Stream fails" in {
        val result = professionalBodiesService.professionalBodies("no-file.json")

        whenReady(result.failed) {
          result =>
            result mustBe an[Exception]
            result.getMessage must include("failed to load bodies")
        }
      }
    }

    "validateYearInRange" when {
      "return true when subscription is in range" in {
        val result = professionalBodiesService.validateYearInRange(Seq("100 Women in Finance Association"), 2019)
        whenReady(result) {
          result =>
            result mustEqual true
        }
      }

      "return a submissionValidationException when subscription is out of range" in {
        val result = professionalBodiesService.validateYearInRange(Seq("100 Women in Finance Association"), 2017)
        whenReady(result.failed) {
          e =>
            e mustBe an[SubmissionValidationException]
            e.getMessage mustBe "Year out of range"
        }
      }
    }

  }

  lazy val professionalBodiesJson: JsValue = Json.parse(
    s"""
       |[
       |  {"name":"subscription", "synonyms": []}
       |]
    """.stripMargin)

  lazy val invalidProfessionalBodiesJson: JsValue = Json.parse(
    s"""
       |[
       |  {"wrong":""}
       |]
    """.stripMargin)

}
