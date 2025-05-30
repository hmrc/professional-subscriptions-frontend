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

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator {
  self: Generators =>

  val generators: Seq[Gen[(Page, JsValue)]] =
    arbitrary[(DuplicateClaimYearSelectionPage.type, JsValue)] ::
      arbitrary[(ReEnterAmountsPage.type, JsValue)] ::
      arbitrary[(AmountsAlreadyInCodePage.type, JsValue)] ::
      arbitrary[(WhichSubscriptionPage, JsValue)] ::
      arbitrary[(RemoveSubscriptionPage.type, JsValue)] ::
      arbitrary[(TaxYearSelectionPage.type, JsValue)] ::
      arbitrary[(EmployerContributionPage, JsValue)] ::
      arbitrary[(YourEmployerPage.type, JsValue)] ::
      arbitrary[(YourAddressPage.type, JsValue)] ::
      arbitrary[(SubscriptionAmountPage, JsValue)] ::
      arbitrary[(ExpensesEmployerPaidPage, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserAnswers: Arbitrary[UserAnswers] =
    Arbitrary {
      for {
        cacheId <- nonEmptyString
        data <- generators match {
          case Nil => Gen.const(Map[Page, JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers(
        cacheId,
        data.map { case (k, v) => Json.obj(k.toString -> v) }.foldLeft(Json.obj())(_ ++ _)
      )
    }

}
