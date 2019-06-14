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

import base.SpecBase
import models.{PSub, UserAnswers}
import models.TaxYearSelection._
import pages.{EmployerContributionPage, SavePSubs, SubscriptionAmountPage, WhichSubscriptionPage}

class PSubsUtilSpec extends SpecBase {

  val ua1 = emptyUserAnswers
    .set(WhichSubscriptionPage(taxYear, index),"sub 1").success.value
    .set(SubscriptionAmountPage(taxYear, index),10).success.value
    .set(EmployerContributionPage(taxYear, index),false).success.value
    .set(WhichSubscriptionPage(taxYear, index + 1),"sub 2").success.value
    .set(SubscriptionAmountPage(taxYear, index + 1),10).success.value
    .set(EmployerContributionPage(taxYear, index + 1),false).success.value
    .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index),"sub 3").success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index),10).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index),false).success.value

  val pSubsUtil = new PSubsUtil

  "psub util" must {
    "remove psub" in {
      val ua2: UserAnswers = ua1.set(SavePSubs(taxYear), pSubsUtil.remove(ua1, taxYear, index)).success.value

      ua1.data.value("subscriptions")(taxYear).as[Seq[PSub]].length mustBe 2
      ua2.data.value("subscriptions")(taxYear).as[Seq[PSub]].length mustBe 1
      ua2.data.value("subscriptions")(getTaxYear(CurrentYearMinus1).toString).as[Seq[PSub]].isEmpty mustBe false
    }
  }

}
