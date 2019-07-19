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
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ConfirmationView

import scala.concurrent.Future

class ConfirmationControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]
  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
  }

  "Confirmation Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ConfirmationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view()(fakeRequest, messages).toString

      application.stop()
    }

    "Remove session on page load" in {

      when(mockSessionRepository.remove(userAnswersId)) thenReturn Future.successful(None)

      val application = applicationBuilder(userAnswers = Some(someUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      val request = FakeRequest(GET, ConfirmationController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      whenReady(result) {
        _ =>
          verify(mockSessionRepository, times(1)).remove(userAnswersId)
      }
      application.stop()
    }
  }
}
