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

import forms.ExpensesEmployerPaidFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.NewIntViewBehaviours
import views.html.ExpensesEmployerPaidView

class ExpensesEmployerPaidViewSpec extends NewIntViewBehaviours {
  private val messageKeyPrefix = "expensesEmployerPaid"
  private val validSubscription = "Test Subscription"

  val form = new ExpensesEmployerPaidFormProvider(frontendAppConfig)()

  "ExpensesEmployerPaidView view" must {
    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
    val view = application.injector.instanceOf[ExpensesEmployerPaidView]

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, validSubscription, taxYear, index)(fakeRequest, messages)

    application.stop()

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithBodyText(applyView(form),
      messages("expensesEmployerPaid.paragraph1", validSubscription),
      "expensesEmployerPaid.paragraph2"
    )

    "behave like a page with an integer value field" when {
      "rendered" must {

        "contain a label for the value" in {
          val doc = asDocument(applyView(form))
          assertContainsLabel(doc, "value", messages(s"$messageKeyPrefix.title"))
        }

        "contain an input for the value" in {
          val doc = asDocument(applyView(form))
          assertRenderedById(doc, "value")
        }

        "show error in the title" in {
          val doc = asDocument(applyView(form.withError(error)))
          doc.title.contains("Error: ") mustBe true
        }
      }

      "rendered with a valid form" must {
        "include the form's value in the value input" in {
          val doc = asDocument(applyView(form.fill(number)))
          doc.getElementById("value").attr("value") mustBe number.toString
        }
      }

      "contain the '£' symbol" in {
        val doc = asDocument(applyView(form))
        doc.select(".govuk-input__prefix").text mustBe "£"
      }

      "show an error prefix in the browser title" in {
        val doc = asDocument(applyView(form.withError(error)))
        assertEqualsValue(
          doc = doc,
          cssSelector = "title",
          expectedValue = s"""${messages("error.browser.title.prefix")} ${messages(s"$messageKeyPrefix.title")} – ${messages("service.name")} – ${messages("site.gov.uk")}"""
        )
      }
    }
  }
}
