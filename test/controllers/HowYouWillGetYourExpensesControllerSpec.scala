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
import generators.Generators
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1, getTaxYear}
import models.{NpsDataFormats, PSub, PSubsByYear}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{HowYouWillGetYourExpensesCurrentAndPreviousYearView, HowYouWillGetYourExpensesCurrentView}
import NpsDataFormats.npsDataFormatsFormats

class HowYouWillGetYourExpensesControllerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "HowYouWillGetYourExpenses Controller" must {

    "return OK and the correct view for a GET when user has selected" must {

      "Current year only for changes when subscription amount has decreased from nps amount" in {

        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 100).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), false).success.value
          .set(NpsData, Map(getTaxYear(CurrentYear) -> 1000)).success.value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission.url, hasClaimIncreased = false)(request, messages).toString

        application.stop()
      }

      "Current year only for changes when subscription amount has decreased from nps amount due to employer contribution" in {

        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 120).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
          .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 100).success.value
          .set(NpsData, Map(getTaxYear(CurrentYear) -> 100)).success.value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission.url, hasClaimIncreased = false)(request, messages).toString

        application.stop()
      }

      "Current year only for changes when subscription amount has increased from nps amount" in {

        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
          .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 200).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
          .set(NpsData, Map(
            getTaxYear(CurrentYear) -> 100
          )).success.value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission.url, true)(request, messages).toString

        application.stop()
      }

      "Current year only for changes with no NpsData" in {

        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
          .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 200).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission.url, true)(request, messages).toString

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
          view(routes.SubmissionController.submission.url, true)(request, messages).toString

        application.stop()
      }

    }

    "return OK and the previous year view when user has only selected previous year for changes" must {
      "include CY-1 then return OK and previous year view" in {

        val application = applicationBuilder(userAnswers = Some(userYearsAnswersCYMinus2)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK

        application.stop()

      }

      "include CY-1 then return OK and the current and previous year view" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersPrevious)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK

        application.stop()

      }
    }

    "user has only selected current and previous years for changes" must {

      "and includes CY-1 then return OK and the current and previous year view amount has decreased from nps amount" in {

        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 100).success.value
          .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
          .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), "100 Women in Finance").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), 100).success.value
          .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus1).toString, index), 10).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), true).success.value
          .set(NpsData, Map(
            getTaxYear(CurrentYear) -> 1000,
            getTaxYear(CurrentYearMinus1) -> 1000
          )).success.value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentAndPreviousYearView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission.url, currentYearMinus1Selected = true, hasClaimIncreased = false)(request, messages).toString

        application.stop()
      }

      "and includes CY-1 then return OK and the current and previous year view amount has increased from nps amount" in {

        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
          .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
          .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), "100 Women in Finance").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), 1000).success.value
          .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus1).toString, index), 10).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), true).success.value
          .set(NpsData, Map(
            getTaxYear(CurrentYear) -> 100,
            getTaxYear(CurrentYearMinus1) -> 100
          )).success.value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentAndPreviousYearView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission.url, currentYearMinus1Selected = true, hasClaimIncreased = true)(request, messages).toString

        application.stop()
      }

      "and includes CY-1 then return OK and the current and previous year view with no nps data" in {

        val ua = emptyUserAnswers
          .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
          .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
          .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), "100 Women in Finance").success.value
          .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), 1000).success.value
          .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus1).toString, index), 10).success.value
          .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), true).success.value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentAndPreviousYearView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission.url, currentYearMinus1Selected = true, hasClaimIncreased = true)(request, messages).toString

        application.stop()
      }

      "and does not include CY-1 then return OK and the current and previous year view" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersCurrentAndPreviousYears)).build()

        val request = FakeRequest(GET, routes.HowYouWillGetYourExpensesController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowYouWillGetYourExpensesCurrentAndPreviousYearView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(routes.SubmissionController.submission.url, currentYearMinus1Selected = false, hasClaimIncreased = true)(request, messages).toString

        application.stop()
      }
    }
  }

}
