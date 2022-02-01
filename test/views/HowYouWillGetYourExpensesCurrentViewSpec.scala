/*
 * Copyright 2022 HM Revenue & Customs
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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.HowYouWillGetYourExpensesCurrentView

class HowYouWillGetYourExpensesCurrentViewSpec extends ViewBehaviours {

  "HowYouWillGetYourExpenses view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentView]

    def createView(hasClaimAmountIncreased: Boolean = true): HtmlFormat.Appendable = {
      view.apply("", hasClaimAmountIncreased)(fakeRequest, messages)
    }

    application.stop()

    behave like normalPage(createView(), "howYouWillGetYourExpenses")

    behave like pageWithBackLink(createView())


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
