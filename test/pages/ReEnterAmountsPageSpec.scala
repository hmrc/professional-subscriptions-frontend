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

package pages

import models.{Address, PSub, PSubsByYear, UserAnswers}
import pages.behaviours.PageBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.libs.json.JsPath


class ReEnterAmountsPageSpec extends PageBehaviours {

  "ReEnterAmountsPage" must {

    beRetrievable[Boolean](ReEnterAmountsPage)

    beSettable[Boolean](ReEnterAmountsPage)

    beRemovable[Boolean](ReEnterAmountsPage)

    "cleanup" must {

      "does not remove data for true" in {

        forAll(arbitrary[UserAnswers], arbitrary[Address], Gen.nonEmptyListOf(arbitrary[String]), Gen.nonEmptyListOf(arbitrary[PSub])) {
          case (baseUserAnswers, address, employers, psubs) =>

            val year = 1

            val userAnswers = baseUserAnswers
              .set(TestSummarySubscriptionsPage, Map(year.toString -> psubs)).success.value
              .set(CitizensDetailsAddress, address).success.value
              .set(YourEmployersNames, employers).success.value

            val results = ReEnterAmountsPage.cleanup(Some(true), userAnswers).success.value

            results.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) must be(defined)
            results.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).value.get(year).value.nonEmpty must be(true)
            results.get(CitizensDetailsAddress) must be(defined)
            results.get(YourEmployersNames) must be(defined)

        }
      }

      "remove data for remove in false" in {

        forAll(arbitrary[UserAnswers], arbitrary[Address], Gen.nonEmptyListOf(arbitrary[String]), Gen.nonEmptyListOf(arbitrary[PSub])) {
          case (baseUserAnswers, address, employers, psubs) =>

            val year = 1

            val userAnswers = baseUserAnswers
              .set(TestSummarySubscriptionsPage, Map(year.toString -> psubs)).success.value
              .set(CitizensDetailsAddress, address).success.value
              .set(YourEmployersNames, employers).success.value

            val results = ReEnterAmountsPage.cleanup(Some(false), userAnswers).success.value

            results.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) must be(defined)
            results.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).value.get(year).value.isEmpty must be(true)
            results.get(CitizensDetailsAddress) must not be(defined)
            results.get(YourEmployersNames) must not be(defined)

        }
      }

      "remove data for remove if no value" in {

        forAll(arbitrary[UserAnswers], arbitrary[Address], Gen.nonEmptyListOf(arbitrary[String]), Gen.nonEmptyListOf(arbitrary[PSub])) {
          case (baseUserAnswers, address, employers, psubs) =>

            val year = 1

            val userAnswers = baseUserAnswers
              .set(TestSummarySubscriptionsPage, Map(year.toString -> psubs)).success.value
              .set(CitizensDetailsAddress, address).success.value
              .set(YourEmployersNames, employers).success.value

            val results = ReEnterAmountsPage.cleanup(None, userAnswers).success.value

            results.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) must be(defined)
            results.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).value.get(year).value.isEmpty must be(true)
            results.get(CitizensDetailsAddress) must not be(defined)
            results.get(YourEmployersNames) must not be(defined)

        }
      }
    }
  }


  object TestSummarySubscriptionsPage extends QuestionPage[Map[String, Seq[PSub]]] {
    override def path: JsPath = SummarySubscriptionsPage.path
  }

  object TestNpsData extends QuestionPage[Map[String, Int]] {
    override def path: JsPath = NpsData.path
  }
}


