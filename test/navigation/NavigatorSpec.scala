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

      "go from 'tax year selection' to 'AmountsAlreadyInCodeController' when there are amounts in the nps data for any of the selected tax years" in {
        navigator.nextPage(TaxYearSelectionPage, NormalMode, someUserAnswers)
          .mustBe(AmountsAlreadyInCodeController.onPageLoad(NormalMode))
      }

      "go from 'tax year selection' to 'Summary subscriptions' when there is zero for all nps data for all selected tax years" in {
        val ua = emptyUserAnswers
          .set(TaxYearSelectionPage, Seq(CurrentYear, CurrentYearMinus1)).success.value
          .set(NpsData, Map(
            getTaxYear(CurrentYear) -> 0,
            getTaxYear(CurrentYearMinus1) -> 0
          ))(NpsDataFormats.formats).success.value

        navigator.nextPage(TaxYearSelectionPage, NormalMode, ua)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'tax year selection' to 'session expired' when get nps data has failed" in {
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
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'remove subscription' to 'summary' when false" in {
        val answers = someUserAnswers.set(RemoveSubscriptionPage, false).success.value

        navigator.nextPage(RemoveSubscriptionPage, NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'remove subscription' to 'summary' when true" in {
        val answers = someUserAnswers.set(RemoveSubscriptionPage, true).success.value

        navigator.nextPage(RemoveSubscriptionPage, NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
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

      "go from 'update employer' to 'is this your address'" in {
        navigator.nextPage(UpdateYourEmployerPage, NormalMode, emptyUserAnswers)
          .mustBe(YourAddressController.onPageLoad(NormalMode))
      }

      "go from 'update address' to 'check your answers'" in {
        navigator.nextPage(UpdateYourAddressPage, NormalMode, emptyUserAnswers)
          .mustBe(CheckYourAnswersController.onPageLoad())
      }

      "go from 'cannot claim due to employer contribution' to 'subscriptions summary'" in {
        navigator.nextPage(CannotClaimEmployerContributionPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'expenses employer paid' to 'subscriptions summary' when subscription amount is less than the employer contribution" in {
        val answers = emptyUserAnswers
          .set(SubscriptionAmountPage(taxYear, index), 100).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 10).success.value

        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is equal to the employer contribution" in {
        val answers = emptyUserAnswers
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 10).success.value

        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, answers)
          .mustBe(CannotClaimEmployerContributionController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is more than the employer contribution" in {
        val answers = emptyUserAnswers
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 100).success.value

        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, answers)
          .mustBe(CannotClaimEmployerContributionController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'expenses employer paid' to 'session expired' when no valid data" in {
        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'summary' to 'your employer' when the psub amounts for a single year add up to < 2500 and current year selected" in {
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

      "go from 'summary' to 'your address' when the psub amounts for a single year add up to < 2500 and only previous years selected" in {
        val answers = emptyUserAnswers
          .set(TaxYearSelectionPage, Seq(CurrentYearMinus1)).success.value
          .set(SavePSubs(s"$taxYear"),
            Seq(
              PSub("Psub", 10, false, None),
              PSub("Psub2", 100, true, Some(50))
            )
          ).success.value

        navigator.nextPage(SummarySubscriptionsPage, NormalMode, answers)
          .mustBe(YourAddressController.onPageLoad(NormalMode))
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
          .mustBe(SelfAssessmentClaimController.onPageLoad(NormalMode))
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
          .mustBe(SelfAssessmentClaimController.onPageLoad(NormalMode))
      }

      "go from 'summary' to 'no further action' when no psubs are submitted" in {
        val answers = emptyUserAnswers
          .set(TaxYearSelectionPage, Seq(CurrentYear, CurrentYearMinus1)).success.value

        navigator.nextPage(SummarySubscriptionsPage, NormalMode, answers)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from 'summary' to 'no further action' when and empty list of psubs are submitted" in {
        val answers = emptyUserAnswers.set(SavePSubs(getTaxYear(CurrentYear).toString),Seq()).success.value
          .set(TaxYearSelectionPage, Seq(CurrentYear, CurrentYearMinus1)).success.value

        navigator.nextPage(SummarySubscriptionsPage, NormalMode, answers)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from AmountsAlreadyInCodePage to AmountsYouNeedToChangeController when answered false" in {
        val ua = someUserAnswers.set(AmountsAlreadyInCodePage, false).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, NormalMode, ua)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from AmountsAlreadyInCodePage to ReEnterAmountsController when answered true" in {
        val ua = someUserAnswers.set(AmountsAlreadyInCodePage, true).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, NormalMode, ua)
          .mustBe(ReEnterAmountsController.onPageLoad(NormalMode))
      }

      "go from AmountsAlreadyInCodePage to SessionExpiredController when no data" in {
        navigator.nextPage(AmountsAlreadyInCodePage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from ReEnterAmountsPage to ReEnterAmountsController when answered true" in {
        val ua = someUserAnswers.set(ReEnterAmountsPage, true).success.value

        navigator.nextPage(ReEnterAmountsPage, NormalMode, ua)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from ReEnterAmountsPage to ReEnterAmountsController when answered false" in {
        val ua = someUserAnswers.set(ReEnterAmountsPage, false).success.value

        navigator.nextPage(ReEnterAmountsPage, NormalMode, ua)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from ReEnterAmountsPage to SessionExpiredController when no data" in {
        navigator.nextPage(ReEnterAmountsPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'cannot claim duplicate subscriptions' to 'subscriptions summary'" in {
        navigator.nextPage(DuplicateSubscriptionPage, NormalMode, emptyUserAnswers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }
    }

    "in Check mode" must {

      "go from 'tax year selection' to 'SummarySubscriptionsController' when professional subscriptions are available" in {
        navigator.nextPage(TaxYearSelectionPage, CheckMode, someUserAnswers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'tax year selection' to 'session expired' when get professional subscriptions has failed" in {
        navigator.nextPage(TaxYearSelectionPage, CheckMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from AmountsAlreadyInCodePage to ReEnterAmountsController when answered true" in {
        val ua = someUserAnswers.set(AmountsAlreadyInCodePage, true).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, CheckMode, ua)
          .mustBe(ReEnterAmountsController.onPageLoad(CheckMode))
      }

      "go from AmountsAlreadyInCodePage to AmountsYouNeedToChangeController when answered false" in {
        val ua = someUserAnswers.set(AmountsAlreadyInCodePage, false).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, CheckMode, ua)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from AmountsAlreadyInCodePage to SessionExpiredController when no data" in {
        navigator.nextPage(AmountsAlreadyInCodePage, CheckMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from ReEnterAmountsPage to ReEnterAmountsController when answered true" in {
        val ua = someUserAnswers.set(ReEnterAmountsPage, true).success.value

        navigator.nextPage(ReEnterAmountsPage, CheckMode, ua)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from ReEnterAmountsPage to ReEnterAmountsController when answered false" in {
        val ua = someUserAnswers.set(ReEnterAmountsPage, false).success.value

        navigator.nextPage(ReEnterAmountsPage, CheckMode, ua)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from ReEnterAmountsPage to SessionExpiredController when no data" in {
        navigator.nextPage(ReEnterAmountsPage, CheckMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'which subscription' to 'how much you paid'" in {
        navigator.nextPage(WhichSubscriptionPage(taxYear, index), CheckMode, emptyUserAnswers)
          .mustBe(SubscriptionAmountController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'how much you paid' to 'did your employer pay anything'" in {
        navigator.nextPage(SubscriptionAmountPage(taxYear, index), CheckMode, emptyUserAnswers)
          .mustBe(EmployerContributionController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'did your employer pay anything' to 'how much your employer contributed' when true" in {
        val answers = emptyUserAnswers.set(EmployerContributionPage(taxYear, index), true).success.value

        navigator.nextPage(EmployerContributionPage(taxYear, index), CheckMode, answers)
          .mustBe(ExpensesEmployerPaidController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'did your employer pay anything' to 'SummarySubscriptions' when false" in {
        val answers = emptyUserAnswers.set(EmployerContributionPage(taxYear, index), false).success.value

        navigator.nextPage(EmployerContributionPage(taxYear, index), CheckMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'expenses employer paid' to 'SummarySubscriptions' when subscription amount is less than the employer contribution" in {
        val answers = emptyUserAnswers
          .set(SubscriptionAmountPage(taxYear, index), 100).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 10).success.value

        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), CheckMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is equal to the employer contribution" in {
        val answers = emptyUserAnswers
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 10).success.value

        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), CheckMode, answers)
          .mustBe(CannotClaimEmployerContributionController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is more than the employer contribution" in {
        val answers = emptyUserAnswers
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 100).success.value

        navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), CheckMode, answers)
          .mustBe(CannotClaimEmployerContributionController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'cannot claim due to employer contribution' to 'SummarySubscriptions'" in {
        navigator.nextPage(CannotClaimEmployerContributionPage(taxYear, index), CheckMode, someUserAnswers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'cannot claim duplicate subscriptions' to 'subscriptions summary'" in {
        navigator.nextPage(DuplicateSubscriptionPage, CheckMode, emptyUserAnswers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'remove subscription' to 'summary' when false" in {
        val answers = someUserAnswers.set(RemoveSubscriptionPage, false).success.value

        navigator.nextPage(RemoveSubscriptionPage, CheckMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'remove subscription' to 'summary' when true" in {
        val answers = someUserAnswers.set(RemoveSubscriptionPage, true).success.value

        navigator.nextPage(RemoveSubscriptionPage, CheckMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'summary' to 'CYA' when the psub amounts for a single year add up to < 2500" in {
        val answers = emptyUserAnswers
          .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value
          .set(SavePSubs(s"$taxYear"),
            Seq(
              PSub("Psub", 10, false, None),
              PSub("Psub2", 100, true, Some(50))
            )
          ).success.value

        navigator.nextPage(SummarySubscriptionsPage, CheckMode, answers)
          .mustBe(CheckYourAnswersController.onPageLoad())
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

        navigator.nextPage(SummarySubscriptionsPage, CheckMode, answers)
          .mustBe(SelfAssessmentClaimController.onPageLoad(CheckMode))
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

        navigator.nextPage(SummarySubscriptionsPage, CheckMode, answers)
          .mustBe(SelfAssessmentClaimController.onPageLoad(CheckMode))
      }

      "go from 'summary' to 'no further action' when no psubs are submitted" in {
        val answers = emptyUserAnswers
          .set(TaxYearSelectionPage, Seq(CurrentYear, CurrentYearMinus1)).success.value

        navigator.nextPage(SummarySubscriptionsPage, CheckMode, answers)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from 'summary' to 'no further action' when and empty list of psubs are submitted" in {
        val answers = emptyUserAnswers.set(SavePSubs(getTaxYear(CurrentYear).toString),Seq()).success.value
          .set(TaxYearSelectionPage, Seq(CurrentYear, CurrentYearMinus1)).success.value

        navigator.nextPage(SummarySubscriptionsPage, CheckMode, answers)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from 'is this your employer' to 'CYA' when true" in {
        val answers = emptyUserAnswers.set(YourEmployerPage, true).success.value

        navigator.nextPage(YourEmployerPage, CheckMode, answers)
          .mustBe(CheckYourAnswersController.onPageLoad())
      }

      "go from 'is this your employer' to 'update later page' when false" in {
        val answers = emptyUserAnswers.set(YourEmployerPage, false).success.value

        navigator.nextPage(YourEmployerPage, NormalMode, answers)
          .mustBe(UpdateYourEmployerInformationController.onPageLoad())
      }

      "go to 'session expired' when no data for 'is this your employer'" in {
        navigator.nextPage(YourEmployerPage, CheckMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'update employer' to 'is this your address'" in {
        navigator.nextPage(UpdateYourEmployerPage, CheckMode, emptyUserAnswers)
          .mustBe(CheckYourAnswersController.onPageLoad())
      }

      "go from 'is this your address' to 'check your answers' when true" in {
        val answers = emptyUserAnswers.set(YourAddressPage, true).success.value

        navigator.nextPage(YourAddressPage, CheckMode, answers)
          .mustBe(CheckYourAnswersController.onPageLoad())
      }

      "go from 'is this your address' to 'update later page' when false" in {
        val answers = emptyUserAnswers.set(YourAddressPage, false).success.value

        navigator.nextPage(YourAddressPage, CheckMode, answers)
          .mustBe(UpdateYourAddressController.onPageLoad())
      }

      "go to 'session expired' when no data for 'is this your address'" in {
        navigator.nextPage(YourAddressPage, CheckMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'update address' to 'check your answers'" in {
        navigator.nextPage(UpdateYourAddressPage, CheckMode, emptyUserAnswers)
          .mustBe(CheckYourAnswersController.onPageLoad())
      }

      "go to CheckYourAnswers from a page that doesn't exist in the edit route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers(userAnswersId)) mustBe CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
