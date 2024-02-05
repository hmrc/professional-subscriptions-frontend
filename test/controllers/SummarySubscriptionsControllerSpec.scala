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

package controllers

import base.SpecBase
import models.NpsDataFormats.npsDataFormatsFormats
import models.TaxYearSelection._
import models.{NormalMode, PSub, PSubsByYear}
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SummarySubscriptionsControllerSpec extends SpecBase {

  "SummarySubscriptions Controller" must {

    "return OK and the correct view for a GET when no subscription data available" in {

      val npsData = Map(getTaxYear(CurrentYear) -> 300)

      val psubs = Map((getTaxYear(CurrentYear), Seq.empty[PSub]))

      val ua = emptyUserAnswers
        .set(SummarySubscriptionsPage, psubs)(PSubsByYear.pSubsByYearFormats).success.value
        .set(NpsData, npsData).success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, routes.SummarySubscriptionsController.onPageLoad(NormalMode).url)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "return OK and the correct view for a GET when part subscription data available" in {

      val npsData = Map(
        getTaxYear(CurrentYear) -> 300,
        getTaxYear(CurrentYearMinus1) -> 300
      )

      val ua = userAnswersCurrentAndPrevious
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
        .set(SubscriptionAmountPage(taxYear, index), 100000).success.value
        .set(ExpensesEmployerPaidPage(taxYear, index), 200).success.value
        .set(EmployerContributionPage(taxYear, index), true).success.value
        .set(NpsData, npsData).success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, routes.SummarySubscriptionsController.onPageLoad(NormalMode).url)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "return OK and the correct view for a GET when all data available" in {

      val ua = userAnswersCurrent

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, routes.SummarySubscriptionsController.onPageLoad(NormalMode).url)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "redirect to SessionExpired when no TaxYearSelection for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.SummarySubscriptionsController.onPageLoad(NormalMode).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
