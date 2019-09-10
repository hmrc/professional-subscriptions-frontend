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

package controllers

import base.SpecBase
import generators.Generators
import models.{PSub, PSubsByYear}
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1, getTaxYear}
import org.scalatest.prop.PropertyChecks
import pages.{EmployerContributionPage, ExpensesEmployerPaidPage, NpsData, SubscriptionAmountPage, SummarySubscriptionsPage, WhichSubscriptionPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.time.TaxYear
import views.html.{HowYouWillGetYourExpensesCurrentAndPreviousYearView, HowYouWillGetYourExpensesCurrentView, HowYouWillGetYourExpensesPreviousView}

class HowYouWillGetYourExpensesControllerSpec extends SpecBase with PropertyChecks with Generators {

  "HowYouWillGetYourExpenses Controller" must {

    "return OK and the correct view for a GET when user has selected" must {
      "Current year only for changes" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersCurrent)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission().url)(fakeRequest, messages).toString

        application.stop()
      }

      "full Current year and incomplete previous years for changes" in {
        val psubs = Map(
          getTaxYear(CurrentYear) -> Seq(PSub("name", 1, true, Some(1))),
          getTaxYear(CurrentYearMinus1) -> Seq.empty[PSub]
        )

        val ua = emptyUserAnswers
          .set(SummarySubscriptionsPage, psubs)(PSubsByYear.pSubsByYearFormats).success.value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission().url)(fakeRequest, messages).toString

        application.stop()
      }

    }

    "return OK and the previous year view when user has only selected previous year for changes" must {
      "include CY-1 then return OK and previous year view" in {

        val application = applicationBuilder(userAnswers = Some(userYearsAnswersCYMinus2)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesPreviousView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission().url, false)(request, messages).toString

        application.stop()

      }

      "include CY-1 then return OK and the current and previous year view" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersPrevious)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesPreviousView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission().url, true)(request, messages).toString

        application.stop()

      }
    }

    "user has only selected current and previous years for changes" must {

      "and includes CY-1 then return OK and the current and previous year view" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersCurrentAndPrevious)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentAndPreviousYearView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission().url, true)(request, messages).toString

        application.stop()
      }

      "and does not include CY-1 then return OK and the current and previous year view" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersCurrentAndPreviousYears)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentAndPreviousYearView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission().url, false)(request, messages).toString

        application.stop()
      }
    }
  }

}
