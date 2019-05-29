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
import models._
import pages._
import org.scalatest.mockito.MockitoSugar
import services.TaiService
import TaxYearSelection._

class NavigatorSpec extends SpecBase with MockitoSugar {

  val navigator = new Navigator
  val mockTaiService: TaiService = mock[TaiService]

  "Navigator" when {

    "in Normal mode" must {

      "go to Index from a page that doesn't exist in the route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers(userAnswersId)) mustBe IndexController.onPageLoad()
      }

      "go from 'which subscription' to 'how much you paid'" in {
        navigator.nextPage(WhichSubscriptionPage, NormalMode, emptyUserAnswers)
          .mustBe(SubscriptionAmountController.onPageLoad(NormalMode))
      }

      "go from 'how much you paid' to 'did your employer pay anything'" in {
        navigator.nextPage(SubscriptionAmountPage, NormalMode, emptyUserAnswers)
          .mustBe(EmployerContributionController.onPageLoad(NormalMode))
      }

      "go from 'did your employer pay anything' to 'how much' when true" in {
        val answers = emptyUserAnswers.set(EmployerContributionPage, true).success.value

        navigator.nextPage(EmployerContributionPage, NormalMode, answers)
          .mustBe(ExpensesEmployerPaidController.onPageLoad(NormalMode))
      }

      "go from 'did your employer pay anything' to 'add another psub' when false" in {
        val answers = emptyUserAnswers.set(EmployerContributionPage, false).success.value

        navigator.nextPage(EmployerContributionPage, NormalMode, answers)
          .mustBe(AddAnotherSubscriptionController.onPageLoad(NormalMode))
      }

      "go to 'session expired' when no data for 'employer contribution page'" in {
        navigator.nextPage(EmployerContributionPage, NormalMode, emptyUserAnswers)
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

      "go from 'add another psub' to 'summary' when true" ignore {
        val answers = emptyUserAnswers.set(AddAnotherSubscriptionPage, true).success.value

        navigator.nextPage(AddAnotherSubscriptionPage, NormalMode, answers)
          .mustBe(???)
      }

      "go from 'add another psub' to 'claim amount' when false" in {
        val answers = emptyUserAnswers.set(AddAnotherSubscriptionPage, false).success.value

        navigator.nextPage(AddAnotherSubscriptionPage, NormalMode, answers)
          .mustBe(ClaimAmountController.onPageLoad())
      }

      "go to 'session expired' when no data for 'add another psub'" in {
        navigator.nextPage(AddAnotherSubscriptionPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'tax year selection' to 'which subscription' when all psubs are empty with 1 tax year" in {

        val answers = emptyUserAnswers.set(ProfessionalSubscriptions,
          Seq(ProfessionalSubscriptionAmount(None, 2019), ProfessionalSubscriptionAmount(None, 2018))).success.value
          .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value

        navigator.nextPage(TaxYearSelectionPage, NormalMode, answers)
          .mustBe(WhichSubscriptionController.onPageLoad(NormalMode))
      }

      "go from 'tax year selection' to 'which subscription' when all psubs gross amounts are 0 with 1 tax year" in {

        val answers = emptyUserAnswers.set(ProfessionalSubscriptions,
          Seq(ProfessionalSubscriptionAmount(Some(EmploymentExpense(0)), 2019), ProfessionalSubscriptionAmount(Some(EmploymentExpense(0)), 2018))).success.value
          .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value

        navigator.nextPage(TaxYearSelectionPage, NormalMode, answers)
          .mustBe(WhichSubscriptionController.onPageLoad(NormalMode))
      }

      "go from 'tax year selection' to 'which sunscription' when there is a mix of empty and 0 gross amounts with 1 tax year" in {

        val answers = emptyUserAnswers.set(ProfessionalSubscriptions,
          Seq(ProfessionalSubscriptionAmount(None, 2019), ProfessionalSubscriptionAmount(Some(EmploymentExpense(0)), 2018))).success.value
          .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value

        navigator.nextPage(TaxYearSelectionPage, NormalMode, answers)
          .mustBe(WhichSubscriptionController.onPageLoad(NormalMode))
      }

      "go from 'tax year selection' to 'Task list summary' for all other values with 1 tax year" ignore {

        val answers = emptyUserAnswers.set(ProfessionalSubscriptions,
          Seq(
            ProfessionalSubscriptionAmount(None, 2019),
            ProfessionalSubscriptionAmount(Some(EmploymentExpense(100)), 2018)
          )).success.value

        navigator.nextPage(TaxYearSelectionPage, NormalMode, answers)
          .mustBe(???)
      }

      "go from 'tax year selection' to 'Task list summary' when more than 1 tax year has been selected" ignore {

        val answers = emptyUserAnswers.set(TaxYearSelectionPage, Seq(CurrentYear, CurrentYearMinus1)).success.value

        navigator.nextPage(TaxYearSelectionPage, NormalMode, answers)
          .mustBe(???)
      }

      "go from 'tax year selection' to 'session expired' when no professional subscription is found" in {
        navigator.nextPage(TaxYearSelectionPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad())
      }

      "go from 'update employer' to 'is this your address'" in {
        navigator.nextPage(UpdateYourEmployerPage, NormalMode, emptyUserAnswers)
          .mustBe(YourAddressController.onPageLoad(NormalMode))
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
