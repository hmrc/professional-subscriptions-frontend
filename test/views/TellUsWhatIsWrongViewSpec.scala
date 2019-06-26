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

import forms.TellUsWhatIsWrongFormProvider
import models.TaxYearSelection.CurrentYear
import models.{EmploymentExpense, NormalMode, TaxYearSelection}
import pages.{NpsData, TaxYearSelectionPage, TellUsWhatIsWrongPage}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.RadioCheckboxOption
import views.behaviours.CheckboxTableViewBehaviours
import views.html.TellUsWhatIsWrongView

class TellUsWhatIsWrongViewSpec extends CheckboxTableViewBehaviours[TaxYearSelection] {

  val ua = someUserAnswers.set(TellUsWhatIsWrongPage, Seq(CurrentYear)).success.value

  val application = applicationBuilder(userAnswers = Some(ua)).build()

  val form = new TellUsWhatIsWrongFormProvider()()

  val npsData: Map[String, Seq[EmploymentExpense]] = ua.get(NpsData).get

  val taxYearSelection: Seq[TaxYearSelection] = ua.get(TaxYearSelectionPage).get

  val sortedNpsDataAsSeq: Seq[Seq[EmploymentExpense]] = npsData.toSeq.sortWith(_._1 > _._1).map(_._2)

  def applyView(form: Form[Seq[TaxYearSelection]]): HtmlFormat.Appendable =
    application.injector.instanceOf[TellUsWhatIsWrongView].apply(form, NormalMode, taxYearSelection, sortedNpsDataAsSeq)(fakeRequest, messages)

  val messageKeyPrefix = "tellUsWhatIsWrong"

  val options: Seq[RadioCheckboxOption] = TaxYearSelection.options

  "TellUsWhatIsWrongView" must {

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like checkboxTablePage(ua, form, applyView, messageKeyPrefix)
  }

  application.stop()
}
