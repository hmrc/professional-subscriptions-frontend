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
import controllers.routes._
import forms.YourEmployerFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.{any, eq => eqs}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages.{YourEmployerPage, YourEmployersNames}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import services.TaiService

import scala.concurrent.Future

class YourEmployerControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSessionService: SessionService = mock[SessionService]
  override def beforeEach(): Unit = {
    reset(mockSessionService)
  }

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new YourEmployerFormProvider()
  val form: Form[Boolean] = formProvider()
  private val mockTaiService = mock[TaiService]
  private val employments = Seq("HMRC Longbenton")

  lazy val yourEmployerRoute: String = routes.YourEmployerController.onPageLoad(NormalMode).url

  "YourEmployer Controller" must {

    "return OK and the correct view for a GET" in {
      val ua = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[TaiService].toInstance(mockTaiService))
        .overrides(bind[SessionService].toInstance(mockSessionService))
        .build()

      when(mockTaiService.getEmployments(any(), any())(any(), any())).thenReturn(Future.successful(taiEmployment))
      when(mockSessionService.set(any())(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(GET, yourEmployerRoute)

      val result = route(application, request).value

      val ua2 = ua.set(YourEmployersNames, employments).success.value

      status(result) mustEqual OK

      whenReady(result) {
        _ =>
          verify(mockSessionService, times(1)).set(eqs(ua2))(any())
      }

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val ua = userAnswersCurrent.set(YourEmployerPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[SessionService].toInstance(mockSessionService))
        .overrides(bind[TaiService].toInstance(mockTaiService))
        .build()

      when(mockTaiService.getEmployments(any(), any())(any(), any())).thenReturn(Future.successful(taiEmployment))
      when(mockSessionService.set(any())(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(GET, yourEmployerRoute)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val ua = userAnswersCurrent
        .set(YourEmployerPage, true).success.value
        .set(YourEmployersNames, employments).success.value

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[TaiService].toInstance(mockTaiService))
          .overrides(bind[SessionService].toInstance(mockSessionService))
          .build()

      val request =
        FakeRequest(POST, yourEmployerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      when(mockSessionService.set(any())(any())).thenReturn(Future.successful(true))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      whenReady(result) {
        _ =>
          verify(mockSessionService, times(1)).set(eqs(ua))(any())
      }

      application.stop()
    }

    "redirect to 'Update your employer' on GET when no employer is located" in {

      val ua = userAnswersCurrent

      val application = applicationBuilder(Some(ua))
        .overrides(bind[TaiService].toInstance(mockTaiService))
        .build()

      when(mockTaiService.getEmployments(any(), any())(any(), any())).thenReturn(Future.successful(Seq.empty))

      val request = FakeRequest(GET, yourEmployerRoute)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual UpdateYourEmployerInformationController.onPageLoad().url

      application.stop()

    }

    "redirect to 'Technical Difficulties' on GET when call to Tai fails" in {

      val ua = userAnswersCurrent

      val application = applicationBuilder(Some(ua))
        .overrides(bind[TaiService].toInstance(mockTaiService))
        .build()

      when(mockTaiService.getEmployments(any(), any())(any(), any())).thenReturn(Future.failed(new RuntimeException))

      val request = FakeRequest(GET, yourEmployerRoute)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual TechnicalDifficultiesController.onPageLoad.url

      application.stop()

    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(YourEmployersNames, employments).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request =
        FakeRequest(POST, yourEmployerRoute)
          .withFormUrlEncodedBody(("value", ""))

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, yourEmployerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, yourEmployerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no YourEmployersNames is found" in {

      val ua = emptyUserAnswers.set(YourEmployerPage, true).success.value

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[TaiService].toInstance(mockTaiService))
          .build()

      val request =
        FakeRequest(POST, yourEmployerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
