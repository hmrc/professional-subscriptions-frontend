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

    "in Normal mode" must {

      "go to Index from a page that doesn't exist in the route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers(userAnswersId)) mustBe IndexController.onPageLoad()
      }

      "go from 'tax year selection' to 'task list summary' when professional subscriptions are available" in {
        val answers = emptyUserAnswers
          .set(NpsData, Map(taxYear -> Seq.empty)).success.value
          .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value

        navigator.nextPage(TaxYearSelectionPage, NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad())
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

      "go from 'claim amount' to 'is this your employer' when current year" in {
        val answers = emptyUserAnswers.set(TaxYearSelectionPage, Seq(TaxYearSelection.CurrentYear)).success.value

        navigator.nextPage(ClaimAmountPage(taxYear, index), NormalMode, answers)
          .mustBe(YourEmployerController.onPageLoad(NormalMode))
      }

      "go from 'claim amount' to 'is this your employer' when current year & previous years" in {
        val answers = emptyUserAnswers.set(
          TaxYearSelectionPage,
          Seq(
            TaxYearSelection.CurrentYear,
            TaxYearSelection.CurrentYearMinus1
          )).success.value

        navigator.nextPage(ClaimAmountPage(taxYear, index), NormalMode, answers)
          .mustBe(YourEmployerController.onPageLoad(NormalMode))
      }

      "go from 'claim amount' to 'is this your employer' when previous years only" in {
        val answers = emptyUserAnswers.set(TaxYearSelectionPage, Seq(TaxYearSelection.CurrentYearMinus1)).success.value

        navigator.nextPage(ClaimAmountPage(taxYear, index), NormalMode, answers)
          .mustBe(YourAddressController.onPageLoad(NormalMode))
      }

      "go from 'summary page' to 'which subscription'" ignore {
        navigator.nextPage(SummarySubscriptionsPage, NormalMode, someUserAnswers)
          .mustBe(WhichSubscriptionController.onPageLoad(NormalMode, taxYear, index))
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


    }

    "in Check mode" must {

      "go to CheckYourAnswers from a page that doesn't exist in the edit route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers(userAnswersId)) mustBe CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
