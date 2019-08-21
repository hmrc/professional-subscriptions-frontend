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

import controllers.routes._
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
    case YourEmployerPage => yourEmployer
    case YourAddressPage => yourAddress
    case UpdateYourEmployerPage => _ => YourAddressController.onPageLoad(NormalMode)
    case UpdateYourAddressPage => _ => CheckYourAnswersController.onPageLoad()
    case DuplicateClaimForOtherYearsPage(year, index) => ua => duplicateClaimForOtherYears(ua, year, index)
    case DuplicateClaimYearSelectionPage => _ => SummarySubscriptionsController.onPageLoad(NormalMode)
    case RemoveSubscriptionPage => _ => SummarySubscriptionsController.onPageLoad(NormalMode)
    case AmountsAlreadyInCodePage => ua => amountsAlreadyInCode(ua)
    case ReEnterAmountsPage => ua => reEnterAmounts(ua)
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
    case YourAddressPage => changeYourAddress
    case UpdateYourEmployerPage => _ => CheckYourAnswersController.onPageLoad()
    case UpdateYourAddressPage => _ => CheckYourAnswersController.onPageLoad()
    case RemoveSubscriptionPage => _ => SummarySubscriptionsController.onPageLoad(CheckMode)
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

  def employerContributionNextPage(
                                    userAnswers: UserAnswers,
                                    year: String,
                                    index: Int,
                                    professionalBodies: Seq[ProfessionalBody],
                                    mode: Mode): Call = {

    (mode, userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.formats)) match {
      case (NormalMode, Some(psubsByYear)) =>
        employerContributionNormalMode(userAnswers, psubsByYear, professionalBodies, year, index)
      case (CheckMode, _) =>
        employerContributionCheckMode(userAnswers, year, index)
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  def employerContributionNormalMode(
                                      userAnswers: UserAnswers,
                                      psubsByYear: Map[Int, Seq[PSub]],
                                      professionalBodies: Seq[ProfessionalBody],
                                      year: String,
                                      index: Int): Call = {

    userAnswers.get(EmployerContributionPage(year, index)) match {
      case Some(true) =>
        ExpensesEmployerPaidController.onPageLoad(NormalMode, year, index)
      case Some(false) =>
        if (createDuplicateCheckbox(psubsByYear, professionalBodies, year, index).checkboxOption.isEmpty) {
          SummarySubscriptionsController.onPageLoad(NormalMode)
        } else {
          DuplicateClaimForOtherYearsController.onPageLoad(NormalMode, year, index)
        }
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  def employerContributionCheckMode(
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

  def expensesEmployerPaidNextPage(
                                    userAnswers: UserAnswers,
                                    year: String,
                                    index: Int,
                                    professionalBodies: Seq[ProfessionalBody],
                                    mode: Mode
                                  ): Call = {
    (
      mode,
      userAnswers.get(SubscriptionAmountPage(year, index)),
      userAnswers.get(ExpensesEmployerPaidPage(year, index)),
      userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.formats)) match {

      case (NormalMode, Some(subscriptionAmount), Some(employerContribution), Some(psubsByYear)) =>
        expensesEmployerPaidNormalMode(subscriptionAmount, employerContribution, psubsByYear, professionalBodies, year, index)
      case (CheckMode, Some(subscriptionAmount), Some(employerContribution), _) =>
        expensesEmployerPaidCheckMode(subscriptionAmount, employerContribution, year, index)
      case _ => SessionExpiredController.onPageLoad()

    }
  }

  def expensesEmployerPaidNormalMode(
                                      subscriptionAmount: Int,
                                      employerContribution: Int,
                                      psubsByYear: Map[Int, Seq[PSub]],
                                      professionalBodies: Seq[ProfessionalBody],
                                      year: String,
                                      index: Int): Call = {

    if (employerContribution >= subscriptionAmount) {
      CannotClaimEmployerContributionController.onPageLoad(NormalMode, year, index)
    } else if (createDuplicateCheckbox(psubsByYear, professionalBodies, year, index).checkboxOption.nonEmpty) {
      DuplicateClaimForOtherYearsController.onPageLoad(NormalMode, year, index)
    } else {
      SummarySubscriptionsController.onPageLoad(NormalMode)
    }
  }

  def expensesEmployerPaidCheckMode(
                                     subscriptionAmount: Int,
                                     employerContribution: Int,
                                     year: String,
                                     index: Int): Call = {

    if (employerContribution >= subscriptionAmount) {
      CannotClaimEmployerContributionController.onPageLoad(CheckMode, year, index)
    } else {
      SummarySubscriptionsController.onPageLoad(CheckMode)
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
    case Some(true) => YourAddressController.onPageLoad(NormalMode)
    case Some(false) => UpdateYourEmployerInformationController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def yourAddress(userAnswers: UserAnswers): Call = userAnswers.get(YourAddressPage) match {
    case Some(true) => CheckYourAnswersController.onPageLoad()
    case Some(false) => UpdateYourAddressController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def changeYourEmployer(userAnswers: UserAnswers): Call = userAnswers.get(YourEmployerPage) match {
    case Some(true) => CheckYourAnswersController.onPageLoad()
    case Some(false) => UpdateYourEmployerInformationController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def changeYourAddress(userAnswers: UserAnswers): Call = userAnswers.get(YourAddressPage) match {
    case Some(true) => CheckYourAnswersController.onPageLoad()
    case Some(false) => UpdateYourAddressController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def taxYearSelection(userAnswers: UserAnswers): Call = {
    (userAnswers.get(NpsData)(NpsDataFormats.formats), userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.formats)) match {
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
    (userAnswers.get(NpsData)(NpsDataFormats.formats), userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.formats)) match {
      case (Some(_), Some(_)) =>
        SummarySubscriptionsController.onPageLoad(CheckMode)
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def summarySubscriptions(userAnswers: UserAnswers): Call = {
    userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.formats) match {
      case Some(psubsByYear) =>
        val taxYears = psubsByYear.keys.map(getTaxYearPeriod).toSeq

        if (claimAmountMinusDeductionsAllYears(taxYears, psubsByYear).exists(_ >= 2500))
          SelfAssessmentClaimController.onPageLoad(NormalMode)
        else if (psubsByYear.forall(p => p._2.isEmpty))
          NoFurtherActionController.onPageLoad()
        else if (taxYears.contains(CurrentYear))
          YourEmployerController.onPageLoad(NormalMode)
        else
          YourAddressController.onPageLoad(NormalMode)
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def changeSummarySubscriptions(userAnswers: UserAnswers): Call = {
    userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.formats) match {
      case Some(psubsByYear) =>
        val taxYears = psubsByYear.keys.map(getTaxYearPeriod).toSeq

        if (claimAmountMinusDeductionsAllYears(taxYears, psubsByYear).exists(_ >= 2500))
          SelfAssessmentClaimController.onPageLoad(CheckMode)
        else if (psubsByYear.forall(p => p._2.isEmpty))
          NoFurtherActionController.onPageLoad()
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
}
