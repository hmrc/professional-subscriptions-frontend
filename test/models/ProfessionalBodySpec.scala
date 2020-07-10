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

package models

import base.SpecBase
import play.api.libs.json.Json

class ProfessionalBodySpec extends SpecBase {

  "ProfessionalBody" must {

    "return JsObject when toAutoComplete is called" in {

      val professionalBody = ProfessionalBody("test", List("test"), None)

      professionalBody.toAutoCompleteJson mustBe validProfessionalBodyJson
    }

    "toDisplayText should return a psub with no year when no year present" in {

      val professionalBody = ProfessionalBody("test", List("test"), None)

      professionalBody.toDisplayText mustBe "test"
    }

    "toDisplayText should return a psub with a year when a year is present" in {

      val professionalBody = ProfessionalBody("test", List("test"), Some(2018))

      professionalBody.toDisplayText mustBe "test, with effect from 6 April 2018"
    }

    "return true when the year is after the startYear" in {
      val professionalBody = ProfessionalBody("test", List("test"), Some(2018))
      professionalBody.validateStartYear(2019) mustEqual true

    }

    "return false when the year is before the startYear" in {
      val professionalBody = ProfessionalBody("test", List("test"), Some(2018))
      professionalBody.validateStartYear(2017) mustEqual false

    }

    "return true when the year is the same as the startYear" in {
      val professionalBody = ProfessionalBody("test", List("test"), Some(2018))
      professionalBody.validateStartYear(2018) mustEqual true

    }


    "return true when no year is present" in {
      val professionalBody = ProfessionalBody("test", List("test"), None)
      professionalBody.validateStartYear(2018) mustEqual true

    }
  }

  val validProfessionalBodyJson = Json.parse(
    """
      |{
      |   "displayName":"test",
      |   "synonyms":["test"]
      |}
    """.stripMargin
  )
}
