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
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages.MergedJourneyFlag
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService

import scala.concurrent.Future

class IndexControllerSpec
    extends SpecBase
    with MockitoSugar
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach {

  private val mockSessionService: SessionService = mock[SessionService]

  override def beforeEach(): Unit =
    reset(mockSessionService)

  "onPageLoad" must {
    "redirect to the first page of the service after resetting user answers" in {
      val argCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionService.set(argCaptor.capture())(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswersCurrent))
        .overrides(bind[SessionService].toInstance(mockSessionService))
        .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual navigator.firstPage().url
      argCaptor.getValue.data mustBe Json.obj(MergedJourneyFlag.toString -> false)

      application.stop()
    }

    "redirect to the first page of the service after setting merged journey flag" in {
      val argCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionService.set(argCaptor.capture())(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SessionService].toInstance(mockSessionService))
        .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(true).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual navigator.firstPage().url
      argCaptor.getValue.data mustBe Json.obj(MergedJourneyFlag.toString -> true)

      application.stop()
    }
  }

  "start" must {
    "redirect to index with merged journey flag if user is on a merged journey" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersCurrent.set(MergedJourneyFlag, true).get))
        .overrides(bind[SessionService].toInstance(mockSessionService))
        .build()

      val request = FakeRequest(GET, routes.IndexController.start.url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IndexController.onPageLoad(true).url

      application.stop()
    }
    "redirect to index if there are no user answers" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SessionService].toInstance(mockSessionService))
        .build()

      val request = FakeRequest(GET, routes.IndexController.start.url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url

      application.stop()
    }
  }

}
