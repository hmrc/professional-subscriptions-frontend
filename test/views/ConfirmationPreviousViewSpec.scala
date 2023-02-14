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

import models.Address
import models.TaxYearSelection._
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import views.behaviours.NewViewBehaviours
import views.html.ConfirmationPreviousView

class ConfirmationPreviousViewSpec extends NewViewBehaviours {

  val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  "ConfirmationPreviousView" must {

    val view = application.injector.instanceOf[ConfirmationPreviousView]
    def applyView(
                  currentYearMinus1: Boolean = true,
                  address: Option[Address] = Some(validAddress),
                  updateAddressUrl: String = "addressURL"
                 )(fakeRequest: FakeRequest[AnyContent], messages: Messages): Html =
      view.apply(currentYearMinus1, address, "addressURL")(fakeRequest, messages)

    val viewWithAnswers = applyView()(fakeRequest, messages)

    def normalPage(view: HtmlFormat.Appendable,
                   messageKeyPrefix: String,
                   messageKeySuffix: Option[String] = None): Unit = {

      "behave like a normal page" when {

        "rendered" must {

          "have the correct banner title" in {

            val doc = asDocument(view)
            assertRenderedByCssSelector(doc, ".hmrc-header__service-name")
          }

          "display the correct browser title" in {

            val doc = asDocument(view)
            assertEqualsMessage(
              doc = doc,
              cssSelector = "title",
              expectedMessageKey =
                if (messageKeySuffix.isEmpty) s"${messages(s"$messageKeyPrefix.title")} – ${messages("service.name")} – ${messages("site.gov.uk")}"
                else s"${messages(s"$messageKeyPrefix.title.${messageKeySuffix.get}")} – ${messages("service.name")} – ${messages("site.gov.uk")}"
            )
          }

          "display the correct heading" in {
            val doc = asDocument(view)
            assertRenderedByCssSelector(doc, "h1.govuk-panel__title")
          }

          "display language toggles" in {

            val doc = asDocument(view)
            assertRenderedByCssSelector(doc, ".hmrc-language-select")
          }
        }
      }
    }

    "display correct static text" in {

      val doc = asDocument(viewWithAnswers)

      assertContainsMessages(doc,
        "confirmation.heading",
        "confirmation.whatHappensNext",
        "confirmation.confirmationLetter"
      )
    }

    "not display currentYearMinusOneDelay when currentYearMinus1 is false" in {

      val doc = asDocument(applyView(currentYearMinus1 = false)(fakeRequest, messages))

      assertDoesntContainText(doc,
        messages("confirmation.currentYearMinusOneDelay")
      )
    }

    "display address" in {

      val doc = asDocument(viewWithAnswers)

      assertRenderedById(doc, "citizenDetailsAddress")
    }

    "display correct content when no address" in {

      val doc = asDocument(applyView(address = None)(fakeRequest, messages))

      assertNotRenderedById(doc, "citizenDetailsAddress")
      assertRenderedById(doc, "no-address")
    }
  }

  application.stop()
}
