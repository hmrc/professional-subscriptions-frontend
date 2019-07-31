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
import connectors.{CitizenDetailsConnector, TaiConnector}
import models.TaxCodeStatus.Live
import models.TaxYearSelection._
import models.{ETag, EmploymentExpense, TaxCodeRecord, TaxYearSelection}
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

class TaiServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with IntegrationPatience {

  private val mockTaiConnector = mock[TaiConnector]
  private val mockCitizenDetailsConnector = mock[CitizenDetailsConnector]
  private val currentYearInt = getTaxYear(CurrentYear)
  private val taiService = new TaiService(mockTaiConnector, mockCitizenDetailsConnector)

  "TaiService" must {
    "getTaxCodeRecords" when {
      "return seq of TaxCodeRecords" in {
        val taxCodeRecords = Seq(TaxCodeRecord("1150L", Live))

        when(mockTaiConnector.getTaxCodeRecords(fakeNino, currentYearInt))
          .thenReturn(Future.successful(taxCodeRecords))

        val result = taiService.taxCodeRecords(fakeNino, currentYearInt)

        whenReady(result) {
          _ mustBe taxCodeRecords
        }
      }
    }

    "when getEmployments" when {
      "return a sequence of employments" in {
        when(mockTaiConnector.getEmployments(fakeNino, currentYearInt))
          .thenReturn(Future.successful(taiEmployment))

        val result = taiService.getEmployments(fakeNino, currentYearInt)

        whenReady(result) {
          result =>
            result mustBe taiEmployment
        }
      }

      "return an exception on future failed" in {
        when(mockTaiConnector.getEmployments(fakeNino, currentYearInt))
          .thenReturn(Future.failed(new RuntimeException))

        val result = taiService.getEmployments(fakeNino, currentYearInt)

        whenReady(result.failed) {
          result =>
            result mustBe a[Exception]
        }
      }
    }

    "getPsubAmount" must {
      "return a Map of tax year to sequence of employments on success for one tax year" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(any(), any())(any(), any()))
          .thenReturn(Future.successful(Seq(EmploymentExpense(100))))

        val result: Future[Map[Int, Seq[EmploymentExpense]]] = taiService.getPsubAmount(Seq(CurrentYear), fakeNino)

        whenReady(result) {
          _ mustBe
            Map(
              TaxYearSelection.getTaxYear(CurrentYear) -> Seq(EmploymentExpense(100))
            )
        }
      }

      "return a Map of tax year to sequence of employments on success for multiple tax years" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(any(), any())(any(), any()))
          .thenReturn(
            Future.successful(Seq(EmploymentExpense(100))),
            Future.successful(Seq(EmploymentExpense(200)))
          )

        val result: Future[Map[Int, Seq[EmploymentExpense]]] = taiService.getPsubAmount(Seq(CurrentYear, CurrentYearMinus1), fakeNino)

        whenReady(result) {
          _ mustBe
            Map(
              TaxYearSelection.getTaxYear(CurrentYear) -> Seq(EmploymentExpense(100)),
              TaxYearSelection.getTaxYear(CurrentYearMinus1) -> Seq(EmploymentExpense(200))
            )
        }
      }
    }

    "updatePsubAmount" when {
      "must succeed on successful update" in {
        when(mockCitizenDetailsConnector.getEtag(fakeNino))
          .thenReturn(Future.successful(ETag(etag)))
        when(mockTaiConnector.updateProfessionalSubscriptionAmount(fakeNino, taxYearInt, etag, 100))
          .thenReturn(Future.successful[Unit](()))

        val result = taiService.updatePsubAmount(fakeNino, Seq(taxYearInt -> 100))

        whenReady(result) { _ =>
          succeed
        }
      }

      "must exception on failed tai PSub update" in {
        when(mockCitizenDetailsConnector.getEtag(fakeNino))
          .thenReturn(Future.successful(ETag(etag)))
        when(mockTaiConnector.updateProfessionalSubscriptionAmount(fakeNino, taxYearInt, etag, 100))
          .thenReturn(Future.failed(new RuntimeException))

        val result = taiService.updatePsubAmount(fakeNino, Seq(taxYearInt -> 100))

        whenReady(result.failed) {
          _ mustBe a[RuntimeException]
        }
      }

      "must exception on failed citizen details ETag request" in {
        when(mockCitizenDetailsConnector.getEtag(fakeNino))
          .thenReturn(Future.failed(new RuntimeException))

        when(mockTaiConnector.updateProfessionalSubscriptionAmount(fakeNino, taxYearInt, etag, 100))
          .thenReturn(Future.successful[Unit](()))

        val result = taiService.updatePsubAmount(fakeNino, Seq(taxYearInt -> 100))

        whenReady(result.failed) {
          _ mustBe a[RuntimeException]
        }
      }
    }
  }

}
