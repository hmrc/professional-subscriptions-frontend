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

package utils

import base.SpecBase
import models.{Address, TaxYearSelection, UserAnswers}
import org.scalatest.prop.PropertyChecks
import pages.{CitizensDetailsAddress, YourAddressPage}

class CheckYourAnswersHelperSpec extends SpecBase with PropertyChecks {

  private def helper(ua: UserAnswers) = new CheckYourAnswersHelper(ua)


  "yourAddress" when {
    "correct" must {
      "display the correct label, answer and message args" in {
        val ua = emptyUserAnswers.set(YourAddressPage, true).success.value
        val ua2 = ua.set(CitizensDetailsAddress, validAddress).success.value
        helper(ua2).yourAddress.get.label mustBe "yourAddress.checkYourAnswersLabel"
        helper(ua2).yourAddress.get.answer mustBe "site.yes"
        helper(ua2).yourAddress.get.labelArgs.head mustBe Address.asString(validAddress)
      }
    }

      "incorrect" must {
        "display the correct label, answer, and message args" in {
          val ua = emptyUserAnswers.set(YourAddressPage, false).success.value
          val ua2 = ua.set(CitizensDetailsAddress, validAddress).success.value
          helper(ua2).yourAddress.get.label mustBe "yourAddress.checkYourAnswersLabel"
          helper(ua2).yourAddress.get.answer mustBe "site.no"
          helper(ua2).yourAddress.get.labelArgs.head mustBe Address.asString(validAddress)
        }
    }
  }

  "taxYearSelection" must {
    "display the correct label and answer" in {
      val taxYears = Gen.nonEmptyContainerOf[Seq, TaxYearSelection](Gen.oneOf(
        TaxYearSelection.CurrentYear,
        TaxYearSelection.CurrentYearMinus1,
        TaxYearSelection.CurrentYearMinus2,
        TaxYearSelection.CurrentYearMinus3,
        TaxYearSelection.CurrentYearMinus4
      ))

      forAll(taxYears) {
        taxYearSeq =>
          val ua = emptyUserAnswers.set(TaxYearSelectionPage, taxYearSeq).success.value
          helper(ua).taxYearSelection.get.label mustBe "taxYearSelection.checkYourAnswersLabel"
          helper(ua).taxYearSelection.get.answer mustBe taxYearSeq.map {
            taxYear =>
              messages(s"taxYearSelection.$taxYear",
                TaxYearSelection.getTaxYear(taxYear).toString,
                (TaxYearSelection.getTaxYear(taxYear) + 1).toString
              )
          }.mkString("<br>")
      }
    }
  }


}
