/*
 * Copyright 2022 HM Revenue & Customs
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
import models.PSub
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1, getTaxYear}
import models.auditing._
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AmountsAlreadyInCodePage, QuestionPage, SavePSubs, SummarySubscriptionsPage}
import play.api.inject.bind
import play.api.libs.json.JsPath
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubmissionService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.Future

class SubmissionControllerSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  private val mockSubmissionService = mock[SubmissionService]
  private val mockAuditConnector = mock[AuditConnector]

  override def beforeEach(): Unit = {
    reset(mockSubmissionService)
    reset(mockAuditConnector)
  }

  "Submission" must {
    "submit psubs and redirect to ConfirmationCurrentController on success" in {

      when(mockSubmissionService.submitPSub(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val answers = userAnswersCurrent.set(AmountsAlreadyInCodePage, true).success.value

      val application = applicationBuilder(Some(answers))
        .overrides(
          bind[SubmissionService].toInstance(mockSubmissionService),
          bind[AuditConnector].toInstance(mockAuditConnector)
        )
        .build()

      val auditDataCaptor = ArgumentCaptor.forClass(classOf[AuditData])

      val request = FakeRequest(GET, SubmissionController.submission.url)
      val result = route(application, request).value

      whenReady(result) {
        _ =>

          verify(mockAuditConnector, times(1))
            .sendExplicitAudit(
              eqTo("updateProfessionalSubscriptions"),
              auditDataCaptor.capture()
            )(any(), any(), any())

          val auditData = auditDataCaptor.getValue

          auditData.nino mustEqual fakeNino
          auditData.userAnswers mustEqual dataToAuditCurrent

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual ConfirmationCurrentController.onPageLoad().url
      }
      application.stop()
    }

    "when a year has no data submit psubs and redirect to ConfirmationCurrentController on success" in {

      when(mockSubmissionService.submitPSub(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val answers = userAnswersCurrent.set(AmountsAlreadyInCodePage, true).success.value
              .set(SavePSubs(getTaxYear(CurrentYearMinus1).toString), Seq.empty).success.value

      val application = applicationBuilder(Some(answers))
        .overrides(
          bind[SubmissionService].toInstance(mockSubmissionService),
          bind[AuditConnector].toInstance(mockAuditConnector)
        )
        .build()

      val auditDataCaptor = ArgumentCaptor.forClass(classOf[AuditData])

      val request = FakeRequest(GET, SubmissionController.submission.url)
      val result = route(application, request).value

      whenReady(result) {
        _ =>

          verify(mockAuditConnector, times(1))
            .sendExplicitAudit(
              eqTo("updateProfessionalSubscriptions"),
              auditDataCaptor.capture()
            )(any(), any(), any())

          val auditData = auditDataCaptor.getValue

          auditData.nino mustEqual fakeNino
          auditData.userAnswers mustEqual dataToAuditCurrent

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual ConfirmationCurrentController.onPageLoad().url
      }
      application.stop()
    }


    "submit psubs and redirect to ConfirmationPreviousController on success" in {

      when(mockSubmissionService.submitPSub(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val answers = userAnswersPrevious.set(AmountsAlreadyInCodePage, true).success.value
        .set(SavePSubs(getTaxYear(CurrentYear).toString), Seq.empty).success.value

      val application = applicationBuilder(Some(answers))
        .overrides(
          bind[SubmissionService].toInstance(mockSubmissionService),
          bind[AuditConnector].toInstance(mockAuditConnector)
        )
        .build()

      val captor = ArgumentCaptor.forClass(classOf[AuditData])

      val request = FakeRequest(GET, SubmissionController.submission.url)
      val result = route(application, request).value

      whenReady(result) {
        _ =>

          verify(mockAuditConnector, times(1))
            .sendExplicitAudit(
              eqTo("updateProfessionalSubscriptions"),
              captor.capture()
            )(any(), any(), any())

          val auditData = captor.getValue

          auditData.nino mustEqual fakeNino
          auditData.userAnswers mustEqual dataToAuditPrevious

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual ConfirmationPreviousController.onPageLoad().url
      }
      application.stop()
    }

    "submit psubs and redirect to ConfirmationCurrentPreviousController on success" in {

      when(mockSubmissionService.submitPSub(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val answers = userAnswersCurrentAndPrevious.set(AmountsAlreadyInCodePage, true).success.value

      val application = applicationBuilder(Some(answers))
        .overrides(
          bind[SubmissionService].toInstance(mockSubmissionService),
          bind[AuditConnector].toInstance(mockAuditConnector)
        )
        .build()

      val captor = ArgumentCaptor.forClass(classOf[AuditData])

      val request = FakeRequest(GET, SubmissionController.submission.url)
      val result = route(application, request).value

      whenReady(result) {
        _ =>

          verify(mockAuditConnector, times(1))
            .sendExplicitAudit(
              eqTo("updateProfessionalSubscriptions"),
              captor.capture()
            )(any(), any(), any())

          val auditData = captor.getValue

          auditData.nino mustEqual fakeNino
          auditData.userAnswers mustEqual dataToAuditCurrentAndPrevious

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual ConfirmationCurrentPreviousController.onPageLoad().url
      }
      application.stop()
    }

    "redirect to Technical Difficulties on fail" in {

      when(mockSubmissionService.submitPSub(any(), any(), any())(any(), any()))
        .thenReturn(Future.failed(new RuntimeException))

      val answers = userAnswersCurrentAndPrevious.set(AmountsAlreadyInCodePage, true).success.value

      val application = applicationBuilder(Some(answers))
        .overrides(bind[SubmissionService].toInstance(mockSubmissionService),
          bind[AuditConnector].toInstance(mockAuditConnector)
        )
        .build()

      val request = FakeRequest(GET, SubmissionController.submission.url)

      val result = route(application, request).value

      val captor = ArgumentCaptor.forClass(classOf[AuditData])

      whenReady(result) {
        _ =>

          verify(mockAuditConnector, times(1))
            .sendExplicitAudit(
              eqTo("updateProfessionalSubscriptionsFailure"),
              captor.capture()
            )(any(), any(), any())

          val auditData = captor.getValue

          auditData.nino mustEqual fakeNino
          auditData.userAnswers mustEqual dataToAuditCurrentAndPrevious

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual TechnicalDifficultiesController.onPageLoad.url
      }

      application.stop()

    }

    "redirect to session expired when all psubs are empty" in {

      when(mockSubmissionService.submitPSub(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val answers = emptyUserAnswers
        .set(TestSummarySubscriptionsPage, Map("2018" -> Seq.empty[PSub])).success.value

      val application = applicationBuilder(Some(answers))
        .overrides(
          bind[SubmissionService].toInstance(mockSubmissionService),
          bind[AuditConnector].toInstance(mockAuditConnector)
        )
        .build()

      val auditDataCaptor = ArgumentCaptor.forClass(classOf[AuditData])

      val request = FakeRequest(GET, SubmissionController.submission.url)
      val result = route(application, request).value

      whenReady(result) {
        _ =>

          verify(mockAuditConnector, times(0))
            .sendExplicitAudit(
              eqTo("updateProfessionalSubscriptions"),
              auditDataCaptor.capture()
            )(any(), any(), any())


          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url
      }
      application.stop()
    }
  }

}

object TestSummarySubscriptionsPage extends QuestionPage[Map[String, Seq[PSub]]] {
  override def path: JsPath = SummarySubscriptionsPage.path
}
