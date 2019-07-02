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

@Singleton
class Navigator @Inject()() {

  private val routeMap: Page => UserAnswers => Call = {
    case WhichSubscriptionPage(year, index) => _ => SubscriptionAmountController.onPageLoad(NormalMode, year, index)
    case SubscriptionAmountPage(year, index) => _ => EmployerContributionController.onPageLoad(NormalMode, year, index)
    case EmployerContributionPage(year, index) => ua => employerContribution(ua, year, index)
    case CannotClaimEmployerContributionPage(_, _) => _ => SummarySubscriptionsController.onPageLoad()
    case DuplicateSubscriptionPage => _ => SummarySubscriptionsController.onPageLoad()
    case TaxYearSelectionPage => taxYearSelection
    case SummarySubscriptionsPage => ua => summarySubscriptions(ua)
    case YourEmployerPage => yourEmployer
    case YourAddressPage => yourAddress
    case UpdateYourEmployerPage => _ => YourAddressController.onPageLoad(NormalMode)
    case UpdateYourAddressPage => _ => CheckYourAnswersController.onPageLoad()
    case ExpensesEmployerPaidPage(year, index) => ua => expensesEmployerPaid(ua, year, index)
    case RemoveSubscriptionPage => _ => SummarySubscriptionsController.onPageLoad()
    case AmountsAlreadyInCodePage => ua => amountsAlreadyInCode(ua)
    case AmountsYouNeedToChangePage => _ => SummarySubscriptionsController.onPageLoad()
    case _ => _ => IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
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

  private def employerContribution(userAnswers: UserAnswers, year: String, index: Int): Call = userAnswers.get(EmployerContributionPage(year, index)) match {
    case Some(true) => ExpensesEmployerPaidController.onPageLoad(NormalMode, year, index)
    case Some(false) => SummarySubscriptionsController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def expensesEmployerPaid(userAnswers: UserAnswers, year: String, index: Int): Call = {
    (userAnswers.get(SubscriptionAmountPage(year, index)), userAnswers.get(ExpensesEmployerPaidPage(year, index))) match {
      case (Some(subscriptionAmount), Some(employerContribution)) =>
        if (employerContribution >= subscriptionAmount) CannotClaimEmployerContributionController.onPageLoad(year, index)
        else SummarySubscriptionsController.onPageLoad()
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


  private def taxYearSelection(userAnswers: UserAnswers): Call = {

    import models.NpsDataFormats.formats

    (userAnswers.get(NpsData), userAnswers.get(TaxYearSelectionPage)) match {
      case (Some(_), Some(_)) =>
        AmountsAlreadyInCodeController.onPageLoad(NormalMode)
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }


  private def summarySubscriptions(userAnswers: UserAnswers): Call = {

    import models.PSubsByYear.formats

    (userAnswers.get(TaxYearSelectionPage), userAnswers.get(SummarySubscriptionsPage)) match {
      case (Some(taxYears), Some(subscriptions)) =>
        val yearTotals: Seq[Int] = taxYears.map {
          taxYear =>
            if (subscriptions.keys.exists(_ == getTaxYear(taxYear)))
              subscriptions(getTaxYear(taxYear)).map {
                psub =>
                  psub.amount - psub.employerContributionAmount.filter(_ => psub.employerContributed).getOrElse(0)
              }.sum
            else
              0
        }

        if (yearTotals.exists(_ >= 2500))
          SelfAssessmentClaimController.onPageLoad()
        else if (subscriptions.forall(p => p._2.isEmpty))
          NoFurtherActionController.onPageLoad()
        else
          YourEmployerController.onPageLoad(NormalMode)

      case (Some(_), None) =>
        NoFurtherActionController.onPageLoad()
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def amountsAlreadyInCode(userAnswers: UserAnswers): Call = userAnswers.get(AmountsAlreadyInCodePage) match {
    case Some(true) => NoFurtherActionController.onPageLoad()
    case Some(false) => AmountsYouNeedToChangeController.onPageLoad(NormalMode)
    case _ => SessionExpiredController.onPageLoad()
  }
}
