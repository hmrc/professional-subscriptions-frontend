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
import forms.ExpensesEmployerPaidFormProvider
import models.{NormalMode, ProfessionalBody}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages.{ExpensesEmployerPaidPage, WhichSubscriptionPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ProfessionalBodiesService
import views.html.ExpensesEmployerPaidView

import scala.concurrent.Future

class ExpensesEmployerPaidControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]
  private val mockProfessionalBodiesService = mock[ProfessionalBodiesService]

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    reset(mockProfessionalBodiesService)
  }

  private val formProvider = new ExpensesEmployerPaidFormProvider(frontendAppConfig)
  private val form = formProvider()
  private val validAmount = 20
  private val validSubscription = "Test Subscription"

  private val userAnswersWithoutSub = emptyUserAnswers
    .set(ExpensesEmployerPaidPage(taxYear, index), validAmount).success.value

  private val userAnswersWithoutAmount = emptyUserAnswers
    .set(WhichSubscriptionPage(taxYear, index), validSubscription).success.value

  private val fullUserAnswers = emptyUserAnswers
    .set(ExpensesEmployerPaidPage(taxYear, index), validAmount).success.value
    .set(WhichSubscriptionPage(taxYear, index), validSubscription).success.value

  def onwardRoute = Call("GET", "/foo")

  lazy val ExpensesEmployerPaidRoute: String = routes.ExpensesEmployerPaidController.onPageLoad(NormalMode, taxYear, index).url

  "ExpensesEmployerPaid Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(Some(userAnswersWithoutAmount)).build()

      val request = FakeRequest(GET, ExpensesEmployerPaidRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ExpensesEmployerPaidView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, validSubscription, taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers)).build()

      val request = FakeRequest(GET, ExpensesEmployerPaidRoute)

      val view = application.injector.instanceOf[ExpensesEmployerPaidView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAmount), NormalMode, validSubscription, taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersCurrent))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
          .build()

      val request =
        FakeRequest(POST, ExpensesEmployerPaidRoute)
          .withFormUrlEncodedBody(("value", validAmount.toString))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockProfessionalBodiesService.professionalBodies)
        .thenReturn(List(ProfessionalBody("Arable Research Institute Association", List.empty, None)))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers)).build()

      val request =
        FakeRequest(POST, ExpensesEmployerPaidRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[ExpensesEmployerPaidView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, validSubscription, taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, ExpensesEmployerPaidRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if WhichSubscription is empty" in {

      val application = applicationBuilder(Some(userAnswersWithoutSub)).build()

      val request = FakeRequest(GET, ExpensesEmployerPaidRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()

    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, ExpensesEmployerPaidRoute)
          .withFormUrlEncodedBody(("value", validAmount.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if WhichSubscription is empty" in {

      val application = applicationBuilder(Some(userAnswersWithoutSub)).build()

      val request =
        FakeRequest(POST, ExpensesEmployerPaidRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()

    }
  }
}
