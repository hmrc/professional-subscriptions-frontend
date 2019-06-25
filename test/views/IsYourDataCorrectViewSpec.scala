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
import forms.IsYourDataCorrectFormProvider
import models.{EmploymentExpense, NormalMode, TaxYearSelection}
import pages.{NpsData, TaxYearSelectionPage}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.IsYourDataCorrectView
import models.TaxYearSelection._

class IsYourDataCorrectViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "isYourDataCorrect"

  val form = new IsYourDataCorrectFormProvider()()

  "IsYourDataCorrect view" must {

    val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

    val view = application.injector.instanceOf[IsYourDataCorrectView]

    val npsData: Map[String, Seq[EmploymentExpense]] = someUserAnswers.get(NpsData).get

    val taxYearSelection: Seq[TaxYearSelection] = someUserAnswers.get(TaxYearSelectionPage).get

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, taxYearSelection, npsData)(fakeRequest, messages)

    def taxYearText(taxYear: Int) = {
      messages(s"taxYearSelection.${getTaxYearPeriod(taxYear)}", taxYear.toString, (taxYear + 1).toString)
    }

    application.stop()

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, routes.IsYourDataCorrectController.onSubmit(NormalMode).url)

    "have correct content" in {
      val doc = asDocument(applyView(form))

      val taxYears: Seq[TaxYearSelection] = someUserAnswers.get(TaxYearSelectionPage).get

      val npsData: Map[String, Seq[EmploymentExpense]] = someUserAnswers.get(NpsData).get

      taxYears.map(
        taxYear => {
          assert(doc.getElementById(taxYear.toString).text() == taxYearText(getTaxYear(taxYear)))

          if (npsData(getTaxYear(taxYear).toString).nonEmpty) {
            assert(doc.getElementById(s"${taxYear.toString}-amount").text() == "£" + npsData(getTaxYear(taxYear).toString).head.grossAmount.toString)
          } else {
            assert(doc.getElementById(s"${taxYear.toString}-amount").text() == "£0")
          }
        }
      )
    }
  }
}
