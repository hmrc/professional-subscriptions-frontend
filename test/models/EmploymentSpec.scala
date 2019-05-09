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

import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsValue, Json}

class EmploymentSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues {

  val validEmploymentsJson: JsValue = Json.parse(
    """{
      |  "data" : {
      |    "employments": [{
      |      "name": "Employment Name 1",
      |      "startDate": "2018-06-27"
      |    },
      |    {
      |      "name": "Employment Name 2",
      |      "startDate": "2018-06-27"
      |    }]
      |  }
      |}""".stripMargin)


  val validEmployments: Seq[Employment] = Seq(Employment("Employment Name 1"), Employment("Employment Name 2"))


  "Employment" must {
    "must deserialise from json" in {
      val result = validEmploymentsJson.as[Seq[Employment]]
      result mustBe validEmployments
    }
  }
}
