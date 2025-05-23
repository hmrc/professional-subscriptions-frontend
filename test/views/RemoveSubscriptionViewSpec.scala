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

import controllers.routes._
import forms.RemoveSubscriptionFormProvider
import models.NormalMode
import pages.PSubPage
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.NewYesNoViewBehaviours
import views.html.RemoveSubscriptionView

class RemoveSubscriptionViewSpec extends NewYesNoViewBehaviours {

  val messageKeyPrefix = "removeSubscription"

  val form = new RemoveSubscriptionFormProvider()()

  "RemoveSubscription view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[RemoveSubscriptionView]

    val subscription = userAnswersCurrentAndPrevious.get(PSubPage(taxYear, 0)).get

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, taxYear, 0, subscription.nameOfProfessionalBody)(fakeRequest, messages)

    application.stop()

    behave.like(normalPage(applyView(form), messageKeyPrefix))

    behave.like(pageWithBackLink(applyView(form)))

    behave.like(yesNoPage(applyView, messageKeyPrefix, RemoveSubscriptionController.onSubmit(taxYear, 0).url))
  }

}
