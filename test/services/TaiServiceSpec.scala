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
import models.{ETag, TaxCodeRecord}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.collection.JavaConversions.collectionAsScalaIterable

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
          .thenReturn(Future.successful(100))

        val result = taiService.getPsubAmount(Seq(CurrentYear), fakeNino)

        whenReady(result) {
          _ mustBe
            Map(
              getTaxYear(CurrentYear) -> 100
            )
        }
      }

      "return a Map of tax year to sequence of employments on success for multiple tax years" in {
        when(mockTaiConnector.getProfessionalSubscriptionAmount(any(), any())(any(), any()))
          .thenReturn(
            Future.successful(100),
            Future.successful(200)
          )

        val result = taiService.getPsubAmount(Seq(CurrentYear, CurrentYearMinus1), fakeNino)

        whenReady(result) {
          _ mustBe
            Map(
              getTaxYear(CurrentYear) -> 100,
              getTaxYear(CurrentYearMinus1) -> 200
            )
        }
      }
    }

    "updatePsubAmount" when {
      "called must submit a separate etag and update pair for each submitted year" in  {
        when(mockCitizenDetailsConnector.getEtag(any())(any(), any())).thenReturn(Future.successful(ETag(4534)), Future.successful(ETag(8989)))
        when(mockTaiConnector.updateProfessionalSubscriptionAmount(any(), any(), any(), any())(any(), any())).thenReturn(Future.successful[Unit]())

        val result = taiService.updatePsubAmount(fakeNino, Seq(1967 -> 234, 1978 -> 563))

        whenReady(result) { _ =>
          val captor = ArgumentCaptor.forClass(classOf[Int])
          verify(mockCitizenDetailsConnector, times(2)).getEtag(any())(any(), any())
          verify(mockTaiConnector, times(2)).updateProfessionalSubscriptionAmount(any(), any(), captor.capture(), any())(any(), any())
          val etags = captor.getAllValues
          etags.toSeq must contain theSameElementsInOrderAs Seq(4534, 8989)
        }

      }

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
