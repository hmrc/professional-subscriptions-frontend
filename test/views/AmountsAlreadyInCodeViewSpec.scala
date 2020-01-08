/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.routes._
import forms.AmountsAlreadyInCodeFormProvider
import models.NpsDataFormats.npsDataFormatsFormats
import models.TaxYearSelection._
import models.{NormalMode, PSubsByYear, TaxYearSelection}
import pages.{NpsData, SummarySubscriptionsPage}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.AmountsAlreadyInCodeView

class AmountsAlreadyInCodeViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "amountsAlreadyInCode"

  val form = new AmountsAlreadyInCodeFormProvider()(userAnswersCurrentAndPrevious)

  "AmountsAlreadyInCode view" must {

    val application = applicationBuilder(userAnswers = Some(userAnswersCurrentAndPrevious)).build()

    val view = application.injector.instanceOf[AmountsAlreadyInCodeView]

    val npsData = userAnswersCurrentAndPrevious.get(NpsData).get

    val taxYearSelection: Seq[TaxYearSelection] = userAnswersCurrentAndPrevious.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)
      .get.map(year => getTaxYearPeriod(year._1)).toSeq

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, taxYearSelection, npsData)(fakeRequest, messages)

    def taxYearText(taxYear: Int) = {
      messages(s"taxYearSelection.${getTaxYearPeriod(taxYear)}", taxYear.toString, (taxYear + 1).toString)
    }

    application.stop()

    behave like normalPage(applyView(form), messageKeyPrefix, Some("multiple"))

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(
      form = form,
      createView = applyView,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = AmountsAlreadyInCodeController.onSubmit(NormalMode).url,
      legendLabel = Some("amountsAlreadyInCode.label.multiple"),
      messageKeySuffix = Some("multiple")
    )

    "have correct content" in {
      val doc = asDocument(applyView(form))

      val taxYears: Seq[TaxYearSelection] = userAnswersCurrentAndPrevious.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)
        .get.map(year => getTaxYearPeriod(year._1)).toSeq

      val npsData = userAnswersCurrentAndPrevious.get(NpsData).get

      taxYears.map(
        taxYear => {
          assert(doc.getElementById(taxYear.toString).text() == taxYearText(getTaxYear(taxYear)))
          assert(doc.getElementById(s"${taxYear.toString}-amount").text() ==  messages(messageKeyPrefix + ".tableHeading2", "£" + npsData(getTaxYear(taxYear))))
        }
      )
    }
  }
}
