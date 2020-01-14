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
import generators.Generators
import models.NpsDataFormats._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.MustMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._


class NpsDataFormatsSpec extends SpecBase with MustMatchers with ScalaCheckPropertyChecks with Generators {

  "NpsDataFormats" must {
    "deserialise" in {

      forAll(arbitrary[Int], arbitrary[Int]) {
        (taxYear, employmentExpense) =>

          val json: JsValue = Json.obj(
            taxYear.toString -> employmentExpense
          )

          json.validate[Map[Int, Int]] mustEqual JsSuccess(Map(taxYear -> employmentExpense))
      }
    }

    "must fail to deserialise when invalid json" in {
      val json = Json.obj("" -> "")

      json.validate[Map[Int, Int]] mustBe an[JsError]
    }
  }
}
