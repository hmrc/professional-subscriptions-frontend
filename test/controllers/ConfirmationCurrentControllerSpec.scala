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
import connectors.TaiConnector
import models.TaxCodeStatus.Live
import models.TaxYearSelection.{CurrentYear, getTaxYear}
import models.{EnglishRate, NpsDataFormats, TaxCodeRecord}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{ClaimAmountService, SessionService}

import scala.concurrent.Future

class ConfirmationCurrentControllerSpec
    extends SpecBase
    with MockitoSugar
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach {

  private val mockSessionService: SessionService         = mock[SessionService]
  private val mockTaiConnector: TaiConnector             = mock[TaiConnector]
  private val mockClaimAmountService: ClaimAmountService = mock[ClaimAmountService]
  private val claimAmountService                         = new ClaimAmountService(frontendAppConfig)
  private val claimAmount: Int                           = 800

  private val claimAmountsAndRates: Seq[EnglishRate] = Seq(
    EnglishRate(
      frontendAppConfig.englishBasicRate,
      frontendAppConfig.englishHigherRate,
      claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
      claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
    )
  )

  override def beforeEach(): Unit =
    reset(mockSessionService)

  "ConfirmationCurrentController" must {
    "return OK and the correct ConfirmationCurrentView for a GET with specific answers" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any()))
        .thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrent))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()
      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result  = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "Redirect to TechnicalDifficulties when call to Tai fails" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.failed(new Exception))

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrent))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()
      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe routes.TechnicalDifficultiesController.onPageLoad.url

      application.stop()
    }

    "Redirect to SessionExpired when missing userAnswers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request     = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result      = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "show correct view on a decrease when they are saving less in their code" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any()))
        .thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association")
        .success
        .value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 100)
        .success
        .value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10)
        .success
        .value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true)
        .success
        .value
        .set(YourEmployerPage, true)
        .success
        .value
        .set(NpsData, Map(getTaxYear(CurrentYear) -> 1000))(NpsDataFormats.npsDataFormatsFormats)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()

      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result  = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "show correct view on an increase when they are saving more in their code" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any()))
        .thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association")
        .success
        .value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 100)
        .success
        .value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10)
        .success
        .value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true)
        .success
        .value
        .set(YourEmployerPage, true)
        .success
        .value
        .set(NpsData, Map(getTaxYear(CurrentYear) -> 15))(NpsDataFormats.npsDataFormatsFormats)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()
      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result  = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "show correct view when there is no Nps data for CY" in {
      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any()))
        .thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association")
        .success
        .value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 100)
        .success
        .value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10)
        .success
        .value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true)
        .success
        .value
        .set(YourEmployerPage, true)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()

      val request = FakeRequest(GET, routes.ConfirmationCurrentController.onPageLoad().url)
      val result  = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }
  }

}
