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

import controllers.routes
import models._
import pages._
import play.api.i18n.Messages
import viewmodels.AnswerRow

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages)  {

  def employerContribution: Option[AnswerRow] = userAnswers.get(EmployerContributionPage) map {
    x => AnswerRow("employerContribution.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true, routes.EmployerContributionController.onPageLoad(CheckMode).url)
  }

  def yourEmployer: Option[AnswerRow] = (userAnswers.get(YourEmployerPage), userAnswers.get(YourEmployersNames)) match {
    case (Some(x), Some(employers)) =>
      Some(AnswerRow("yourEmployer.checkYourAnswersLabel",
        if (x) "site.yes" else "site.no",
        answerIsMessageKey = true,
        routes.YourEmployerController.onPageLoad(CheckMode).url,
        Employment.asLabel(employers)
      ))
    case _ => None
  }

  def yourAddress: Option[AnswerRow] = (userAnswers.get(YourAddressPage), userAnswers.get(CitizensDetailsAddress)) match {
    case (Some(x), Some(address)) =>
      Some(AnswerRow("yourAddress.checkYourAnswersLabel",
        if (x) "site.yes" else "site.no",
        true,
        routes.YourAddressController.onPageLoad(CheckMode).url,
        Address.asString(address))
      )
    case _ => None
  }

  def taxYearSelection: Option[AnswerRow] = userAnswers.get(TaxYearSelectionPage) map {
    taxYears =>
      AnswerRow("taxYearSelection.checkYourAnswersLabel",
        taxYears.map {
          taxYear =>
            messages(s"taxYearSelection.$taxYear",
              TaxYearSelection.getTaxYear(taxYear).toString,
              (TaxYearSelection.getTaxYear(taxYear) + 1).toString
            )
        }.mkString("<br>"),
        answerIsMessageKey = false,
        routes.TaxYearSelectionController.onPageLoad(CheckMode).url
      )
  }

  def sameAmountAllYears: Option[AnswerRow] = userAnswers.get(SameAmountAllYearsPage) map {
    x => AnswerRow("sameAmountAllYears.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.SameAmountAllYearsController.onPageLoad(CheckMode).url)
  }

  def subscriptionAmount: Option[AnswerRow] = userAnswers.get(SubscriptionAmountPage) map {
    x => AnswerRow(
      label = "subscriptionAmount.checkYourAnswersLabel",
      answer = s"$x",
      answerIsMessageKey = false,
      changeUrl = routes.SubscriptionAmountController.onPageLoad(CheckMode).url
    )
  }

  def expensesEmployerPaid: Option[AnswerRow] = userAnswers.get(ExpensesEmployerPaidPage) map {
    x => AnswerRow("ExpensesEmployerPaid.checkYourAnswersLabel",s"$x", false, routes.ExpensesEmployerPaidController.onPageLoad(CheckMode).url)
  }

  def addAnotherSubscription: Option[AnswerRow] = userAnswers.get(AddAnotherSubscriptionPage) map {
    x => AnswerRow("addAnotherSubscription.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true,
      routes.AddAnotherSubscriptionController.onPageLoad(CheckMode).url)
  }
}
