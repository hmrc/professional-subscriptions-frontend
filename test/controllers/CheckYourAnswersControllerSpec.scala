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
import pages._
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

  "Check Your Answers Controller" must {

    "return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers
        .set(TaxYearSelectionPage, Seq(CurrentYear, CurrentYearMinus1)).success.value
        .set(WhichSubscriptionPage(taxYear, index), psubWithoutEmployerContribution.name).success.value
        .set(SubscriptionAmountPage(taxYear, index), psubWithoutEmployerContribution.amount).success.value
        .set(EmployerContributionPage(taxYear, index), psubWithoutEmployerContribution.employerContributed).success.value
        .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), psubWithoutEmployerContribution.name).success.value
        .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), psubWithoutEmployerContribution.amount).success.value
        .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), psubWithoutEmployerContribution.employerContributed).success.value
        .set(YourEmployerPage, true).success.value
        .set(YourAddressPage, true).success.value

      val CYAHelper = new CheckYourAnswersHelper(ua)

      val pSubsUtil = new PSubsUtil

      val taxYearSelection = Seq(AnswerSection(
        headingKey = None,
        rows = Seq(
          CYAHelper.taxYearSelection
        ).flatten
      ))

      val subscriptions = ua.get(TaxYearSelectionPage).get.flatMap(
        taxYear =>
          pSubsUtil.getByYear(ua, getTaxYear(taxYear).toString).zipWithIndex.map {
            case (psub, i) =>
              AnswerSection(
                headingKey = if (i == 0) Some(s"taxYearSelection.${getTaxYearPeriod(getTaxYear(taxYear))}") else None,
                rows = Seq(
                  CYAHelper.whichSubscription(getTaxYear(taxYear).toString, i, psub),
                  CYAHelper.subscriptionAmount(getTaxYear(taxYear).toString, i, psub),
                  CYAHelper.employerContribution(getTaxYear(taxYear).toString, i, psub),
                  CYAHelper.expensesEmployerPaid(getTaxYear(taxYear).toString, i, psub)
                ).flatten,
                messageArgs = Seq(getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString): _*
              )
          }
      )

      val personalData = Seq(AnswerSection(
        headingKey = Some("checkYourAnswers.yourDetails"),
        rows = Seq(
          CYAHelper.yourEmployer,
          CYAHelper.yourAddress
        ).flatten
      ))

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(taxYearSelection ++ subscriptions ++ personalData)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPageLoad().url)

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
