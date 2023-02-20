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

import models.{CheckMode, NormalMode}
import views.behaviours.NewViewBehaviours
import views.html.CannotClaimYearSpecificView

class CannotClaimYearSpecificViewSpec extends NewViewBehaviours {

  "CannotClaimYearSpecific view" must {

    val subscription = "psub"
    val onwardUrl = "/url"

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[CannotClaimYearSpecificView]

    val applyView = view.apply(NormalMode, onwardUrl, subscription, taxYearInt)(fakeRequest, messages)

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
  }
}
