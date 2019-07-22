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

package utils

import models.TaxYearSelection._
import models.{PSub, TaxYearSelection, UserAnswers}
import play.api.libs.json.JsValue

object PSubsUtil {
  def remove(userAnswers: UserAnswers, year: String, index: Int): Seq[PSub] = {
    userAnswers.data.value("subscriptions")(year).as[Seq[PSub]].zipWithIndex.filter(_._2 != index).map(_._1)
  }

  def getByYear(userAnswers: UserAnswers, year: String): Seq[PSub] = {
    userAnswers.data.value("subscriptions")(year).as[Seq[PSub]]
  }

  def claimAmountMinusDeductions(psubs: Seq[PSub]): Int = {
    psubs.map {
      psub =>
        psub.amount - psub.employerContributionAmount.filter(_ => psub.employerContributed).getOrElse(0)
    }.sum
  }

  def claimAmountMinusDeductionsAllYears(taxYears: Seq[TaxYearSelection], psubsByYear: Map[Int, Seq[PSub]]): Seq[Int] = {
    taxYears.map {
      year =>
        val psubs = psubsByYear.getOrElse(getTaxYear(year), Seq.empty)
        claimAmountMinusDeductions(psubs)
    }
  }

  def isDuplicate(userAnswers: UserAnswers, year: String): Boolean = {
    val allPSubNames: Seq[JsValue] = userAnswers.data("subscriptions")(year).as[Seq[JsValue]].map(psub => psub("name"))

    allPSubNames.size != allPSubNames.distinct.size
  }

  def isDuplicateInSeqPsubs(psubs: Seq[PSub]): Boolean = {
    val allPSubNames = psubs.map(_.name)

    allPSubNames.size != allPSubNames.distinct.size
  }
}
