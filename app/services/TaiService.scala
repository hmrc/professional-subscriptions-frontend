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

import com.google.inject.Inject
import connectors.{CitizenDetailsConnector, TaiConnector}
import models.TaxYearSelection._
import models.{Employment, EmploymentExpense, TaxCodeRecord, TaxYearSelection}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class TaiService @Inject()(taiConnector: TaiConnector,
                           citizenDetailsConnector: CitizenDetailsConnector) {

  def taxCodeRecords(nino: String, year: Int)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TaxCodeRecord]] = {
    taiConnector.getTaxCodeRecords(nino, year)
  }

  def getEmployments(nino: String, year: Int)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Employment]] = {
    taiConnector.getEmployments(nino, year)
  }

  def getPsubAmount(taxYearSelection: Seq[TaxYearSelection], nino: String)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Map[Int, Seq[EmploymentExpense]]] = {

    val taxYears: Seq[Int] = taxYearSelection.map(getTaxYear)

    Future.sequence(
      taxYears map {
        taxYear =>
          taiConnector.getProfessionalSubscriptionAmount(nino, taxYear).map {
            psubAmount =>
              (taxYear, psubAmount)
          }
      }
    ).map(_.toMap)
  }

  def updatePsubAmount(nino: String, yearAndAmount: Seq[(Int, Int)])
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    syncSubmissions(yearAndAmount) {
      case (year, amount) =>
        citizenDetailsConnector.getEtag(nino) andThen {
          case Failure(e) => Logger.warn("etag invalid", e)
        } flatMap {
          response =>
            taiConnector.updateProfessionalSubscriptionAmount(nino, year, response.etag, amount)
        }
    }
  }

  private def syncSubmissions(inputs: Seq[Tuple2[Int, Int]])(flatMapFunction: Tuple2[Int, Int] => Future[Unit])
                             (implicit ec: ExecutionContext): Future[Unit] = {
    inputs.foldLeft(Future.successful[Unit](()))(
      (previousFutureResult, nextInput) =>
        previousFutureResult.flatMap {
          _ => flatMapFunction(nextInput)
        }
    )
  }
}
