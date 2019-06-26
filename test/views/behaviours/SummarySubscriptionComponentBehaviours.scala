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

package views.behaviours

import models.TaxYearSelection.getTaxYear
import models.{PSub, TaxYearSelection, UserAnswers}
import org.jsoup.nodes.Document
import pages.{SummarySubscriptionsPage, TaxYearSelectionPage}
import play.twirl.api.Html

trait SummarySubscriptionComponentBehaviours extends ViewBehaviours {

  def pageWithSummarySubscriptionComponent(applyView: Html,
                                           messageKeyPrefix: String,
                                           userAnswers: UserAnswers
                                          ): Unit = {

    "subscription summary component" must {
      val subscriptions: Map[String, Seq[PSub]] = userAnswers.get(SummarySubscriptionsPage).get
      val doc: Document = asDocument(applyView)
      val taxYears: Seq[TaxYearSelection] = someUserAnswers.get(TaxYearSelectionPage).get

      taxYears.zipWithIndex.foreach {
        case (taxYearSelection, i) =>
          s"render a heading of ${messages(s"taxYearSelection.$taxYearSelection", getTaxYear(taxYearSelection).toString, (getTaxYear(taxYearSelection) + 1).toString)}" in {
            assert(doc.getElementById(taxYearSelection.toString).getElementsByTag("h2").text ==
              messages(s"taxYearSelection.$taxYearSelection", getTaxYear(taxYearSelection).toString, (getTaxYear(taxYearSelection) + 1).toString))
          }
          s"render the $taxYearSelection psub name correctly" in {
            assert(doc.getElementById(taxYearSelection.toString).getElementsByTag("h3").eq(0).text() contains
              subscriptions(getTaxYear(taxYearSelection).toString)(i).name)
          }
          s"render the $taxYearSelection psub amount correctly" in {
            assert(doc.getElementById(taxYearSelection.toString).getElementsByClass("cya-answer").eq(0).text() contains
              s"£${subscriptions(getTaxYear(taxYearSelection).toString)(i).amount}")
          }
          s"render the $taxYearSelection psub employerContributionAmount correctly" in {
            if (subscriptions(getTaxYear(taxYearSelection).toString)(i).employerContributionAmount.isDefined) {
              assert(doc.getElementById(taxYearSelection.toString).getElementsByClass("cya-answer").eq(1).text() contains
                s"£${subscriptions(getTaxYear(taxYearSelection).toString)(i).employerContributionAmount.get}")
            } else {
              assert(doc.getElementById(taxYearSelection.toString).getElementsByClass("cya-answer").eq(1).text() contains "£0")
            }
          }
          s"render the $taxYearSelection edit link correctly" in {
            assert(doc.getElementById(taxYearSelection.toString).getElementsByTag("a").eq(1).attr("href") contains
              s"/professional-subscriptions/which-subscription-are-you-claiming-for/${getTaxYear(taxYearSelection)}/$i")
          }
          s"render the $taxYearSelection remove link correctly" in {
            assert(doc.getElementById(taxYearSelection.toString).getElementsByTag("a").eq(2).attr("href") contains
              s"/professional-subscriptions/remove-subscription/${getTaxYear(taxYearSelection)}/$i")
          }
          s"render the $taxYearSelection add link correctly" in {
            assert(doc.getElementById(taxYearSelection.toString).getElementsByTag("a").eq(0).attr("href") contains
              s"/professional-subscriptions/which-subscription-are-you-claiming-for/${getTaxYear(taxYearSelection)}/${subscriptions(getTaxYear(taxYearSelection).toString).length}")
          }
      }
    }
  }
}
