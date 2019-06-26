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

sealed trait TellUsWhatIsWrong

object TellUsWhatIsWrong extends Enumerable.Implicits {

  case object Taxyeartochange extends WithName("taxYearToChange") with TellUsWhatIsWrong
  case object Taxyeartochange2 extends WithName("taxYearToChange2") with TellUsWhatIsWrong

  val values: Set[TellUsWhatIsWrong] = Set(
    Taxyeartochange, Taxyeartochange2
  )

  val options: Set[RadioCheckboxOption] = values.map {
    value =>
      RadioCheckboxOption("tellUsWhatIsWrong", value.toString)
  }

  implicit val enumerable: Enumerable[TellUsWhatIsWrong] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)
}
