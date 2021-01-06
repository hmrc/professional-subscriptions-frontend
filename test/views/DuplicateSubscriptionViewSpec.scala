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

import models.NormalMode
import pages.DuplicateSubscriptionPage
import views.behaviours.ViewBehaviours
import views.html.DuplicateSubscriptionView

class DuplicateSubscriptionViewSpec extends ViewBehaviours {

  "DuplicateSubscription view" must {

    val forwardURL = navigator.nextPage(DuplicateSubscriptionPage, NormalMode, emptyUserAnswers).url

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[DuplicateSubscriptionView]

    val applyView = view.apply(NormalMode, forwardURL)(fakeRequest, messages)

    behave like normalPage(applyView, "duplicateSubscription")

    behave like pageWithBackLink(applyView)

    "have correct content" in {
      val doc = asDocument(applyView)

      assertContainsMessages(doc, messages("duplicateSubscription.para1"))

      doc.getElementById("continue").text() mustBe messages("duplicateSubscription.button")
      doc.getElementById("continue").attr("href") mustBe forwardURL
    }
  }
}
