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
import forms.ReEnterAmountsFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReEnterAmountsPage
import play.api.inject.bind
import play.api.libs.json.{JsBoolean, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService

import scala.concurrent.Future

class ReEnterAmountsControllerSpec
    extends SpecBase
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with BeforeAndAfterEach {

  private val mockSessionService: SessionService = mock[SessionService]

  override def beforeEach(): Unit =
    reset(mockSessionService)

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ReEnterAmountsFormProvider()
  val form         = formProvider()

  lazy val reEnterAmountsRoute = routes.ReEnterAmountsController.onPageLoad(NormalMode).url

  "ReEnterAmounts Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, reEnterAmountsRoute)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId, Json.obj(ReEnterAmountsPage.toString -> JsBoolean(true)))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, reEnterAmountsRoute)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SessionService].toInstance(mockSessionService))
          .build()

      val request =
        FakeRequest(POST, reEnterAmountsRoute)
          .withFormUrlEncodedBody(("value", "true"))

      when(mockSessionService.set(any())(any())).thenReturn(Future.successful(true))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, reEnterAmountsRoute)
          .withFormUrlEncodedBody(("value", ""))

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, reEnterAmountsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, reEnterAmountsRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
