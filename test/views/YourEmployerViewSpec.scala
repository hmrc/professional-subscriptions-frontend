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

import base.SpecBase
import controllers.routes
import forms.YourEmployerFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.NewYesNoViewBehaviours
import views.html.YourEmployerView

class YourEmployerViewSpec extends NewYesNoViewBehaviours with SpecBase {

  val messageKeyPrefix    = "yourEmployer"
  private val employments = Seq("HMRC Longbenton")

  val form = new YourEmployerFormProvider()()

  "YourEmployer view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[YourEmployerView]

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, employments)(fakeRequest, messages)

    application.stop()

    behave.like(normalPage(applyView(form), messageKeyPrefix))

    behave.like(pageWithBackLink(applyView(form)))

    behave.like(
      yesNoPage(
        createView = applyView,
        messageKeyPrefix = messageKeyPrefix,
        expectedFormAction = routes.YourEmployerController.onSubmit(NormalMode).url
      )
    )

    behave.like(pageWithBodyText(applyView(form), employments.head))

  }

}
