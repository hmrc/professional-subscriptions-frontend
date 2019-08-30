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

import models.TaxYearSelection
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators {
  self: ModelGenerators =>

  implicit lazy val arbitraryDuplicateClaimYearSelectionUserAnswersEntry: Arbitrary[(DuplicateClaimYearSelectionPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DuplicateClaimYearSelectionPage.type]
        value <- arbitrary[TaxYearSelection].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDuplicateClaimForOtherYearsUserAnswersEntry: Arbitrary[(DuplicateClaimForOtherYearsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DuplicateClaimForOtherYearsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryReEnterAmountsUserAnswersEntry: Arbitrary[(ReEnterAmountsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ReEnterAmountsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmountsAlreadyInCodeUserAnswersEntry: Arbitrary[(AmountsAlreadyInCodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmountsAlreadyInCodePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRemoveSubscriptionUserAnswersEntry: Arbitrary[(RemoveSubscriptionPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RemoveSubscriptionPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhichSubscriptionUserAnswersEntry: Arbitrary[(WhichSubscriptionPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WhichSubscriptionPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEmployerContributionUserAnswersEntry: Arbitrary[(EmployerContributionPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EmployerContributionPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryYourEmployerUserAnswersEntry: Arbitrary[(YourEmployerPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[YourEmployerPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryYourAddressUserAnswersEntry: Arbitrary[(YourAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[YourAddressPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTaxYearSelectionUserAnswersEntry: Arbitrary[(TaxYearSelectionPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TaxYearSelectionPage.type]
        value <- arbitrary[TaxYearSelection].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySubscriptionAmountUserAnswersEntry: Arbitrary[(SubscriptionAmountPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SubscriptionAmountPage]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryExpensesEmployerPaidUserAnswersEntry: Arbitrary[(ExpensesEmployerPaidPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ExpensesEmployerPaidPage]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

}
