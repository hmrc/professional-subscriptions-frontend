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

package utils

import controllers.routes._
import models.TaxYearSelection._
import models._
import pages._
import play.api.i18n.Messages
import viewmodels.AnswerRow

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def reEnterAmounts: Option[AnswerRow] = userAnswers.get(ReEnterAmountsPage).map { x =>
    AnswerRow(
      "reEnterAmounts.checkYourAnswersLabel",
      if (x) "site.yes" else "site.no",
      answerIsMessageKey = true,
      ReEnterAmountsController.onPageLoad(CheckMode).url
    )
  }

  def amountsAlreadyInCode: Option[AnswerRow] = userAnswers.get(AmountsAlreadyInCodePage) match {
    case Some(x) =>
      Some(
        AnswerRow(
          label = "amountsAlreadyInCode.checkYourAnswersLabel",
          answer = if (x) "site.yes" else "site.no",
          answerIsMessageKey = true,
          changeUrl = AmountsAlreadyInCodeController.onPageLoad(CheckMode).url,
          editText = None,
          hiddenText = Some("amountsAlreadyInCode.checkYourAnswersLabel.hidden")
        )
      )
    case _ => None
  }

  def taxYearSelection: Option[AnswerRow] =
    userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).map { taxYears =>
      AnswerRow(
        label = "taxYearSelection.checkYourAnswersLabel",
        answer = taxYears.keys
          .map { taxYear =>
            messages(s"taxYearSelection.${getTaxYearPeriod(taxYear)}", taxYear.toString, (taxYear + 1).toString)
          }
          .mkString("<br>"),
        answerIsMessageKey = false,
        changeUrl = TaxYearSelectionController.onPageLoad(CheckMode).url,
        editText = None,
        hiddenText = Some("taxYearSelection.checkYourAnswersLabel.hidden")
      )
    }

  def whichSubscription(year: String, index: Int, pSub: PSub): Option[AnswerRow] = {
    val taxYr = getTaxYearPeriod(year.toInt).toString
    Some(
      AnswerRow(
        label = "whichSubscription.checkYourAnswersLabel",
        answer = pSub.nameOfProfessionalBody,
        answerIsMessageKey = false,
        changeUrl = WhichSubscriptionController.onPageLoad(CheckMode, year, index).url,
        editText = None,
        hiddenText = Some("whichSubscription.checkYourAnswersLabel.hidden"),
        hiddenTextArgs =
          Seq(pSub.nameOfProfessionalBody, messages(s"taxYearSelection.$taxYr", year, (year.toInt + 1).toString))
      )
    )
  }

  def subscriptionAmount(year: String, index: Int, pSub: PSub): Option[AnswerRow] =
    Some(
      AnswerRow(
        label = "subscriptionAmount.checkYourAnswersLabel",
        answer = s"£${pSub.amount}",
        answerIsMessageKey = false,
        changeUrl = SubscriptionAmountController.onPageLoad(CheckMode, year, index).url,
        editText = None,
        hiddenText = Some("subscriptionAmount.checkYourAnswersLabel.hidden"),
        hiddenTextArgs = Seq(
          pSub.nameOfProfessionalBody,
          messages(s"taxYearSelection.${getTaxYearPeriod(year.toInt)}", year, year.toInt + 1)
        )
      )
    )

  def employerContribution(year: String, index: Int, pSub: PSub): Option[AnswerRow] =
    Some(
      AnswerRow(
        label = "employerContribution.checkYourAnswersLabel",
        answer = if (pSub.employerContributed) "site.yes" else "site.no",
        answerIsMessageKey = true,
        changeUrl = EmployerContributionController.onPageLoad(CheckMode, year, index).url,
        editText = None,
        hiddenText = Some("employerContribution.checkYourAnswersLabel.hidden"),
        hiddenTextArgs = Seq(
          pSub.nameOfProfessionalBody,
          messages(s"taxYearSelection.${getTaxYearPeriod(year.toInt)}", year, year.toInt + 1)
        )
      )
    )

  def expensesEmployerPaid(year: String, index: Int, pSub: PSub): Option[AnswerRow] =
    pSub.employerContributionAmount match {
      case Some(x) =>
        Some(
          AnswerRow(
            label = "expensesEmployerPaid.checkYourAnswersLabel",
            answer = s"£$x",
            answerIsMessageKey = false,
            changeUrl = ExpensesEmployerPaidController.onPageLoad(CheckMode, year, index).url,
            editText = None,
            hiddenText = Some("expensesEmployerPaid.checkYourAnswersLabel.hidden"),
            hiddenTextArgs = Seq(
              pSub.nameOfProfessionalBody,
              messages(s"taxYearSelection.${getTaxYearPeriod(year.toInt)}", year, year.toInt + 1)
            )
          )
        )
      case _ => None
    }

  def yourEmployer: Option[AnswerRow] = (userAnswers.get(YourEmployerPage), userAnswers.get(YourEmployersNames)) match {
    case (Some(x), Some(employers)) =>
      Some(
        AnswerRow(
          label = "yourEmployer.checkYourAnswersLabel",
          answer = if (x) "site.yes" else "site.no",
          answerIsMessageKey = true,
          changeUrl = YourEmployerController.onPageLoad(CheckMode).url,
          editText = Some("checkYourAnswers.editText"),
          hiddenText = None,
          labelArgs = Seq(Employment.asLabel(employers))
        )
      )
    case _ => None
  }

  def yourAddress: Option[AnswerRow] =
    (userAnswers.get(YourAddressPage), userAnswers.get(CitizensDetailsAddress)) match {
      case (Some(x), Some(address)) =>
        Some(
          AnswerRow(
            label = "yourAddress.checkYourAnswersLabel",
            answer = if (x) "site.yes" else "site.no",
            answerIsMessageKey = true,
            changeUrl = YourAddressController.onPageLoad(CheckMode).url,
            editText = Some("checkYourAnswers.editText"),
            hiddenText = None,
            labelArgs = Seq(Address.asString(address))
          )
        )
      case _ => None
    }

}
