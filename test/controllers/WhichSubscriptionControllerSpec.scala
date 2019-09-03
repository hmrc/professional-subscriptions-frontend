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
import models.ProfessionalSubscriptionOptions.TechnicalDifficulties
import models.TaxYearSelection.CurrentYearMinus1
import models.{NormalMode, ProfessionalBody, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalacheck.Arbitrary.arbitrary
import pages.WhichSubscriptionPage
import play.api.inject.bind
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ProfessionalBodiesService
import views.html.WhichSubscriptionView
import controllers.routes._
import generators.{Generators, ModelGenerators}
import uk.gov.hmrc.time.TaxYear

import scala.concurrent.Future

class WhichSubscriptionControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach with Generators with ModelGenerators {

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]
  private val mockProfessionalBodiesService: ProfessionalBodiesService = mock[ProfessionalBodiesService]

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    reset(mockProfessionalBodiesService)
  }

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new WhichSubscriptionFormProvider()

  lazy val whichSubscriptionRoute: String = WhichSubscriptionController.onPageLoad(NormalMode, taxYear, index).url


  "WhichSubscription Controller" must {

    "return OK and the correct view for a GET" in {

      when(mockProfessionalBodiesService.professionalBodies()).thenReturn(Future.successful(Seq(ProfessionalBody("subscription", List(""),None))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
        .build()

      val request = FakeRequest(GET, whichSubscriptionRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[WhichSubscriptionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(formProvider(Nil), NormalMode, Seq(ProfessionalBody("subscription", List(""),None)), taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      when(mockProfessionalBodiesService.professionalBodies()).thenReturn(Future.successful(Seq(ProfessionalBody("subscription", List(""),None))))

      val userAnswers = UserAnswers(userAnswersId, Json.obj(WhichSubscriptionPage.toString -> JsString("answer")))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
        .build()

      val request = FakeRequest(GET, whichSubscriptionRoute)

      val view = application.injector.instanceOf[WhichSubscriptionView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(formProvider(Nil).fill("answer"), NormalMode, Seq(ProfessionalBody("subscription", List(""),None)), taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      val request =
        FakeRequest(POST, whichSubscriptionRoute)
          .withFormUrlEncodedBody(("subscription", "validPsub"))

      when(mockProfessionalBodiesService.professionalBodies()).thenReturn(Future.successful(Seq(ProfessionalBody("validPsub", List.empty, None))))
      when(mockProfessionalBodiesService.validateYearInRange(any(), any())(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "redirect to the duplicate page when duplicate data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersCurrent))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, WhichSubscriptionController.onSubmit(NormalMode, taxYear, index +1).url)
          .withFormUrlEncodedBody(("subscription", "Arable Research Institute Association"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual DuplicateSubscriptionController.onPageLoad(NormalMode).url

      application.stop()
    }

    "redirect to the specific year page when year specific psub is submitted before start date" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, WhichSubscriptionController.onPageLoad(NormalMode, "2017", index).url)
          .withFormUrlEncodedBody(("subscription", "100 Women in Finance Association"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual CannotClaimYearSpecificController.onPageLoad(NormalMode, "100 Women in Finance Association", "2017").url

      application.stop()
    }

    "redirect to next when year specific psub is submitted after start date" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      val request =
        FakeRequest(POST, WhichSubscriptionController.onPageLoad(NormalMode, "2018", index).url)
          .withFormUrlEncodedBody(("subscription", "100 Women in Finance Association"))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when Psub does not exist" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
          .build()

      val request =
        FakeRequest(POST, whichSubscriptionRoute)
          .withFormUrlEncodedBody(("subscription", "invalidAnswer"))

      val allSubscriptions = Seq(ProfessionalBody("validProfessionalBody", Nil, None))

      when(mockProfessionalBodiesService.professionalBodies()).thenReturn(Future.successful(allSubscriptions))

      val result = route(application, request).value

      val expectedView = application.injector.instanceOf[WhichSubscriptionView]

      val boundForm = formProvider(allSubscriptions)
        .bind(Map("subscription" -> "invalidProfessionalBody"))

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual expectedView(boundForm, NormalMode, allSubscriptions, TaxYear.current.currentYear.toString, 0)(fakeRequest, messages).toString

      application.stop()
    }


    "return a Bad Request and errors when invalid data is submitted" in {

      when(mockProfessionalBodiesService.professionalBodies()).thenReturn(Future.successful(Seq(ProfessionalBody("subscription", List(""),None))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
        .build()

      val request =
        FakeRequest(POST, whichSubscriptionRoute)
          .withFormUrlEncodedBody(("subscription", ""))

      val boundForm = formProvider(Nil).bind(Map("subscription" -> ""))

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

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, whichSubscriptionRoute)
          .withFormUrlEncodedBody(("subscription", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Technical Difficulties for a GET if no subscriptions are returned" in {

      when(mockProfessionalBodiesService.professionalBodies()).thenReturn(Future.failed(new Exception))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
        .build()

      val request = FakeRequest(GET, whichSubscriptionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }
  }
}
