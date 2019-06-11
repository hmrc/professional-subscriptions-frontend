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
import org.jsoup.nodes.Document
import pages.TaxYearSelectionPage
import play.twirl.api.Html

trait SummarySubscriptionComponentBehaviours extends ViewBehaviours {

  def pageWithSummarySubscriptionComponent(applyView: Html,
                                           messageKeyPrefix: String): Unit = {

    "subscription summary component" must {
      val doc: Document = asDocument(applyView)

      someUserAnswers.get(TaxYearSelectionPage).get.foreach {
        taxYear =>
          s"render a heading of ${messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString)}" in {
            assert(doc.getElementById(taxYear.toString).getElementsByTag("h2").text == messages(s"taxYearSelection.$taxYear", getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString))
          }
          s"render an element with id $taxYear" in {
            assert(doc.getElementById(taxYear.toString) != null)
          }
          s"render correct table headings for $taxYear" in {
            assert(doc.getElementById(taxYear.toString).getElementsByTag("th").text() contains messages(s"$messageKeyPrefix.tableHeading1"))
            assert(doc.getElementById(taxYear.toString).getElementsByTag("th").text() contains messages(s"$messageKeyPrefix.tableHeading2"))
            assert(doc.getElementById(taxYear.toString).getElementsByTag("th").text() contains messages(s"$messageKeyPrefix.tableHeading3"))
          }
          s"render the $taxYear psub name correctly" in {
            assert(doc.getElementById(taxYear.toString).text() contains "Test Psub")
          }
          s"render an edit link for each psub for ${getTaxYear(taxYear)}" in {
            assert(doc.getElementById(taxYear.toString).getElementsByTag("a").attr("href") contains s"${getTaxYear(taxYear)}")
          }
          s"render a remove link for each psub for ${getTaxYear(taxYear)}" in {
            assert(doc.getElementById(taxYear.toString).getElementsByTag("a").attr("href") contains s"/professional-subscriptions/remove-subscription/${getTaxYear(taxYear)}")
          }
      }
    }
  }
}
