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

package views.behaviours

import models.TaxYearSelection._
import models.{NormalMode, NpsDataFormats, PSub, PSubsByYear, TaxYearSelection}
import org.jsoup.nodes.Document
import pages.{NpsData, SummarySubscriptionsPage}
import views.html.SummarySubscriptionsView

trait SummarySubscriptionComponentBehaviours extends NewViewBehaviours {

  def pageWithSummarySubscriptionComponent(view: SummarySubscriptionsView, messageKeyPrefix: String): Unit = {

    "subscription summary component" when {
      "has subscriptions added" must {
        val userAnswers = userAnswersCurrentAndPrevious
        val subscriptions: Map[Int, Seq[PSub]] =
          userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats).get
        val npsData: Map[Int, Int] = userAnswers.get(NpsData)(NpsDataFormats.npsDataFormatsFormats).get
        val taxYears: Seq[TaxYearSelection] = userAnswers
          .get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)
          .get
          .map(year => getTaxYearPeriod(year._1))
          .toSeq
        val applyView = view.apply(
          subscriptions,
          npsData,
          navigator.nextPage(SummarySubscriptionsPage, NormalMode, userAnswers).url,
          NormalMode,
          arePsubsEmpty = true
        )(fakeRequest, messages)
        val doc: Document = asDocument(applyView)

        taxYears.foreach { taxYear =>
          s"render a heading of ${messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString)}" in
            assert(
              doc.getElementById(taxYear.toString).getElementsByTag("h2").text ==
                messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString)
            )

          subscriptions(getTaxYear(taxYear)).zipWithIndex.foreach { case (subscription, i) =>
            s"render the $taxYear subscription $i name correctly" in
              assert(
                doc.getElementById(s"${taxYear.toString}-subscription-$i").getElementsByTag("h3").eq(0).text() contains
                  subscriptions(getTaxYear(taxYear))(i).nameOfProfessionalBody
              )
            s"render the $taxYear subscription $i amount correctly" in
              assert(
                doc
                  .getElementById(s"${taxYear.toString}-subscription-$i")
                  .getElementsByClass("govuk-summary-list__value")
                  .eq(0)
                  .text() contains
                  s"£${subscriptions(getTaxYear(taxYear))(i).amount}"
              )
            s"render the $taxYear subscription $i employerContributionAmount correctly" in {
              if (subscriptions(getTaxYear(taxYear))(i).employerContributionAmount.isDefined) {
                assert(
                  doc
                    .getElementById(s"${taxYear.toString}-subscription-$i")
                    .getElementsByClass("govuk-summary-list__value")
                    .eq(1)
                    .text() contains
                    s"£${subscriptions(getTaxYear(taxYear))(i).employerContributionAmount.get}"
                )
              } else {
                assert(
                  doc
                    .getElementById(s"${taxYear.toString}-subscription-$i")
                    .getElementsByClass("cya-answer")
                    .eq(1)
                    .text() contains "£0"
                )
              }
            }
            s"render the $taxYear subscription $i edit link correctly" in
              assert(
                doc
                  .getElementById(s"${taxYear.toString}-subscription-$i")
                  .getElementsByTag("a")
                  .eq(0)
                  .attr("href") contains
                  s"/professional-subscriptions/which-subscription-are-you-claiming-for/${getTaxYear(taxYear)}/$i"
              )
            s"render the $taxYear subscription $i remove link correctly" in
              assert(
                doc
                  .getElementById(s"${taxYear.toString}-subscription-$i")
                  .getElementsByTag("a")
                  .eq(1)
                  .attr("href") contains
                  s"/professional-subscriptions/remove-subscription/${getTaxYear(taxYear)}/$i"
              )
          }

          s"render the $taxYear add link text correctly" in
            assert(
              doc.getElementById(taxYear.toString).getElementsByTag("a").eq(2).text() contains
                messages("summarySubscriptions.addAnother")
            )

          s"render the $taxYear add link screen reader text correctly" in
            assert(
              doc.getElementById(taxYear.toString).getElementsByTag("a").eq(2).text() contains
                messages(
                  "summarySubscriptions.link.hiddenTextAddAnother",
                  messages(
                    s"taxYearSelection.$taxYear",
                    getTaxYear(taxYear).toString,
                    (getTaxYear(taxYear) + 1).toString
                  )
                )
            )

          s"render the $taxYear add link href correctly" in
            assert(
              doc.getElementById(taxYear.toString).getElementsByTag("a").eq(2).attr("href") contains
                s"/professional-subscriptions/which-subscription-are-you-claiming-for/${getTaxYear(taxYear)}/${subscriptions(getTaxYear(taxYear)).length}"
            )
        }

        "hide the submit button and display guidance text" in {
          assertContainsMessages(doc, "summarySubscriptions.continueClaim", "summarySubscriptions.atLeastOne")
          assertNotRenderedById(doc, "continue")
        }
      }

      "has no subscriptions added" must {
        val subscriptions: Map[Int, Seq[PSub]] =
          Map(getTaxYear(CurrentYear) -> Seq.empty, getTaxYear(CurrentYearMinus1) -> Seq.empty)
        val npsData: Map[Int, Int]          = Map(getTaxYear(CurrentYear) -> 100, getTaxYear(CurrentYearMinus1) -> 0)
        val taxYears: Seq[TaxYearSelection] = Seq(CurrentYear, CurrentYearMinus1)
        val applyView = view.apply(
          subscriptions,
          npsData,
          navigator.nextPage(SummarySubscriptionsPage, NormalMode, userAnswersCurrentAndPrevious).url,
          NormalMode,
          false
        )(fakeRequest, messages)
        val doc: Document = asDocument(applyView)

        "render change link when nps data present" in {
          val taxYear = taxYears.head
          assert(
            doc.getElementById(taxYear.toString).getElementsByTag("a").eq(0).text() contains
              messages("summarySubscriptions.change")
          )

          assert(
            doc.getElementById(taxYear.toString).getElementsByTag("a").eq(0).text() contains
              messages(
                "summarySubscriptions.link.hiddenTextChange",
                messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString)
              )
          )

          assert(
            doc.getElementById(taxYear.toString).getElementsByTag("a").eq(0).attr("href") contains
              s"/professional-subscriptions/which-subscription-are-you-claiming-for/${getTaxYear(taxYear)}/${subscriptions(getTaxYear(taxYear)).length}"
          )
        }

        "render add link when no nps data" in {
          val taxYear = taxYears.last
          assert(
            doc.getElementById(taxYear.toString).getElementsByTag("a").eq(0).text() contains
              messages("summarySubscriptions.addA")
          )

          assert(
            doc.getElementById(taxYear.toString).getElementsByTag("a").eq(0).text() contains
              messages(
                "summarySubscriptions.link.hiddenTextAdd",
                messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString)
              )
          )

          assert(
            doc.getElementById(taxYear.toString).getElementsByTag("a").eq(0).attr("href") contains
              s"/professional-subscriptions/which-subscription-are-you-claiming-for/${getTaxYear(taxYear)}/${subscriptions(getTaxYear(taxYear)).length}"
          )
        }

        "show submit button" in
          assertRenderedById(doc, "continue")
      }
    }
  }

}
