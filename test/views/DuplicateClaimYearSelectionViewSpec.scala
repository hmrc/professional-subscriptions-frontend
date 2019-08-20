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

import forms.DuplicateClaimYearSelectionFormProvider
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1}
import models.{CreateDuplicateCheckBox, NormalMode, TaxYearSelection, WithName}
import play.api.Application
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.CheckboxViewBehaviours
import views.html.DuplicateClaimYearSelectionView

class DuplicateClaimYearSelectionViewSpec extends CheckboxViewBehaviours[TaxYearSelection] {

  val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  val form = new DuplicateClaimYearSelectionFormProvider()()

  private val taxYearSelection: Seq[WithName with TaxYearSelection] = Seq(CurrentYear, CurrentYearMinus1)
  private val checkboxOptions = TaxYearSelection.getTaxYearCheckboxOptions(taxYearSelection)
  private val duplicateTaxYearCheckbox = CreateDuplicateCheckBox(checkboxOptions, hasDuplicateTaxYear = false, hasInvalidTaxYears = false)

  def applyView(form: Form[Seq[TaxYearSelection]]): HtmlFormat.Appendable =
    application.injector.instanceOf[DuplicateClaimYearSelectionView].apply(form, NormalMode, duplicateTaxYearCheckbox, taxYear, index)(fakeRequest, messages)

  val messageKeyPrefix = "duplicateClaimYearSelection"

  "DuplicateClaimYearSelectionView" must {

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like checkboxPage(form, applyView, messageKeyPrefix, duplicateTaxYearCheckbox.checkboxOption)
  }

  application.stop()
}
