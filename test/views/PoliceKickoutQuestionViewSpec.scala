/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.routes
import forms.PoliceKickoutQuestionFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.NewYesNoViewBehaviours
import views.html.PoliceKickoutQuestionView

class PoliceKickoutQuestionViewSpec extends NewYesNoViewBehaviours {

  val messageKeyPrefix = "policeKickoutQuestion"

  val form = new PoliceKickoutQuestionFormProvider()()

  "PoliceKickoutQuestion view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[PoliceKickoutQuestionView]

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, taxYear, index)(fakeRequest, messages)

    application.stop()

    behave.like(pageWithBackLink(applyView(form)))

    behave.like(
      yesNoPage(
        applyView,
        messageKeyPrefix,
        routes.PoliceKickoutQuestionController.onSubmit(NormalMode, taxYear, index).url
      )
    )

    "display page content" in {
      val doc = asDocument(applyView(form))
      assertContainsMessages(doc, "policeKickoutQuestion.title", "policeKickoutQuestion.heading")
    }

  }

}
