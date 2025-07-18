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
import connectors.CitizenDetailsConnector
import models.NormalMode
import org.mockito.ArgumentMatchers.{any, eq => eqs}
import org.mockito.MockitoSugar._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages.CitizensDetailsAddress
import play.api.inject.bind
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class YourAddressControllerSpec
    extends SpecBase
    with MockitoSugar
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach {

  private val mockSessionService: SessionService = mock[SessionService]

  override def beforeEach(): Unit =
    reset(mockSessionService)

  lazy val yourAddressRoute: String = routes.YourAddressController.onPageLoad(NormalMode).url
  private val mockCitizenDetailsConnector: CitizenDetailsConnector = mock[CitizenDetailsConnector]
  private val userAnswers                                          = emptyUserAnswers

  lazy val incorrectJson: JsValue = Json.parse(
    s"""
       |{
       |  "IncorrectJson": "incorrectJson"
       |}
     """.stripMargin
  )

  "YourAddress Controller" must {

    "redirect to next page and the correct view for a GET and save address to CitizensDetailsAddress" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .overrides(bind[SessionService].toInstance(mockSessionService))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, json = Json.toJson(validAddress), Map.empty)))
      when(mockSessionService.set(any())(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(GET, yourAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(routes.CheckYourAnswersController.onPageLoad.url)

      val newUserAnswers = userAnswers.set(CitizensDetailsAddress, validAddress).success.value

      whenReady(result)(_ => verify(mockSessionService, times(1)).set(eqs(newUserAnswers))(any()))

      application.stop()
    }

    "redirect to CheckYourAnswers if address line one and postcode missing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, json = emptyAddressJson, Map.empty)))

      val request =
        FakeRequest(GET, yourAddressRoute).withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url

      application.stop()
    }

    "redirect to CheckYourAnswers if 404 returned from getAddress" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(404, json = null, Map.empty)))

      val request =
        FakeRequest(GET, yourAddressRoute).withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url
      application.stop()

    }

    "redirect to Phone Us if 423 returned from getAddress" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(423, json = null, Map.empty)))

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ContactUsController.onPageLoad().url

      application.stop()
    }

    "redirect to CheckYourAnswers if 500 returned from getAddress" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(500, json = null, Map.empty)))

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url

      application.stop()
    }

    "redirect to CheckYourAnswers if any other status returned from getAddress" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(123, json = null, Map.empty)))

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url

      application.stop()
    }

    "redirect to Technical Difficulties when call to CitizensDetails fails" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any())).thenReturn(Future.failed(new Exception))

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad.url

      application.stop()
    }

    "redirect to CheckYourAnswers when could not parse Json to Address model" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector))
        .build()

      when(mockCitizenDetailsConnector.getAddress(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, json = incorrectJson, Map.empty)))

      val request =
        FakeRequest(GET, yourAddressRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url

      application.stop()
    }
  }

}
