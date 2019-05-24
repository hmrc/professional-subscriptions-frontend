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

package models

import base.SpecBase
import play.api.libs.json.Json

class ProfessionalBodySpec extends SpecBase {

  "ProfessionalBody" must {

    "return JsObject when toAutoComplete is called" in {

      val professionalBody = ProfessionalBody("test", List("test"))

      professionalBody.toAutoCompleteJson mustBe validProfessionalBodyJson
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
