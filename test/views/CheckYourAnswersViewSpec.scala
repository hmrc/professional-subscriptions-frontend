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

import utils.CheckYourAnswersHelper
import viewmodels.AnswerSection
import views.behaviours.ViewBehaviours
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewBehaviours {

  "CheckYourAnswers view" must {

    val application = applicationBuilder().build()

    val view = application.injector.instanceOf[CheckYourAnswersView]

    val cyaHelper = new CheckYourAnswersHelper(someUserAnswers)

    val sections = Seq(AnswerSection(headingKey = None, headingClasses = None, subheadingKey = None, rows = Seq()))

    val applyView = view.apply(sections)(fakeRequest, messages)

    behave like normalPage(applyView, "checkYourAnswers")

    behave like pageWithBackLink(applyView)

    "have correct content" in {
      val doc = asDocument(applyView)

      assertContainsMessages(doc, messages(
        "checkYourAnswers.disclaimerHeading",
        "checkYourAnswers.disclaimer",
        "checkYourAnswers.prosecuted"
      ))

      doc.getElementById("submit").text() mustBe messages("checkYourAnswers.submit")
    }
  }

}
