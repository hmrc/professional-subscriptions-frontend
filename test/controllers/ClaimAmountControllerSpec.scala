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
import connectors.TaiConnector
import models.TaxCodeStatus.Ceased
import models.TaxYearSelection.CurrentYear
import models.{EnglishRate, ScottishRate, TaxCodeRecord, TaxYearSelection}
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ClaimAmountService
import views.html.ClaimAmountView

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ClaimAmountControllerSpec extends SpecBase with ScalaFutures with IntegrationPatience with OptionValues with MockitoSugar {

  private val subscriptionAmount = 100
  private val subscriptionAmountWithDeduction = 90
  private val deduction = Some(10)

  private val mockTaiConnector = mock[TaiConnector]
  private val mockClaimAmountService = mock[ClaimAmountService]

  "ClaimAmount Controller" must {

    "return OK and the correct view for a GET where all data is present" in {

      val userAnswers = emptyUserAnswers
        .set(SubscriptionAmountPage, subscriptionAmount).success.value
        .set(EmployerContributionPage, true).success.value
        .set(ExpensesEmployerPaidPage, deduction.get).success.value
        .set(SubscriptionAmountAndAnyDeductions, subscriptionAmountWithDeduction).success.value
        .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value

      val mockSessionRepository = mock[SessionRepository]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .build()

      val claimAmountService = application.injector.instanceOf[ClaimAmountService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockTaiConnector.getTaxCodeRecord(any(), any())(any(), any()))
        .thenReturn(Future.successful(Seq(TaxCodeRecord("S1000L", Ceased))))

      val englishRate = EnglishRate(
        basicRate = frontendAppConfig.englishBasicRate,
        higherRate = frontendAppConfig.englishHigherRate,
        calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, subscriptionAmountWithDeduction),
        calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, subscriptionAmountWithDeduction)
      )

      val scottishRate = ScottishRate(
        starterRate = frontendAppConfig.scottishStarterRate,
        basicRate = frontendAppConfig.scottishBasicRate,
        higherRate = frontendAppConfig.scottishHigherRate,
        calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.scottishStarterRate, subscriptionAmountWithDeduction),
        calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.scottishBasicRate, subscriptionAmountWithDeduction),
        calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.scottishHigherRate, subscriptionAmountWithDeduction)
      )

      val request = FakeRequest(GET, routes.ClaimAmountController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimAmountView]

      whenReady(result) {
        _ =>
          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(subscriptionAmountWithDeduction, subscriptionAmount, deduction,
              employerContribution = Some(true), Seq(englishRate, scottishRate))(fakeRequest, messages).toString

          verify(mockSessionRepository, times(1)).set(userAnswers)

          application.stop()

      }
    }

    "return OK and the correct view for a GET where Subscription Amount is present with no EmployerContribution" in {

      val userAnswers = emptyUserAnswers
        .set(SubscriptionAmountPage, subscriptionAmount).success.value
        .set(SubscriptionAmountAndAnyDeductions, subscriptionAmount).success.value
        .set(TaxYearSelectionPage, Seq(CurrentYear)).success.value


      val mockSessionRepository = mock[SessionRepository]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .build()

      val claimAmountService = application.injector.instanceOf[ClaimAmountService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockTaiConnector.getTaxCodeRecord(any(), any())(any(), any()))
        .thenReturn(Future.successful(Seq(TaxCodeRecord("S1000L", Ceased))))

      val englishRate = EnglishRate(
        basicRate = frontendAppConfig.englishBasicRate,
        higherRate = frontendAppConfig.englishHigherRate,
        calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, subscriptionAmount),
        calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, subscriptionAmount
        ))

      val scottishRate = ScottishRate(
        starterRate = frontendAppConfig.scottishStarterRate,
        basicRate = frontendAppConfig.scottishBasicRate,
        higherRate = frontendAppConfig.scottishHigherRate,
        calculatedStarterRate = claimAmountService.calculateTax(frontendAppConfig.scottishStarterRate, subscriptionAmount),
        calculatedBasicRate = claimAmountService.calculateTax(frontendAppConfig.scottishBasicRate, subscriptionAmount),
        calculatedHigherRate = claimAmountService.calculateTax(frontendAppConfig.scottishHigherRate, subscriptionAmount)
      )

      val request = FakeRequest(GET, routes.ClaimAmountController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimAmountView]

      whenReady(result) {
        _ =>
          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(subscriptionAmount, subscriptionAmount, None,
              employerContribution = None, Seq(englishRate, scottishRate))(fakeRequest, messages).toString

          verify(mockSessionRepository, times(1)).set(userAnswers)

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