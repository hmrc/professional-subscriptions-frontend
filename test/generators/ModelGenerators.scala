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

package generators

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryTaxYearSelection: Arbitrary[TaxYearSelection] =
    Arbitrary {
      Gen.oneOf(TaxYearSelection.values)
    }

  implicit lazy val arbitraryTaxCodeStatus: Arbitrary[TaxCodeStatus] =
    Arbitrary {
      Gen.oneOf(TaxCodeStatus.values)
    }

  implicit lazy val arbitrarySubscriptions: Arbitrary[PSubYears] =
    Arbitrary {
      for {
        year <- arbitrary[String].suchThat(_.nonEmpty)
        psubs <- Gen.listOf(
          for {
            name <- arbitrary[String]
            amount <- arbitrary[Int]
            employerContributed <- arbitrary[Boolean]
            employerContributionAmount <- arbitrary[Option[Int]]
          } yield PSub(name, amount, employerContributed, employerContributionAmount)
        ).suchThat(_.nonEmpty)
      } yield PSubYears(Map(year -> psubs))
    }

  implicit lazy val arbitraryPSub: Arbitrary[PSub] =
    Arbitrary {
      for {
        name <- arbitrary[String]
        amount <- arbitrary[Int]
        employerContributed <- arbitrary[Boolean]
        employerContributionAmount <- arbitrary[Option[Int]]
      } yield PSub(name, amount, employerContributed, employerContributionAmount)
    }
}
