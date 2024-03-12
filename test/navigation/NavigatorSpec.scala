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

package navigation

import base.SpecBase
import controllers.routes
import controllers.routes._
import models.TaxYearSelection._
import models._
import org.scalatestplus.mockito.MockitoSugar
import pages._
import utils.PSubsUtil.policeFederationOfEnglandAndWales

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
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers(userAnswersId)) mustBe IndexController.start
      }

      "go from 'tax year selection' to 'AmountsAlreadyInCodeController' when there are amounts in the nps data for any of the selected tax years" in {
        navigator.nextPage(TaxYearSelectionPage, NormalMode, userAnswersCurrentAndPrevious)
          .mustBe(AmountsAlreadyInCodeController.onPageLoad(NormalMode))
      }

      "go from 'tax year selection' to 'Summary subscriptions' when there is zero for all nps data for all selected tax years" in {
        val ua = userAnswersCurrentAndPrevious
          .set(NpsData, Map(
            getTaxYear(CurrentYear) -> 0,
            getTaxYear(CurrentYearMinus1) -> 0
          ))(NpsDataFormats.npsDataFormatsFormats).success.value

        navigator.nextPage(TaxYearSelectionPage, NormalMode, ua)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'tax year selection' to 'session expired' when get nps data has failed" in {
        navigator.nextPage(TaxYearSelectionPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad)
      }

      "go from 'which subscription' to 'how much you paid'" in {
        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(taxYear, index), "Arable Research Institute Association").success.value
        navigator.nextPage(WhichSubscriptionPage(taxYear, index), NormalMode, ua)
          .mustBe(SubscriptionAmountController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'which subscription' to 'do you work for the Metropolitan or West Yorkshire Police Force?'" in {
        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(taxYear, index), policeFederationOfEnglandAndWales).success.value
        navigator.nextPage(WhichSubscriptionPage(taxYear, index), NormalMode, ua)
          .mustBe(PoliceKickoutQuestionController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'do you work for the Metropolitan or West Yorkshire Police Force?' to 'you cannot claim tax relief for this professional subscription'" in {
        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(taxYear, index), policeFederationOfEnglandAndWales).success.value
          .set(PoliceKickoutQuestionPage(taxYear, index), true).success.value
        navigator.nextPage(PoliceKickoutQuestionPage(taxYear, index), NormalMode, ua)
          .mustBe(PoliceKickoutController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'do you work for the Metropolitan or West Yorkshire Police Force?' to 'how much you paid'" in {
        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(taxYear, index), policeFederationOfEnglandAndWales).success.value
          .set(PoliceKickoutQuestionPage(taxYear, index), false).success.value
        navigator.nextPage(PoliceKickoutQuestionPage(taxYear, index), NormalMode, ua)
          .mustBe(SubscriptionAmountController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'how much you paid' to 'did your employer pay anything'" in {
        navigator.nextPage(SubscriptionAmountPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(EmployerContributionController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'did your employer pay anything' to 'how much' when true" in {
        val userAnswers = userAnswersCurrentAndPrevious
          .set(EmployerContributionPage(taxYear, index), true).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(EmployerContributionPage(taxYear, index), NormalMode, userAnswers)

        result mustBe ExpensesEmployerPaidController.onPageLoad(NormalMode, taxYear, index)
      }

      "go from 'did your employer pay anything' to 'duplicate claim' when false and the checkbox is not empty" in {
        val userAnswers = userAnswersCurrentAndPrevious
          .set(EmployerContributionPage(taxYear, index), false).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(EmployerContributionPage(taxYear, index), NormalMode, userAnswers)

        result mustBe DuplicateClaimForOtherYearsController.onPageLoad(NormalMode, taxYear, index)
      }

      "go from 'did your employer pay anything' to 'summary subscription' when false and the checkbox is empty" in {
        val userAnswers = userAnswersCurrent
          .set(EmployerContributionPage(taxYear, index), false).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(EmployerContributionPage(taxYear, index), NormalMode, userAnswers)

        result mustBe SummarySubscriptionsController.onPageLoad(NormalMode)
      }

      "go from 'did your employer pay anything' to 'session expired' if there is no psubs" in {
        val userAnswers = emptyUserAnswers
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(EmployerContributionPage(taxYear, index), NormalMode, userAnswers)

        result mustBe SessionExpiredController.onPageLoad
      }

      "go from 'did your employer pay anything' to 'session expired' if EmployerContribution is not answered" in {

        val userAnswers = emptyUserAnswers
          .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(EmployerContributionPage(taxYear, index), NormalMode, userAnswers)

        result mustBe SessionExpiredController.onPageLoad
      }

      "go from 'remove subscription' to 'summary' when false" in {
        val answers = userAnswersCurrentAndPrevious.set(RemoveSubscriptionPage, false).success.value

        navigator.nextPage(RemoveSubscriptionPage, NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'remove subscription' to 'summary' when true" in {
        val answers = userAnswersCurrentAndPrevious.set(RemoveSubscriptionPage, true).success.value

        navigator.nextPage(RemoveSubscriptionPage, NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'cannot claim due to employer contribution' to 'subscriptions summary'" in {
        navigator.nextPage(CannotClaimEmployerContributionPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'expenses employer paid' to 'duplicate claim' when checkbox is not empty" in {

        val userAnswers = userAnswersCurrentAndPrevious
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, userAnswers)

        result mustBe DuplicateClaimForOtherYearsController.onPageLoad(NormalMode, taxYear, index)
      }

      "go from 'expenses employer paid' to 'summary' when subscription amount is less than the employer contribution and there is no checkboxes" in {
        val userAnswers = userAnswersCurrent
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, userAnswers)

        result mustBe SummarySubscriptionsController.onPageLoad(NormalMode)
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is equal to the employer contribution" in {
        val userAnswers = userAnswersCurrent
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 10).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value


        val result = navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, userAnswers)

        result mustBe CannotClaimEmployerContributionController.onPageLoad(NormalMode, taxYear, index)
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is more than the employer contribution" in {
        val userAnswers = userAnswersCurrent
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 100).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, userAnswers)
        result mustBe CannotClaimEmployerContributionController.onPageLoad(NormalMode, taxYear, index)
      }

      "go from 'expenses employer paid' to 'session expired' when no psub data" in {
        val userAnswers = emptyUserAnswers
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), NormalMode, userAnswers)
        result mustBe SessionExpiredController.onPageLoad
      }

      "go from 'duplicate claim' to 'duplicate claim year selection' when true" in {
        val answers = emptyUserAnswers.set(DuplicateClaimForOtherYearsPage(taxYear, index), true).success.value

        navigator.nextPage(DuplicateClaimForOtherYearsPage(taxYear, index), NormalMode, answers)
          .mustBe(DuplicateClaimYearSelectionController.onPageLoad(NormalMode, taxYear, index))
      }

      "go from 'duplicate claim' to 'summary subscriptions' when true" in {
        val answers = emptyUserAnswers.set(DuplicateClaimForOtherYearsPage(taxYear, index), false).success.value

        navigator.nextPage(DuplicateClaimForOtherYearsPage(taxYear, index), NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'duplicate claim' to 'session expired' when no valid data" in {
        navigator.nextPage(DuplicateClaimForOtherYearsPage(taxYear, index), NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad)
      }

      "go from 'duplicate claim year selection' to 'summary subscriptions'" in {
        val answers = emptyUserAnswers.set(DuplicateClaimYearSelectionPage, Seq(CurrentYearMinus1)).success.value

        navigator.nextPage(DuplicateClaimYearSelectionPage, NormalMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'summary' to 'CheckYourAnswers' when the psub amounts for a single year add up to < 2500 and current year selected" in {
        val answers = userAnswersCurrent
          .set(SavePSubs(s"$taxYear"),
            Seq(
              PSub("Psub", 10, false, None),
              PSub("Psub2", 100, true, Some(50))
            )
          ).success.value

        navigator.nextPage(SummarySubscriptionsPage, NormalMode, answers)
          .mustBe(YourAddressController.onPageLoad(NormalMode))
      }

      "go from 'summary' to 'your address' when the psub amounts for a single year add up to < 2500 and only previous years selected" in {
        navigator.nextPage(SummarySubscriptionsPage, NormalMode, userAnswersPrevious)
          .mustBe(YourAddressController.onPageLoad(NormalMode))
      }

      "go from 'summary' to 'SA claim' when the psub amounts for a single year add up to > 2500" in {
        val answers = userAnswersCurrent
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
        val answers = userAnswersCurrentAndPrevious
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

      "go from AmountsAlreadyInCodePage to AmountsYouNeedToChangeController when answered false" in {
        val ua = userAnswersCurrentAndPrevious.set(AmountsAlreadyInCodePage, false).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, NormalMode, ua)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from AmountsAlreadyInCodePage to ReEnterAmountsController when answered true" in {
        val ua = userAnswersCurrentAndPrevious.set(AmountsAlreadyInCodePage, true).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, NormalMode, ua)
          .mustBe(ReEnterAmountsController.onPageLoad(NormalMode))
      }

      "go from AmountsAlreadyInCodePage to SessionExpiredController when no data" in {
        navigator.nextPage(AmountsAlreadyInCodePage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad)
      }

      "go from ReEnterAmountsPage to ReEnterAmountsController when answered true" in {
        val ua = userAnswersCurrentAndPrevious.set(ReEnterAmountsPage, true).success.value

        navigator.nextPage(ReEnterAmountsPage, NormalMode, ua)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from ReEnterAmountsPage to ReEnterAmountsController when answered false" in {
        val ua = userAnswersCurrentAndPrevious.set(ReEnterAmountsPage, false).success.value

        navigator.nextPage(ReEnterAmountsPage, NormalMode, ua)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from ReEnterAmountsPage to SessionExpiredController when no data" in {
        navigator.nextPage(ReEnterAmountsPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad)
      }

      "go from 'cannot claim duplicate subscriptions' to 'subscriptions summary'" in {
        navigator.nextPage(DuplicateSubscriptionPage, NormalMode, emptyUserAnswers)
          .mustBe(SummarySubscriptionsController.onPageLoad(NormalMode))
      }

      "go from 'Submission' to 'confirmation merged journey' if user has the merged journey flag" in {
        navigator.nextPage(Submission, NormalMode, userAnswersCurrent.set(MergedJourneyFlag, true).success.value)
          .mustBe(routes.ConfirmationMergedJourneyController.onPageLoad())

      }

      "go from 'Submission' to 'confirmation current'" in {
        navigator.nextPage(Submission, NormalMode, userAnswersCurrent)
          .mustBe(ConfirmationCurrentController.onPageLoad())

      }

      "go from 'Submission' to 'confirmation current and previous'" in {
        navigator.nextPage(Submission, NormalMode, userAnswersCurrentAndPreviousYears)
          .mustBe(ConfirmationCurrentPreviousController.onPageLoad())
      }

      "go from 'Submission' to 'confirmation previous'" in {
        navigator.nextPage(Submission, NormalMode, userAnswersPrevious)
          .mustBe(ConfirmationPreviousController.onPageLoad())
      }

      "go from 'Submission' to 'session expired'" in {
        navigator.nextPage(Submission, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad)
      }

      "go from 'HowYouWillGetYourExpenses' to 'Submission'" in {
        navigator.nextPage(HowYouWillGetYourExpensesPage, NormalMode, emptyUserAnswers)
          .mustBe(SubmissionController.submission)
      }

      "go from 'update employer' to 'How you will get your expenses'" in {
        navigator.nextPage(UpdateYourEmployerPage, NormalMode, emptyUserAnswers)
          .mustBe(HowYouWillGetYourExpensesController.onPageLoad())
      }

      "go to 'session expired' when no data for 'is this your employer'" in {
        navigator.nextPage(YourEmployerPage, NormalMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad)
      }

      "go from 'is this your employer' to 'How you will get your expenses' when true" in {
        val answers = emptyUserAnswers.set(YourEmployerPage, true).success.value

        navigator.nextPage(YourEmployerPage, NormalMode, answers)
          .mustBe(HowYouWillGetYourExpensesController.onPageLoad())
      }

      "go from 'is this your employer' to 'update later page' when false" in {
        val answers = emptyUserAnswers.set(YourEmployerPage, false).success.value

        navigator.nextPage(YourEmployerPage, NormalMode, answers)
          .mustBe(UpdateYourEmployerInformationController.onPageLoad())
      }

      "Go from 'CheckYourAnswers' to IsYourEmployerShown when CurrentYear" in {
        navigator.nextPage(CheckYourAnswersPage, NormalMode, userAnswersCurrent)
          .mustBe(YourEmployerController.onPageLoad(NormalMode))
      }

      "Go from 'CheckYourAnswers' to IsYourEmployerShown when CurrentYear & PreviousYear" in {
        navigator.nextPage(CheckYourAnswersPage, NormalMode, userAnswersCurrentAndPrevious)
          .mustBe(YourEmployerController.onPageLoad(NormalMode))
      }

      "Go from 'CheckYourAnswers' to 'How you will get your expenses' when Previous Year" in {
        navigator.nextPage(CheckYourAnswersPage, NormalMode, userAnswersPrevious)
          .mustBe(HowYouWillGetYourExpensesController.onPageLoad())
      }
    }

    "in Check mode" must {

      "go from 'tax year selection' to 'SummarySubscriptionsController' when professional subscriptions are available" in {
        navigator.nextPage(TaxYearSelectionPage, CheckMode, userAnswersCurrentAndPrevious)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'tax year selection' to 'session expired' when get professional subscriptions has failed" in {
        navigator.nextPage(TaxYearSelectionPage, CheckMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad)
      }

      "go from AmountsAlreadyInCodePage to ReEnterAmountsController when answered true" in {
        val ua = userAnswersCurrentAndPrevious.set(AmountsAlreadyInCodePage, true).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, CheckMode, ua)
          .mustBe(ReEnterAmountsController.onPageLoad(CheckMode))
      }

      "go from AmountsAlreadyInCodePage to AmountsYouNeedToChangeController when answered false" in {
        val ua = userAnswersCurrentAndPrevious.set(AmountsAlreadyInCodePage, false).success.value

        navigator.nextPage(AmountsAlreadyInCodePage, CheckMode, ua)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from AmountsAlreadyInCodePage to SessionExpiredController when no data" in {
        navigator.nextPage(AmountsAlreadyInCodePage, CheckMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad)
      }

      "go from ReEnterAmountsPage to ReEnterAmountsController when answered true" in {
        val ua = userAnswersCurrentAndPrevious.set(ReEnterAmountsPage, true).success.value

        navigator.nextPage(ReEnterAmountsPage, CheckMode, ua)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from ReEnterAmountsPage to ReEnterAmountsController when answered false" in {
        val ua = userAnswersCurrentAndPrevious.set(ReEnterAmountsPage, false).success.value

        navigator.nextPage(ReEnterAmountsPage, CheckMode, ua)
          .mustBe(NoFurtherActionController.onPageLoad())
      }

      "go from ReEnterAmountsPage to SessionExpiredController when no data" in {
        navigator.nextPage(ReEnterAmountsPage, CheckMode, emptyUserAnswers)
          .mustBe(SessionExpiredController.onPageLoad)
      }

      "go from 'which subscription' to 'how much you paid'" in {
        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(taxYear, index), "Arable Research Institute Association").success.value
        navigator.nextPage(WhichSubscriptionPage(taxYear, index), CheckMode, ua)
          .mustBe(SubscriptionAmountController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'which subscription' to 'do you work for the Metropolitan or West Yorkshire Police Force?'" in {
        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(taxYear, index), policeFederationOfEnglandAndWales).success.value
        navigator.nextPage(WhichSubscriptionPage(taxYear, index), CheckMode, ua)
          .mustBe(PoliceKickoutQuestionController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'do you work for the Metropolitan or West Yorkshire Police Force?' to 'you cannot claim tax relief for this professional subscription'" in {
        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(taxYear, index), policeFederationOfEnglandAndWales).success.value
          .set(PoliceKickoutQuestionPage(taxYear, index), true).success.value
        navigator.nextPage(PoliceKickoutQuestionPage(taxYear, index), CheckMode, ua)
          .mustBe(PoliceKickoutController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'do you work for the Metropolitan or West Yorkshire Police Force?' to 'how much you paid'" in {
        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(taxYear, index), policeFederationOfEnglandAndWales).success.value
          .set(PoliceKickoutQuestionPage(taxYear, index), false).success.value
        navigator.nextPage(PoliceKickoutQuestionPage(taxYear, index), CheckMode, ua)
          .mustBe(SubscriptionAmountController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'how much you paid' to 'did your employer pay anything'" in {
        navigator.nextPage(SubscriptionAmountPage(taxYear, index), CheckMode, emptyUserAnswers)
          .mustBe(EmployerContributionController.onPageLoad(CheckMode, taxYear, index))
      }

      "go from 'did your employer pay anything' to 'how much your employer contributed' when true" in {
        val userAnswers = userAnswersCurrentAndPrevious
          .set(EmployerContributionPage(taxYear, index), true).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(EmployerContributionPage(taxYear, index), CheckMode, userAnswers)

        result mustBe ExpensesEmployerPaidController.onPageLoad(CheckMode, taxYear, index)
      }

      "go from 'did your employer pay anything' to 'SummarySubscriptions' when false" in {
        val userAnswers = userAnswersCurrentAndPrevious
          .set(EmployerContributionPage(taxYear, index), false).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(EmployerContributionPage(taxYear, index), CheckMode, userAnswers)

        result mustBe SummarySubscriptionsController.onPageLoad(CheckMode)
      }

      "go from 'did your employer pay anything' to 'session expired' if EmployerContribution is not answered" in {
        val userAnswers = emptyUserAnswers
          .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(EmployerContributionPage(taxYear, index), CheckMode, userAnswers)

        result mustBe SessionExpiredController.onPageLoad
      }

      "go from 'expenses employer paid' to 'SummarySubscriptions' when subscription amount is less than the employer contribution" in {
        val userAnswers = userAnswersCurrent
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), CheckMode, userAnswers)

        result mustBe SummarySubscriptionsController.onPageLoad(CheckMode)
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is equal to the employer contribution" in {
        val userAnswers = userAnswersCurrent
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 10).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), CheckMode, userAnswers)

        result mustBe CannotClaimEmployerContributionController.onPageLoad(CheckMode, taxYear, index)
      }

      "go from 'expenses employer paid' to 'cannot claim due to employer contribution' when subscription amount is more than the employer contribution" in {
        val userAnswers = userAnswersCurrent
          .set(SubscriptionAmountPage(taxYear, index), 10).success.value
          .set(ExpensesEmployerPaidPage(taxYear, index), 100).success.value
          .set(ProfessionalBodies, Seq(ProfessionalBody("Arable Research Institute Association", Nil, None))).success.value

        val result = navigator.nextPage(ExpensesEmployerPaidPage(taxYear, index), CheckMode, userAnswers)

        result mustBe CannotClaimEmployerContributionController.onPageLoad(CheckMode, taxYear, index)
      }

      "go from 'cannot claim due to employer contribution' to 'SummarySubscriptions'" in {
        navigator.nextPage(CannotClaimEmployerContributionPage(taxYear, index), CheckMode, userAnswersCurrentAndPrevious)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'cannot claim duplicate subscriptions' to 'subscriptions summary'" in {
        navigator.nextPage(DuplicateSubscriptionPage, CheckMode, emptyUserAnswers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'remove subscription' to 'summary' when false" in {
        val answers = userAnswersCurrentAndPrevious.set(RemoveSubscriptionPage, false).success.value

        navigator.nextPage(RemoveSubscriptionPage, CheckMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'remove subscription' to 'summary' when true" in {
        val answers = userAnswersCurrentAndPrevious.set(RemoveSubscriptionPage, true).success.value

        navigator.nextPage(RemoveSubscriptionPage, CheckMode, answers)
          .mustBe(SummarySubscriptionsController.onPageLoad(CheckMode))
      }

      "go from 'summary' to 'CYA' when the psub amounts for a single year add up to < 2500" in {
        val answers = userAnswersCurrent
          .set(SavePSubs(s"$taxYear"),
            Seq(
              PSub("Psub", 10, false, None),
              PSub("Psub2", 100, true, Some(50))
            )
          ).success.value

        navigator.nextPage(SummarySubscriptionsPage, CheckMode, answers)
          .mustBe(CheckYourAnswersController.onPageLoad)
      }

      "go from 'summary' to 'SA claim' when the psub amounts for a single year add up to > 2500" in {
        val answers = userAnswersCurrent
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
        val answers = userAnswersCurrentAndPrevious
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

      "go to CheckYourAnswers from a page that doesn't exist in the edit route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers(userAnswersId)) mustBe CheckYourAnswersController.onPageLoad
      }
    }
  }
}
