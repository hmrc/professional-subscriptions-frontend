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

import base.SpecBase
import models.TaxCodeStatus.{Ceased, Live, PotentiallyCeased}
import models._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar

class ClaimAmountServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val claimAmountService = new ClaimAmountService(frontendAppConfig)

  "ClaimAmountService" must {

    "calculateTax" when {
      "return a string without decimals when a whole number" in {

        claimAmountService.calculateTax(10, 100) mustBe "10"
        claimAmountService.calculateTax(20, 250) mustBe "50"
      }

      "return a string with decimals when not a whole number" in {

        claimAmountService.calculateTax(15, 10) mustBe "1.50"
        claimAmountService.calculateTax(5, 125) mustBe "6.25"
      }
    }

    "band1" when {
      "return 20% of claim amount as a string with contribution from employer" in {
        claimAmountService.calculateTax(
          percentage = frontendAppConfig.englishBasicRate,
          amount = 50
        ) mustBe "10"
      }

      "return 20% of claim amount as a string" in {
        claimAmountService.calculateTax(
          percentage = frontendAppConfig.englishBasicRate,
          amount = 100
        ) mustBe "20"
      }
    }

    "band2" when {
      "return 40% of claim amount as a string" in {
        claimAmountService.calculateTax(
          percentage = frontendAppConfig.englishHigherRate,
          amount = 180
        ) mustBe "72"
      }
    }

    "getRates" when {
      "english tax code record must return english rates when there is a Live english code" in {
        val claimAmount = 100
        val rates =
          claimAmountService.getRates(Seq(TaxCodeRecord("850L", Live), TaxCodeRecord("S850L", Ceased)), claimAmount)

        rates mustBe Seq(
          EnglishRate(
            basicRate = frontendAppConfig.englishBasicRate,
            higherRate = frontendAppConfig.englishHigherRate,
            calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
            calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
          )
        )
      }

      "non-live tax codes that are english must return english rates" in {
        val claimAmount = 100
        val rates = claimAmountService.getRates(
          Seq(TaxCodeRecord("850L", PotentiallyCeased), TaxCodeRecord("850L", Ceased)),
          claimAmount
        )

        rates mustBe Seq(
          EnglishRate(
            basicRate = frontendAppConfig.englishBasicRate,
            higherRate = frontendAppConfig.englishHigherRate,
            calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
            calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
          )
        )
      }

      "scottish tax code record must return scottish rates when there is a Live scottish code" in {
        val claimAmount = 100
        val rates =
          claimAmountService.getRates(Seq(TaxCodeRecord("S850L", Live), TaxCodeRecord("850L", Ceased)), claimAmount)

        rates mustBe Seq(
          ScottishRate(
            starterRate = frontendAppConfig.scottishStarterRate,
            basicRate = frontendAppConfig.scottishBasicRate,
            intermediateRate = frontendAppConfig.scottishIntermediateRate,
            higherRate = frontendAppConfig.scottishHigherRate,
            advancedRate = frontendAppConfig.scottishAdvancedRate,
            topRate = frontendAppConfig.scottishTopRate,
            calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.scottishStarterRate, claimAmount),
            calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.scottishBasicRate, claimAmount),
            calculatedIntermediateRate =
              claimAmountService.calculateTax(frontendAppConfig.scottishIntermediateRate, claimAmount),
            calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.scottishHigherRate, claimAmount),
            calculatedAdvancedRate =
              claimAmountService.calculateTax(frontendAppConfig.scottishAdvancedRate, claimAmount),
            calculatedTopRate = claimAmountService.calculateTax(frontendAppConfig.scottishTopRate, claimAmount)
          )
        )
      }

      "non-live tax codes that are scottish must return scottish rates" in {
        val claimAmount = 100
        val rates = claimAmountService.getRates(
          Seq(TaxCodeRecord("S850L", PotentiallyCeased), TaxCodeRecord("S850L", Ceased)),
          claimAmount
        )

        rates mustBe Seq(
          ScottishRate(
            starterRate = frontendAppConfig.scottishStarterRate,
            basicRate = frontendAppConfig.scottishBasicRate,
            intermediateRate = frontendAppConfig.scottishIntermediateRate,
            higherRate = frontendAppConfig.scottishHigherRate,
            advancedRate = frontendAppConfig.scottishAdvancedRate,
            topRate = frontendAppConfig.scottishTopRate,
            calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.scottishStarterRate, claimAmount),
            calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.scottishBasicRate, claimAmount),
            calculatedIntermediateRate =
              claimAmountService.calculateTax(frontendAppConfig.scottishIntermediateRate, claimAmount),
            calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.scottishHigherRate, claimAmount),
            calculatedAdvancedRate =
              claimAmountService.calculateTax(frontendAppConfig.scottishAdvancedRate, claimAmount),
            calculatedTopRate = claimAmountService.calculateTax(frontendAppConfig.scottishTopRate, claimAmount)
          )
        )
      }

      "non-live tax codes that are english and scottish must return head value" in {
        val claimAmount = 100
        val rates = claimAmountService.getRates(
          Seq(TaxCodeRecord("850L", PotentiallyCeased), TaxCodeRecord("S850L", Ceased)),
          claimAmount
        )

        rates mustBe Seq(
          EnglishRate(
            basicRate = frontendAppConfig.englishBasicRate,
            higherRate = frontendAppConfig.englishHigherRate,
            calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
            calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
          )
        )
      }

      "no tax code record must return both english and scottish rates" in {
        val claimAmount = 100
        val rates       = claimAmountService.getRates(Seq(), claimAmount)

        rates mustBe Seq(
          EnglishRate(
            basicRate = frontendAppConfig.englishBasicRate,
            higherRate = frontendAppConfig.englishHigherRate,
            calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
            calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
          ),
          ScottishRate(
            starterRate = frontendAppConfig.scottishStarterRate,
            basicRate = frontendAppConfig.scottishBasicRate,
            intermediateRate = frontendAppConfig.scottishIntermediateRate,
            higherRate = frontendAppConfig.scottishHigherRate,
            advancedRate = frontendAppConfig.scottishAdvancedRate,
            topRate = frontendAppConfig.scottishTopRate,
            calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.scottishStarterRate, claimAmount),
            calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.scottishBasicRate, claimAmount),
            calculatedIntermediateRate =
              claimAmountService.calculateTax(frontendAppConfig.scottishIntermediateRate, claimAmount),
            calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.scottishHigherRate, claimAmount),
            calculatedAdvancedRate =
              claimAmountService.calculateTax(frontendAppConfig.scottishAdvancedRate, claimAmount),
            calculatedTopRate = claimAmountService.calculateTax(frontendAppConfig.scottishTopRate, claimAmount)
          )
        )
      }
    }

    "english tax code record must return english rates when there is multiple Live codes and english code is the head" in {
      val claimAmount = 100
      val rates =
        claimAmountService.getRates(Seq(TaxCodeRecord("850L", Live), TaxCodeRecord("S850L", Live)), claimAmount)

      rates mustBe Seq(
        EnglishRate(
          basicRate = frontendAppConfig.englishBasicRate,
          higherRate = frontendAppConfig.englishHigherRate,
          calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
          calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
        )
      )
    }
  }

}
