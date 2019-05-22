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
import connectors.CitizenDetailsConnector
import controllers.routes._
import forms.YourAddressFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import pages.{CitizensDetailsAddress, YourAddressPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.{JsBoolean, JsValue, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.http.HttpResponse
import views.html.YourAddressView

import scala.concurrent.Future

class YourAddressControllerSpec extends SpecBase with ScalaFutures with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new YourAddressFormProvider()
  val form: Form[Boolean] = formProvider()
  private val mockSessionRepository = mock[SessionRepository]
  lazy val yourAddressRoute: String = routes.YourAddressController.onPageLoad(NormalMode).url
  private val mockCitizenDetailsConnector: CitizenDetailsConnector = mock[CitizenDetailsConnector]
  private val userAnswers = emptyUserAnswers

  when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

  lazy val incorrectJson: JsValue = Json.parse(
    s"""
       |{
       |  "IncorrectJson": "incorrectJson"
       |}
     """.stripMargin
  )


  "YourAddress Controller" must {

    "return OK and the correct view for a GET and save address to CitizensDetailsAddress" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector)).build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())) thenReturn Future.successful(HttpResponse(200, Some(Json.toJson(validAddress))))

      val request = FakeRequest(GET, yourAddressRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[YourAddressView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, validAddress)(fakeRequest, messages).toString

      val newUserAnswers = userAnswers.set(CitizensDetailsAddress, validAddress).success.value

      whenReady(result) {
        _ =>
          verify(mockSessionRepository, times(1)).set(newUserAnswers)
      }

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val ua = UserAnswers(userAnswersId, Json.obj(YourAddressPage.toString -> JsBoolean(true)))

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector)).build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())) thenReturn Future.successful(HttpResponse(200, Some(Json.toJson(validAddress))))

      val request = FakeRequest(GET, yourAddressRoute)

      val view = application.injector.instanceOf[YourAddressView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), NormalMode, validAddress)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val ua = userAnswers.set(CitizensDetailsAddress, validAddress).success.value

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val ua = emptyUserAnswers.set(CitizensDetailsAddress, validAddress).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      val request =
        FakeRequest(POST, yourAddressRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[YourAddressView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, validAddress)(fakeRequest, messages).toString

      application.stop()
    }

    "return Sessions Expired when invalid data is submitted and CitizenDetails cannot be found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      val request =
        FakeRequest(POST, yourAddressRoute)
          .withFormUrlEncodedBody(("value", ""))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to UpdateYourAddress if address line one and postcode missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())) thenReturn Future.successful(HttpResponse(200, Some(emptyAddressJson)))

      val request =
        FakeRequest(GET, yourAddressRoute).withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual UpdateYourAddressController.onPageLoad().url

      application.stop()
    }

    "redirect to UpdateYourAddress if 404 returned from getAddress" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())) thenReturn Future.successful(HttpResponse(404, None))

      val request =
        FakeRequest(GET, yourAddressRoute).withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual UpdateYourAddressController.onPageLoad().url
      application.stop()

    }

    "redirect to Phone Us if 423 returned from getAddress" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())) thenReturn Future.successful(HttpResponse(423, None))

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual ContactUsController.onPageLoad().url

      application.stop()
    }

    "redirect to UpdateYourAddress if 500 returned from getAddress" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())) thenReturn Future.successful(HttpResponse(500, None))

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual UpdateYourAddressController.onPageLoad().url

      application.stop()
    }

    "redirect to Technical Difficulties if any other status returned from getAddress" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())) thenReturn Future.successful(HttpResponse(123, None))

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Technical Difficulties when call to CitizensDetails fails" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())) thenReturn Future.failed(new Exception)

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual TechnicalDifficultiesController.onPageLoad().url

      application.stop()

    }

    "redirect to UpdateYourAddress when could not parse Json to Address model" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())) thenReturn Future.successful(HttpResponse(200, Some(incorrectJson)))

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual UpdateYourAddressController.onPageLoad().url

      application.stop()
    }

  }
}
