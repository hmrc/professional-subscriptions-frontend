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
import models.{Address, UserAnswers}
import org.scalatest.prop.PropertyChecks
import pages.{CitizensDetailsAddress, _}

class CheckYourAnswersHelperSpec extends SpecBase with PropertyChecks{

  private def helper(ua: UserAnswers) = new CheckYourAnswersHelper(ua)


  "yourAddress" when {
    "correct" must {
      "display the correct label, answer and message args" in {

        val ua = emptyUserAnswers.set(YourAddressPage, true).success.value
        val ua2 = ua.set(CitizensDetailsAddress, validAddress).success.value
        helper(ua2).yourAddress.get.label mustBe "yourAddress.checkYourAnswersLabel"
      }
    }

      "incorrect" must {
        "display the correct label, answer, and message args" in {
          val ua = emptyUserAnswers.set(YourAddressPage, false).success.value
          val ua2 = ua.set(CitizensDetailsAddress, validAddress).success.value
          helper(ua2).yourAddress.get.label mustBe "yourAddress.checkYourAnswersLabel"
          helper(ua2).yourAddress.get.answer mustBe "site.no"
          helper(ua2).yourAddress.get.labelArgs.head mustBe Address.asString(validAddress)
        }
    }
  }
}
