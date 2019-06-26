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
import org.jsoup.select.Elements
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

      taxYears.foreach {
        taxYear =>
          s"render a heading of ${messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString)}" in {
            assert(doc.getElementById(taxYear.toString).getElementsByTag("h2").text ==
              messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString))
          }

          s"render the $taxYear table headings correctly" in {
            assert(doc.getElementById(taxYear.toString).getElementsByTag("th").eq(0).text() contains
              messages(s"$messageKeyPrefix.tableHeading1"))
            assert(doc.getElementById(taxYear.toString).getElementsByTag("th").eq(1).text() contains
              messages(s"$messageKeyPrefix.tableHeading2"))
            assert(doc.getElementById(taxYear.toString).getElementsByTag("th").eq(2).text() contains
              messages(s"$messageKeyPrefix.tableHeading3"))
          }

          subscriptions(getTaxYear(taxYear).toString).zipWithIndex.foreach {
            case (subscription, i) =>
              s"render the $taxYear subscription $i name correctly" in {
                assert(doc.getElementById(s"${taxYear.toString}-subscription-$i").getElementsByTag("td").eq(0).text() contains
                  subscriptions(getTaxYear(taxYear).toString)(i).name)
              }
              s"render the $taxYear subscription $i amount correctly" in {
                assert(doc.getElementById(s"${taxYear.toString}-subscription-$i").getElementsByTag("td").eq(1).text() contains
                  s"£${subscriptions(getTaxYear(taxYear).toString)(i).amount}")
              }
              s"render the $taxYear subscription $i employerContributionAmount correctly" in {
                if (subscriptions(getTaxYear(taxYear).toString)(i).employerContributionAmount.isDefined) {
                  assert(doc.getElementById(s"${taxYear.toString}-subscription-$i").getElementsByTag("td").eq(2).text() contains
                    s"£${subscriptions(getTaxYear(taxYear).toString)(i).employerContributionAmount.get}")
                } else {
                  assert(doc.getElementById(s"${taxYear.toString}-subscription-$i").getElementsByTag("td").eq(2).text() contains "£0")
                }
              }
              s"render the $taxYear subscription $i edit link correctly" in {
                assert(doc.getElementById(s"${taxYear.toString}-subscription-$i").getElementsByTag("a").attr("href") contains
                  s"/professional-subscriptions/which-subscription-are-you-claiming-for/${getTaxYear(taxYear)}/$i")
              }
              s"render the $taxYear subscription $i remove link correctly" in {
                assert(doc.getElementById(s"${taxYear.toString}-subscription-$i").getElementsByTag("a").last().attr("href") contains
                  s"/professional-subscriptions/remove-subscription/${getTaxYear(taxYear)}/$i")
              }
          }

          s"render the $taxYear add link correctly" in {
            assert(doc.getElementById(taxYear.toString).nextElementSibling().getElementsByTag("a").attr("href") contains
              s"/professional-subscriptions/which-subscription-are-you-claiming-for/${getTaxYear(taxYear)}/${subscriptions(getTaxYear(taxYear).toString).length}")
          }
      }
    }
  }
}
