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

package pages

import com.google.inject.Inject
import models.{PSub, PSubsByYear, UserAnswers}
import play.api.libs.json.JsPath

import scala.util.Try

case object ReEnterAmountsPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "reEnterAmounts"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(false) =>
        val mainCleanupOfUserAnswers: Try[UserAnswers] =
          userAnswers
            .remove(NpsData)
            .flatMap(_.remove(DuplicateClaimForOtherYearsPage("", 0)))
            .flatMap(_.remove(CitizensDetailsAddress))
            .flatMap(_.remove(YourEmployersNames))


        val emptySummarySubscription: Option[Map[Int, Seq[Nothing]]] =
          userAnswers
            .get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)
            .map(
              _.map {
                case (year, _) => (year, Seq.empty)
              }
            )

        emptySummarySubscription match {
          case None => mainCleanupOfUserAnswers
          case Some(subscriptions) => mainCleanupOfUserAnswers.flatMap(_.set(SummarySubscriptionsPage, subscriptions)(PSubsByYear.pSubsByYearFormats))
        }

      case _ => Try(userAnswers)
    }
  }
}
