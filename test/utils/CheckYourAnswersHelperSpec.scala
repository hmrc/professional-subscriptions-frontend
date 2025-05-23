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

package utils

import base.SpecBase
import models.{Address, PSubsByYear, UserAnswers}
import models.TaxYearSelection._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class CheckYourAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks {

  private def helper(ua: UserAnswers) = new CheckYourAnswersHelper(ua)

  "taxYearSelection" must {
    "display the correct label and answer" in
      userAnswersCurrent.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).map { taxYearSeq =>
        helper(userAnswersCurrent).taxYearSelection.get.label mustBe "taxYearSelection.checkYourAnswersLabel"
        helper(userAnswersCurrent).taxYearSelection.get.answer mustBe taxYearSeq
          .map { taxYear =>
            messages(
              s"taxYearSelection.${getTaxYearPeriod(taxYear._1)}",
              taxYear._1.toString,
              (taxYear._1 + 1).toString
            )
          }
          .mkString("<br>")
      }
  }

  "whichSubscription" must {
    "display the correct label, answer" in {
      helper(emptyUserAnswers)
        .whichSubscription(taxYear, index, psubWithoutEmployerContribution)
        .get
        .label mustBe "whichSubscription.checkYourAnswersLabel"
      helper(emptyUserAnswers)
        .whichSubscription(taxYear, index, psubWithoutEmployerContribution)
        .get
        .answer mustBe psubWithoutEmployerContribution.nameOfProfessionalBody
    }
  }

  "subscriptionAmount" when {
    "display the correct label, answer" in {
      helper(emptyUserAnswers)
        .subscriptionAmount(taxYear, index, psubWithoutEmployerContribution)
        .get
        .label mustBe "subscriptionAmount.checkYourAnswersLabel"
      helper(emptyUserAnswers)
        .subscriptionAmount(taxYear, index, psubWithoutEmployerContribution)
        .get
        .answer mustBe s"£${psubWithoutEmployerContribution.amount}"
    }
  }

  "employerContribution" when {
    "true" must {
      "display the correct label, answer and message args" in {
        helper(emptyUserAnswers)
          .employerContribution(taxYear, index, psubWithEmployerContribution)
          .get
          .label mustBe "employerContribution.checkYourAnswersLabel"
        helper(emptyUserAnswers)
          .employerContribution(taxYear, index, psubWithEmployerContribution)
          .get
          .answer mustBe "site.yes"
      }
    }

    "false" must {
      "display the correct label, answer, and message args" in {
        helper(emptyUserAnswers)
          .employerContribution(taxYear, index, psubWithoutEmployerContribution)
          .get
          .label mustBe "employerContribution.checkYourAnswersLabel"
        helper(emptyUserAnswers)
          .employerContribution(taxYear, index, psubWithoutEmployerContribution)
          .get
          .answer mustBe "site.no"
      }
    }
  }

  "expensesEmployerPaid" when {
    "display the correct label, answer" in {
      helper(emptyUserAnswers)
        .expensesEmployerPaid(taxYear, index, psubWithEmployerContribution)
        .get
        .label mustBe "expensesEmployerPaid.checkYourAnswersLabel"
      helper(emptyUserAnswers)
        .expensesEmployerPaid(taxYear, index, psubWithEmployerContribution)
        .get
        .answer mustBe s"£${psubWithEmployerContribution.employerContributionAmount.get}"
    }
  }

  "yourAddress" when {
    "true" must {
      "display the correct label, answer and message args" in {
        val ua  = emptyUserAnswers.set(YourAddressPage, true).success.value
        val ua2 = ua.set(CitizensDetailsAddress, validAddress).success.value
        helper(ua2).yourAddress.get.label mustBe "yourAddress.checkYourAnswersLabel"
        helper(ua2).yourAddress.get.answer mustBe "site.yes"
        helper(ua2).yourAddress.get.labelArgs.head mustBe Address.asString(validAddress)
      }
    }

    "false" must {
      "display the correct label, answer, and message args" in {
        val ua  = emptyUserAnswers.set(YourAddressPage, false).success.value
        val ua2 = ua.set(CitizensDetailsAddress, validAddress).success.value
        helper(ua2).yourAddress.get.label mustBe "yourAddress.checkYourAnswersLabel"
        helper(ua2).yourAddress.get.answer mustBe "site.no"
        helper(ua2).yourAddress.get.labelArgs.head mustBe Address.asString(validAddress)
      }
    }

    "is empty" must {
      "return None" in {
        helper(emptyUserAnswers).yourAddress mustBe None
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

}
