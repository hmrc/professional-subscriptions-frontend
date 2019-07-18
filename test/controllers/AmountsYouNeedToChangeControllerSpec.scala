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
import forms.AmountsYouNeedToChangeFormProvider
import models.TaxYearSelection.CurrentYear
import models.{EmploymentExpense, NormalMode, TaxYearSelection}
import models.TaxYearSelection._
import models.NpsDataFormats.formats
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages.{AmountsYouNeedToChangePage, NpsData, TaxYearSelectionPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.AmountsYouNeedToChangeView

import scala.concurrent.Future

class AmountsYouNeedToChangeControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]
  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
  }

  def onwardRoute = Call("GET", "/foo")

  lazy val amountsYouNeedToChangeRoute = routes.AmountsYouNeedToChangeController.onPageLoad(NormalMode).url

  val formProvider = new AmountsYouNeedToChangeFormProvider()
  val form = formProvider()

  "AmountsYouNeedToChange Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

      val request = FakeRequest(GET, amountsYouNeedToChangeRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AmountsYouNeedToChangeView]

      val npsData: Map[Int, Seq[EmploymentExpense]] = someUserAnswers.get(NpsData).get

      val taxYearSelection: Seq[TaxYearSelection] = someUserAnswers.get(TaxYearSelectionPage).get

      val sortedNpsDataAsSeq: Seq[Seq[EmploymentExpense]] = npsData.toSeq.sortWith(_._1 > _._1).map(_._2)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, taxYearSelection, sortedNpsDataAsSeq)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val ua = someUserAnswers.set(AmountsYouNeedToChangePage, Seq(CurrentYear)).success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, amountsYouNeedToChangeRoute)

      val view = application.injector.instanceOf[AmountsYouNeedToChangeView]

      val result = route(application, request).value

      val npsData: Map[Int, Seq[EmploymentExpense]] = ua.get(NpsData).get

      val taxYearSelection: Seq[TaxYearSelection] = ua.get(TaxYearSelectionPage).get

      val sortedNpsDataAsSeq: Seq[Seq[EmploymentExpense]] = npsData.toSeq.sortWith(_._1 > _._1).map(_._2)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(ua.get(AmountsYouNeedToChangePage).get), NormalMode, taxYearSelection, sortedNpsDataAsSeq)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(someUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      val request =
        FakeRequest(POST, amountsYouNeedToChangeRoute)
          .withFormUrlEncodedBody(("value[0]", TaxYearSelection.values.head.toString))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

      val request =
        FakeRequest(POST, amountsYouNeedToChangeRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[AmountsYouNeedToChangeView]

      val result = route(application, request).value

      val npsData: Map[Int, Seq[EmploymentExpense]] = someUserAnswers.get(NpsData).get

      val taxYearSelection: Seq[TaxYearSelection] = someUserAnswers.get(TaxYearSelectionPage).get

      val sortedNpsDataAsSeq: Seq[Seq[EmploymentExpense]] = npsData.toSeq.sortWith(_._1 > _._1).map(_._2)

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, taxYearSelection, sortedNpsDataAsSeq)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, amountsYouNeedToChangeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, amountsYouNeedToChangeRoute)
          .withFormUrlEncodedBody(("value", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
