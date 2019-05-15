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
import models.{EnglishRate, ScottishRate}
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.ClaimAmountService
import views.html.ClaimAmountView


class ClaimAmountControllerSpec extends SpecBase with ScalaFutures with IntegrationPatience with OptionValues with MockitoSugar {

  private val subscriptionAmount = 100
  private val subscriptionAmountWithDeduction = 90
  private val deduction = Some(10)

  "ClaimAmount Controller" must {

    "return OK and the correct view for a GET where all data is present" in {

      val userAnswers = emptyUserAnswers
        .set(SubscriptionAmountPage, subscriptionAmount).success.value
        .set(EmployerContributionPage, true).success.value
        .set(ExpensesEmployerPaidPage, deduction.get).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val claimAmountService = application.injector.instanceOf[ClaimAmountService]

      val englishRate = EnglishRate(
        basicRate = frontendAppConfig.taxPercentageBand1,
        higherRate = frontendAppConfig.taxPercentageBand2,
        calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageBand1, subscriptionAmountWithDeduction),
        calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageBand2, subscriptionAmountWithDeduction)
      )

      val scottishRate = ScottishRate(
        starterRate = frontendAppConfig.taxPercentageScotlandBand1,
        basicRate = frontendAppConfig.taxPercentageScotlandBand2,
        higherRate = frontendAppConfig.taxPercentageScotlandBand3,
        calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageScotlandBand1, subscriptionAmountWithDeduction),
        calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageScotlandBand2, subscriptionAmountWithDeduction),
        calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageScotlandBand3, subscriptionAmountWithDeduction)
      )

      val request = FakeRequest(GET, routes.ClaimAmountController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimAmountView]

      whenReady(result) {
        _ =>
          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(subscriptionAmountWithDeduction, subscriptionAmount, deduction,
              employerContribution = Some(true), englishRate, scottishRate)(fakeRequest, messages).toString

          application.stop()

      }
    }

    "return OK and the correct view for a GET where Subscription Amount is present with no EmployerContribution" in {
      val userAnswers = emptyUserAnswers
        .set(SubscriptionAmountPage, subscriptionAmount).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val claimAmountService = application.injector.instanceOf[ClaimAmountService]

      val englishRate = EnglishRate(
        basicRate = frontendAppConfig.taxPercentageBand1,
        higherRate = frontendAppConfig.taxPercentageBand2,
        calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageBand1, subscriptionAmount),
        calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageBand2, subscriptionAmount
        ))

      val scottishRate = ScottishRate(
        starterRate = frontendAppConfig.taxPercentageScotlandBand1,
        basicRate = frontendAppConfig.taxPercentageScotlandBand2,
        higherRate = frontendAppConfig.taxPercentageScotlandBand3,
        calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageScotlandBand1, subscriptionAmount),
        calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageScotlandBand2, subscriptionAmount),
        calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.taxPercentageScotlandBand3, subscriptionAmount)
      )

      val request = FakeRequest(GET, routes.ClaimAmountController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimAmountView]

      whenReady(result) {
        _ =>
          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(subscriptionAmount, subscriptionAmount, None,
              employerContribution = None, englishRate, scottishRate)(fakeRequest, messages).toString

          application.stop()
      }
    }

    "redirect to Session Expired for a GET" when {
      "no existing data is found" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        val request = FakeRequest(GET, routes.ClaimAmountController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }
    }
  }
}