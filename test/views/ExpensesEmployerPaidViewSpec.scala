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
import forms.ExpensesEmployerPaidFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.IntViewBehaviours
import views.html.ExpensesEmployerPaidView

class ExpensesEmployerPaidViewSpec extends IntViewBehaviours {

  val messageKeyPrefix = "expensesEmployerPaid"
  val validSubscription = "Test Subscription"

  val form = new ExpensesEmployerPaidFormProvider()()

  "ExpensesEmployerPaidView view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[ExpensesEmployerPaidView]

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, validSubscription, taxYear, index)(fakeRequest, messages)

    application.stop()

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like intPage(form, applyView, messageKeyPrefix, routes.ExpensesEmployerPaidController.onSubmit(NormalMode, taxYear, index).url)

    behave like pageWithBodyText(applyView(form),
      "expensesEmployerPaid.paragraph1",
      messages("expensesEmployerPaid.paragraph2", validSubscription)
    )

    "contain the '£' symbol" in {
      val doc = asDocument(applyView(form))
      doc.getElementsByClass("govuk-currency-input__inner__unit").text mustBe "£"
    }
  }
}
