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
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages.{EmployerContributionPage, ExpensesEmployerPaidPage, SubscriptionAmountPage, WhichSubscriptionPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.PSubsUtil
import views.html.DuplicateSubscriptionView

import scala.concurrent.Future

class DuplicateSubscriptionControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val mockPSubsUtil = mock[PSubsUtil]
  private val mockSessionRepository = mock[SessionRepository]

  "DuplicateSubscription Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.DuplicateSubscriptionController.onPageLoad(taxYear, index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[DuplicateSubscriptionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(NormalMode, taxYear, index)(fakeRequest, messages).toString

      application.stop()
    }

    "call remove util with correct args" in {

      val userAnswers = emptyUserAnswers
        .set(WhichSubscriptionPage(taxYear, index),"sub").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[PSubsUtil].toInstance(mockPSubsUtil))
        .build()

      val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(captor.capture())) thenReturn Future.successful(true)
      when(mockPSubsUtil.remove(userAnswers, taxYear, index)) thenReturn Seq.empty

      val request = FakeRequest(POST, routes.DuplicateSubscriptionController.onSubmit(taxYear, index).url)

      val result = route(application, request).value

      whenReady(result) {
        _ =>

          assert(captor.getValue.data == Json.obj("subscriptions" -> Json.obj(taxYear -> Json.arr())))

          verify(mockPSubsUtil, times(1)).remove(userAnswers, taxYear, index)
      }

      application.stop()
    }
  }
}
