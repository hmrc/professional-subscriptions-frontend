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

package models

import base.SpecBase
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.OptionValues

class EmploymentSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "Employment" must {
    "must deserialise from json" in {
      val result = validEmploymentJson.as[Seq[Employment]]
      result mustBe taiEmployment
    }
  }

  "asLabel" must {
    "return correct string" in {
      val employments = Seq("Employment 1", "Employment 2", "Employment 3")
      Employment.asLabel(employments) mustEqual "<p>Employment 1<br>Employment 2<br>Employment 3</p>"
    }
  }

}
