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
import pages.{ExpensesEmployerPaidPage, SubscriptionAmountPage}

class ClaimAmountServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val claimAmountService = new ClaimAmountService

  "ClaimAmountService" must {

    "Return subscription amount minus employer contribution amount" in  {
      val userAnswers = emptyUserAnswers
        .set(SubscriptionAmountPage,120).success.value
        .set(ExpensesEmployerPaidPage, 20).success.value

      val claimAmount = claimAmountService.calculateClaimAmount(userAnswers)

      claimAmount mustBe Some(100)

    }

    "Return subscription amount when no employer contribution amount" in  {
      val userAnswers = emptyUserAnswers
        .set(SubscriptionAmountPage,120).success.value

      val claimAmount = claimAmountService.calculateClaimAmount(userAnswers)

      claimAmount mustBe Some(120)

    }

    "Return None when there is no subscription amount" in {
      val userAnswers = emptyUserAnswers

      val claimAmount = claimAmountService.calculateClaimAmount(userAnswers)

      claimAmount mustBe None
    }
  }
}

