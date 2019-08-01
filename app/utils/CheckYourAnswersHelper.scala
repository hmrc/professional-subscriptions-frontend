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
import models.TaxYearSelection._
import models.NpsDataFormats._
import pages._
import play.api.i18n.Messages
import viewmodels.AnswerRow

import scala.collection.immutable.ListMap

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def duplicateClaimForOtherYears: Option[AnswerRow] = userAnswers.get(DuplicateClaimForOtherYearsPage) map {
    x => AnswerRow(
      label = "duplicateClaimForOtherYears.checkYourAnswersLabel",
      answer = if (x) "site.yes" else "site.no",
      answerIsMessageKey = true,
      changeUrl = DuplicateClaimForOtherYearsController.onPageLoad(CheckMode).url,
      editText = None,
      hiddenText = None
    )
  }

  def taxYearText(taxYear: TaxYearSelection): String =
    messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString)

  def npsDataFormatted(npsData: Map[Int, Seq[EmploymentExpense]]): String = {

    val sortedData: Seq[(Int, Seq[EmploymentExpense])] = ListMap(sort(npsData): _*).toSeq

    val years: Seq[String] = sortedData.map(x => taxYearText(getTaxYearPeriod(x._1)))

    val amounts: Seq[String] = sortedData.map(_._2.map(_.grossAmount).headOption.getOrElse(0).toString)

    val yearsAndAmounts: Seq[(String, String)] = years zip amounts

    s"<p>${yearsAndAmounts.map(x => s"${x._1} - £${x._2}").mkString("<br>")}</p>"
  }


  def amountsYouNeedToChange: Option[AnswerRow] = userAnswers.get(AmountsYouNeedToChangePage) map {
    taxYears =>
      AnswerRow(
        label = "amountsYouNeedToChange.checkYourAnswersLabel",
        answer = taxYears.map {
          taxYear =>
            messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString)
        }.mkString("<br>"),
        answerIsMessageKey = false,
        changeUrl = AmountsYouNeedToChangeController.onPageLoad(CheckMode).url,
        editText = None,
        hiddenText = Some("amountsYouNeedToChange.checkYourAnswersLabel.hidden")
      )
  }

  def amountsAlreadyInCode: Option[AnswerRow] = (userAnswers.get(AmountsAlreadyInCodePage), userAnswers.get(NpsData)) match {
    case (Some(x), Some(npsData)) =>
      Some(AnswerRow(
        label = "amountsAlreadyInCode.checkYourAnswersLabel",
        answer = if (x) "site.yes" else "site.no",
        answerIsMessageKey = true,
        changeUrl = AmountsAlreadyInCodeController.onPageLoad(CheckMode).url,
        editText = None,
        hiddenText = Some("amountsAlreadyInCode.checkYourAnswersLabel.hidden"),
        labelArgs = Seq(npsDataFormatted(npsData))
      ))
    case _ => None
  }

  def taxYearSelection: Option[AnswerRow] = userAnswers.get(TaxYearSelectionPage) map {
    taxYears =>
      AnswerRow(
        label = "taxYearSelection.checkYourAnswersLabel",
        answer = taxYears.map {
          taxYear =>
            messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString)
        }.mkString("<br>"),
        answerIsMessageKey = false,
        changeUrl = TaxYearSelectionController.onPageLoad(CheckMode).url,
        editText = None,
        hiddenText = Some("taxYearSelection.checkYourAnswersLabel.hidden")
      )
  }

  def whichSubscription(year: String, index: Int, pSub: PSub): Option[AnswerRow] = {
    var taxYr = getTaxYearPeriod(year.toInt).toString
    Some(AnswerRow(
      label = "whichSubscription.checkYourAnswersLabel",
      answer = pSub.name,
      answerIsMessageKey = false,
      changeUrl = WhichSubscriptionController.onPageLoad(CheckMode, year, index).url,
      editText = None,
      hiddenText = Some("whichSubscription.checkYourAnswersLabel.hidden"),
      hiddenTextArgs = Seq(pSub.name, messages(s"taxYearSelection.$taxYr", year.toString, (year.toInt+1).toString))
    ))
  }

  def subscriptionAmount(year: String, index: Int, pSub: PSub): Option[AnswerRow] = {
    var taxYr = getTaxYearPeriod(year.toInt).toString
    Some(AnswerRow(
      label = "subscriptionAmount.checkYourAnswersLabel",
      answer = s"£${pSub.amount}",
      answerIsMessageKey = false,
      changeUrl = SubscriptionAmountController.onPageLoad(CheckMode, year, index).url,
      editText = None,
      hiddenText = Some("subscriptionAmount.checkYourAnswersLabel.hidden"),
      hiddenTextArgs = Seq(pSub.name, messages(s"taxYearSelection.$taxYr", year.toString, (year.toInt+1).toString))
    ))
  }

  def employerContribution(year: String, index: Int, pSub: PSub): Option[AnswerRow] = {
    var taxYr = getTaxYearPeriod(year.toInt).toString
    Some(AnswerRow(
      label = "employerContribution.checkYourAnswersLabel",
      answer = if (pSub.employerContributed) "site.yes" else "site.no",
      answerIsMessageKey = true,
      changeUrl = EmployerContributionController.onPageLoad(CheckMode, year, index).url,
      editText = None,
      hiddenText = Some("employerContribution.checkYourAnswersLabel.hidden"),
      hiddenTextArgs = Seq(pSub.name, messages(s"taxYearSelection.$taxYr", year.toString, (year.toInt+1).toString))
    ))
  }

  def expensesEmployerPaid(year: String, index: Int, pSub: PSub): Option[AnswerRow] = pSub.employerContributionAmount match {
    case Some(x) =>
      var taxYr = getTaxYearPeriod(year.toInt).toString
      Some(AnswerRow(
        label = "expensesEmployerPaid.checkYourAnswersLabel",
        answer = s"£$x",
        answerIsMessageKey = false,
        changeUrl = ExpensesEmployerPaidController.onPageLoad(CheckMode, year, index).url,
        editText = None,
        hiddenText = Some("expensesEmployerPaid.checkYourAnswersLabel.hidden"),
        hiddenTextArgs = Seq(pSub.name, messages(s"taxYearSelection.$taxYr", year.toString, (year.toInt+1).toString))
      ))
    case _ => None
  }

  def yourEmployer: Option[AnswerRow] = (userAnswers.get(YourEmployerPage), userAnswers.get(YourEmployersNames)) match {
    case (Some(x), Some(employers)) =>
      Some(AnswerRow(
        label = "yourEmployer.checkYourAnswersLabel",
        answer = if (x) "site.yes" else "site.no",
        answerIsMessageKey = true,
        changeUrl = YourEmployerController.onPageLoad(CheckMode).url,
        editText = Some("checkYourAnswers.editText"),
        hiddenText = None,
        labelArgs = Seq(Employment.asLabel(employers))
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
        editText = Some("checkYourAnswers.editText"),
        hiddenText = None,
        labelArgs = Seq(Address.asString(address))
      ))
    case _ => None
  }

}
