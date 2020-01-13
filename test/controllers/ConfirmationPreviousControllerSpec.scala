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
import controllers.routes.SessionExpiredController
import models.TaxYearSelection.{CurrentYearMinus1, getTaxYear}
import models.UserAnswers
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmployerContributionPage, ExpensesEmployerPaidPage, NpsData, SubscriptionAmountPage, WhichSubscriptionPage, YourAddressPage, YourEmployerPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ConfirmationPreviousView

import scala.concurrent.Future

class ConfirmationPreviousControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
  }

  "ConfirmationPreviousController" must {
    "return OK and the correct ConfirmationPreviousView for a GET with specific answers" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersPrevious))
        .build()

      val request = FakeRequest(GET, routes.ConfirmationPreviousController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ConfirmationPreviousView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(
          currentYearMinus1Claim = true,
          address = Some(validAddress),
          updateAddressUrl = "addressURL"
        )(request, messages).toString

      application.stop()
    }

    "Redirect to SessionExpired when missing userAnswers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.ConfirmationPreviousController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "Remove session on page load" in {

      when(mockSessionRepository.remove(userAnswersId)) thenReturn Future.successful(None)

      val application = applicationBuilder(userAnswers = Some(userAnswersPrevious))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      val request = FakeRequest(GET, routes.ConfirmationPreviousController.onPageLoad().url)

      val result = route(application, request).value

      whenReady(result) {
        _ =>
          verify(mockSessionRepository, times(1)).remove(userAnswersId)
      }

      application.stop()
    }
  }
}
