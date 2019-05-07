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

import controllers.routes
import forms.YourAddressFormProvider
import models.NormalMode
import org.jsoup.nodes.Element
import play.api.Application
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.YourAddressView

class YourAddressViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "yourAddress"

  val form = new YourAddressFormProvider()()

  val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  "YourAddress view" must {

    val view = application.injector.instanceOf[YourAddressView]

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, validAddress)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, routes.YourAddressController.onSubmit(NormalMode).url)

    "behave like page with address" in {

      val doc = asDocument(applyView(form))

      val address: Element = doc.getElementById("citizenDetailsAddress")

      address.text must include ("6 Howsell Road Llanddew Line 3 Line 4 Line 5 DN16 3FB GREAT BRITAIN")
    }
  }

  application.stop()

}
