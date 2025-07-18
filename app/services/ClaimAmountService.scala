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

package services

import com.google.inject.Inject
import config.FrontendAppConfig
import models.TaxCodeStatus.Live
import models.{EnglishRate, Rates, ScottishRate, TaxCodeRecord}

import scala.math.BigDecimal.RoundingMode

class ClaimAmountService @Inject() (
    appConfig: FrontendAppConfig
) {

  def calculateTax(percentage: Int, amount: Int): String = {

    val calculatedResult = BigDecimal((amount.toDouble / 100) * percentage).setScale(2, RoundingMode.DOWN)

    if (calculatedResult.isWhole) {
      "%.0f".format(calculatedResult)
    } else {
      calculatedResult.toString
    }
  }

  def englishRate(claimAmount: Int): EnglishRate =
    EnglishRate(
      basicRate = appConfig.englishBasicRate,
      higherRate = appConfig.englishHigherRate,
      calculatedBasicRate = calculateTax(appConfig.englishBasicRate, claimAmount),
      calculatedHigherRate = calculateTax(appConfig.englishHigherRate, claimAmount)
    )

  def scottishRate(claimAmount: Int): ScottishRate =
    ScottishRate(
      starterRate = appConfig.scottishStarterRate,
      basicRate = appConfig.scottishBasicRate,
      intermediateRate = appConfig.scottishIntermediateRate,
      higherRate = appConfig.scottishHigherRate,
      advancedRate = appConfig.scottishAdvancedRate,
      topRate = appConfig.scottishTopRate,
      calculatedStarterRate = calculateTax(appConfig.scottishStarterRate, claimAmount),
      calculatedBasicRate = calculateTax(appConfig.scottishBasicRate, claimAmount),
      calculatedIntermediateRate = calculateTax(appConfig.scottishIntermediateRate, claimAmount),
      calculatedHigherRate = calculateTax(appConfig.scottishHigherRate, claimAmount),
      calculatedAdvancedRate = calculateTax(appConfig.scottishAdvancedRate, claimAmount),
      calculatedTopRate = calculateTax(appConfig.scottishTopRate, claimAmount)
    )

  def getRates(taxCodeRecords: Seq[TaxCodeRecord], claimAmount: Int): Seq[Rates] = {

    val liveRecords = filterRecords(taxCodeRecords)

    liveRecords match {
      case Some(taxCodeRecord) if taxCodeRecord.taxCode(0).toUpper != 'S' =>
        Seq(englishRate(claimAmount))
      case Some(taxCodeRecord) if taxCodeRecord.taxCode(0).toUpper == 'S' =>
        Seq(scottishRate(claimAmount))
      case _ =>
        Seq(englishRate(claimAmount), scottishRate(claimAmount))
    }
  }

  def filterRecords(taxCodeRecord: Seq[TaxCodeRecord]): Option[TaxCodeRecord] =
    taxCodeRecord.find(_.status == Live) match {
      case Some(liveTaxCodeRecord) => Some(liveTaxCodeRecord)
      case None                    => taxCodeRecord.headOption
    }

}
