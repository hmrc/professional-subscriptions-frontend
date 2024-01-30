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

package models.auditing

import base.SpecBase
import generators.{Generators, ModelGenerators}
import models.PSub
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{OptionValues}
import play.api.libs.json._

class AuditSubmissionDataSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks with OptionValues
  with Generators with ModelGenerators {

  "deserialization to JSON" must {

    "ContainsCurrentYearUserData" in {
      val sut: AuditSubmissionData = ContainsCurrentYearUserData(
        previouslyClaimedAmountsFromNPS = Map(1 -> 1),
        hasUserChangedClaimedAmount = Some(true),
        subscriptions = Map(1 -> Seq(PSub("name", 1, employerContributed = true, Some(1)))),
        yourEmployersNames = Seq("a"),
        yourEmployer = true,
        userCurrentCitizensDetailsAddress = Some(validAddress)
      )

      val expectedJson = Json.obj(
        "previouslyClaimedAmountsFromNPS" -> Json.obj("1" -> 1),
        "hasUserChangedClaimedAmount"-> true,
        "subscriptions" -> Json.obj(
          "1" -> JsArray(
            Seq(
              Json.obj(
                "nameOfProfessionalBody" -> JsString("name"),
                "amount" -> JsNumber(1),
                "employerContributed" -> JsBoolean(true),
                "employerContributionAmount" -> 1
              )
            )
          )
        ),
        "yourEmployersNames" -> JsArray(Seq(JsString("a"))),
        "yourEmployer" -> true,
        "userCurrentCitizensDetailsAddress" -> Json.obj(
          "line1" -> "6 Howsell Road",
          "line2" -> "Llanddew",
          "line3" -> "Line 3",
          "line4" -> "Line 4",
          "line5" -> "Line 5",
          "postcode" -> "DN16 3FB",
          "country" -> "GREAT BRITAIN"
        )
      )

      Json.toJson(sut) mustEqual expectedJson
    }

    "PreviousYearsUserData" in {
      val sut: AuditSubmissionData = PreviousYearsUserData(
        previouslyClaimedAmountsFromNPS = Map(1 -> 1),
        hasUserChangedClaimedAmount = Some(true),
        subscriptions = Map(1 -> Seq(PSub("name", 1, employerContributed = true, Some(1)))),
        userCurrentCitizensDetailsAddress = Some(validAddress)
      )

      val expectedJson = Json.obj(
        "previouslyClaimedAmountsFromNPS" -> Json.obj("1" -> 1),
        "hasUserChangedClaimedAmount"-> true,
        "subscriptions" -> Json.obj(
          "1" -> JsArray(
            Seq(
              Json.obj(
                "nameOfProfessionalBody" -> JsString("name"),
                "amount" -> JsNumber(1),
                "employerContributed" -> JsBoolean(true),
                "employerContributionAmount" -> 1
              )
            )
          )
        ),
        "userCurrentCitizensDetailsAddress" -> Json.obj(
          "line1" -> "6 Howsell Road",
          "line2" -> "Llanddew",
          "line3" -> "Line 3",
          "line4" -> "Line 4",
          "line5" -> "Line 5",
          "postcode" -> "DN16 3FB",
          "country" -> "GREAT BRITAIN"
        )
      )

      Json.toJson(sut) mustEqual expectedJson
    }

  }
}
