/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.routes.{HowYouWillGetYourExpensesController, _}
import javax.inject.{Inject, Singleton}
import models.TaxYearSelection._
import models._
import pages._
import play.api.mvc.Call
import utils.PSubsUtil._

@Singleton
class Navigator @Inject()() {

  private val routeMap: Page => UserAnswers => Call = {
    case WhichSubscriptionPage(year, index) => _ => SubscriptionAmountController.onPageLoad(NormalMode, year, index)
    case SubscriptionAmountPage(year, index) => _ => EmployerContributionController.onPageLoad(NormalMode, year, index)
    case CannotClaimEmployerContributionPage(_, _) => _ => SummarySubscriptionsController.onPageLoad(NormalMode)
    case DuplicateSubscriptionPage => _ => SummarySubscriptionsController.onPageLoad(NormalMode)
    case TaxYearSelectionPage => taxYearSelection
    case SummarySubscriptionsPage => ua => summarySubscriptions(ua)
    case DuplicateClaimForOtherYearsPage(year, index) => ua => duplicateClaimForOtherYears(ua, year, index)
    case DuplicateClaimYearSelectionPage => _ => SummarySubscriptionsController.onPageLoad(NormalMode)
    case RemoveSubscriptionPage => _ => SummarySubscriptionsController.onPageLoad(NormalMode)
    case AmountsAlreadyInCodePage => ua => amountsAlreadyInCode(ua)
    case ReEnterAmountsPage => ua => reEnterAmounts(ua)
    case EmployerContributionPage(year, index) => ua => employerContribution(ua, year, index)
    case ExpensesEmployerPaidPage(year, index) => ua => expensesEmployerPaid(ua, year, index)
    case YourAddressPage => _ => CheckYourAnswersController.onPageLoad()
    case UpdateYourAddressPage => _ => CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersPage => checkYourAnswers
    case YourEmployerPage => yourEmployer
    case UpdateYourEmployerPage => _ => HowYouWillGetYourExpensesController.onPageLoad
    case HowYouWillGetYourExpensesPage => _ => SubmissionController.submission()
    case Submission => submission
    case _ => _ => IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case TaxYearSelectionPage => changeTaxYearSelection
    case AmountsAlreadyInCodePage => ua => changeAmountsAlreadyInCode(ua)
    case ReEnterAmountsPage => ua => changeReEnterAmounts(ua)
    case DuplicateSubscriptionPage => _ => SummarySubscriptionsController.onPageLoad(CheckMode)
    case CannotClaimEmployerContributionPage(_, _) => _ => SummarySubscriptionsController.onPageLoad(CheckMode)
    case WhichSubscriptionPage(year, index) => _ => SubscriptionAmountController.onPageLoad(CheckMode, year, index)
    case SubscriptionAmountPage(year, index) => _ => EmployerContributionController.onPageLoad(CheckMode, year, index)
    case SummarySubscriptionsPage => ua => changeSummarySubscriptions(ua)
    case YourEmployerPage => changeYourEmployer
    case UpdateYourEmployerPage => _ => CheckYourAnswersController.onPageLoad()
    case UpdateYourAddressPage => _ => CheckYourAnswersController.onPageLoad()
    case RemoveSubscriptionPage => _ => SummarySubscriptionsController.onPageLoad(CheckMode)
    case EmployerContributionPage(year, index) => changeEmployerContribution(_, year, index)
    case ExpensesEmployerPaidPage(year, index) => changeExpensesEmployerPaid(_, year, index)
    case _ => _ => CheckYourAnswersController.onPageLoad()
  }

  def firstPage(): Call = {
    TaxYearSelectionController.onPageLoad(NormalMode)
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
        ExpensesEmployerPaidController.onPageLoad(NormalMode, year, index)
      case (Some(false), Some(psubsByYear), Some(professionalBodies)) =>
        if (createDuplicateCheckbox(psubsByYear, professionalBodies, year, index).checkboxOption.isEmpty) {
          SummarySubscriptionsController.onPageLoad(NormalMode)
        } else {
          DuplicateClaimForOtherYearsController.onPageLoad(NormalMode, year, index)
        }
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def changeEmployerContribution(
                                          userAnswers: UserAnswers,
                                          year: String,
                                          index: Int): Call = {

    userAnswers.get(EmployerContributionPage(year, index)) match {
      case Some(true) =>
        ExpensesEmployerPaidController.onPageLoad(CheckMode, year, index)
      case Some(false) =>
        SummarySubscriptionsController.onPageLoad(CheckMode)
      case _ =>
        SessionExpiredController.onPageLoad()
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
          CannotClaimEmployerContributionController.onPageLoad(NormalMode, year, index)
        } else if (createDuplicateCheckbox(psubsByYear, professionalBodies, year, index).checkboxOption.nonEmpty) {
          DuplicateClaimForOtherYearsController.onPageLoad(NormalMode, year, index)
        } else {
          SummarySubscriptionsController.onPageLoad(NormalMode)
        }
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  private def changeExpensesEmployerPaid(
                                          userAnswers: UserAnswers,
                                          year: String,
                                          index: Int): Call = {

    (userAnswers.get(ExpensesEmployerPaidPage(year, index)), userAnswers.get(SubscriptionAmountPage(year, index))) match {
      case (Some(contrib), Some(subAmount)) if contrib >= subAmount => CannotClaimEmployerContributionController.onPageLoad(CheckMode, year, index)
      case _ => SummarySubscriptionsController.onPageLoad(CheckMode)
    }
  }

  private def duplicateClaimForOtherYears(userAnswers: UserAnswers, year: String, index: Int): Call = {
    userAnswers.get(DuplicateClaimForOtherYearsPage(year, index)) match {
      case Some(true) => DuplicateClaimYearSelectionController.onPageLoad(NormalMode, year, index)
      case Some(false) => SummarySubscriptionsController.onPageLoad(NormalMode)
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  private def yourEmployer(userAnswers: UserAnswers): Call = userAnswers.get(YourEmployerPage) match {
    case Some(true) => HowYouWillGetYourExpensesController.onPageLoad
    case Some(false) => UpdateYourEmployerInformationController.onPageLoad
    case _ => SessionExpiredController.onPageLoad
  }

  private def changeYourEmployer(userAnswers: UserAnswers): Call = userAnswers.get(YourEmployerPage) match {
    case Some(true) => CheckYourAnswersController.onPageLoad()
    case Some(false) => UpdateYourEmployerInformationController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def taxYearSelection(userAnswers: UserAnswers): Call = {
    (userAnswers.get(NpsData)(NpsDataFormats.npsDataFormatsFormats), userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)) match {
      case (Some(npsData), Some(psubsByYear)) =>
        if (psubsByYear.forall(year => npsData.getOrElse(year._1, 0) == 0)) {
          SummarySubscriptionsController.onPageLoad(NormalMode)
        } else {
          AmountsAlreadyInCodeController.onPageLoad(NormalMode)
        }
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def changeTaxYearSelection(userAnswers: UserAnswers): Call = {
    (userAnswers.get(NpsData)(NpsDataFormats.npsDataFormatsFormats), userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)) match {
      case (Some(_), Some(_)) =>
        SummarySubscriptionsController.onPageLoad(CheckMode)
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def summarySubscriptions(userAnswers: UserAnswers): Call = {
    userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) match {
      case Some(psubsByYear) =>
        val taxYears = psubsByYear.keys.map(getTaxYearPeriod).toSeq

        if (claimAmountMinusDeductionsAllYears(taxYears, psubsByYear).exists(_ >= 2500))
          SelfAssessmentClaimController.onPageLoad(NormalMode)
        else
          YourAddressController.onPageLoad(NormalMode)
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def changeSummarySubscriptions(userAnswers: UserAnswers): Call = {
    userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) match {
      case Some(psubsByYear) =>
        val taxYears = psubsByYear.keys.map(getTaxYearPeriod).toSeq

        if (claimAmountMinusDeductionsAllYears(taxYears, psubsByYear).exists(_ >= 2500))
          SelfAssessmentClaimController.onPageLoad(CheckMode)
        else
          CheckYourAnswersController.onPageLoad()
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def amountsAlreadyInCode(userAnswers: UserAnswers): Call = userAnswers.get(AmountsAlreadyInCodePage) match {
    case Some(true) => ReEnterAmountsController.onPageLoad(NormalMode)
    case Some(false) => NoFurtherActionController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def changeAmountsAlreadyInCode(userAnswers: UserAnswers): Call = userAnswers.get(AmountsAlreadyInCodePage) match {
    case Some(true) => ReEnterAmountsController.onPageLoad(CheckMode)
    case Some(false) => NoFurtherActionController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def reEnterAmounts(userAnswers: UserAnswers): Call = userAnswers.get(ReEnterAmountsPage) match {
    case Some(true) => SummarySubscriptionsController.onPageLoad(NormalMode)
    case Some(false) => NoFurtherActionController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def changeReEnterAmounts(userAnswers: UserAnswers): Call = userAnswers.get(ReEnterAmountsPage) match {
    case Some(true) => SummarySubscriptionsController.onPageLoad(CheckMode)
    case Some(false) => NoFurtherActionController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def checkYourAnswers(userAnswers: UserAnswers): Call = {
    userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).map(_.filter(_._2.nonEmpty).keys.toSeq) match {
      case Some(years) =>
        years match {
          case years if years.contains(getTaxYear(CurrentYear)) => YourEmployerController.onPageLoad(NormalMode)
          case _ => HowYouWillGetYourExpensesController.onPageLoad()
        }
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  private def submission(userAnswers: UserAnswers): Call = userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).map {
    subscriptions =>
      val filteredEmptySubscriptions: Seq[Int] = subscriptions.filter(_._2.nonEmpty).keys.toSeq

      filteredEmptySubscriptions match {
        case years if years.contains(getTaxYear(CurrentYear)) && years.length == 1 =>
          ConfirmationCurrentController.onPageLoad()
        case years if !years.contains(getTaxYear(CurrentYear)) =>
          ConfirmationPreviousController.onPageLoad()
        case _ =>
          ConfirmationCurrentPreviousController.onPageLoad()
      }
  }.getOrElse(SessionExpiredController.onPageLoad())

}
