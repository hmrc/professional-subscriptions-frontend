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
import org.mockito.MockitoSugar.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService

class ConfirmationPreviousControllerSpec
    extends SpecBase
    with MockitoSugar
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach {

  private val mockSessionService: SessionService = mock[SessionService]

  override def beforeEach(): Unit =
    reset(mockSessionService)

  "ConfirmationPreviousController" must {
    "return OK and the correct ConfirmationPreviousView for a GET with specific answers" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersPrevious))
        .build()

      val request = FakeRequest(GET, routes.ConfirmationPreviousController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "Redirect to SessionExpired when missing userAnswers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.ConfirmationPreviousController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
