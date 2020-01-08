/*
 * Copyright 2020 HM Revenue & Customs
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
import models.ProfessionalBody
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global

class ProfessionalBodiesServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val professionalBodiesService = new ProfessionalBodiesService(Environment.simple(), frontendAppConfig)

  "ProfessionalBodiesService" must {
    "professionalBodies" when {
      "must return a sequence of professional bodies" in {
        val x = professionalBodiesService.professionalBodies

        x.head mustEqual ProfessionalBody("100 Women in Finance Association", Nil, Some(2018))
      }
    }

    "validateYearInRange" when {
      "return true when subscription is in range" in {
        val result = professionalBodiesService.validateYearInRange(Seq("100 Women in Finance Association"), 2019)

        result mustEqual true
      }

      "return a submissionValidationException when subscription is out of range" in {
        val result = professionalBodiesService.validateYearInRange(Seq("100 Women in Finance Association"), 2017)

        result mustEqual false
      }

      "return true when single subscription is in range" in {
        val result = professionalBodiesService.validateYearInRange("100 Women in Finance Association", 2019)

        result mustEqual true
      }

      "return a submissionValidationException when single subscription is out of range" in {
        val result = professionalBodiesService.validateYearInRange("100 Women in Finance Association", 2017)

        result mustEqual false
      }
    }
  }
}

