/*
 * Copyright 2023 HM Revenue & Customs
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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsSuccess, Json}

class PSubSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks {

  "PSub" must {
    "deserialise" in
      forAll(arbitrary[String], arbitrary[Int], arbitrary[Boolean], arbitrary[Option[Int]]) {
        (name, amount, employerContributed, employerContributionAmount) =>
          val json = Json.obj(
            "nameOfProfessionalBody"     -> name,
            "amount"                     -> amount,
            "employerContributed"        -> employerContributed,
            "employerContributionAmount" -> employerContributionAmount
          )

          json.validate[PSub] mustEqual JsSuccess(PSub(name, amount, employerContributed, employerContributionAmount))
      }

    "deserialise old model name is used" in
      forAll(arbitrary[String], arbitrary[Int], arbitrary[Boolean], arbitrary[Option[Int]]) {
        (name, amount, employerContributed, employerContributionAmount) =>
          val json = Json.obj(
            "name"                       -> name,
            "amount"                     -> amount,
            "employerContributed"        -> employerContributed,
            "employerContributionAmount" -> employerContributionAmount
          )

          json.validate[PSub] mustEqual JsSuccess(PSub(name, amount, employerContributed, employerContributionAmount))
      }

    "must fail to deserialise when invalid json" in {
      val json = Json.obj("" -> "")

      json.validate[PSub] mustBe an[JsError]
    }
  }

}
