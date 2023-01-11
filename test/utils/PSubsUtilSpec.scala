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
import models.TaxYearSelection._
import models.{PSub, PSubsByYear, UserAnswers}
import pages.{EmployerContributionPage, SavePSubs, SubscriptionAmountPage, SummarySubscriptionsPage, WhichSubscriptionPage}
import utils.PSubsUtil._

class PSubsUtilSpec extends SpecBase {

  val ua1 = emptyUserAnswers
    .set(WhichSubscriptionPage(taxYear, index),"sub 1").success.value
    .set(SubscriptionAmountPage(taxYear, index),10).success.value
    .set(EmployerContributionPage(taxYear, index),false).success.value
    .set(WhichSubscriptionPage(taxYear, index + 1),"sub 2").success.value
    .set(SubscriptionAmountPage(taxYear, index + 1),10).success.value
    .set(EmployerContributionPage(taxYear, index + 1),false).success.value
    .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index),"sub 3").success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index),10).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index),false).success.value

  private val psubs1 = Seq(PSub("psub1", 100, false, None), PSub("psub2", 250, true, Some(50)))
  private val psubs2 = Seq(PSub("psub3", 100, true, Some(10)))
  private val duplicatePsub = Seq(PSub("psub1", 100, false, None), PSub("psub1", 100, false, None))
  private val emptyPsubs = Seq.empty
  private val psubsByYear = Map(getTaxYear(CurrentYear) -> psubs1, getTaxYear(CurrentYearMinus1) -> psubs2)
  private val psubsByYearWithEmptyYear = Map(getTaxYear(CurrentYear) -> psubs1, getTaxYear(CurrentYearMinus1) -> emptyPsubs)
  private val psubsAllEmpty = Map(getTaxYear(CurrentYear) -> emptyPsubs)
  private val psubToDuplicate = PSub("TestPsubName", 100, false, None)


  "psub util" must {
    "remove psub" in {
      val ua2: UserAnswers = ua1.set(SavePSubs(taxYear), remove(ua1, taxYear, index)).success.value

      ua1.data.value("subscriptions")(taxYear).as[Seq[PSub]].length mustBe 2
      ua2.data.value("subscriptions")(taxYear).as[Seq[PSub]].length mustBe 1
      ua2.data.value("subscriptions")(getTaxYear(CurrentYearMinus1).toString).as[Seq[PSub]].isEmpty mustBe false
    }

    "claimAmountMinusDeductions" must {
      "return a total from a seq of psubs" in {
        claimAmountMinusDeductions(psubs1) mustEqual 300
        claimAmountMinusDeductions(psubs2) mustEqual 90
      }
    }

    "claimAmountMinusDeductionsAllYears" must {
      "return a seq of ints for the total claim for all psubs per year" in {
        claimAmountMinusDeductionsAllYears(Seq(CurrentYear, CurrentYearMinus1), psubsByYear) mustEqual Seq(300, 90)
        claimAmountMinusDeductionsAllYears(Seq(CurrentYear), psubsByYearWithEmptyYear) mustEqual Seq(300)
        claimAmountMinusDeductionsAllYears(Seq(CurrentYear), psubsAllEmpty) mustEqual Seq(0)
      }
    }

    "isDuplicate" must {
      "return true when subscription is a duplicate" in {
        val answers = emptyUserAnswers.set(SavePSubs(taxYear), duplicatePsub).success.value
        isDuplicate(answers, taxYear) mustBe true
      }

      "return false when subscription is not a duplicate" in {
        val answers = emptyUserAnswers.set(SavePSubs(taxYear), psubs1).success.value
        isDuplicate(answers, taxYear) mustBe false
      }
    }

    "isDuplicateInSeqPsubs" must {
      "return true when subscription is a duplicate" in {
        isDuplicateInSeqPsubs(duplicatePsub) mustBe true
      }

      "return false when subscription is not a duplicate" in {
        isDuplicateInSeqPsubs(psubs1) mustBe false
      }
    }

    "duplicatePsubsUserAnswers" must {
      "duplicate a psub to multiple tax years" in {
        val taxYearsToUpate = Seq(CurrentYearMinus1, CurrentYearMinus2, CurrentYearMinus3)
        val userAnswersToUpdate = emptyUserAnswers
        val allPsubs = Map(
          getTaxYear(CurrentYearMinus1) -> Seq.empty,
          getTaxYear(CurrentYearMinus2) -> Seq.empty,
          getTaxYear(CurrentYearMinus3) -> Seq.empty
        )
        val result = duplicatePsubsUserAnswers(
          taxYearsToUpate,
          userAnswersToUpdate,
          allPsubs,
          psubToDuplicate
        )

        result.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) mustBe Some(Map(
          getTaxYear(CurrentYearMinus1) -> Seq(psubToDuplicate),
          getTaxYear(CurrentYearMinus2) -> Seq(psubToDuplicate),
          getTaxYear(CurrentYearMinus3) -> Seq(psubToDuplicate)
        ))
      }

      "duplicate a psub to multiple tax years without duplicating an exisiting psubs that is the same" in {
        val taxYearsToUpate = Seq(CurrentYearMinus1, CurrentYearMinus2, CurrentYearMinus3)
        val allPsubs = Map(
          getTaxYear(CurrentYearMinus1) -> Seq(psubToDuplicate),
          getTaxYear(CurrentYearMinus2) -> Seq.empty,
          getTaxYear(CurrentYearMinus3) -> Seq.empty
        )
        val userAnswersToUpdate = emptyUserAnswers.set(SummarySubscriptionsPage, allPsubs)(PSubsByYear.pSubsByYearFormats).success.value
        val result = duplicatePsubsUserAnswers(
          taxYearsToUpate,
          userAnswersToUpdate,
          allPsubs,
          psubToDuplicate
        )

        result.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) mustBe Some(Map(
          getTaxYear(CurrentYearMinus1) -> Seq(psubToDuplicate),
          getTaxYear(CurrentYearMinus2) -> Seq(psubToDuplicate),
          getTaxYear(CurrentYearMinus3) -> Seq(psubToDuplicate)
        ))
      }
    }
  }
}
