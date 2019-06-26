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
import forms.TellUsWhatIsWrongFormProvider
import models.TaxYearSelection.CurrentYear
import models.{EmploymentExpense, NormalMode, TaxYearSelection}
import models.TaxYearSelection._
import navigation.{FakeNavigator, Navigator}
import pages.{NpsData, TaxYearSelectionPage, TellUsWhatIsWrongPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.TellUsWhatIsWrongView

class TellUsWhatIsWrongControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  lazy val tellUsWhatIsWrongRoute = routes.TellUsWhatIsWrongController.onPageLoad(NormalMode).url

  val formProvider = new TellUsWhatIsWrongFormProvider()
  val form = formProvider()

  "TellUsWhatIsWrong Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

      val request = FakeRequest(GET, tellUsWhatIsWrongRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[TellUsWhatIsWrongView]

      val npsData: Map[String, Seq[EmploymentExpense]] = someUserAnswers.get(NpsData).get

      val taxYearSelection: Seq[TaxYearSelection] = someUserAnswers.get(TaxYearSelectionPage).get

      val sortedNpsDataAsSeq: Seq[Seq[EmploymentExpense]] = npsData.toSeq.sortWith(_._1 > _._1).map(_._2)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, taxYearSelection, sortedNpsDataAsSeq)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val ua = someUserAnswers.set(TellUsWhatIsWrongPage, Seq(CurrentYear)).success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, tellUsWhatIsWrongRoute)

      val view = application.injector.instanceOf[TellUsWhatIsWrongView]

      val result = route(application, request).value

      val npsData: Map[String, Seq[EmploymentExpense]] = ua.get(NpsData).get

      val taxYearSelection: Seq[TaxYearSelection] = ua.get(TaxYearSelectionPage).get

      val sortedNpsDataAsSeq: Seq[Seq[EmploymentExpense]] = npsData.toSeq.sortWith(_._1 > _._1).map(_._2)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(ua.get(TellUsWhatIsWrongPage).get), NormalMode, taxYearSelection, sortedNpsDataAsSeq)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(someUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      val request =
        FakeRequest(POST, tellUsWhatIsWrongRoute)
          .withFormUrlEncodedBody(("value[0]", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(someUserAnswers)).build()

      val request =
        FakeRequest(POST, tellUsWhatIsWrongRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[TellUsWhatIsWrongView]

      val result = route(application, request).value

      val npsData: Map[String, Seq[EmploymentExpense]] = someUserAnswers.get(NpsData).get

      val taxYearSelection: Seq[TaxYearSelection] = someUserAnswers.get(TaxYearSelectionPage).get

      val sortedNpsDataAsSeq: Seq[Seq[EmploymentExpense]] = npsData.toSeq.sortWith(_._1 > _._1).map(_._2)

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, taxYearSelection, sortedNpsDataAsSeq)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, tellUsWhatIsWrongRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, tellUsWhatIsWrongRoute)
          .withFormUrlEncodedBody(("value", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
