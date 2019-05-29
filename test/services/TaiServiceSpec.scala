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
      "return a list of ProfessionalSubscriptionAmount on success for one tax year" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(EmploymentExpense(100))))

        val result: Future[Seq[ProfessionalSubscriptionAmount]] = taiService.getPsubAmount(Seq(CurrentYear), fakeNino)

        whenReady(result) {
          _ mustBe Seq(ProfessionalSubscriptionAmount(Some(EmploymentExpense(100)), TaxYearSelection.getTaxYear(CurrentYear)))
        }
      }

      "return a list of ProfessionalSubscriptionAmount on success for multiple tax years" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(any(), any())(any(), any()))
          .thenReturn(
            Future.successful(Seq(EmploymentExpense(100))),
            Future.successful(Seq(EmploymentExpense(200)))
          )

        val result: Future[Seq[ProfessionalSubscriptionAmount]] = taiService.getPsubAmount(Seq(CurrentYear, CurrentYearMinus1), fakeNino)

        whenReady(result) {
          _ mustBe Seq(
            ProfessionalSubscriptionAmount(Some(EmploymentExpense(100)), TaxYearSelection.getTaxYear(CurrentYear)),
            ProfessionalSubscriptionAmount(Some(EmploymentExpense(200)), TaxYearSelection.getTaxYear(CurrentYearMinus1))
          )
        }
      }
    }
  }

}
