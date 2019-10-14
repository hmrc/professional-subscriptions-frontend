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

package models

import play.api.libs.json._
import models.TaxYearSelection.getTaxYearPeriod
import pages.SummarySubscriptionsPage

final case class PSubsByYear(subscriptions: Map[Int, Seq[PSub]]) {

  def isValid: Boolean = subscriptions.exists(_._2.nonEmpty)

}

object PSubsByYear {

  def apply(taxYearSelections: Seq[Int], previousPsubData: Option[Map[Int, Seq[PSub]]]): PSubsByYear = {

    val subscriptions = previousPsubData match {
      case Some(psubsByYear)  => taxYearSelections.map(year => year -> psubsByYear.getOrElse(year, Seq.empty[PSub])).toMap
      case None               => taxYearSelections.map(year => year -> Seq.empty[PSub]).toMap
    }

    PSubsByYear(subscriptions)
  }


  def orderTaxYears(PSubsByYear: Map[Int, Seq[PSub]]): Seq[TaxYearSelection] = {
    PSubsByYear.map {
      psubsByYear =>
        getTaxYearPeriod(psubsByYear._1)
    }.toSeq.sortWith(_.toString < _.toString)
  }


  implicit lazy val pSubsByYearFormats: Format[Map[Int, Seq[PSub]]] = {
    new Format[Map[Int, Seq[PSub]]] {

      def writes(m: Map[Int, Seq[PSub]]): JsValue = {
        Json.toJson(m.map {
          case (key, value) => key.toString -> value
        })
      }

      def reads(json: JsValue): JsResult[Map[Int, Seq[PSub]]] = {
        json.validate[Map[String, Seq[JsValue]]].map (psubsByYear =>
          psubsByYear.map{ psubsForYear =>
            (
              psubsForYear._1.toInt,
              psubsForYear._2.map(_.validate[PSub]).collect {case JsSuccess(validPsub, _) => validPsub}
            )
          }
        )
      }
    }
  }

  def emptyAllPsubs(userAnswers: UserAnswers): Option[Map[Int, Seq[PSub]]] = {
    userAnswers.get(SummarySubscriptionsPage)
      .map(
        _.map {
          case (year, _) => (year, Seq.empty[PSub])
        }
      )
  }

  implicit lazy val reads: Reads[PSubsByYear] = Json.reads[PSubsByYear]

  implicit lazy val writes: Writes[PSubsByYear] = Json.writes[PSubsByYear]
}
