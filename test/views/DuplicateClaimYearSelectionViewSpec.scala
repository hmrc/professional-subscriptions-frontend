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

import forms.DuplicateClaimYearSelectionFormProvider
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1}
import models.{CreateDuplicateCheckbox, NormalMode, TaxYearSelection, WithName}
import play.api.Application
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.NewCheckboxViewBehaviours
import views.html.DuplicateClaimYearSelectionView

class DuplicateClaimYearSelectionViewSpec extends NewCheckboxViewBehaviours[TaxYearSelection] {

  val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  val form = new DuplicateClaimYearSelectionFormProvider()()

  private val taxYearSelection: Seq[WithName with TaxYearSelection] = Seq(CurrentYear, CurrentYearMinus1)
  private val checkboxOptions = TaxYearSelection.getTaxYearCheckboxOptions(taxYearSelection)
  private val duplicateTaxYearCheckbox = CreateDuplicateCheckbox(checkboxOptions, hasDuplicateTaxYear = false, hasInvalidTaxYears = false)

  def applyGenericView(form: Form[Seq[TaxYearSelection]], duplicateTaxYearCheckbox: CreateDuplicateCheckbox): HtmlFormat.Appendable =
    application.injector.instanceOf[DuplicateClaimYearSelectionView].apply(form, NormalMode, duplicateTaxYearCheckbox, taxYear, index)(fakeRequest, messages)

  def applyCheckBoxView(form: Form[Seq[TaxYearSelection]]): HtmlFormat.Appendable =
    application.injector.instanceOf[DuplicateClaimYearSelectionView].apply(form, NormalMode, duplicateTaxYearCheckbox, taxYear, index)(fakeRequest, messages)

  val messageKeyPrefix = "duplicateClaimYearSelection"

  application.stop()

  "DuplicateClaimYearSelectionView" must {

    behave like normalPage(applyGenericView(form, duplicateTaxYearCheckbox), messageKeyPrefix)

    behave like pageWithBackLink(applyGenericView(form, duplicateTaxYearCheckbox))

    behave like checkboxPage(form, applyCheckBoxView, messageKeyPrefix, duplicateTaxYearCheckbox.checkboxOption)


    "display correct content when there has been duplicated tax years" in {
      val duplicateTaxYearCheckbox = CreateDuplicateCheckbox(checkboxOptions, hasDuplicateTaxYear = true, hasInvalidTaxYears = false)

      val doc = asDocument(applyGenericView(form, duplicateTaxYearCheckbox))

      assertContainsMessages(doc,
        "duplicateClaimYearSelection.duplicateTaxYear.cannotBeDuplicated",
        "duplicateClaimYearSelection.duplicateTaxYear.alreadyAdded"
      )

      assertDoesntContainMessages(doc,
        "duplicateClaimYearSelection.invalidTaxYear.notApproved",
        "duplicateClaimYearSelection.duplicateAndInvalid.because",
        "duplicateClaimYearSelection.duplicateAndInvalid.notApproved"
      )

    }

    "display correct content when there has been invalid tax years" in {

      val duplicateTaxYearCheckbox = CreateDuplicateCheckbox(checkboxOptions, hasDuplicateTaxYear = false, hasInvalidTaxYears = true)

      val doc = asDocument(applyGenericView(form, duplicateTaxYearCheckbox))

      assertContainsMessages(doc,
        "duplicateClaimYearSelection.duplicateTaxYear.cannotBeDuplicated",
        "duplicateClaimYearSelection.invalidTaxYear.notApproved"
      )

      assertDoesntContainMessages(doc,
        "duplicateClaimYearSelection.duplicateTaxYear.alreadyAdded",
        "duplicateClaimYearSelection.duplicateAndInvalid.because",
        "duplicateClaimYearSelection.duplicateAndInvalid.alreadyAdded"
      )
    }

    "display correct content when there has been both invalid and duplicate tax years" in {

      val duplicateTaxYearCheckbox = CreateDuplicateCheckbox(checkboxOptions, hasDuplicateTaxYear = true, hasInvalidTaxYears = true)

      val doc = asDocument(applyGenericView(form, duplicateTaxYearCheckbox))

      assertContainsMessages(doc,
        "duplicateClaimYearSelection.duplicateAndInvalid.cannotBeDuplicated",
        "duplicateClaimYearSelection.duplicateAndInvalid.because",
        "duplicateClaimYearSelection.duplicateAndInvalid.alreadyAdded",
        "duplicateClaimYearSelection.duplicateAndInvalid.notApproved"
      )
    }

    "not display duplication and invalid content when both are false" in {

      val duplicateTaxYearCheckbox = CreateDuplicateCheckbox(checkboxOptions, hasDuplicateTaxYear = false, hasInvalidTaxYears = false)

      val doc = asDocument(applyGenericView(form, duplicateTaxYearCheckbox))

      assertDoesntContainMessages(doc,
        "duplicateClaimYearSelection.invalidTaxYear.cannotBeDuplicated",
        "duplicateClaimYearSelection.invalidTaxYear.notApproved",
        "duplicateClaimYearSelection.duplicateTaxYear.cannotBeDuplicated",
        "duplicateClaimYearSelection.duplicateTaxYear.alreadyAdded",
        "duplicateClaimYearSelection.duplicateAndInvalid.cannotBeDuplicated",
        "duplicateClaimYearSelection.duplicateAndInvalid.because",
        "duplicateClaimYearSelection.duplicateAndInvalid.alreadyAdded",
        "duplicateClaimYearSelection.duplicateAndInvalid.notApproved"
      )
    }
  }

}

