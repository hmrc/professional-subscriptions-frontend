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

import views.behaviours.NewViewBehaviours
import views.html.SessionExpiredView

class SessionExpiredViewSpec extends NewViewBehaviours {

  "Session Expired view" must {

    val application = applicationBuilder().build()

    val view = application.injector.instanceOf[SessionExpiredView]

    val applyView = view.apply()(fakeRequest, messages)

    application.stop()

    behave.like(normalPage(applyView, "session_expired"))

    "have correct content" in {
      val doc = asDocument(applyView)

      assertContainsMessages(doc, messages("session_expired.guidance"))

      doc.getElementById("continue").text() mustBe messages("site.startAgain")
      doc.getElementById("continue").attr("href") mustBe controllers.routes.IndexController.start.url
    }
  }

}
