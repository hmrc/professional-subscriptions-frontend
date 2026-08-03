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

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.*
import pages.*

trait PageGenerators {

  given Arbitrary[DuplicateClaimYearSelectionPage.type] =
    Arbitrary(DuplicateClaimYearSelectionPage)

  given Arbitrary[DuplicateClaimForOtherYearsPage.type] =
    Arbitrary(DuplicateClaimForOtherYearsPage)

  given Arbitrary[ReEnterAmountsPage.type] =
    Arbitrary(ReEnterAmountsPage)

  given Arbitrary[AmountsAlreadyInCodePage.type] =
    Arbitrary(AmountsAlreadyInCodePage)

  given Arbitrary[RemoveSubscriptionPage.type] =
    Arbitrary(RemoveSubscriptionPage)

  given Arbitrary[TaxYearSelectionPage.type] =
    Arbitrary(TaxYearSelectionPage)

  given Arbitrary[EmployerContributionPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield EmployerContributionPage(year, index)
    }

  given Arbitrary[YourEmployerPage.type] =
    Arbitrary(YourEmployerPage)

  given Arbitrary[YourAddressPage.type] =
    Arbitrary(YourAddressPage)

  given Arbitrary[WhichSubscriptionPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield WhichSubscriptionPage(year, index)
    }

  given Arbitrary[SubscriptionAmountPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield SubscriptionAmountPage(year, index)
    }

  given Arbitrary[ExpensesEmployerPaidPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield ExpensesEmployerPaidPage(year, index)
    }

  given Arbitrary[SummarySubscriptionsPage.type] =
    Arbitrary(SummarySubscriptionsPage)

  given Arbitrary[PSubPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield PSubPage(year, index)
    }

}
