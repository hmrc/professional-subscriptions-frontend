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

import base.SpecBase
import generators.{Generators, ModelGenerators}
import models.ProfessionalSubscriptionOptions._
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.OptionValues

class ProfessionalSubscriptionOptionsSpec
    extends SpecBase
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with Generators
    with ModelGenerators {

  "return the correct values" in {
    val psubOptionValues: Seq[ProfessionalSubscriptionOptions] = ProfessionalSubscriptionOptions.values

    psubOptionValues.head mustBe PSNoYears
    psubOptionValues(1) mustBe PSAllYearsAllAmountsSameAsClaimAmount
    psubOptionValues(2) mustBe PSSomeYears
    psubOptionValues(3) mustBe TechnicalDifficulties
  }

}
