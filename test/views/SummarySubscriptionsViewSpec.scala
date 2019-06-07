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

package views

import models.{NormalMode, TaxYearSelection}
import org.jsoup.nodes.Document
import pages.{SummarySubscriptionsPage, TaxYearSelectionPage}
import views.behaviours.ViewBehaviours
import views.html.SummarySubscriptionsView

class SummarySubscriptionsViewSpec extends ViewBehaviours {

  "SummarySubscriptions view" must {

    val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

    val view = application.injector.instanceOf[SummarySubscriptionsView]

    val applyView = view.apply(someUserAnswers.get(TaxYearSelectionPage).get, navigator.nextPage(SummarySubscriptionsPage, NormalMode, emptyUserAnswers).url)(fakeRequest, messages)

    val doc: Document = asDocument(applyView)

    behave like normalPage(applyView, "summarySubscriptions")

    behave like pageWithBackLink(applyView)

    someUserAnswers.get(TaxYearSelectionPage).get.foreach {
      taxYear =>
        s"render an element with id $taxYear with text ${TaxYearSelection.getTaxYear(taxYear).toString}" in {
          assert(doc.getElementById(taxYear.toString).text() == TaxYearSelection.getTaxYear(taxYear).toString)
        }
    }
  }
}
