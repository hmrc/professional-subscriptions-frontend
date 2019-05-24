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

package services

import base.SpecBase
import models.{EnglishRate, ScottishRate, TaxCodeRecord}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages.ExpensesEmployerPaidPage


class ClaimAmountServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val claimAmountService = new ClaimAmountService(frontendAppConfig)

  "ClaimAmountService" must {

    "Return subscription amount minus employer contribution amount" in {

      val claimAmount = claimAmountService.calculateClaimAmount(Some(true), Some(20), 120)

      claimAmount mustBe 100
    }

    "Return subscription amount when no employer contribution amount" in {

      val claimAmount = claimAmountService.calculateClaimAmount(None, None, 120)

      claimAmount mustBe 120
    }
  }

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

  "englishBasicTaxRate" when {
    "return 20% of claim amount as a string with contribution from employer when user pays basic tax rate" in {
      val userAnswers = emptyUserAnswers.set(ExpensesEmployerPaidPage, 50).success.value
      val actualClaimAmount = claimAmountService.calculateClaimAmount(employerContribution = Option(true), expensesEmployerPaid = Some(20), subscriptionAmount = 100)

      actualClaimAmount mustBe 80

      claimAmountService.calculateTax(
        percentage = frontendAppConfig.englishBasicRate,
        amount = actualClaimAmount
      ) mustBe "16"
    }
  }
  "return 20% of claim amount as a string when no  contribution from employer" in {
    val userAnswers = emptyUserAnswers
    val actualClaimAmount = claimAmountService.calculateClaimAmount(Option(false), None, 100)

    actualClaimAmount mustBe 100

    claimAmountService.calculateTax(
      percentage = frontendAppConfig.englishBasicRate,
      amount = actualClaimAmount
    ) mustBe "20"
  }

  "englishHigherTaxRate" when {
    "return 40% of claim amount as a string with contribution from employer when customer pays higher tax rate " in {
      val userAnswers = emptyUserAnswers.set(ExpensesEmployerPaidPage, 50).success.value
      val actualClaimAmount = claimAmountService.calculateClaimAmount(employerContribution = Option(true), expensesEmployerPaid = Some(20), subscriptionAmount = 100)

      actualClaimAmount mustBe 80

      claimAmountService.calculateTax(
        percentage = frontendAppConfig.englishHigherRate,
        amount = actualClaimAmount
      ) mustBe "32"
    }

    "getRates" when {
      "english tax code record must return english rates" in {
        val claimAmount = 100
        val rates = claimAmountService.getRates(Seq(TaxCodeRecord("850L")), claimAmount)

        rates mustBe Seq(EnglishRate(
          basicRate = frontendAppConfig.englishBasicRate,
          higherRate = frontendAppConfig.englishHigherRate,
          calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
          calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
        ))
      }
    }
  }
  "scottish tax code record must return scottish rates" in {
    val claimAmount = 100
    val rates = claimAmountService.getRates(Seq(TaxCodeRecord("S850L")), claimAmount)

    rates mustBe Seq(ScottishRate(
      starterRate = frontendAppConfig.scottishStarterRate,
      basicRate = frontendAppConfig.scottishBasicRate,
      higherRate = frontendAppConfig.scottishHigherRate,
      calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.scottishStarterRate, claimAmount),
      calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.scottishBasicRate, claimAmount),
      calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.scottishHigherRate, claimAmount)
    ))
  }

  "no tax code record must return both english and scottish rates" in {
    val claimAmount = 100
    val rates = claimAmountService.getRates(Seq(), claimAmount)

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
        higherRate = frontendAppConfig.scottishHigherRate,
        calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.scottishStarterRate, claimAmount),
        calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.scottishBasicRate, claimAmount),
        calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.scottishHigherRate, claimAmount)
      ))
  }
}