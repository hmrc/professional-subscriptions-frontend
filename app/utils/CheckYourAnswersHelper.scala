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

package utils

import controllers.routes._
import models._
import pages._
import play.api.i18n.Messages
import viewmodels.AnswerRow

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages)  {

  def removeSubscription: Option[AnswerRow] = userAnswers.get(RemoveSubscriptionPage) map {
    x => AnswerRow("removeSubscription.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true, RemoveSubscriptionController.onPageLoad().url)
  }

  def employerContribution: Option[AnswerRow] = userAnswers.get(EmployerContributionPage) map {
    x => AnswerRow(
      label = "employerContribution.checkYourAnswersLabel",
      answer = if (x) "site.yes" else "site.no",
      answerIsMessageKey = true,
      changeUrl = EmployerContributionController.onPageLoad(CheckMode).url)
  }

  def yourEmployer: Option[AnswerRow] = (userAnswers.get(YourEmployerPage), userAnswers.get(YourEmployersNames)) match {
    case (Some(x), Some(employers)) =>
      Some(AnswerRow(
        label = "yourEmployer.checkYourAnswersLabel",
        answer = if (x) "site.yes" else "site.no",
        answerIsMessageKey = true,
        changeUrl = YourEmployerController.onPageLoad(CheckMode).url,
        labelArgs = Employment.asLabel(employers)
      ))
    case _ => None
  }

  def yourAddress: Option[AnswerRow] = (userAnswers.get(YourAddressPage), userAnswers.get(CitizensDetailsAddress)) match {
    case (Some(x), Some(address)) =>
      Some(AnswerRow(
        label = "yourAddress.checkYourAnswersLabel",
        answer = if (x) "site.yes" else "site.no",
        answerIsMessageKey = true,
        changeUrl = YourAddressController.onPageLoad(CheckMode).url,
        labelArgs = Address.asString(address))
      )
    case _ => None
  }

  def taxYearSelection: Option[AnswerRow] = userAnswers.get(TaxYearSelectionPage) map {
    taxYears =>
      AnswerRow(
        label = "taxYearSelection.checkYourAnswersLabel",
        answer = taxYears.map {
          taxYear =>
            messages(s"taxYearSelection.$taxYear",
              TaxYearSelection.getTaxYear(taxYear).toString,
              (TaxYearSelection.getTaxYear(taxYear) + 1).toString
            )
        }.mkString("<br>"),
        answerIsMessageKey = false,
        changeUrl = TaxYearSelectionController.onPageLoad(CheckMode).url
      )
  }

  def whichSubscription: Option[AnswerRow] = userAnswers.get(WhichSubscriptionPage) map {
    x => AnswerRow("whichSubscription.checkYourAnswersLabel", s"$x", false, WhichSubscriptionController.onPageLoad(CheckMode).url)
  }

  def sameAmountAllYears: Option[AnswerRow] = userAnswers.get(SameAmountAllYearsPage) map {
    x => AnswerRow(
      label = "sameAmountAllYears.checkYourAnswersLabel",
      answer = if (x) "site.yes" else "site.no",
      answerIsMessageKey = true,
      changeUrl = SameAmountAllYearsController.onPageLoad(CheckMode).url)
  }

  def subscriptionAmount: Option[AnswerRow] = userAnswers.get(SubscriptionAmountPage) map {
    x => AnswerRow(
      label = "subscriptionAmount.checkYourAnswersLabel",
      answer = s"£$x",
      answerIsMessageKey = false,
      changeUrl = SubscriptionAmountController.onPageLoad(CheckMode).url
    )
  }

  def expensesEmployerPaid: Option[AnswerRow] = userAnswers.get(ExpensesEmployerPaidPage) map {
    x => AnswerRow(
      label = "expensesEmployerPaid.checkYourAnswersLabel",
      answer = s"£$x",
      answerIsMessageKey = false,
      changeUrl = ExpensesEmployerPaidController.onPageLoad(CheckMode).url)
  }

}
