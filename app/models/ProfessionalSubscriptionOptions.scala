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

package models

sealed trait ProfessionalSubscriptionOptions

object ProfessionalSubscriptionOptions extends Enumerable.Implicits {

  case object PSNoYears extends WithName("freNoYears") with ProfessionalSubscriptionOptions
  case object PSSomeYears extends WithName("freAllYearsAllAmountsDifferentToClaimAmount") with ProfessionalSubscriptionOptions
  case object PSAllYearsAllAmountsSameAsClaimAmount extends WithName("freAllYearsAllAmountsSameAsClaimAmount") with ProfessionalSubscriptionOptions
  case object TechnicalDifficulties extends WithName("technicalDifficulties") with ProfessionalSubscriptionOptions

  val values: Seq[ProfessionalSubscriptionOptions] = Seq(
    PSNoYears,
    PSAllYearsAllAmountsSameAsClaimAmount,
    PSSomeYears,
    TechnicalDifficulties
  )

  implicit val enumerable: Enumerable[ProfessionalSubscriptionOptions] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
