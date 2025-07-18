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
import forms.DuplicateClaimYearSelectionFormProvider
import models.TaxYearSelection.{CurrentYearMinus3, getTaxYear}
import models.{NormalMode, ProfessionalBody, TaxYearSelection}
import navigation.{FakeNavigator, Navigator}
import org.mockito.MockitoSugar._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ProfessionalBodiesService

class DuplicateClaimYearSelectionControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute = Call("GET", "/foo")

  lazy val duplicateClaimYearSelectionRoute: String =
    routes.DuplicateClaimYearSelectionController.onPageLoad(NormalMode, taxYear, index).url

  val formProvider                      = new DuplicateClaimYearSelectionFormProvider()
  val form: Form[Seq[TaxYearSelection]] = formProvider()

  private val mockProfessionalBodiesService = mock[ProfessionalBodiesService]

  override def beforeEach(): Unit =
    reset(mockProfessionalBodiesService)

  "DuplicateClaimYearSelection Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrentAndPrevious)).build()

      val request = FakeRequest(GET, duplicateClaimYearSelectionRoute)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockProfessionalBodiesService.professionalBodies).thenReturn(professionalBodies)

      val application =
        applicationBuilder(Some(userAnswersCurrentAndPrevious))
          .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, duplicateClaimYearSelectionRoute)
          .withFormUrlEncodedBody(("value[0]", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "redirect to SummarySubscriptions on POST when checkbox is empty" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrent))
        .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
        .build()

      val request =
        FakeRequest(POST, duplicateClaimYearSelectionRoute)
          .withFormUrlEncodedBody(("value[0]", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "redirect to SummarySubscriptions on GET when checkbox is empty" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrent)).build()

      val request = FakeRequest(GET, duplicateClaimYearSelectionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SummarySubscriptionsController.onPageLoad(NormalMode).url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrentAndPrevious)).build()

      val request =
        FakeRequest(POST, duplicateClaimYearSelectionRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, duplicateClaimYearSelectionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a GET if AmountsYouNeedToChange is empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, duplicateClaimYearSelectionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, duplicateClaimYearSelectionRoute)
          .withFormUrlEncodedBody(("value", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if AmountsYouNeedToChange is empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, duplicateClaimYearSelectionRoute)
          .withFormUrlEncodedBody(("value", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST when SummarySubscription is empty" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, duplicateClaimYearSelectionRoute)
          .withFormUrlEncodedBody(("value[0]", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST when no duplicate psub is found" in {

      val invalidYearRoute: String =
        routes.DuplicateClaimYearSelectionController
          .onPageLoad(NormalMode, getTaxYear(CurrentYearMinus3).toString, index)
          .url

      val application =
        applicationBuilder(Some(userAnswersPrevious))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, invalidYearRoute)
          .withFormUrlEncodedBody(("value[0]", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

  val professionalBodies: List[ProfessionalBody] = List(
    ProfessionalBody("professionalSubscription1", Nil, None),
    ProfessionalBody("professionalSubscription1", Nil, None)
  )

}
