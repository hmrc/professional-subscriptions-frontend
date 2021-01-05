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

package views

import models.NpsDataFormats.sort
import models.{NpsDataFormats, PSub}
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1, getTaxYear, getTaxYearPeriod}
import utils.CheckYourAnswersHelper
import viewmodels.AnswerSection
import views.behaviours.ViewBehaviours
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewBehaviours {

  "CheckYourAnswers view" must {

    val application = applicationBuilder().build()

    val view = application.injector.instanceOf[CheckYourAnswersView]

    val cyaHelper = new CheckYourAnswersHelper(userAnswersCurrentAndPrevious)

    val subs = Map(
      getTaxYear(CurrentYear) -> Seq(PSub("psub1", 100, true, Some(10)), PSub("psub2", 100, false, None)),
      getTaxYear(CurrentYearMinus1) -> Seq(PSub("psub3", 100, true, Some(10)))
    )

    val taxYearSelection: Seq[AnswerSection] = Seq(AnswerSection(
      headingKey = Some("checkYourAnswers.taxYearsClaiming"),
      headingClasses = Some("visually-hidden"),
      subheadingKey = None,
      rows = Seq(
        cyaHelper.taxYearSelection,
        cyaHelper.amountsAlreadyInCode,
        cyaHelper.reEnterAmounts
      ).flatten
    ))
    
    val subscriptions: Seq[AnswerSection] = {
      NpsDataFormats.sort(subs).zipWithIndex.flatMap {
        case (psubByYear, yearIndex) =>
          psubByYear._2.zipWithIndex.map {
            case (psub, subsIndex) =>
              val taxYear = psubByYear._1

              AnswerSection(
                headingKey = if (yearIndex == 0 && subsIndex == 0) Some("checkYourAnswers.yourSubscriptions") else None,
                headingClasses = None,
                subheadingKey = if (subsIndex == 0) Some(s"taxYearSelection.${getTaxYearPeriod(taxYear)}") else None,
                rows = Seq(
                  cyaHelper.whichSubscription(taxYear.toString, subsIndex, psub),
                  cyaHelper.subscriptionAmount(taxYear.toString, subsIndex, psub),
                  cyaHelper.employerContribution(taxYear.toString, subsIndex, psub),
                  cyaHelper.expensesEmployerPaid(taxYear.toString, subsIndex, psub)
                ).flatten,
                messageArgs = Seq(taxYear.toString, (taxYear + 1).toString): _*
              )
          }
      }
    }

    val personalData: Seq[AnswerSection] = Seq(AnswerSection(
      headingKey = Some("checkYourAnswers.yourDetails"),
      headingClasses = None,
      subheadingKey = None,
      rows = Seq(
        cyaHelper.yourEmployer,
        cyaHelper.yourAddress
      ).flatten
    ))

    val sections = taxYearSelection ++ subscriptions ++ personalData

    val applyView = view.apply(sections)(fakeRequest, messages)

    val doc = asDocument(applyView)

    behave like normalPage(applyView, "checkYourAnswers")

    behave like pageWithBackLink(applyView)

    "have correct content" in {
      assertContainsMessages(doc, messages(
        "checkYourAnswers.disclaimerHeading",
        "checkYourAnswers.disclaimer",
        "checkYourAnswers.prosecuted"
      ))

      doc.getElementById("continue").text() mustBe messages("checkYourAnswers.submit")
    }

    "have correct headings" in {
      doc.getElementsByTag("h2").eq(0).text() mustBe messages("checkYourAnswers.taxYearsClaiming")
      doc.getElementsByTag("h2").eq(0).hasClass("visually-hidden") mustBe true

      doc.getElementsByTag("h2").eq(1).text() mustBe messages("checkYourAnswers.yourSubscriptions")
      doc.getElementsByTag("h3").eq(0).text() mustBe messages(s"taxYearSelection.$CurrentYear", getTaxYear(CurrentYear).toString, (getTaxYear(CurrentYear) + 1).toString)
      doc.getElementsByTag("h3").eq(1).text() mustBe messages(s"taxYearSelection.$CurrentYearMinus1", getTaxYear(CurrentYearMinus1).toString, (getTaxYear(CurrentYearMinus1) + 1).toString)
      doc.getElementsByTag("h3").size() mustBe 2

      doc.getElementsByTag("h2").eq(2).text() mustBe messages("checkYourAnswers.yourDetails")

      doc.getElementsByTag("h2").eq(3).text() mustBe messages("checkYourAnswers.disclaimerHeading")

    }

  }

}
