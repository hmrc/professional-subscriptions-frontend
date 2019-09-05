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

import models.Address
import models.TaxYearSelection._
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.ConfirmationPreviousView

class ConfirmationPreviousViewSpec extends ViewBehaviours {

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

    behave like normalPage(viewWithAnswers, "confirmation")

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
