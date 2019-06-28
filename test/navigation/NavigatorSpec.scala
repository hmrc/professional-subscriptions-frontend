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

package navigation

import base.SpecBase
import controllers.routes._
import models.TaxYearSelection._
import models._
import org.scalatest.mockito.MockitoSugar
import pages._

class NavigatorSpec extends SpecBase with MockitoSugar {

  "Navigator" when {

    "first page" must {
      "go to tax year selection" in {
        navigator.firstPage() mustBe TaxYearSelectionController.onPageLoad(NormalMode)
      }
    }

    "in Normal mode" must {

      "go to Index from a page that doesn't exist in the route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers(userAnswersId)) mustBe IndexController.onPageLoad()
      }

      "go from 'tax year selection' to 'is your data correct' when professional subscriptions are available" in {
        val answers = emptyUserAnswers
          .set(NpsData, Map(getTaxYear(CurrentYear) -> Seq.empty)).success.value
          .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value

        navigator.nextPage(TaxYearSelectionPage, NormalMode, answers)
          .mustBe(AmountsAlreadyInCodeController.onPageLoad(NormalMode))
      }

      "go from 'tax year selection' to 'session expired' when get professional subscriptions has failed" in {
        navigator.nextPage(TaxYearSelectionPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'which subscription' to 'how much you paid'" in {
        navigator.nextPage(WhichSubscriptionPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(SubscriptionAmountController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'how much you paid' to 'did your employer pay anything'" in {
        navigator.nextPage(SubscriptionAmountPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(EmployerContributionController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'did your employer pay anything' to 'how much' when true" in {
        val answers = emptyUserAnswers.set(EmployerContributionPage(taxYear, index), true).success.value

        navigator.nextPage(EmployerContributionPage(taxYear, index), NormalMode, answers)
          .mustBe(ExpensesEmployerPaidController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'did your employer pay anything' to 'summary' when false" in {
        val answers = emptyUserAnswers.set(EmployerContributionPage(taxYear, index), false).success.value

        navigator.nextPage(EmployerContributionPage(taxYear, index), NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad())
      }

      "go from 'remove subscription' to 'summary' when false" in {
        val answers = someUserAnswers.set(RemoveSubscriptionPage, false).success.value

        navigator.nextPage(RemoveSubscriptionPage, NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad())
      }

      "go from 'remove subscription' to 'summary' when true" in {
        val answers = someUserAnswers.set(RemoveSubscriptionPage, true).success.value

        navigator.nextPage(RemoveSubscriptionPage, NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad())
      }

      "go to 'session expired' when no data for 'employer contribution page'" in {
        navigator.nextPage(EmployerContributionPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }


      "go from 'is this your employer' to 'is this your address' when true" in {
        val answers = emptyUserAnswers.set(YourEmployerPage, true).success.value

        navigator.nextPage(YourEmployerPage, NormalMode, answers)
          .mustBe(YourAddressController.onPageLoad(NormalMode))
      }

      "go from 'is this your employer' to 'update later page' when false" in {
        val answers = emptyUserAnswers.set(YourEmployerPage, false).success.value

        navigator.nextPage(YourEmployerPage, NormalMode, answers)
          .mustBe(UpdateYourEmployerInformationController.onPageLoad())
      }

      "go to 'session expired' when no data for 'is this your employer'" in {
        navigator.nextPage(YourEmployerPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'is this your address' to 'check your answers' when true" in {
        val answers = emptyUserAnswers.set(YourAddressPage, true).success.value

        navigator.nextPage(YourAddressPage, NormalMode, answers)
          .mustBe(CheckYourAnswersController.onPageLoad())
      }

      "go from 'is this your address' to 'update later page' when false" in {
        val answers = emptyUserAnswers.set(YourAddressPage, false).success.value

        navigator.nextPage(YourAddressPage, NormalMode, answers)
          .mustBe(UpdateYourAddressController.onPageLoad())
      }

      "go to 'session expired' when no data for 'is this your address'" in {
        navigator.nextPage(YourAddressPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'tax year selection' to 'session expired' when no professional subscription is found" in {
        navigator.nextPage(TaxYearSelectionPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'update employer' to 'is this your address'" in {
        navigator.nextPage(UpdateYourEmployerPage, NormalMode, emptyUserAnswers)
          .mustBe(YourAddressController.onPageLoad(NormalMode))
      }

      "go from 'update address' to 'check your answers'" in {
        navigator.nextPage(UpdateYourAddressPage, NormalMode, emptyUserAnswers)
          .mustBe(CheckYourAnswersController.onPageLoad())
      }

      "go from 'summary page' to 'YourEmployerController'" in {
        navigator.nextPage(SummarySubscriptionsPage, NormalMode, someUserAnswers)
          .mustBe(YourEmployerController.onPageLoad(NormalMode))
      }

      "go from 'cannot claim due to employer contribution' to 'subscriptions summary'" in {
        navigator.nextPage(CannotClaimEmployerContributionPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(SummarySubscriptionsController.onPageLoad())
      }

      "go from 'expenses employer paid' to 'subscriptions summary' when subscription amount is less than the employer contribution" in {
        val answers = emptyUserAnswers
          .set(SubscriptionAmountPage(taxYear, index), 100).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 10).success.value

        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad())
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is equal to the employer contribution" in {
        val answers = emptyUserAnswers
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 10).success.value

        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, answers)
          .mustBe(CannotClaimEmployerContributionController.onPageLoad(taxYear, index))
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is more than the employer contribution" in {
        val answers = emptyUserAnswers
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 100).success.value

        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, answers)
          .mustBe(CannotClaimEmployerContributionController.onPageLoad(taxYear, index))
      }

      "go from 'expenses employer paid' to 'session expired' when no valid data" in {
        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'summary' to 'your employer' when the psub amounts for a single year add up to < 2500" in {
        val answers = emptyUserAnswers
          .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value
          .set(SavePSubs(s"$taxYear"),
            Seq(
              PSub("Psub", 10, false, None),
              PSub("Psub2", 100, true, Some(50))
            )
          ).success.value

        navigator.nextPage(SummarySubscriptionsPage, NormalMode, answers)
          .mustBe(YourEmployerController.onPageLoad(NormalMode))
      }

      "go from 'summary' to 'SA claim' when the psub amounts for a single year add up to > 2500" in {
        val answers = emptyUserAnswers
          .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value
          .set(SavePSubs(s"$taxYear"),
            Seq(
              PSub("Psub", 2000, false, None),
              PSub("Psub2", 1000, true, Some(300))
            )
          ).success.value

        navigator.nextPage(SummarySubscriptionsPage, NormalMode, answers)
          .mustBe(SelfAssessmentClaimController.onPageLoad())
      }

      "go from 'summary' to 'SA claim' when the psub amounts for a single year add up to < 2500 and empty seq returned" in {
        val answers = emptyUserAnswers
          .set(TaxYearSelectionPage, Seq(CurrentYear, CurrentYearMinus1)).success.value
          .set(SavePSubs(
            getTaxYear(CurrentYear).toString),
            Seq(
              PSub("Psub", 2000, false, None),
              PSub("Psub2", 1000, true, Some(300))
            )
          ).success.value

        navigator.nextPage(SummarySubscriptionsPage, NormalMode, answers)
          .mustBe(SelfAssessmentClaimController.onPageLoad())
      }

      "go from AmountsAlreadyInCodePage to AmountsYouNeedToChangeController when answered false" in {
        val ua = someUserAnswers.set(AmountsAlreadyInCodePage, false).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, NormalMode, ua)
          .mustBe(AmountsYouNeedToChangeController.onPageLoad(NormalMode))
      }

      "go from AmountsAlreadyInCodePage to AmountsYouNeedToChangeController when answered true" ignore {
        val ua = someUserAnswers.set(AmountsAlreadyInCodePage, true).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, NormalMode, ua)
          .mustBe(AmountsYouNeedToChangeController.onPageLoad(NormalMode))
      }

      "go from AmountsAlreadyInCodePage to SessionExpiredController when no data" in {
        navigator.nextPage(AmountsAlreadyInCodePage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from AmountsYouNeedToChangePage to SummarySubscriptionsController" in {
        val ua = someUserAnswers.set(AmountsYouNeedToChangePage, Seq(CurrentYear)).success.value

        navigator.nextPage(AmountsYouNeedToChangePage, NormalMode, ua)
          .mustBe(SummarySubscriptionsController.onPageLoad())
      }
    }

    "in Check mode" must {

      "go to CheckYourAnswers from a page that doesn't exist in the edit route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers(userAnswersId)) mustBe CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
