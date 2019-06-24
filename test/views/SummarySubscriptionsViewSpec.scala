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

import models.NormalMode
import models.TaxYearSelection.getTaxYear
import pages.{SummarySubscriptionsPage, TaxYearSelectionPage}
import views.behaviours.{SummarySubscriptionComponentBehaviours, ViewBehaviours}
import views.html.SummarySubscriptionsView

class SummarySubscriptionsViewSpec extends ViewBehaviours with SummarySubscriptionComponentBehaviours {

  "SummarySubscriptions view" must {

    val messageKeyPrefix = "summarySubscriptions"

    val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

    val view = application.injector.instanceOf[SummarySubscriptionsView]

    val subscriptions = someUserAnswers.get(SummarySubscriptionsPage).get

    val subs = someUserAnswers.get(TaxYearSelectionPage).get.flatMap(
      taxYear =>
        Map(getTaxYear(taxYear) -> subscriptions(getTaxYear(taxYear).toString))
    ).toMap

    val applyView = view.apply(subs, navigator.nextPage(SummarySubscriptionsPage, NormalMode, someUserAnswers).url, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView, messageKeyPrefix)

    behave like pageWithBackLink(applyView)

    behave like pageWithSummarySubscriptionComponent(applyView, messageKeyPrefix, someUserAnswers)
  }
}
