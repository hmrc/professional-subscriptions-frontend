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

package models

import uk.gov.hmrc.time.TaxYear
import viewmodels.RadioCheckboxOption

sealed trait TaxYearSelection

object TaxYearSelection extends Enumerable.Implicits {

  case object NextYear extends WithName("nextYear") with TaxYearSelection

  case object CurrentYear extends WithName("currentYear") with TaxYearSelection

  case object CurrentYearMinus1 extends WithName("currentYearMinus1") with TaxYearSelection

  case object CurrentYearMinus2 extends WithName("currentYearMinus2") with TaxYearSelection

  case object CurrentYearMinus3 extends WithName("currentYearMinus3") with TaxYearSelection

  case object CurrentYearMinus4 extends WithName("currentYearMinus4") with TaxYearSelection

  val values: Seq[TaxYearSelection] = Seq(
    CurrentYear,
    CurrentYearMinus1,
    CurrentYearMinus2,
    CurrentYearMinus3,
    CurrentYearMinus4
  )

  val options: Seq[RadioCheckboxOption] = Seq(
    taxYearCheckboxOption(TaxYear.current, CurrentYear),
    taxYearCheckboxOption(TaxYear.current.back(1), CurrentYearMinus1),
    taxYearCheckboxOption(TaxYear.current.back(2), CurrentYearMinus2),
    taxYearCheckboxOption(TaxYear.current.back(3), CurrentYearMinus3),
    taxYearCheckboxOption(TaxYear.current.back(4), CurrentYearMinus4)
  )

  def getTaxYearCheckboxOptions(taxYearSelection: Seq[TaxYearSelection]): Seq[RadioCheckboxOption] = {
    taxYearSelection.map {
      case CurrentYear => taxYearCheckboxOption(TaxYear.current, CurrentYear)
      case CurrentYearMinus1 => taxYearCheckboxOption(TaxYear.current.back(1), CurrentYearMinus1)
      case CurrentYearMinus2 => taxYearCheckboxOption(TaxYear.current.back(2), CurrentYearMinus2)
      case CurrentYearMinus3 => taxYearCheckboxOption(TaxYear.current.back(3), CurrentYearMinus3)
      case CurrentYearMinus4 => taxYearCheckboxOption(TaxYear.current.back(4), CurrentYearMinus4)
      case _ => throw new IllegalArgumentException("Invalid tax year selected")
    }
  }

  def getTaxYear(year: TaxYearSelection): Int = year match {
    case NextYear => TaxYear.current.next.startYear
    case CurrentYear => TaxYear.current.startYear
    case CurrentYearMinus1 => TaxYear.current.back(1).startYear
    case CurrentYearMinus2 => TaxYear.current.back(2).startYear
    case CurrentYearMinus3 => TaxYear.current.back(3).startYear
    case CurrentYearMinus4 => TaxYear.current.back(4).startYear
  }

  def taxYearString(yearsBack: Int): String = {
    val start: String = TaxYear.current.back(yearsBack).starts.toString("d MMMM yyyy")
    val end: String = TaxYear.current.back(yearsBack).finishes.toString("d MMMM yyyy")

    s"$start to $end"
  }

  def getTaxYearPeriod(year: Int): TaxYearSelection = {

    val currentYear = TaxYear.current.startYear
    val currentYearMinus1 = TaxYear.current.back(1).startYear
    val currentYearMinus2 = TaxYear.current.back(2).startYear
    val currentYearMinus3 = TaxYear.current.back(3).startYear
    val currentYearMinus4 = TaxYear.current.back(4).startYear

    year match {
      case `currentYear` => CurrentYear
      case `currentYearMinus1` => CurrentYearMinus1
      case `currentYearMinus2` => CurrentYearMinus2
      case `currentYearMinus3` => CurrentYearMinus3
      case `currentYearMinus4` => CurrentYearMinus4
      case _ => throw new IllegalArgumentException("Invalid tax year selected")
    }
  }

  def filterSelectedTaxYear(taxYearSelection: Seq[TaxYearSelection], claimYear: String): Seq[TaxYearSelection] = {
    taxYearSelection.filterNot(_ == getTaxYearPeriod(claimYear.toInt))
  }

  def filterDuplicateSubTaxYears(
                                  psubsByYear: Map[Int, Seq[PSub]],
                                  taxYearSelection: Seq[TaxYearSelection],
                                  yearToDuplicate: String,
                                  indexToDuplicate: Int): Seq[TaxYearSelection] = {

    val psubToCheck = psubsByYear(yearToDuplicate.toInt)(indexToDuplicate)

    val duplicatedTaxYears =
      psubsByYear.filter {
        _._2.exists(_.name == psubToCheck.name)
      }.map(filteredPSubByYear => getTaxYearPeriod(filteredPSubByYear._1)).toSeq

    taxYearSelection.filterNot(duplicatedTaxYears.contains(_))
  }

  def filterYearSpecific(
                          psubsByYear: Map[Int, Seq[PSub]],
                          professionalBodies: Seq[ProfessionalBody],
                          taxYearSelection: Seq[TaxYearSelection],
                          year: String,
                          index: Int): Seq[TaxYearSelection] = {


    val psubToCheck: PSub = psubsByYear(year.toInt)(index)
    val getStartYear = professionalBodies.filter(_.name == psubToCheck.name).head.startYear

    getStartYear match {
      case Some(startYear) =>
        taxYearSelection.filter(taxYearSelection => getTaxYear(taxYearSelection) >= startYear)
      case _ =>
        taxYearSelection
    }
  }

  private def taxYearCheckboxOption(taxYear: TaxYear, option: TaxYearSelection) =
    RadioCheckboxOption(
      keyPrefix = "taxYearSelection",
      option = s"$option",
      messageArgs = Seq(taxYear.startYear.toString.format("YYYY"), taxYear.finishYear.toString.format("YYYY")): _*
    )

  def createDuplicateCheckbox(
                               psubsByYear: Map[Int, Seq[PSub]],
                               allProfessionalBodies: Seq[ProfessionalBody],
                               year: String,
                               index: Int): CreateDuplicateCheckbox = {

    val orderedTaxYears = PSubsByYear.orderTaxYears(psubsByYear)
    val filterSelectedTaxYears: Seq[TaxYearSelection] = filterSelectedTaxYear(orderedTaxYears, year)
    val filterDuplicatedTaxYears: Seq[TaxYearSelection] = filterDuplicateSubTaxYears(psubsByYear, filterSelectedTaxYears, year, index)
    val filterInvalidTaxYears: Seq[TaxYearSelection] = filterYearSpecific(psubsByYear, allProfessionalBodies, filterDuplicatedTaxYears, year, index)

    val hasDuplicateTaxYears: Boolean = filterDuplicatedTaxYears.length < filterSelectedTaxYears.length
    val hasInvalidTaxYears: Boolean = filterInvalidTaxYears.length < filterDuplicatedTaxYears.length

    CreateDuplicateCheckbox(getTaxYearCheckboxOptions(filterInvalidTaxYears), hasDuplicateTaxYears, hasInvalidTaxYears)
  }

  implicit val enumerable: Enumerable[TaxYearSelection] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
