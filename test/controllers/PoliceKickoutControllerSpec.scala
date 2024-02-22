/*
 * Copyright 2024 HM Revenue & Customs
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
import models.TaxYearSelection.{CurrentYear, getTaxYear}
import models.{ClaimUnsuccessful, NormalMode, ProfessionalBody, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{ProfessionalBodiesService, SessionService}
import utils.PSubsUtil.policeFederationOfEnglandAndWales

import scala.concurrent.Future

class PoliceKickoutControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {
  private val mockSessionService: SessionService = mock[SessionService]
  private val mockProfessionalBodiesService = mock[ProfessionalBodiesService]

  override def beforeEach(): Unit = {
    reset(mockSessionService)
    reset(mockProfessionalBodiesService)
  }

  def onwardRoute = Call("GET", "/foo")

  override def userAnswersCurrent: UserAnswers = emptyUserAnswers
    .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), policeFederationOfEnglandAndWales).success.value
    .set(PoliceKickoutQuestionPage(taxYear, index), true).success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
    .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 200).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
    .set(NpsData, Map(
      getTaxYear(CurrentYear) -> 300
    )).success.value
    .set(YourEmployerPage, true).success.value
    .set(CitizensDetailsAddress, validAddress).success.value
    .set(YourEmployersNames, Seq.empty[String]).success.value
    .set(CitizensDetailsAddress, validAddress).success.value

  val userAnswersMergedJourney = userAnswersCurrent.copy().set(MergedJourneyFlag, true).success.value

  lazy val PoliceKickoutRoute: String = routes.PoliceKickoutController.onPageLoad(NormalMode, taxYear, index).url

  "PoliceKickout Controller" must {
    "return OK and the correct view for a GET" in {
      val application = applicationBuilder(Some(userAnswersCurrent)).build()
      val request = FakeRequest(GET, PoliceKickoutRoute)
      val result = route(application, request).value
      status(result) mustEqual OK
      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(GET, PoliceKickoutRoute)
      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url
      application.stop()
    }

    "redirect to configured page during merged journey" in {
      val application = applicationBuilder(Some(userAnswersMergedJourney)).build()
      val request =
        FakeRequest(POST, PoliceKickoutRoute)
          .withFormUrlEncodedBody(("value", "true"))
      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.mergedJourneyContinueUrl(ClaimUnsuccessful)
      application.stop()
    }

    "redirect to the next page when not in merged journey" in {
      val application =
        applicationBuilder(userAnswers = Some(userAnswersCurrent))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .overrides(bind[SessionService].toInstance(mockSessionService))
          .overrides(bind[ProfessionalBodiesService].toInstance(mockProfessionalBodiesService))
          .build()

      val request =
        FakeRequest(POST, PoliceKickoutRoute)
          .withFormUrlEncodedBody("value" -> "false")
      when(mockSessionService.set(any())(any())).thenReturn(Future.successful(true))
      when(mockProfessionalBodiesService.professionalBodies)
        .thenReturn(List(ProfessionalBody(policeFederationOfEnglandAndWales, Nil, None)))
      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
      application.stop()
    }
  }
}
