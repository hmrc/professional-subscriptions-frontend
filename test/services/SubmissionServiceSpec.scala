/*
 * Copyright 2020 HM Revenue & Customs
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
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, eq => equalTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.time.TaxYear

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
  private val psubs1TotalAmount = 300
  private val psubs2 = Seq(PSub("psub3", 100, true, Some(10)))
  private val psubs2TotalAmount = 90
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

    when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
      .thenReturn(Future.successful[Unit](()))
  }

  "SubmissionService" when {
    "submitPSub" must {
      val beforeApril = new LocalDate(LocalDate.now.getYear, 2, 4)
      val afterApril = new LocalDate(LocalDate.now.getYear, 6, 4)
      val april5th = new LocalDate(LocalDate.now.getYear, 4, 5)

      "submit correct submission amounts when date is before April 6th and currentYear is passed in and no next year record" in {
        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(true)

        when(mockTaiConnector.isYearAvailable(any(), any())(any(), any()))
          .thenReturn(Future.successful(false))

        val result = submissionService.submitPSub(fakeNino, Map(TaxYear.current.startYear -> psubs1), beforeApril)

        whenReady(result) { _ =>
            val yearAndAmountCaptor = ArgumentCaptor.forClass[Seq[(Int, Int)]](classOf[Seq[(Int, Int)]])
            verify(mockTaiService, times(1)).updatePsubAmount(any(), yearAndAmountCaptor.capture())(any(), any())
            yearAndAmountCaptor.getValue must contain theSameElementsAs Seq(TaxYear.current.startYear -> psubs1TotalAmount)
        }
      }

      "submit correct submission amounts, including next tax year as copy of current year when date is before April 6th" +
        " and currentYear is passed in and next year record available" in {
        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(true)

        when(mockTaiConnector.isYearAvailable(any(), any())(any(), any()))
          .thenReturn(Future.successful(true))

        val result = submissionService.submitPSub(
          fakeNino,
          Map(TaxYear.current.startYear -> psubs2, TaxYear.current.back(1).startYear -> psubs1),
          beforeApril)

        whenReady(result) { _ =>
          val yearAndAmountCaptor = ArgumentCaptor.forClass[Seq[(Int, Int)]](classOf[Seq[(Int, Int)]])
          verify(mockTaiService, times(1)).updatePsubAmount(any(), yearAndAmountCaptor.capture())(any(), any())
          yearAndAmountCaptor.getValue must contain theSameElementsAs Seq(
            TaxYear.current.startYear -> psubs2TotalAmount,
            TaxYear.current.back(1).startYear -> psubs1TotalAmount,
            TaxYear.current.forwards(1).startYear -> psubs2TotalAmount)
        }
      }

      "submit correct submission amounts, , including next tax year as copy of current year " +
        " when date is in April, current year and next year record is available" in {
        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(true)

        when(mockTaiConnector.isYearAvailable(any(), any())(any(), any()))
          .thenReturn(Future.successful(true))

        val result = submissionService.submitPSub(fakeNino, Map(TaxYear.current.startYear -> psubs1), april5th)

        whenReady(result) { _ =>
            val yearAndAmountCaptor = ArgumentCaptor.forClass[Seq[(Int, Int)]](classOf[Seq[(Int, Int)]])
            verify(mockTaiService, times(1)).updatePsubAmount(any(), yearAndAmountCaptor.capture())(any(), any())
            yearAndAmountCaptor.getValue must contain theSameElementsAs Seq(
              TaxYear.current.startYear -> psubs1TotalAmount,
              TaxYear.current.forwards(1).startYear -> psubs1TotalAmount)
        }
      }

      "submit correct submission amounts when date is after April, current year selected" in {
        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(true)

        val result = submissionService.submitPSub(fakeNino, Map(TaxYear.current.startYear -> psubs1), afterApril)

        whenReady(result) { _ =>
            val yearAndAmountCaptor = ArgumentCaptor.forClass[Seq[(Int, Int)]](classOf[Seq[(Int, Int)]])
            verify(mockTaiService, times(1)).updatePsubAmount(any(), yearAndAmountCaptor.capture())(any(), any())
            yearAndAmountCaptor.getValue must contain theSameElementsAs Map(TaxYear.current.startYear -> psubs1TotalAmount)
        }
      }

      "submit correct data when no current year in selection" in {
        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(true)

        when(mockTaiConnector.isYearAvailable(any(), any())(any(), any()))
          .thenReturn(Future.successful(false))

        val result = submissionService.submitPSub(
          fakeNino,
          Map(TaxYear.current.back(1).startYear -> psubs1,
              TaxYear.current.back(2).startYear -> psubs2),
          beforeApril)

        whenReady(result) { _ =>
            val yearAndAmountCaptor = ArgumentCaptor.forClass[Seq[(Int, Int)]](classOf[Seq[(Int, Int)]])
            verify(mockTaiService, times(1)).updatePsubAmount(any(), yearAndAmountCaptor.capture())(any(), any())
            yearAndAmountCaptor.getValue must contain theSameElementsAs Map(
              TaxYear.current.back(1).startYear -> psubs1TotalAmount,
              TaxYear.current.back(2).startYear -> psubs2TotalAmount)
        }
      }

      "return future success when submitPsub succeeds" in {
        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful[Unit](()))

        when(mockTaiConnector.isYearAvailable(any(), any())(any(), any()))
          .thenReturn(Future.successful(true))

        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(true)

        val result = submissionService.submitPSub(fakeNino, psubsByYear)

        whenReady(result) {
          _ => succeed
        }
      }

      "return future failed when exception" in {
        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException))

        when(mockTaiConnector.isYearAvailable(any(), any())(any(), any()))
          .thenReturn(Future.successful(true))

        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(false)

        val result = submissionService.submitPSub(fakeNino, psubsByYear)

        whenReady(result.failed) {
          e =>
            e mustBe a[RuntimeException]
        }
      }

      "Sends years and subscriptions to TaiService for submission, excluding empty years" in {
        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful[Unit](()))

        when(mockTaiConnector.isYearAvailable(any(), any())(any(), any()))
          .thenReturn(Future.successful(true))

        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(true)

        val result = submissionService.submitPSub(fakeNino, psubsByYearWithEmptyYear)

        whenReady(result) {
          _ => {
            if(TaxYear.current.startYear == LocalDate.now.getYear){
              val expectedSubmission = Seq (
                TaxYear.current.startYear -> 300
              )
              verify(mockTaiService, times(1)).updatePsubAmount(any(), equalTo(expectedSubmission))(any(), any())

            } else {
              val expectedSubmission = Seq (
                TaxYear.current.startYear -> 300,
                TaxYear.current.finishYear -> 300
              )
              verify(mockTaiService, times(1)).updatePsubAmount(any(), equalTo(expectedSubmission))(any(), any())
            }

          }
        }
      }

      "Return failed future when psub data is invalid due to year out of range" in {
        when(mockTaiConnector.isYearAvailable(any(), any())(any(), any()))
          .thenReturn(Future.successful(true))

        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(false)

        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful[Unit](()))

        val result = submissionService.submitPSub(fakeNino, psubsWithOneYear)

        whenReady(result.failed) {
          e =>
            e mustBe an[SubmissionValidationException]
            e.getMessage mustBe "Invalid Psubs"
        }
      }

      "Return failed future when psub data is invalid because of duplicate subscription" in {
        when(mockTaiService.updatePsubAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful[Unit](()))

        when(mockTaiConnector.isYearAvailable(any(), any())(any(), any()))
          .thenReturn(Future.successful(true))

        when(mockProfessionalBodiesService.validateYearInRange(any[Seq[String]](), any()))
          .thenReturn(true)

        val result = submissionService.submitPSub(fakeNino, psubsWithDuplicatePsubs)

        whenReady(result.failed) {
          e =>
            e mustBe an[SubmissionValidationException]
            e.getMessage mustBe "Invalid Psubs"
        }
      }
    }
  }
}
