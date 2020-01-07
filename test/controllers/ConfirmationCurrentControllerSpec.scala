/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.routes.{SessionExpiredController, TechnicalDifficultiesController}
import models.TaxCodeStatus.Live
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1, getTaxYear}
import models.{EnglishRate, TaxCodeRecord, UserAnswers, NpsDataFormats}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages.{EmployerContributionPage, ExpensesEmployerPaidPage, NpsData, SubscriptionAmountPage, WhichSubscriptionPage, YourAddressPage, YourEmployerPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ClaimAmountService
import views.html.ConfirmationCurrentView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConfirmationCurrentControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]
  private val mockTaiConnector: TaiConnector = mock[TaiConnector]
  private val mockClaimAmountService: ClaimAmountService = mock[ClaimAmountService]
  private val claimAmountService = new ClaimAmountService(frontendAppConfig)
  private val claimAmount: Int = 800
  private val claimAmountsAndRates: Seq[EnglishRate] = Seq(EnglishRate(
    frontendAppConfig.englishBasicRate,
    frontendAppConfig.englishHigherRate,
    claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
    claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
  ))

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
  }

  "ConfirmationCurrentController" must {
    "return OK and the correct ConfirmationCurrentView for a GET with specific answers" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrent))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()
      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result = route(application, request).value
      val view = application.injector.instanceOf[ConfirmationCurrentView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(
          claimAmountsAndRates = claimAmountsAndRates,
          claimAmount = claimAmount,
          address = Some(validAddress),
          employerCorrect = Some(true),
          hasClaimIncreased = true,
          npsAmountForCY = 300
        )(request, messages).toString

      application.stop()
    }

    "Redirect to TechnicalDifficulties when call to Tai fails" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.failed(new Exception))

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrent))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()
      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }

    "Redirect to SessionExpired when missing userAnswers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "Remove session on page load" in {
      when(mockSessionRepository.remove(userAnswersId)) thenReturn Future.successful(None)
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrent))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result = route(application, request).value

      whenReady(result) {
        _ =>
          verify(mockSessionRepository, times(1)).remove(userAnswersId)
      }

      application.stop()
    }

    "show correct view on a decrease when they are saving less in their code" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 100).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
        .set(YourEmployerPage, true).success.value
        .set(NpsData, Map(getTaxYear(CurrentYear) -> 1000))(NpsDataFormats.npsDataFormatsFormats).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()

      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result = route(application, request).value
      val view = application.injector.instanceOf[ConfirmationCurrentView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(
          claimAmountsAndRates = claimAmountsAndRates,
          claimAmount = 90,
          address = None,
          employerCorrect = Some(true),
          hasClaimIncreased = false,
          npsAmountForCY = 1000
        )(request, messages).toString

      application.stop()
    }

     "show correct view on an increase when they are saving more in their code" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 100).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
        .set(YourEmployerPage, true).success.value
        .set(NpsData, Map(getTaxYear(CurrentYear) -> 15))(NpsDataFormats.npsDataFormatsFormats).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()
      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result = route(application, request).value
      val view = application.injector.instanceOf[ConfirmationCurrentView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(
          claimAmountsAndRates = claimAmountsAndRates,
          claimAmount = 90,
          address = None,
          employerCorrect = Some(true),
          hasClaimIncreased = true,
          npsAmountForCY = 15
        )(request = request, messages = messages).toString

      application.stop()
    }

    "show correct view when there is no Nps data for CY" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 100).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
        .set(YourEmployerPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()

      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result = route(application, request).value
      val view = application.injector.instanceOf[ConfirmationCurrentView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(
          claimAmountsAndRates = claimAmountsAndRates,
          claimAmount = 90,
          address = None,
          employerCorrect = Some(true),
          hasClaimIncreased = true,
          npsAmountForCY = 0
        )(request = request, messages = messages).toString

      application.stop()
    }
  }
}
