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

import models.TaxYearSelection
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages.*
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators {
  self: ModelGenerators =>

  given Arbitrary[(DuplicateClaimYearSelectionPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DuplicateClaimYearSelectionPage.type]
        value <- arbitrary[TaxYearSelection].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(DuplicateClaimForOtherYearsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DuplicateClaimForOtherYearsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(ReEnterAmountsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ReEnterAmountsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(AmountsAlreadyInCodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmountsAlreadyInCodePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(RemoveSubscriptionPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RemoveSubscriptionPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(WhichSubscriptionPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WhichSubscriptionPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(EmployerContributionPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EmployerContributionPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(YourEmployerPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[YourEmployerPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(YourAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[YourAddressPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(TaxYearSelectionPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TaxYearSelectionPage.type]
        value <- arbitrary[TaxYearSelection].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(SubscriptionAmountPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SubscriptionAmountPage]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  given Arbitrary[(ExpensesEmployerPaidPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ExpensesEmployerPaidPage]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

}
