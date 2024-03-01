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

import controllers.routes

import javax.inject.{Inject, Singleton}
import models.TaxYearSelection._
import models._
import pages._
import play.api.mvc.Call
import utils.PSubsUtil._

@Singleton
class Navigator @Inject()() {

  private val routeMap: Page => UserAnswers => Call = {
    case WhichSubscriptionPage(year, index) => ua => whichSubscription(NormalMode, ua, year, index)
    case PoliceKickoutQuestionPage(year, index) => ua => policeKickoutQuestion(NormalMode, ua, year, index)
    case PoliceKickoutPage => ua => policeKickout(NormalMode, ua)
    case SubscriptionAmountPage(year, index) => _ => routes.EmployerContributionController.onPageLoad(NormalMode, year, index)
    case CannotClaimEmployerContributionPage(_, _) => _ => routes.SummarySubscriptionsController.onPageLoad(NormalMode)
    case DuplicateSubscriptionPage => _ => routes.SummarySubscriptionsController.onPageLoad(NormalMode)
    case TaxYearSelectionPage => taxYearSelection
    case SummarySubscriptionsPage => ua => summarySubscriptions(ua)
    case DuplicateClaimForOtherYearsPage(year, index) => ua => duplicateClaimForOtherYears(ua, year, index)
    case DuplicateClaimYearSelectionPage => _ => routes.SummarySubscriptionsController.onPageLoad(NormalMode)
    case RemoveSubscriptionPage => _ => routes.SummarySubscriptionsController.onPageLoad(NormalMode)
    case AmountsAlreadyInCodePage => ua => amountsAlreadyInCode(ua)
    case ReEnterAmountsPage => ua => reEnterAmounts(ua)
    case EmployerContributionPage(year, index) => ua => employerContribution(ua, year, index)
    case ExpensesEmployerPaidPage(year, index) => ua => expensesEmployerPaid(ua, year, index)
    case YourAddressPage => _ => routes.CheckYourAnswersController.onPageLoad
    case UpdateYourAddressPage => _ => routes.CheckYourAnswersController.onPageLoad
    case CheckYourAnswersPage => checkYourAnswers
    case YourEmployerPage => yourEmployer
    case UpdateYourEmployerPage => _ => routes.HowYouWillGetYourExpensesController.onPageLoad()
    case HowYouWillGetYourExpensesPage => _ => routes.SubmissionController.submission
    case Submission => submission
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case TaxYearSelectionPage => changeTaxYearSelection
    case AmountsAlreadyInCodePage => ua => changeAmountsAlreadyInCode(ua)
    case ReEnterAmountsPage => ua => changeReEnterAmounts(ua)
    case DuplicateSubscriptionPage => _ => routes.SummarySubscriptionsController.onPageLoad(CheckMode)
    case CannotClaimEmployerContributionPage(_, _) => _ => routes.SummarySubscriptionsController.onPageLoad(CheckMode)
    case WhichSubscriptionPage(year, index) => ua => whichSubscription(CheckMode, ua, year, index)
    case PoliceKickoutQuestionPage(year, index) => ua => policeKickoutQuestion(CheckMode, ua, year, index)
    case PoliceKickoutPage => ua => policeKickout(CheckMode, ua)
    case SubscriptionAmountPage(year, index) => _ => routes.EmployerContributionController.onPageLoad(CheckMode, year, index)
    case SummarySubscriptionsPage => ua => changeSummarySubscriptions(ua)
    case YourEmployerPage => changeYourEmployer
    case UpdateYourEmployerPage => _ => routes.CheckYourAnswersController.onPageLoad
    case UpdateYourAddressPage => _ => routes.CheckYourAnswersController.onPageLoad
    case RemoveSubscriptionPage => _ => routes.SummarySubscriptionsController.onPageLoad(CheckMode)
    case EmployerContributionPage(year, index) => changeEmployerContribution(_, year, index)
    case ExpensesEmployerPaidPage(year, index) => changeExpensesEmployerPaid(_, year, index)
    case _ => _ => routes.CheckYourAnswersController.onPageLoad
  }

  def firstPage(): Call = {
    routes.TaxYearSelectionController.onPageLoad(NormalMode)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      routeMap(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private def employerContribution(
                                    userAnswers: UserAnswers,
                                    year: String,
                                    index: Int): Call = {

    (
      userAnswers.get(EmployerContributionPage(year, index)),
      userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats),
      userAnswers.get(ProfessionalBodies)
    ) match {
      case (Some(true), _, _) =>
        routes.ExpensesEmployerPaidController.onPageLoad(NormalMode, year, index)
      case (Some(false), Some(psubsByYear), Some(professionalBodies)) =>
        if (createDuplicateCheckbox(psubsByYear, professionalBodies, year, index).checkboxOption.isEmpty) {
          routes.SummarySubscriptionsController.onPageLoad(NormalMode)
        } else {
          routes.DuplicateClaimForOtherYearsController.onPageLoad(NormalMode, year, index)
        }
      case _ =>
        routes.SessionExpiredController.onPageLoad
    }
  }

  private def changeEmployerContribution(
                                          userAnswers: UserAnswers,
                                          year: String,
                                          index: Int): Call = {

    userAnswers.get(EmployerContributionPage(year, index)) match {
      case Some(true) =>
        routes.ExpensesEmployerPaidController.onPageLoad(CheckMode, year, index)
      case Some(false) =>
        routes.SummarySubscriptionsController.onPageLoad(CheckMode)
      case _ =>
        routes.SessionExpiredController.onPageLoad
    }
  }

  private def expensesEmployerPaid(
                                    userAnswers: UserAnswers,
                                    year: String,
                                    index: Int): Call = {
    (
      userAnswers.get(SubscriptionAmountPage(year, index)),
      userAnswers.get(ExpensesEmployerPaidPage(year, index)),
      userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats),
      userAnswers.get(ProfessionalBodies)) match {
      case (Some(subscriptionAmount), Some(expensesEmployerPaid), Some(psubsByYear), Some(professionalBodies)) =>
        if (expensesEmployerPaid >= subscriptionAmount) {
          routes.CannotClaimEmployerContributionController.onPageLoad(NormalMode, year, index)
        } else if (createDuplicateCheckbox(psubsByYear, professionalBodies, year, index).checkboxOption.nonEmpty) {
          routes.DuplicateClaimForOtherYearsController.onPageLoad(NormalMode, year, index)
        } else {
          routes.SummarySubscriptionsController.onPageLoad(NormalMode)
        }
      case _ => routes.SessionExpiredController.onPageLoad
    }
  }

  private def changeExpensesEmployerPaid(
                                          userAnswers: UserAnswers,
                                          year: String,
                                          index: Int): Call = {

    (userAnswers.get(ExpensesEmployerPaidPage(year, index)), userAnswers.get(SubscriptionAmountPage(year, index))) match {
      case (Some(contrib), Some(subAmount)) if contrib >= subAmount => routes.CannotClaimEmployerContributionController.onPageLoad(CheckMode, year, index)
      case _ => routes.SummarySubscriptionsController.onPageLoad(CheckMode)
    }
  }

  private def duplicateClaimForOtherYears(userAnswers: UserAnswers, year: String, index: Int): Call = {
    userAnswers.get(DuplicateClaimForOtherYearsPage(year, index)) match {
      case Some(true) => routes.DuplicateClaimYearSelectionController.onPageLoad(NormalMode, year, index)
      case Some(false) => routes.SummarySubscriptionsController.onPageLoad(NormalMode)
      case _ => routes.SessionExpiredController.onPageLoad
    }
  }

  private def yourEmployer(userAnswers: UserAnswers): Call = userAnswers.get(YourEmployerPage) match {
    case Some(true) => routes.HowYouWillGetYourExpensesController.onPageLoad()
    case Some(false) => routes.UpdateYourEmployerInformationController.onPageLoad()
    case _ => routes.SessionExpiredController.onPageLoad
  }

  private def changeYourEmployer(userAnswers: UserAnswers): Call = userAnswers.get(YourEmployerPage) match {
    case Some(true) => routes.CheckYourAnswersController.onPageLoad
    case Some(false) => routes.UpdateYourEmployerInformationController.onPageLoad()
    case _ => routes.SessionExpiredController.onPageLoad
  }

  private def taxYearSelection(userAnswers: UserAnswers): Call = {
    (userAnswers.get(NpsData)(NpsDataFormats.npsDataFormatsFormats), userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)) match {
      case (Some(npsData), Some(psubsByYear)) =>
        if (psubsByYear.forall(year => npsData.getOrElse(year._1, 0) == 0)) {
          routes.SummarySubscriptionsController.onPageLoad(NormalMode)
        } else {
          routes.AmountsAlreadyInCodeController.onPageLoad(NormalMode)
        }
      case _ =>
        routes.SessionExpiredController.onPageLoad
    }
  }

  private def whichSubscription(mode: Mode, userAnswers: UserAnswers, year: String, index: Int): Call = {
    userAnswers.get(WhichSubscriptionPage(year, index)) match {
      case Some(utils.PSubsUtil.policeFederationOfEnglandAndWales)  =>
        routes.PoliceKickoutQuestionController.onPageLoad(mode, year, index)
      case Some(_) => routes.SubscriptionAmountController.onPageLoad(mode, year, index)
      case _ => routes.SessionExpiredController.onPageLoad
    }
  }

  private def policeKickoutQuestion(mode: Mode, userAnswers: UserAnswers, year: String, index: Int): Call = {
    userAnswers.get(PoliceKickoutQuestionPage(year, index)) match {
      case Some(true) =>
        routes.PoliceKickoutController.onPageLoad(mode, year, index)
      case Some(false) =>
        routes.SubscriptionAmountController.onPageLoad(mode, year, index)
      case _ => routes.SessionExpiredController.onPageLoad
    }
  }

  private def policeKickout(mode: Mode, userAnswers: UserAnswers): Call = {
    userAnswers.get(MergedJourneyFlag) match {
      case Some(false) => routes.SummarySubscriptionsController.onPageLoad(mode)
      case _ => routes.SessionExpiredController.onPageLoad
    }
  }

  private def changeTaxYearSelection(userAnswers: UserAnswers): Call = {
    (userAnswers.get(NpsData)(NpsDataFormats.npsDataFormatsFormats), userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)) match {
      case (Some(_), Some(_)) =>
        routes.SummarySubscriptionsController.onPageLoad(CheckMode)
      case _ =>
        routes.SessionExpiredController.onPageLoad
    }
  }

  private def summarySubscriptions(userAnswers: UserAnswers): Call = {
    userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) match {
      case Some(psubsByYear) =>
        val taxYears = psubsByYear.keys.map(getTaxYearPeriod).toSeq

        if (claimAmountMinusDeductionsAllYears(taxYears, psubsByYear).exists(_ >= 2500))
          routes.SelfAssessmentClaimController.onPageLoad(NormalMode)
        else
          routes.YourAddressController.onPageLoad(NormalMode)
      case _ =>
        routes.SessionExpiredController.onPageLoad
    }
  }

  private def changeSummarySubscriptions(userAnswers: UserAnswers): Call = {
    userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) match {
      case Some(psubsByYear) =>
        val taxYears = psubsByYear.keys.map(getTaxYearPeriod).toSeq

        if (claimAmountMinusDeductionsAllYears(taxYears, psubsByYear).exists(_ >= 2500))
          routes.SelfAssessmentClaimController.onPageLoad(CheckMode)
        else
          routes.CheckYourAnswersController.onPageLoad
      case _ =>
        routes.SessionExpiredController.onPageLoad
    }
  }

  private def amountsAlreadyInCode(userAnswers: UserAnswers): Call = userAnswers.get(AmountsAlreadyInCodePage) match {
    case Some(true) => routes.ReEnterAmountsController.onPageLoad(NormalMode)
    case Some(false) => routes.NoFurtherActionController.onPageLoad()
    case _ => routes.SessionExpiredController.onPageLoad
  }

  private def changeAmountsAlreadyInCode(userAnswers: UserAnswers): Call = userAnswers.get(AmountsAlreadyInCodePage) match {
    case Some(true) => routes.ReEnterAmountsController.onPageLoad(CheckMode)
    case Some(false) => routes.NoFurtherActionController.onPageLoad()
    case _ => routes.SessionExpiredController.onPageLoad
  }

  private def reEnterAmounts(userAnswers: UserAnswers): Call = userAnswers.get(ReEnterAmountsPage) match {
    case Some(true) => routes.SummarySubscriptionsController.onPageLoad(NormalMode)
    case Some(false) => routes.NoFurtherActionController.onPageLoad()
    case _ => routes.SessionExpiredController.onPageLoad
  }

  private def changeReEnterAmounts(userAnswers: UserAnswers): Call = userAnswers.get(ReEnterAmountsPage) match {
    case Some(true) => routes.SummarySubscriptionsController.onPageLoad(CheckMode)
    case Some(false) => routes.NoFurtherActionController.onPageLoad()
    case _ => routes.SessionExpiredController.onPageLoad
  }

  private def checkYourAnswers(userAnswers: UserAnswers): Call = {
    userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).map(_.filter(_._2.nonEmpty).keys.toSeq) match {
      case Some(years) =>
        years match {
          case years if years.contains(getTaxYear(CurrentYear)) => routes.YourEmployerController.onPageLoad(NormalMode)
          case _ => routes.HowYouWillGetYourExpensesController.onPageLoad()
        }
      case _ => routes.SessionExpiredController.onPageLoad
    }
  }

  private def submission(userAnswers: UserAnswers): Call = userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).map {
    subscriptions =>
      val filteredEmptySubscriptions: Seq[Int] = subscriptions.filter(_._2.nonEmpty).keys.toSeq

      filteredEmptySubscriptions match {
        case years if years.contains(getTaxYear(CurrentYear)) && years.length == 1 =>
          routes.ConfirmationCurrentController.onPageLoad()
        case years if !years.contains(getTaxYear(CurrentYear)) =>
          routes.ConfirmationPreviousController.onPageLoad()
        case _ =>
          routes.ConfirmationCurrentPreviousController.onPageLoad()
      }
  }.getOrElse(routes.SessionExpiredController.onPageLoad)

}
