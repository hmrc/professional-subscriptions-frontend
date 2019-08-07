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
import forms.DuplicateClaimYearSelectionFormProvider
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1}
import models.{NormalMode, TaxYearSelection, WithName}
import navigation.{FakeNavigator, Navigator}
import pages.{DuplicateClaimYearSelectionPage, TaxYearSelectionPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.RadioCheckboxOption
import views.html.DuplicateClaimYearSelectionView

class DuplicateClaimYearSelectionControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")
  lazy val duplicateClaimYearSelectionRoute: String = routes.DuplicateClaimYearSelectionController.onPageLoad(NormalMode, taxYear, index).url

  val formProvider = new DuplicateClaimYearSelectionFormProvider()
  val form: Form[Seq[TaxYearSelection]] = formProvider()

  private val taxYearSelection: Seq[WithName with TaxYearSelection] = Seq(CurrentYearMinus1)
  private val taxYearRadios: Seq[RadioCheckboxOption] = TaxYearSelection.getTaxYearCheckboxOptions(taxYearSelection)

  private val ua = {
    emptyUserAnswers
      .set(TaxYearSelectionPage, taxYearSelection).success.value
  }

  "DuplicateClaimYearSelection Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, duplicateClaimYearSelectionRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[DuplicateClaimYearSelectionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, taxYearRadios, taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val ua2 = ua.set(DuplicateClaimYearSelectionPage, TaxYearSelection.values).success.value

      val application = applicationBuilder(userAnswers = Some(ua2)).build()

      val request = FakeRequest(GET, duplicateClaimYearSelectionRoute)

      val view = application.injector.instanceOf[DuplicateClaimYearSelectionView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(TaxYearSelection.values), NormalMode, taxYearRadios, taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(ua))
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

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request =
        FakeRequest(POST, duplicateClaimYearSelectionRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[DuplicateClaimYearSelectionView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, taxYearRadios, taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, duplicateClaimYearSelectionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if AmountsYouNeedToChange is empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, duplicateClaimYearSelectionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, duplicateClaimYearSelectionRoute)
          .withFormUrlEncodedBody(("value", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if AmountsYouNeedToChange is empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, duplicateClaimYearSelectionRoute)
          .withFormUrlEncodedBody(("value", TaxYearSelection.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}