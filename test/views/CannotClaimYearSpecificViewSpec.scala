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

import models.{CheckMode, NormalMode}
import views.behaviours.ViewBehaviours
import views.html.CannotClaimYearSpecificView

class CannotClaimYearSpecificViewSpec extends ViewBehaviours {

  "CannotClaimYearSpecific view" must {

    val subscription = "psub"
    val onwardUrl = "/url"

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[CannotClaimYearSpecificView]

    val applyView = view.apply(NormalMode, onwardUrl, subscription, taxYear)(fakeRequest, messages)

    application.stop

    behave like normalPage(applyView, "cannotClaimYearSpecific")

    behave like pageWithBackLink(applyView)

    "have correct content" in {
      val doc = asDocument(applyView)

      assertContainsMessages(doc, messages("cannotClaimYearSpecific.para1", subscription))
      assertContainsMessages(doc, messages("cannotClaimYearSpecific.para2", taxYear))

      doc.getElementById("continue").text() mustBe messages("cannotClaimYearSpecific.button")
      doc.getElementById("continue").attr("href") mustBe onwardUrl
    }

    "have correct content in check mode" in {
      val applyView = view.apply(CheckMode, onwardUrl, subscription, taxYear)(fakeRequest, messages)
      val doc = asDocument(applyView)

      doc.getElementById("continue").text() mustBe messages("cannotClaimYearSpecific.changeButton")
      doc.getElementById("continue").attr("href") mustBe onwardUrl
    }
  }
}
