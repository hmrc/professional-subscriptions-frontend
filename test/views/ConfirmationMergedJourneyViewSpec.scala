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

import play.api.Application
import views.behaviours.ConfirmationViewBehaviours
import views.html.ConfirmationMergedJourneyView

class ConfirmationMergedJourneyViewSpec extends ConfirmationViewBehaviours{

  val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  "ConfirmationMergedJourneyView" must {

    val confirmationMergedJourneyView = application.injector.instanceOf[ConfirmationMergedJourneyView]
    val testUrl = "/test/url"

    val view = confirmationMergedJourneyView(testUrl)(fakeRequest, messages)

    behave like normalPage(view, "confirmation.mergedJourney")

    "display correct static text" in {
      val doc = asDocument(view)

      assertContainsMessages(doc,
        "confirmation.mergedJourney.heading",
        "confirmation.mergedJourney.warning",
        "confirmation.mergedJourney.para.1"
      )
    }

    "display the correct link button with redirect" in {
      val doc = asDocument(view)

      doc.getElementsByClass("govuk-button").text() mustBe messages("site.continue")
      doc.getElementsByClass("govuk-button").attr("href") mustBe testUrl
    }
  }

  application.stop()
}
