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

package views

import models.NormalMode
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1, getTaxYear}
import models.PSubsByYear.pSubsByYearFormats
import navigation.{FakeNavigator, Navigator}
import org.scalatestplus.mockito.MockitoSugar
import pages.SummarySubscriptionsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.twirl.api.Html
import views.behaviours.{NewViewBehaviours, SummarySubscriptionComponentBehaviours}
import views.html.SummarySubscriptionsView

class SummarySubscriptionsViewSpec
    extends NewViewBehaviours
    with SummarySubscriptionComponentBehaviours
    with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  "SummarySubscriptions view" must {

    val messageKeyPrefix = "summarySubscriptions"

    val application = applicationBuilder(userAnswers = Some(userAnswersCurrentAndPrevious))
      .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
      .build()

    val view = application.injector.instanceOf[SummarySubscriptionsView]

    val subscriptions = userAnswersCurrentAndPrevious.get(SummarySubscriptionsPage).get

    val npsData = Map(getTaxYear(CurrentYear) -> 300, getTaxYear(CurrentYearMinus1) -> 0)

    def applyView(arePsubsEmpty: Boolean = true): Html =
      view.apply(
        subscriptions = subscriptions,
        npsData = npsData,
        nextPageUrl = onwardRoute.url,
        mode = NormalMode,
        arePsubsEmpty
      )(fakeRequest, messages)

    application.stop()

    behave.like(normalPage(applyView(), messageKeyPrefix))

    behave.like(pageWithBackLink(applyView()))

    behave.like(pageWithSummarySubscriptionComponent(view, messageKeyPrefix))
  }

}
