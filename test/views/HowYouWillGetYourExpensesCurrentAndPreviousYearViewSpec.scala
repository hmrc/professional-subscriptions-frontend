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

import models.TaxYearSelection.taxYearSeq
import play.twirl.api.HtmlFormat
import views.behaviours.NewViewBehaviours
import views.html.HowYouWillGetYourExpensesCurrentAndPreviousYearView

class HowYouWillGetYourExpensesCurrentAndPreviousYearViewSpec extends NewViewBehaviours {

  "HowYouWillGetYourExpenses view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentAndPreviousYearView]

    def createView(hasClaimAmountIncreased: Boolean = true): HtmlFormat.Appendable = {
      view.apply("", currentYearMinus1Selected = true, hasClaimIncreased = hasClaimAmountIncreased)(fakeRequest, messages)
    }

    application.stop()

    behave like normalPage(createView(), "howYouWillGetYourExpenses")

    behave like pageWithBackLink(createView())

    "does show paragraph when CY-1 is selected" in {
      val doc = asDocument(createView())

      val wantedMessage = messages("howYouWillGetYourExpenses.para4", taxYearSeq(1).apply(0), taxYearSeq(1).apply(1))

      assertContainsText(doc, wantedMessage)
    }
    
    "does not show paragraph when CY-1 is not selected" in {
      val doc = asDocument(createView())

      val unwantedMessage = messages("howYouWillGetYourExpenses.para4", taxYearSeq(2).apply(0), taxYearSeq(2).apply(1))

      assertDoesntContainText(doc, unwantedMessage)
    }

    "show the correct content" when {

      "claim amount has increased" in {
        val doc = asDocument(createView())

        assertContainsMessages(doc,
          "howYouWillGetYourExpenses.para1.increased",
          "howYouWillGetYourExpensesCurrent.item1.less"
        )
      }

      "claim amount has decreased" in {
        val doc = asDocument(createView(hasClaimAmountIncreased = false))

        assertContainsMessages(doc,
          "howYouWillGetYourExpenses.para1.decreased",
          "howYouWillGetYourExpensesCurrent.item1.more"
        )
      }
    }
  }
}
