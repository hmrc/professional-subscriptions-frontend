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
import models.TaxYearSelection._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import pages.{EmployerContributionPage, SubscriptionAmountPage, TaxYearSelectionPage, WhichSubscriptionPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubmissionService
import uk.gov.hmrc.http.HttpResponse
import utils.{CheckYourAnswersHelper, PSubsUtil}
import viewmodels.AnswerSection
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val mockSubmissionService = mock[SubmissionService]
  private val mockPSubUtil = mock[PSubsUtil]

  "Check Your Answers Controller" must {

    "return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers.set(TaxYearSelectionPage, Seq(CurrentYear)).success.value
        .set(WhichSubscriptionPage(taxYear, index), psubWithoutEmployerContribution.name).success.value
        .set(SubscriptionAmountPage(taxYear, index), psubWithoutEmployerContribution.amount).success.value
        .set(EmployerContributionPage(taxYear, index), psubWithoutEmployerContribution.employerContributed).success.value

      val CYAHelper = new CheckYourAnswersHelper(ua)

      val sections = Seq(AnswerSection(None, Seq(
        CYAHelper.taxYearSelection
      ).flatten))

      val extraSections = Seq(AnswerSection(
        Some(s"taxYearSelection.${getTaxYearPeriod(taxYearInt)}"),
        Seq(
          CYAHelper.whichSubscription(taxYear, index, psubWithoutEmployerContribution),
          CYAHelper.subscriptionAmount(taxYear, index, psubWithoutEmployerContribution),
          CYAHelper.employerContribution(taxYear, index, psubWithoutEmployerContribution)
        ).flatten,
        Seq(taxYear, (taxYearInt + 1).toString): _*
      ))

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(sections ++ extraSections)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "onSubmit" must {
      "redirect to ConfirmationController on submitPSub success" in {
        when(mockSubmissionService.submitPSub(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(HttpResponse(204))))

        val application = applicationBuilder(Some(someUserAnswers))
          .overrides(bind[SubmissionService].toInstance(mockSubmissionService))
          .build()

        val request = FakeRequest(POST, CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        whenReady(result) {
          _ =>
            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual ConfirmationController.onPageLoad().url
        }

        application.stop()

      }

      "redirect to tech difficulties on submitPSub fails" in {
        when(mockSubmissionService.submitPSub(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(HttpResponse(500))))

        val application = applicationBuilder(Some(someUserAnswers))
          .overrides(bind[SubmissionService].toInstance(mockSubmissionService))
          .build()

        val request = FakeRequest(POST, CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        whenReady(result) {
          _ =>
            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual TechnicalDifficultiesController.onPageLoad().url
        }

        application.stop()

      }
    }
  }
}
