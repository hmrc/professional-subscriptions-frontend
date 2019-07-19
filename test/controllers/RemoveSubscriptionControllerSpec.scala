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
import controllers.routes._
import forms.RemoveSubscriptionFormProvider
import models.{NormalMode, PSub, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.RemoveSubscriptionView

import scala.concurrent.Future

class RemoveSubscriptionControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]
  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
  }

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveSubscriptionFormProvider()
  val form = formProvider()

  lazy val removeSubscriptionRoute = RemoveSubscriptionController.onPageLoad(taxYear, index).url

  "RemoveSubscription Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

      val request = FakeRequest(GET, removeSubscriptionRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveSubscriptionView]

      val subscription = someUserAnswers.get(PSubPage(taxYear, 0)).get

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, taxYear, 0, subscription.name)(fakeRequest, messages).toString

      application.stop()
    }

    "not populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = someUserAnswers.set(RemoveSubscriptionPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, removeSubscriptionRoute)

      val view = application.injector.instanceOf[RemoveSubscriptionView]

      val result = route(application, request).value

      val subscription = someUserAnswers.get(PSubPage(taxYear, 0)).get

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, taxYear, 0, subscription.name)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page on true when valid data is submitted and remove the correct subscription" in {

      val application =
        applicationBuilder(userAnswers = Some(someUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      val argCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(argCaptor.capture())) thenReturn Future.successful(true)

      val request =
        FakeRequest(POST, removeSubscriptionRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      assert(argCaptor.getValue.data.value("subscriptions")(taxYear).as[Seq[PSub]].length == 1)
      assert(argCaptor.getValue.data.value("subscriptions")(taxYear).as[Seq[PSub]].head.name == "100 Women in Finance")

      application.stop()
    }

    "redirect to the next page on false when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(someUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      val argCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(argCaptor.capture())) thenReturn Future.successful(true)

      val request =
        FakeRequest(POST, removeSubscriptionRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      assert(argCaptor.getValue.data.value("subscriptions")(taxYear).as[Seq[PSub]].nonEmpty)

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

      val request =
        FakeRequest(POST, removeSubscriptionRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[RemoveSubscriptionView]

      val result = route(application, request).value

      val subscription = someUserAnswers.get(PSubPage(taxYear, 0)).get

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, taxYear, 0, subscription.name)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, removeSubscriptionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, removeSubscriptionRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
