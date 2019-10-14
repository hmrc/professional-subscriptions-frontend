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
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.MustMatchers
import org.scalatest.prop.PropertyChecks
import play.api.libs.json._
import models.PSubsByYear._


class PSubsByYearSpec extends SpecBase with MustMatchers with PropertyChecks with Generators {

  "PSubsByYear deserialization" must {
    "deserialise a simple json representation of a PSubByYear" in {

      forAll(arbitrary[Int], Gen.listOf(psubGen)) {
        (taxYear, pSubs) =>

          val json: JsValue = Json.obj(
            "subscriptions" -> Json.obj(
              taxYear.toString -> pSubs
            )
          )

          json.validate[PSubsByYear] mustEqual JsSuccess(PSubsByYear(Map(taxYear -> pSubs)))
      }
    }

    "must not deserialise an incomplete psub" in {

      val json: JsValue = Json.obj(
        "subscriptions" -> Json.obj(
          taxYear.toString -> Json.arr(
            Json.obj(
              "nameOfProfessionalBody" -> "invalidPsub"
            ),
            Json.obj(
              "nameOfProfessionalBody" -> "validPsub1",
              "amount" -> 123,
              "employerContributed" -> false
            ),
            Json.obj(
              "nameOfProfessionalBody" -> "validPsub2",
              "amount" -> 123,
              "employerContributed" -> true,
              "employerContributionAmount" -> 1
            )
          )
        )
      )

      json.validate[PSubsByYear] mustEqual JsSuccess(
        PSubsByYear(Map(
          taxYear.toInt -> Seq(
            PSub("validPsub1", 123, false, None),
            PSub("validPsub2", 123, true, Some(1))
          )
        ))
      )
    }

    "must fail to deserialise when invalid json" in {
      val json = Json.obj("" -> "")

      json.validate[PSubsByYear] mustBe an[JsError]
    }
  }

  "PSubByYear.apply" must {
    "construct a PSubByYear when there is no previous Psub data" in {
      forAll(intsAboveValue(0)) {
        taxYears =>

          val previousData: Option[Map[Int, Seq[PSub]]] = None
          val result: Map[Int, Seq[PSub]] = PSubsByYear.apply(Seq(taxYears), previousData).subscriptions

          result.get(taxYears) must be(defined)
          result.get(taxYears).value must be(empty)
      }
    }

    "constructs the new model when there is previous Psub data" in {
      forAll(intsAboveValue(0), arbitrary[Seq[PSub]]) {
        (taxYears, psubs) =>

          val previousData: Option[Map[Int, Seq[PSub]]] = Some(Map(taxYears -> psubs))

          val result = PSubsByYear.apply(Seq(taxYears), previousData)

          result mustEqual PSubsByYear(Map(taxYears -> psubs))
      }

    }
  }

  ""
}
