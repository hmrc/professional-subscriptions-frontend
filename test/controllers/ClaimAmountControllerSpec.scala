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
import models.NormalMode
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages.{EmployerContributionPage, ExpensesEmployerPaidPage, SubscriptionAmountPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ClaimAmountView

class ClaimAmountControllerSpec extends SpecBase with ScalaFutures with IntegrationPatience with OptionValues with MockitoSugar {

  "ClaimAmount Controller" must {

    "return OK and the correct view for a GET where all data is present" in {

      val userAnswers = emptyUserAnswers
        .set(SubscriptionAmountPage, 120).success.value
        .set(ExpensesEmployerPaidPage, 50).success.value
        .set(EmployerContributionPage, true).success.value


      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.ClaimAmountController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimAmountView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(claimAmountAndAnyDeductions = 70, subscriptionAmount = 120, expensesEmployerPaid = Some(50),
          employerContribution = Some(true))(fakeRequest, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET where Subscription Amount is present with no EmployerContribution" in {

      val userAnswers = emptyUserAnswers
        .set(SubscriptionAmountPage, 120).success.value


      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.ClaimAmountController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ClaimAmountView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(claimAmountAndAnyDeductions = 120, subscriptionAmount = 120, expensesEmployerPaid = None,
          employerContribution = Some(false))(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET" when {
      "no existing data is found" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        val request = FakeRequest(GET, routes.ClaimAmountController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }
    }
  }
}
