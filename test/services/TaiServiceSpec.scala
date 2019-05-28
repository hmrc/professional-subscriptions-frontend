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
import models.ProfessionalSubscriptionOptions.{PSAllYearsAllAmountsSameAsClaimAmount, PSNoYears, PSSomeYears}
import models.TaxYearSelection._
import models.{EmploymentExpense, ProfessionalSubscriptionAmount, TaxYearSelection}
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaiServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val mockTaiConnector = mock[TaiConnector]
  private val currentYearInt = getTaxYear(CurrentYear).toString
  private val taiService = new TaiService(mockTaiConnector)

  "TaiService" must {
    "when getEmployments" when {
      "return a sequence of employments" in {
        when(mockTaiConnector.getEmployments(fakeNino, currentYearInt))
          .thenReturn(Future.successful(taiEmployment))

        val result = taiService.getEmployments(fakeNino, CurrentYear)

        whenReady(result) {
          result =>
            result mustBe taiEmployment
        }
      }

      "return an exception on future failed" in {
        when(mockTaiConnector.getEmployments(fakeNino, currentYearInt))
          .thenReturn(Future.failed(new RuntimeException))

        val result = taiService.getEmployments(fakeNino, CurrentYear)

        whenReady(result.failed) {
          result =>
            result mustBe a[Exception]
        }
      }
    }

    "getPsubAmount" must {
      "return a Future[Seq[ProfessionalSubscriptionAmount]] on success" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(EmploymentExpense(100))))

        val result: Future[Seq[ProfessionalSubscriptionAmount]] = taiService.getPsubAmount(Seq(CurrentYear), fakeNino)

        whenReady(result) {
          _ mustBe Seq(ProfessionalSubscriptionAmount(Some(EmploymentExpense(100)), TaxYearSelection.getTaxYear(CurrentYear)))
        }
      }
    }

    "psubResponse" must {

      "return PSNoYears when only 200 and empty sequences are returned for all tax years" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(Seq.empty))
          .thenReturn(Future.successful(Seq.empty))

        val result = taiService.psubResponse(Seq(CurrentYear, CurrentYearMinus1), fakeNino, claimAmount = 100)

        whenReady(result) {
          _ mustBe PSNoYears
        }
      }

      "return PSNoYears when 0 employment expense is returned for all tax years" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(EmploymentExpense(0))))
          .thenReturn(Future.successful(Seq(EmploymentExpense(0))))

        val result = taiService.psubResponse(Seq(CurrentYear, CurrentYearMinus1), fakeNino, claimAmount = 100)

        whenReady(result) {
          _ mustBe PSNoYears
        }
      }

      "return PSNoYears when psubResponse contains combination of undefined and 0 amounts" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(EmploymentExpense(0))))
          .thenReturn(Future.successful(Seq.empty))

        val result = taiService.psubResponse(Seq(CurrentYear, CurrentYearMinus1), fakeNino, claimAmount = 100)

        whenReady(result) {
          _ mustBe PSNoYears
        }
      }

      "return PSAllYearsAllAmountsSameAsClaimAmount grossAmount is the same as claimAmount for all tax years" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(EmploymentExpense(100))))
          .thenReturn(Future.successful(Seq(EmploymentExpense(100))))

        val result = taiService.psubResponse(Seq(CurrentYear, CurrentYearMinus1), fakeNino, claimAmount = 100)

        whenReady(result) {
          _ mustBe PSAllYearsAllAmountsSameAsClaimAmount
        }
      }

      "return PSSomeYears for all other combinations of freAmount (empty, 0, > 0)" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(EmploymentExpense(100))))
          .thenReturn(Future.successful(Seq(EmploymentExpense(60))))
          .thenReturn(Future.successful(Seq(EmploymentExpense(0))))
          .thenReturn(Future.successful(Seq.empty))

        val result = taiService.psubResponse(Seq(CurrentYear, CurrentYearMinus1, CurrentYearMinus2, CurrentYearMinus3), fakeNino, claimAmount = 200)

        whenReady(result) {
          _ mustBe PSSomeYears
        }
      }
    }
  }
}
