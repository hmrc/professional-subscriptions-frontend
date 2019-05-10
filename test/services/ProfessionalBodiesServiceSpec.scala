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
import models.ProfessionalBody
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.Environment
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ProfessionalBodiesServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val mockProfessionalBodiesConnector = mock[ProfessionalBodiesConnector]
  private val mockEnvironment = mock[Environment]
  private val professionalBodiesService = new ProfessionalBodiesService(mockProfessionalBodiesConnector, mockEnvironment)
  private val professionalBodies = Seq(ProfessionalBody("subscription", List("")))

  "ProfessionalBodiesService" must {
    "subscriptions" when {
      "must return a sequence of professional bodies" in {
        when(mockProfessionalBodiesConnector.getProfessionalBodies())
          .thenReturn(Future.successful(HttpResponse(200, Some(professionalBodiesJson))))

        val result = professionalBodiesService.subscriptions()

        whenReady(result) {
          result =>
            result mustBe professionalBodies
        }
      }
    }

    "localSubscriptions" when {
      "must return a sequence of professional bodies" in {
        when(mockEnvironment.resourceAsStream("public/professional-bodies.json"))
          .thenReturn(Some(InputStream()))

        val result = professionalBodiesService.localSubscriptions()

        whenReady(result) {
          result =>
            result mustBe professionalBodies
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

}
