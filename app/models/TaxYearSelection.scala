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

import viewmodels.RadioCheckboxOption

sealed trait TaxYearSelection

object TaxYearSelection extends Enumerable.Implicits {

  case object Test extends WithName("test") with TaxYearSelection
  case object Test2 extends WithName("test2") with TaxYearSelection

  val values: Set[TaxYearSelection] = Set(
    Test, Test2
  )

  val options: Set[RadioCheckboxOption] = values.map {
    value =>
      RadioCheckboxOption("taxYearSelection", value.toString)
  }

  implicit val enumerable: Enumerable[TaxYearSelection] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)
}
