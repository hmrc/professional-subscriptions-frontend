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
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import pages._

class CheckYourAnswersHelperSpec extends SpecBase with PropertyChecks {

  private def helper(ua: UserAnswers) = new CheckYourAnswersHelper(ua)


  "yourAddress" when {
    "true" must {
      "display the correct label, answer and message args" in {
        val ua = emptyUserAnswers.set(YourAddressPage, true).success.value
        val ua2 = ua.set(CitizensDetailsAddress, validAddress).success.value
        helper(ua2).yourAddress.get.label mustBe "yourAddress.checkYourAnswersLabel"
        helper(ua2).yourAddress.get.answer mustBe "site.yes"
        helper(ua2).yourAddress.get.labelArgs.head mustBe Address.asString(validAddress)
      }
    }

      "false" must {
        "display the correct label, answer, and message args" in {
          val ua = emptyUserAnswers.set(YourAddressPage, false).success.value
          val ua2 = ua.set(CitizensDetailsAddress, validAddress).success.value
          helper(ua2).yourAddress.get.label mustBe "yourAddress.checkYourAnswersLabel"
          helper(ua2).yourAddress.get.answer mustBe "site.no"
          helper(ua2).yourAddress.get.labelArgs.head mustBe Address.asString(validAddress)
        }
    }
  }


  "yourEmployer" when {
    val employment = Seq("HMRC Longbenton", "DWP")
    "correct" must {
      "display the correct label, answer, and message args" in {
        val ua1 = emptyUserAnswers.set(YourEmployerPage, true).success.value
        val ua2 = ua1.set(YourEmployersNames, employment).success.value
        helper(ua2).yourEmployer.get.label mustBe "yourEmployer.checkYourAnswersLabel"
        helper(ua2).yourEmployer.get.answer mustBe "site.yes"
        helper(ua2).yourEmployer.get.labelArgs.head mustBe s"<p>${employment.mkString("<br>")}</p>"
      }
    }

    "incorrect" must {
      "display the correct label, answer, and message args" in {
        val ua1 = emptyUserAnswers.set(YourEmployerPage, false).success.value
        val ua2 = ua1.set(YourEmployersNames, employment).success.value
        helper(ua2).yourEmployer.get.label mustBe "yourEmployer.checkYourAnswersLabel"
        helper(ua2).yourEmployer.get.answer mustBe "site.no"
        helper(ua2).yourEmployer.get.labelArgs.head mustBe s"<p>${employment.mkString("<br>")}</p>"
      }
    }
    
    "is empty" must {
      "return None" in {
        helper(emptyUserAnswers).yourEmployer mustBe None
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

  "sameAmountsAllYears" when {
    "true" must {
      "display the correct label, answer" in {
        val ua = emptyUserAnswers.set(SameAmountAllYearsPage, true).success.value
        helper(ua).sameAmountAllYears.get.label mustBe "sameAmountAllYears.checkYourAnswersLabel"
        helper(ua).sameAmountAllYears.get.answer mustBe "site.yes"
      }
    }

    "false" must {
      "display the correct label, answer" in {
        val ua = emptyUserAnswers.set(SameAmountAllYearsPage, false).success.value
        helper(ua).sameAmountAllYears.get.label mustBe "sameAmountAllYears.checkYourAnswersLabel"
        helper(ua).sameAmountAllYears.get.answer mustBe "site.no"
      }
    }
  }

  "addAnotherSubscription" when {
    "true" must {
      "display the correct label, answer" in {
        val ua = emptyUserAnswers.set(AddAnotherSubscriptionPage, true).success.value
        helper(ua).addAnotherSubscription.get.label mustBe "addAnotherSubscription.checkYourAnswersLabel"
        helper(ua).addAnotherSubscription.get.answer mustBe "site.yes"
      }
    }

    "false" must {
      "display the correct label, answer" in {
        val ua = emptyUserAnswers.set(AddAnotherSubscriptionPage, false).success.value
        helper(ua).addAnotherSubscription.get.label mustBe "addAnotherSubscription.checkYourAnswersLabel"
        helper(ua).addAnotherSubscription.get.answer mustBe "site.no"
      }
    }
  }

  "subscriptionAmount" when {
    "20" must {
      "display the correct label, answer" in {
        val ua = emptyUserAnswers.set(SubscriptionAmountPage, 20).success.value
        helper(ua).subscriptionAmount.get.label mustBe "subscriptionAmount.checkYourAnswersLabel"
        helper(ua).subscriptionAmount.get.answer mustBe "20"
      }
    }
  }
}
