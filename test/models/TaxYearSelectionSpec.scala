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

package models

import generators.{Generators, ModelGenerators}
import models.TaxYearSelection._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{OptionValues}
import play.api.libs.json.{JsError, JsString, Json}
import uk.gov.hmrc.time.TaxYear
import viewmodels.RadioCheckboxOption
import base.SpecBase
import org.scalatest.matchers.must.Matchers

class TaxYearSelectionSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks with OptionValues with Generators with ModelGenerators {

  "TaxYearSelection" must {

    "deserialise valid values" in {

      val gen = arbitrary[TaxYearSelection]

      forAll(gen) {
        taxYearSelection =>

          JsString(taxYearSelection.toString).validate[TaxYearSelection].asOpt.value mustEqual taxYearSelection
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TaxYearSelection.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TaxYearSelection] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = arbitrary[TaxYearSelection]

      forAll(gen) {
        taxYearSelection =>

          Json.toJson(taxYearSelection) mustEqual JsString(taxYearSelection.toString)
      }
    }

    "return next years tax year in 'YYYY' format" in {
      val taxYear = NextYear

      getTaxYear(taxYear) mustBe TaxYear.current.next.startYear
    }

    "return current tax year in 'YYYY' format " in {
      val taxYear = CurrentYear

      getTaxYear(taxYear) mustBe TaxYear.current.startYear

    }

    "return current year minus 1 tax year in 'YYYY' format " in {
      val taxYear = CurrentYearMinus1

      getTaxYear(taxYear) mustBe TaxYear.current.back(1).startYear
    }

    "return current year minus 2 tax year in 'YYYY' format " in {
      val taxYear = CurrentYearMinus2

      getTaxYear(taxYear) mustBe TaxYear.current.back(2).startYear
    }

    "return current year minus 3 tax year in 'YYYY' format " in {
      val taxYear = CurrentYearMinus3

      getTaxYear(taxYear) mustBe TaxYear.current.back(3).startYear
    }

    "return current year minus 4 tax year in 'YYYY' format " in {
      val taxYear = CurrentYearMinus4

      getTaxYear(taxYear) mustBe TaxYear.current.back(4).startYear
    }

    "return a sequence of RadioCheckboxOption from options" in {
      val taxYearOptions: Seq[RadioCheckboxOption] = TaxYearSelection.options

      taxYearOptions.head.message.string mustBe s"6 April ${TaxYear.current.startYear} to 5 April ${TaxYear.current.finishYear} (the current tax year)"
      taxYearOptions(1).message.string mustBe s"6 April ${TaxYear.current.back(1).startYear} to 5 April ${TaxYear.current.back(1).finishYear}"
      taxYearOptions(2).message.string mustBe s"6 April ${TaxYear.current.back(2).startYear} to 5 April ${TaxYear.current.back(2).finishYear}"
      taxYearOptions(3).message.string mustBe s"6 April ${TaxYear.current.back(3).startYear} to 5 April ${TaxYear.current.back(3).finishYear}"
      taxYearOptions(4).message.string mustBe s"6 April ${TaxYear.current.back(4).startYear} to 5 April ${TaxYear.current.back(4).finishYear}"
    }

    "return the correct values" in {
      val taxYearValues: Seq[TaxYearSelection] = TaxYearSelection.values

      taxYearValues.head mustBe CurrentYear
      taxYearValues(1) mustBe CurrentYearMinus1
      taxYearValues(2) mustBe CurrentYearMinus2
      taxYearValues(3) mustBe CurrentYearMinus3
      taxYearValues(4) mustBe CurrentYearMinus4
    }

    "filter a value from TaxYearSelection" in {
      val taxYearSelection = Seq(CurrentYear, CurrentYearMinus1, CurrentYearMinus2, CurrentYearMinus3, CurrentYearMinus4)
      val yearToFilter = TaxYearSelection.getTaxYear(CurrentYear).toString

      val filteredTaxYearSelection = filterSelectedTaxYear(taxYearSelection, yearToFilter)

      filteredTaxYearSelection mustBe Seq(CurrentYearMinus1, CurrentYearMinus2, CurrentYearMinus3, CurrentYearMinus4)
    }

    "return an empty sequence when filtering the last item from TaxYearSelection" in {
      val taxYearSelection = Seq(CurrentYear)
      val yearToFilter = TaxYearSelection.getTaxYear(CurrentYear).toString

      val filteredTaxYearSelection = filterSelectedTaxYear(taxYearSelection, yearToFilter)

      filteredTaxYearSelection mustBe Seq.empty
    }

    "return a list of filtered TaxYearSelections removing tax years that contain duplicate psubs" in {

      val psubsByYear: Map[Int, Seq[PSub]] = Map(
        getTaxYear(CurrentYear)       -> Seq(PSub("test1", 100, false, None)),
        getTaxYear(CurrentYearMinus1) -> Seq(PSub("test2", 100, false, None), PSub("test1", 100, false, None)),
        getTaxYear(CurrentYearMinus2) -> Seq(PSub("test2", 100, false, None)),
        getTaxYear(CurrentYearMinus3) -> Seq.empty
      )

      val taxYearSelection = Seq(CurrentYear, CurrentYearMinus1, CurrentYearMinus2, CurrentYearMinus3)
      val yearToDuplicate = getTaxYear(CurrentYear).toString
      val indexToDuplicate = 0

      val filterTaxYearSelection = filterDuplicateSubTaxYears(psubsByYear, taxYearSelection, yearToDuplicate, indexToDuplicate)

      filterTaxYearSelection mustBe Seq(CurrentYearMinus2, CurrentYearMinus3)
    }

    "return a list of filtered TaxYearSelections removing tax years that are before the tax year in the professional body" in {

      val psubs: Map[Int, Seq[PSub]] = Map(
        getTaxYear(CurrentYear) -> Seq(PSub("psub", 100, false, None))
      )

      val psubToCheckYear = getTaxYear(CurrentYear).toString
      val psubToCheckIndex = 0

      val professionalBodies = Seq(ProfessionalBody("psub", Nil, Some(getTaxYear(CurrentYear))))
      val taxYearSelection = Seq(CurrentYear, CurrentYearMinus1, CurrentYearMinus2)

      filterYearSpecific(psubs, professionalBodies, taxYearSelection, psubToCheckYear, psubToCheckIndex) mustBe Seq(CurrentYear)
    }

    "return a list of TaxYearSelections when there is no year specified in the professional body" in {

      val psubs: Map[Int, Seq[PSub]] = Map(
        getTaxYear(CurrentYear) -> Seq(PSub("psub", 100, false, None))
      )

      val psubToCheckYear = getTaxYear(CurrentYear).toString
      val psubToCheckIndex = 0

      val professionalBodies = Seq(ProfessionalBody("psub", Nil, None))
      val taxYearSelection = Seq(CurrentYear, CurrentYearMinus1, CurrentYearMinus2)

      filterYearSpecific(psubs, professionalBodies, taxYearSelection, psubToCheckYear, psubToCheckIndex) mustBe taxYearSelection
    }

    "return a sequence of checkboxOptions while removing the selected tax year" in {

      val psubs: Map[Int, Seq[PSub]] = Map(
        getTaxYear(CurrentYear)       -> Seq(PSub("psub1", 100, false, None)),
        getTaxYear(CurrentYearMinus1) -> Seq(PSub("psub2", 100, false, None)),
        getTaxYear(CurrentYearMinus2) -> Seq(PSub("psub3", 100, false, None)),
        getTaxYear(CurrentYearMinus3) -> Seq(PSub("psub4", 100, false, None)),
        getTaxYear(CurrentYearMinus4) -> Seq(PSub("psub5", 100, false, None))
      )

      val professionalBodies = Seq(ProfessionalBody("psub1", Nil, None))
      val psubToCheckYear = getTaxYear(CurrentYear).toString
      val psubToCheckIndex = 0

      val result = createDuplicateCheckbox(psubs, professionalBodies, psubToCheckYear, psubToCheckIndex)

      result.checkboxOption mustBe getTaxYearCheckboxOptions(Seq(CurrentYearMinus1, CurrentYearMinus2, CurrentYearMinus3, CurrentYearMinus4))
      result.hasDuplicateTaxYear mustBe false
      result.hasInvalidTaxYears mustBe false
    }

    "return a sequence of checkboxOptions while removing the selected tax year and any duplicate tax years" in {

      val psubs: Map[Int, Seq[PSub]] = Map(
        getTaxYear(CurrentYear)       -> Seq(PSub("psub1", 100, false, None)),
        getTaxYear(CurrentYearMinus1) -> Seq(PSub("psub1", 100, false, None)),
        getTaxYear(CurrentYearMinus2) -> Seq(PSub("psub2", 100, false, None))
      )

      val professionalBodies = Seq(ProfessionalBody("psub1", Nil, None))
      val psubToCheckYear = getTaxYear(CurrentYear).toString
      val psubToCheckIndex = 0

      val result = createDuplicateCheckbox(psubs, professionalBodies, psubToCheckYear, psubToCheckIndex)

      result.checkboxOption mustBe getTaxYearCheckboxOptions(Seq(CurrentYearMinus2))
      result.hasDuplicateTaxYear mustBe true
      result.hasInvalidTaxYears mustBe false
    }

    "return a sequence of checkboxOptions while removing the selected tax year and tax years before the professional body year" in {

      val psubs: Map[Int, Seq[PSub]] = Map(
        getTaxYear(CurrentYear)       -> Seq(PSub("psub1", 100, false, None)),
        getTaxYear(CurrentYearMinus1) -> Seq(PSub("psub2", 100, false, None)),
        getTaxYear(CurrentYearMinus2) -> Seq(PSub("psub3", 100, false, None)),
        getTaxYear(CurrentYearMinus3) -> Seq(PSub("psub4", 100, false, None)),
        getTaxYear(CurrentYearMinus4) -> Seq(PSub("psub5", 100, false, None))
      )

      val professionalBodies = Seq(ProfessionalBody("psub1", Nil, Some(getTaxYear(CurrentYearMinus2))))
      val psubToCheckYear = getTaxYear(CurrentYear).toString
      val psubToCheckIndex = 0

      val result = createDuplicateCheckbox(psubs, professionalBodies, psubToCheckYear, psubToCheckIndex)

      result.checkboxOption mustBe getTaxYearCheckboxOptions(Seq(CurrentYearMinus1, CurrentYearMinus2))
      result.hasDuplicateTaxYear mustBe false
      result.hasInvalidTaxYears mustBe true
    }

    "return a sequence of checkboxOptions while removing the selected tax year, any duplicates and any tax years before the professional body year" in {

      val psubs: Map[Int, Seq[PSub]] = Map(
        getTaxYear(CurrentYear)       -> Seq(PSub("psub1", 100, false, None)),
        getTaxYear(CurrentYearMinus1) -> Seq(PSub("psub1", 100, false, None)),
        getTaxYear(CurrentYearMinus2) -> Seq(PSub("psub3", 100, false, None)),
        getTaxYear(CurrentYearMinus3) -> Seq(PSub("psub4", 100, false, None)),
        getTaxYear(CurrentYearMinus4) -> Seq(PSub("psub5", 100, false, None))
      )

      val professionalBodies = Seq(ProfessionalBody("psub1", Nil, Some(getTaxYear(CurrentYearMinus2))))
      val psubToCheckYear = getTaxYear(CurrentYear).toString
      val psubToCheckIndex = 0

      val result = createDuplicateCheckbox(psubs, professionalBodies, psubToCheckYear, psubToCheckIndex)

      result.checkboxOption mustBe getTaxYearCheckboxOptions(Seq(CurrentYearMinus2))
      result.hasDuplicateTaxYear mustBe true
      result.hasInvalidTaxYears mustBe true
    }
    "return a string of showing the current tax year" in {

      taxYearString(0) mustBe s"6 April ${getTaxYear(CurrentYear)} to 5 April ${getTaxYear(CurrentYear) + 1}"
      taxYearString(1) mustBe s"6 April ${getTaxYear(CurrentYearMinus1)} to 5 April ${getTaxYear(CurrentYearMinus1) + 1}"
      taxYearString(2) mustBe s"6 April ${getTaxYear(CurrentYearMinus2)} to 5 April ${getTaxYear(CurrentYearMinus2) + 1}"
      taxYearString(3) mustBe s"6 April ${getTaxYear(CurrentYearMinus3)} to 5 April ${getTaxYear(CurrentYearMinus3) + 1}"
      taxYearString(4) mustBe s"6 April ${getTaxYear(CurrentYearMinus4)} to 5 April ${getTaxYear(CurrentYearMinus4) + 1}"
    }
  }
}
