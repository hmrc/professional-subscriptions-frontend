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
import forms.RemoveSubscriptionFormProvider
import models.TaxYearSelection.{CurrentYear, getTaxYear}
import models.{PSub, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService

import scala.concurrent.Future

class RemoveSubscriptionControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSessionService: SessionService = mock[SessionService]
  override def beforeEach(): Unit = {
    reset(mockSessionService)
  }

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveSubscriptionFormProvider()
  val form = formProvider()

  lazy val removeSubscriptionRoute = routes.RemoveSubscriptionController.onPageLoad(taxYear, index).url

  "RemoveSubscription Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrentAndPrevious)).build()

      val request = FakeRequest(GET, removeSubscriptionRoute)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "not populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersCurrentAndPrevious.set(RemoveSubscriptionPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, removeSubscriptionRoute)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "redirect to the next page on true when valid data is submitted and remove the correct subscription" in {

      val ua = userAnswersCurrent
        .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index +1), "100 Women in Finance").success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index +1), 1000).success.value
        .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index +1), 200).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index +1), true).success.value

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SessionService].toInstance(mockSessionService))
          .build()

      val argCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionService.set(argCaptor.capture())(any())) thenReturn Future.successful(true)

      val request =
        FakeRequest(POST, routes.RemoveSubscriptionController.onSubmit(taxYear, index +1).url)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      assert(argCaptor.getValue.data.value("subscriptions")(taxYear).as[Seq[PSub]].length == 1)
      assert(argCaptor.getValue.data.value("subscriptions")(taxYear).as[Seq[PSub]].head.nameOfProfessionalBody == "Arable Research Institute Association")

      application.stop()
    }

    "redirect to the next page on false when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersCurrentAndPrevious))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SessionService].toInstance(mockSessionService))
          .build()

      val argCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionService.set(argCaptor.capture())(any())) thenReturn Future.successful(true)

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

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrentAndPrevious)).build()

      val request =
        FakeRequest(POST, removeSubscriptionRoute)
          .withFormUrlEncodedBody(("value", ""))

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, removeSubscriptionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, removeSubscriptionRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
