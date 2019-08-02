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

package pages

import java.security.cert.PKIXRevocationChecker.Option

import models.{PSub, TaxYearSelection, UserAnswers}
import models.TaxYearSelection._
import pages.behaviours.PageBehaviours
import play.api.libs.json.Json

class TaxYearSelectionPageSpec extends PageBehaviours {

  val userAnswers = UserAnswers("", Json.obj())

  "TaxYearSelectionPage" must {

    beRetrievable[Seq[TaxYearSelection]](TaxYearSelectionPage)

    beSettable[Seq[TaxYearSelection]](TaxYearSelectionPage)

    beRemovable[Seq[TaxYearSelection]](TaxYearSelectionPage)

    "clean up" when {
      "if no tax years return original userAnswers" in {
        TaxYearSelectionPage.cleanup(None, userAnswers).success.value mustBe userAnswers
      }

      "if some tax years but no subscriptions held return original userAnswers" in {
        TaxYearSelectionPage.cleanup(Some(Seq(CurrentYear)), userAnswers).success.value mustBe userAnswers
      }

      "if some userAnswers and subscriptions only return subscriptions for the selected tax years" in {
        import models.PSubsByYear.formats

        val ua = userAnswers.set(SummarySubscriptionsPage,
          Map(
            getTaxYear(CurrentYear) -> Seq(PSub("PSub", 100, false, None)),
            getTaxYear(CurrentYearMinus1) -> Seq(PSub("PSub", 100, false, None))
          )
        ).success.value

        val expectedua = userAnswers.set(SummarySubscriptionsPage,
          Map(
            getTaxYear(CurrentYear) -> Seq(PSub("PSub", 100, false, None))
          )
        ).success.value

        TaxYearSelectionPage.cleanup(Some(Seq(CurrentYear)), ua).success.value mustBe expectedua
      }
    }
  }
}
