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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.AccessibilityStatementView

class AccessibilityStatementControllerSpec extends SpecBase {

  "AccessibilityStatement Controller" must {
    val mainUrl = "/professional-subscriptions"

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.AccessibilityStatementController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AccessibilityStatementView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(mainUrl, "https://www.tax.service.gov.uk/professional-subscriptions", false)(request, messages).toString

      application.stop()
    }
  }
}