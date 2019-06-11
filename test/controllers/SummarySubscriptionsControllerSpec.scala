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

package controllers

import base.SpecBase
import models.NormalMode
import pages.{SummarySubscriptionsPage, TaxYearSelectionPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SummarySubscriptionsView
import controllers.routes._

class SummarySubscriptionsControllerSpec extends SpecBase {

  "SummarySubscriptions Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

      val request = FakeRequest(GET, routes.SummarySubscriptionsController.onPageLoad(taxYear, index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[SummarySubscriptionsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(someUserAnswers.get(TaxYearSelectionPage).get, navigator.nextPage(SummarySubscriptionsPage, NormalMode, someUserAnswers).url)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to SessionExpired when no TaxYearSelection for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.SummarySubscriptionsController.onPageLoad(taxYear, index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
