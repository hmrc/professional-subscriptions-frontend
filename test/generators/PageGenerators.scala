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
import org.scalacheck.Arbitrary._
import pages._

trait PageGenerators {

  implicit lazy val arbitraryDuplicateClaimYearSelectionPage: Arbitrary[DuplicateClaimYearSelectionPage.type] =
    Arbitrary(DuplicateClaimYearSelectionPage)

  implicit lazy val arbitraryDuplicateClaimForOtherYearsPage: Arbitrary[DuplicateClaimForOtherYearsPage.type] =
    Arbitrary(DuplicateClaimForOtherYearsPage)

  implicit lazy val arbitraryReEnterAmountsPage: Arbitrary[ReEnterAmountsPage.type] =
    Arbitrary(ReEnterAmountsPage)

  implicit lazy val arbitraryAmountsAlreadyInCodePage: Arbitrary[AmountsAlreadyInCodePage.type] =
    Arbitrary(AmountsAlreadyInCodePage)

  implicit lazy val arbitraryRemoveSubscriptionPage: Arbitrary[RemoveSubscriptionPage.type] =
    Arbitrary(RemoveSubscriptionPage)

  implicit lazy val arbitraryTaxYearSelectionPage: Arbitrary[TaxYearSelectionPage.type] =
    Arbitrary(TaxYearSelectionPage)

  implicit lazy val arbitraryEmployerContributionPage: Arbitrary[EmployerContributionPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield EmployerContributionPage(year, index)
    }

  implicit lazy val arbitraryYourEmployerPage: Arbitrary[YourEmployerPage.type] =
    Arbitrary(YourEmployerPage)

  implicit lazy val arbitraryYourAddressPage: Arbitrary[YourAddressPage.type] =
    Arbitrary(YourAddressPage)

  implicit lazy val arbitraryWhichSubscriptionPage: Arbitrary[WhichSubscriptionPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield WhichSubscriptionPage(year, index)
    }

  implicit lazy val arbitrarySubscriptionAmountPage: Arbitrary[SubscriptionAmountPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield SubscriptionAmountPage(year, index)
    }

  implicit lazy val arbitraryExpensesEmployerPaidPage: Arbitrary[ExpensesEmployerPaidPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield ExpensesEmployerPaidPage(year, index)
    }

  implicit lazy val arbitrarySummarySubscriptionsPage: Arbitrary[SummarySubscriptionsPage.type] =
    Arbitrary(SummarySubscriptionsPage)

  implicit lazy val arbitraryPSubPage: Arbitrary[PSubPage] =
    Arbitrary {
      for {
        year  <- arbitrary[String]
        index <- arbitrary[Int]
      } yield PSubPage(year, index)
    }

}
