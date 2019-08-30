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
import forms.WhichSubscriptionFormProvider
import models.{NormalMode, ProfessionalBody}
import play.api.data.{Form, FormError}
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.WhichSubscriptionView

class WhichSubscriptionViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "whichSubscription"
  val subscription = "Law Society"
  val form = new WhichSubscriptionFormProvider()(Nil)

  "WhichSubscriptionView view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[WhichSubscriptionView]

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, Seq(ProfessionalBody(s"$subscription",List(""),None)), taxYear, index)(fakeRequest, messages)

    application.stop()

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    "behave like a page with a string value field" when {

      "rendered" must {

        "contain a label for the value" in {

          val doc = asDocument(applyView(form))
          assertContainsLabel(doc, "subscription", messages(s"$messageKeyPrefix.heading"))
        }

        "contain 2 paragraphs of hint text for the value" in {

          val doc = asDocument(applyView(form))
          doc.getElementById("hint-subscription").text() mustBe messages(s"$messageKeyPrefix.hint1")
        }

        "contain an input for the value" in {

          val doc = asDocument(applyView(form))
          assertRenderedById(doc, "subscription")
        }
      }

      "rendered with a valid form" must {

        "include the form's value in the value input" in {

          val doc = asDocument(applyView(form.fill(s"$subscription")))
          doc.getElementsByAttribute("selected").text() mustBe subscription
        }
      }

      "rendered with an error" must {

        "show an error summary" in {

          val doc = asDocument(applyView(form.withError(error)))
          assertRenderedById(doc, "error-summary-heading")
        }

        "show an error in the value field's label" in {

          val doc = asDocument(applyView(form.withError(FormError("subscription", errorMessage))))
          val errorSpan = doc.getElementsByClass("error-message").first
          errorSpan.text mustBe messages(errorMessage)
        }

        "show an error prefix in the browser title" in {

          val doc = asDocument(applyView(form.withError(error)))
          assertEqualsValue(
            doc = doc,
            cssSelector = "title",
            expectedValue = s"""${messages("error.browser.title.prefix")} ${messages(s"$messageKeyPrefix.title")} - ${frontendAppConfig.serviceTitle}"""
          )
        }
      }
    }
  }
}
