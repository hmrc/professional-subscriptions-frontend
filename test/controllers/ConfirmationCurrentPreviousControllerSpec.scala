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
import controllers.routes.{SessionExpiredController, TechnicalDifficultiesController}
import models.NpsDataFormats.npsDataFormatsFormats
import models.TaxCodeStatus.Live
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1, getTaxYear}
import models.{EnglishRate, TaxCodeRecord, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ClaimAmountService
import views.html.ConfirmationCurrentPreviousView

import scala.concurrent.Future

class ConfirmationCurrentPreviousControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
  }

  val mockTaiConnector: TaiConnector = mock[TaiConnector]
  val mockClaimAmountService: ClaimAmountService = mock[ClaimAmountService]
  val claimAmountService = new ClaimAmountService(frontendAppConfig)
  val claimAmount: Int = 800
  val claimAmountsAndRates: Seq[EnglishRate] = Seq(EnglishRate(
    frontendAppConfig.englishBasicRate,
    frontendAppConfig.englishHigherRate,
    claimAmountService.calculateTax(frontendAppConfig.englishBasicRate, claimAmount),
    claimAmountService.calculateTax(frontendAppConfig.englishHigherRate, claimAmount)
  ))

  val userAnswers: UserAnswers = userAnswersCurrentAndPrevious

  "ConfirmationCurrentAndPreviousYearsController" must {
    "return OK and the correct ConfirmationCurrentAndPreviousYearsView for a GET with specific answers" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()

      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val request = FakeRequest(GET, routes.ConfirmationCurrentPreviousController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ConfirmationCurrentPreviousView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          claimAmountsAndRates = claimAmountsAndRates,
          claimAmount = claimAmount,
          npsAmountForCY = 300,
          currentYearMinus1Claim = true,
          address = Some(validAddress),
          employerCorrect = Some(true),
          hasClaimIncreased = true
        )(request, messages).toString

      application.stop()
    }

    "Redirect to TechnicalDifficulties when call to Tai fails" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()

      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.failed(new Exception))

      val request = FakeRequest(GET, routes.ConfirmationCurrentPreviousController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe TechnicalDifficultiesController.onPageLoad.url

      application.stop()
    }

    "Redirect to SessionExpired when missing userAnswers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.ConfirmationCurrentPreviousController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "Remove session on page load" in {

      when(mockSessionRepository.remove(userAnswersId)) thenReturn Future.successful(None)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector),
          bind[ClaimAmountService].toInstance(mockClaimAmountService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))

      val request = FakeRequest(GET, routes.ConfirmationCurrentPreviousController.onPageLoad().url)

      val result = route(application, request).value

      whenReady(result) {
        _ =>
          verify(mockSessionRepository, times(1)).remove(userAnswersId)
      }

      application.stop()
    }

    "show as an decrease when they are saving less in their code" in {

      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 100).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
        .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), "100 Women in Finance").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), 50).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus1).toString, index), 25).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), true).success.value
        .set(NpsData, Map(
          getTaxYear(CurrentYear) -> 1000,
          getTaxYear(CurrentYearMinus1) -> 0
        )).success.value
        .set(YourEmployerPage, true).success.value
        .set(CitizensDetailsAddress, validAddress).success.value
        .set(YourEmployersNames, Seq.empty[String]).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()

      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val request = FakeRequest(GET, routes.ConfirmationCurrentPreviousController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ConfirmationCurrentPreviousView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          claimAmountsAndRates = claimAmountsAndRates,
          claimAmount = 90,
          npsAmountForCY = 1000,
          currentYearMinus1Claim = true,
          address = Some(validAddress),
          employerCorrect = Some(true),
          hasClaimIncreased = false
        )(request, messages).toString

      application.stop()
    }

    "show as an increase when they are saving more in their code" in {
      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
        .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), "100 Women in Finance").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), 50).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus1).toString, index), 25).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), true).success.value
        .set(NpsData, Map(
          getTaxYear(CurrentYear) -> 500,
          getTaxYear(CurrentYearMinus1) -> 0
        )).success.value
        .set(YourEmployerPage, true).success.value
        .set(CitizensDetailsAddress, validAddress).success.value
        .set(YourEmployersNames, Seq.empty[String]).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()

      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val request = FakeRequest(GET, routes.ConfirmationCurrentPreviousController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ConfirmationCurrentPreviousView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          claimAmountsAndRates = claimAmountsAndRates,
          claimAmount = 990,
          npsAmountForCY = 500,
          currentYearMinus1Claim = true,
          address = Some(validAddress),
          employerCorrect = Some(true),
          hasClaimIncreased = true
        )(request, messages).toString

      application.stop()
    }

    "show as an increase when they are saving more in their code when no NPS data is held" in {
      val ua = emptyUserAnswers
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 10).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
        .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), "100 Women in Finance").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), 50).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus1).toString, index), 25).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), true).success.value
        .set(YourEmployerPage, true).success.value
        .set(CitizensDetailsAddress, validAddress).success.value
        .set(YourEmployersNames, Seq.empty[String]).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiConnector].toInstance(mockTaiConnector))
        .overrides(bind[ClaimAmountService].toInstance(mockClaimAmountService))
        .build()

      when(mockTaiConnector.getTaxCodeRecords(any(), any())(any(), any())).thenReturn(Future.successful(Seq(TaxCodeRecord("850L", Live))))
      when(mockClaimAmountService.getRates(any(), any())).thenReturn(claimAmountsAndRates)

      val request = FakeRequest(GET, routes.ConfirmationCurrentPreviousController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ConfirmationCurrentPreviousView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          claimAmountsAndRates = claimAmountsAndRates,
          claimAmount = 990,
          npsAmountForCY = 0,
          currentYearMinus1Claim = true,
          address = Some(validAddress),
          employerCorrect = Some(true),
          hasClaimIncreased = true
        )(request, messages).toString

      application.stop()
    }
  }
}