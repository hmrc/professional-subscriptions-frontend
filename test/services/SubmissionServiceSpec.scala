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

package services

import base.SpecBase
import connectors.TaiConnector
import models.{PSub, SubmissionValidationException}
import models.TaxYearSelection._
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with BeforeAndAfterEach with IntegrationPatience {
  private val mockTaiService = mock[TaiService]
  private val mockTaiConnector = mock[TaiConnector]
  private val mockProfessionalBodiesService = mock[ProfessionalBodiesService]
  private val submissionService = new SubmissionService(mockTaiService, mockTaiConnector, mockProfessionalBodiesService)

  private val currentTaxYear = Seq(CurrentYear)
  private val taxYearsWithCurrentYear = Seq(CurrentYear, CurrentYearMinus1)
  private val taxYearsWithoutCurrentYear = Seq(CurrentYearMinus1, CurrentYearMinus2)
  private val psubs1 = Seq(PSub("psub1", 100, false, None), PSub("psub2", 250, true, Some(50)))
  private val psubs2 = Seq(PSub("psub3", 100, true, Some(10)))
  private val duplicatePsubs = Seq(PSub("psub1", 100, false, None), PSub("psub1", 100, false, None))
  private val emptyPsubs = Seq.empty
  private val psubsByYear = Map(getTaxYear(CurrentYear) -> psubs1, getTaxYear(CurrentYearMinus1) -> psubs2)
  private val psubsWithOneYear = Map(getTaxYear(CurrentYear) -> psubs1)
  private val psubsWithDuplicatePsubs = Map(getTaxYear(CurrentYear) -> duplicatePsubs)
  private val psubsByYearWithEmptyYear = Map(getTaxYear(CurrentYear) -> psubs1, getTaxYear(CurrentYearMinus1) -> emptyPsubs)

  override def beforeEach(): Unit = {
    reset(mockTaiConnector)
    reset(mockTaiService)
    reset(mockProfessionalBodiesService)
  }

  "SubmissionService" when {
    "getTaxYearsToUpdate" must {
      val beforeApril = new LocalDate(LocalDate.now.getYear, 2, 4)
      val afterApril = new LocalDate(LocalDate.now.getYear, 6, 4)
      val april5th = new LocalDate(LocalDate.now.getYear, 4, 5)

      "return correct taxYears when date is before April 6th and currentYear is passed in and no next year record" in {

        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(400)))

        val result = submissionService.getTaxYearsToUpdate(fakeNino, currentTaxYear, beforeApril)

        whenReady(result) {
          result =>
            result.length mustBe 1
            result.contains(CurrentYear) mustBe true
        }
      }

      "return correct taxYear when date is before April 6th and currentYear is passed in and next year record available" in {

        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200)))

        val result = submissionService.getTaxYearsToUpdate(fakeNino, taxYearsWithCurrentYear, beforeApril)

        whenReady(result) {
          result =>
            result.length mustBe 3
            result.contains(CurrentYear) mustBe true
            result.contains(NextYear) mustBe true
            result.contains(CurrentYearMinus1) mustBe true
        }
      }

      "return correct data when date is in April, current year and next year record is available" in {
        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200)))

        val result = submissionService.getTaxYearsToUpdate(fakeNino, currentTaxYear, april5th)

        whenReady(result) {
          result =>
            result.length mustBe 2
            result.contains(CurrentYear) mustBe true
            result.contains(NextYear) mustBe true
        }
      }

      "return correct data when date is after April, current year selected" in {

        val result = submissionService.getTaxYearsToUpdate(fakeNino, currentTaxYear, afterApril)

        whenReady(result) {
          result =>
            result.length mustBe 1
            result.contains(CurrentYear) mustBe true
        }
      }

      "return correct data when no current year in selection" in {
        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(500)))

        val result = submissionService.getTaxYearsToUpdate(fakeNino, taxYearsWithoutCurrentYear, beforeApril)

        whenReady(result) {
          result =>
            result.length mustBe 2
            result.contains(CurrentYearMinus1) mustBe true
            result.contains(CurrentYearMinus2) mustBe true
        }
      }

    }

    "submitPSub" must {
      "return true when give 204 response" in {
        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(HttpResponse(204))))

        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200)))

        when(mockProfessionalBodiesService.validateYearInRange(any(), any())(any()))
          .thenReturn(Future.successful(false))

        val result: Future[Seq[HttpResponse]] = submissionService.submitPSub(fakeNino, taxYearsWithCurrentYear, psubsByYear)

        whenReady(result) {
          res =>
            res mustBe a[Seq[_]]
            res.head.status mustBe 204
        }
      }

      "return false when give 500 response" in {
        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(HttpResponse(500))))

        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200)))

        when(mockProfessionalBodiesService.validateYearInRange(any(), any())(any()))
          .thenReturn(Future.successful(false))

        val result = submissionService.submitPSub(fakeNino, taxYearsWithCurrentYear, psubsByYear)

        whenReady(result) {
          res =>
            res mustBe a[Seq[_]]
            res.head.status mustBe 500
        }
      }

      "When the year key is present in the data, submit years that have psub data" in {
        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(HttpResponse(204))))

        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200)))

        when(mockProfessionalBodiesService.validateYearInRange(any(), any())(any()))
          .thenReturn(Future.successful(false))

        val result: Future[Seq[HttpResponse]] = submissionService.submitPSub(fakeNino, taxYearsWithCurrentYear, psubsByYearWithEmptyYear)

        whenReady(result) {
          _ =>
            verify(mockTaiService, times(1)).updatePsubAmount(any(), any())(any(), any())
        }
      }

      "When year key is not present in the data, submit years that have psub data " in {
        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(HttpResponse(204))))

        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200)))

        when(mockProfessionalBodiesService.validateYearInRange(any(), any())(any()))
          .thenReturn(Future.successful(false))

        val result: Future[Seq[HttpResponse]] = submissionService.submitPSub(fakeNino, taxYearsWithCurrentYear, psubsWithOneYear)

        whenReady(result) {
          _ =>
            verify(mockTaiService, times(1)).updatePsubAmount(any(), any())(any(), any())
        }
      }

      "Return failed future when psub data is invalid due to year out of range" in {
        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200)))

        when(mockProfessionalBodiesService.validateYearInRange(any(), any())(any()))
          .thenReturn(Future.failed(SubmissionValidationException("Year out of range")))

        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(HttpResponse(204))))

        val result = submissionService.submitPSub(fakeNino, currentTaxYear, psubsWithOneYear)

        whenReady(result.failed) {
          e =>
            e mustBe an[SubmissionValidationException]
            e.getMessage mustBe "Year out of range"
        }
      }

      "Return failed future when psub data is invalid due duplicate subscription" in {
        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(HttpResponse(204))))

        when(mockTaiConnector.taiTaxAccountSummary(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200)))

        when(mockProfessionalBodiesService.validateYearInRange(any(), any())(any()))
          .thenReturn(Future.successful(true))

        val result: Future[Seq[HttpResponse]] = submissionService.submitPSub(fakeNino, taxYearsWithCurrentYear, psubsWithDuplicatePsubs)

        whenReady(result.failed) {
          e =>
            e mustBe an[SubmissionValidationException]
            e.getMessage mustBe "Duplicate Psubs"
        }
      }
    }
  }
}
