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

package service

import base.SpecBase
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages.ExpensesEmployerPaidPage
import models.{Rates, ScottishRate, StandardRate, TaxCodeRecord}


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

  "band1" when {
    "return 20% of claim amount as a string with contribution from employer" in {
      val userAnswers = emptyUserAnswers.set(ExpensesEmployerPaidPage, 50).success.value
      val actualClaimAmount = claimAmountService.calculateClaimAmount(employerContribution = Option(true), expensesEmployerPaid = Some(20),subscriptionAmount = 100)

      actualClaimAmount mustBe 80

      claimAmountService.calculateTax(
        percentage = frontendAppConfig.taxPercentageBand1,
        amount = actualClaimAmount
      ) mustBe "10"
    }
  }
}

