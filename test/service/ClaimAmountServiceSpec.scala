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

class ClaimAmountServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val claimAmountService = new ClaimAmountService

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
}

