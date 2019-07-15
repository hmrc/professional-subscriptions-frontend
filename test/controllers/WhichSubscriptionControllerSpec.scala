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
import forms.WhichSubscriptionFormProvider
import models.{NormalMode, ProfessionalBody, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import pages.WhichSubscriptionPage
import play.api.inject.bind
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ProfessionalBodiesService
import views.html.WhichSubscriptionView

import scala.concurrent.Future

class WhichSubscriptionControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new WhichSubscriptionFormProvider()
  val form = formProvider()

  lazy val whichSubscriptionRoute = routes.WhichSubscriptionController.onPageLoad(NormalMode, taxYear, index).url

  val mockProfessionalBodiesService = mock[ProfessionalBodiesService]

  "WhichSubscription Controller" must {

    "return OK and the correct view for a GET" in {

      when(mockProfessionalBodiesService.localSubscriptions()).thenReturn(Future.successful(Seq(ProfessionalBody("subscription", List(""),None))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
        .build()

      val request = FakeRequest(GET, whichSubscriptionRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[WhichSubscriptionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, Seq(ProfessionalBody("subscription", List(""),None)), taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      when(mockProfessionalBodiesService.localSubscriptions()).thenReturn(Future.successful(Seq(ProfessionalBody("subscription", List(""),None))))

      val userAnswers = UserAnswers(userAnswersId, Json.obj(WhichSubscriptionPage.toString -> JsString("answer")))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
        .build()

      val request = FakeRequest(GET, whichSubscriptionRoute)

      val view = application.injector.instanceOf[WhichSubscriptionView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill("answer"), NormalMode, Seq(ProfessionalBody("subscription", List(""),None)), taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, whichSubscriptionRoute)
          .withFormUrlEncodedBody(("subscription", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "redirect to the duplicate page when duplicate data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(someUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, whichSubscriptionRoute)
          .withFormUrlEncodedBody(("subscription", "100 Women in Finance"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.DuplicateSubscriptionController.onPageLoad().url

      application.stop()
    }

    "redirect to the specific year page when year specific psub is submitted before start date" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, routes.WhichSubscriptionController.onPageLoad(NormalMode, "2017", index).url)
          .withFormUrlEncodedBody(("subscription", "100 Women in Finance Association"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.CannotClaimYearSpecificController.onPageLoad(NormalMode, "100 Women in Finance Association", "2017").url

      application.stop()
    }

    "redirect to next when year specific psub is submitted after start date" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, routes.WhichSubscriptionController.onPageLoad(NormalMode, "2018", index).url)
          .withFormUrlEncodedBody(("subscription", "100 Women in Finance Association"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      when(mockProfessionalBodiesService.localSubscriptions()).thenReturn(Future.successful(Seq(ProfessionalBody("subscription", List(""),None))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
        .build()

      val request =
        FakeRequest(POST, whichSubscriptionRoute)
          .withFormUrlEncodedBody(("subscription", ""))

      val boundForm = form.bind(Map("subscription" -> ""))

      val view = application.injector.instanceOf[WhichSubscriptionView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, Seq(ProfessionalBody("subscription", List(""),None)), taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, whichSubscriptionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, whichSubscriptionRoute)
          .withFormUrlEncodedBody(("subscription", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Technical Difficulties for a GET if no subscriptions are returned" in {

      when(mockProfessionalBodiesService.localSubscriptions()).thenReturn(Future.failed(new Exception))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
        .build()

      val request = FakeRequest(GET, whichSubscriptionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }

    "redirect to Technical Difficulties for a POST if no subscriptions are returned" in {

      when(mockProfessionalBodiesService.localSubscriptions()).thenReturn(Future.failed(new Exception))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
        .build()

      val request =
        FakeRequest(POST, whichSubscriptionRoute)
          .withFormUrlEncodedBody(("subscription", ""))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }
  }
}
