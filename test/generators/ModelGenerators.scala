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

package generators

import models.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {
  self: Generators =>

  given Arbitrary[Address] = Arbitrary {

    for {
      line1    <- arbitrary[Option[String]]
      line2    <- arbitrary[Option[String]]
      line3    <- arbitrary[Option[String]]
      line4    <- arbitrary[Option[String]]
      line5    <- arbitrary[Option[String]]
      postcode <- arbitrary[Option[String]]
      country  <- arbitrary[Option[String]]
    } yield Address(
      line1 = line1,
      line2 = line2,
      line3 = line3,
      line4 = line4,
      line5 = line5,
      postcode = postcode,
      country = country
    )

  }

  given Arbitrary[ProfessionalBody] =
    Arbitrary {
      for {
        name           <- nonEmptyString
        synonyms       <- Gen.listOf(nonEmptyString)
        startYear      <- Gen.option(intsAboveValue(0))
        additionalInfo <- Gen.option(nonEmptyString)
      } yield ProfessionalBody(name, synonyms, startYear, additionalInfo)
    }

  given Arbitrary[TaxYearSelection] =
    Arbitrary {
      Gen.oneOf(TaxYearSelection.values)
    }

  val generatorListOfTaxYearSelection: Gen[Seq[TaxYearSelection]] =
    Gen
      .nonEmptyContainerOf[Set, TaxYearSelection](arbitrary[TaxYearSelection])
      .flatMap(_.toSeq)

  given Arbitrary[TaxCodeStatus] =
    Arbitrary {
      Gen.oneOf(TaxCodeStatus.values)
    }

  given Arbitrary[PSubsByYear] =
    Arbitrary {
      for {
        year <- Gen.choose(0, Int.MaxValue)
        psubs <- Gen
          .listOf(
            for {
              name                       <- arbitrary[String]
              amount                     <- arbitrary[Int]
              employerContributed        <- arbitrary[Boolean]
              employerContributionAmount <- arbitrary[Option[Int]]
            } yield PSub(name, amount, employerContributed, employerContributionAmount)
          )
          .suchThat(_.nonEmpty)
      } yield PSubsByYear(Map(year -> psubs))
    }

  given Arbitrary[PSub] =
    Arbitrary {
      for {
        name                       <- arbitrary[String]
        amount                     <- arbitrary[Int]
        employerContributed        <- arbitrary[Boolean]
        employerContributionAmount <- arbitrary[Option[Int]]
      } yield PSub(name, amount, employerContributed, employerContributionAmount)
    }

}
