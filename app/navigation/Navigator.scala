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

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes._
import pages._
import models._

@Singleton
class Navigator @Inject()() {

  private val routeMap: Page => UserAnswers => Call = {
    case WhichSubscriptionPage => _ => SubscriptionAmountController.onPageLoad(NormalMode)
    case SubscriptionAmountPage => _ => EmployerContributionController.onPageLoad(NormalMode)
    case EmployerContributionPage => employerContribution
    case TaxYearSelectionPage => taxYearSelection
    case YourEmployerPage => yourEmployer
    case YourAddressPage => yourAddress
    case AddAnotherSubscriptionPage => addAnotherSubscription
    case ClaimAmountPage => claimAmount
    case UpdateYourEmployerPage => _ => YourAddressController.onPageLoad(NormalMode)
    case UpdateYourAddressPage => _ => CheckYourAnswersController.onPageLoad()
    case _ => _ => IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      routeMap(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private def addAnotherSubscription(userAnswers: UserAnswers): Call = userAnswers.get(AddAnotherSubscriptionPage) match {
    case Some(true) => ???
    case Some(false) => ClaimAmountController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def employerContribution(userAnswers: UserAnswers): Call = userAnswers.get(EmployerContributionPage) match {
    case Some(true) => ExpensesEmployerPaidController.onPageLoad(NormalMode)
    case Some(false) => AddAnotherSubscriptionController.onPageLoad(NormalMode)
    case _ => SessionExpiredController.onPageLoad()
  }

  private def claimAmount(userAnswers: UserAnswers): Call = userAnswers.get(TaxYearSelectionPage) match {
    case Some(taxYears) => if(taxYears.contains(TaxYearSelection.CurrentYear)){
      YourEmployerController.onPageLoad(NormalMode)
    } else {
      YourAddressController.onPageLoad(NormalMode)
    }
    case _ => SessionExpiredController.onPageLoad()
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
    (userAnswers.get(ProfessionalSubscriptions), userAnswers.get(TaxYearSelectionPage)) match {
      case (Some(professionalSubscription), Some(taxYearSelection)) =>
        if (taxYearSelection.length == 1) {
          professionalSubscription match {
            case psubs if psubs.forall(_.psubAmount.isEmpty) =>
              WhichSubscriptionController.onPageLoad(NormalMode)
            case psubs if psubs.exists(_.psubAmount.isEmpty) && psubs.filterNot(_.psubAmount.isEmpty).forall(_.psubAmount.get.grossAmount == 0) =>
              WhichSubscriptionController.onPageLoad(NormalMode)
            case psubs if psubs.forall(_.psubAmount.isDefined) && psubs.forall(_.psubAmount.get.grossAmount == 0) =>
              WhichSubscriptionController.onPageLoad(NormalMode)
            case _ =>
              ???
          }
        } else {
          ???
        }
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

}
