/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{Employment, TaxCodeRecord, TaxYearSelection}
import play.api.Logger.logger
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
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Map[Int, Int]] = {

    val taxYears: Seq[Int] = taxYearSelection.map(getTaxYear)

    Future.sequence(taxYears.map(taiConnector.getProfessionalSubscriptionAmount(nino, _)))
      .map(taxYears.zip(_).map {
        case (taxYear, amount) => (taxYear, amount.getOrElse(0))
      })
      .map(_.toMap)
  }

  def updatePsubAmount(nino: String, yearAndAmount: Seq[(Int, Int)])
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    syncSubmissions(yearAndAmount) {
      case (year, amount) =>
        citizenDetailsConnector.getEtag(nino) andThen {
          case Failure(e) => logger.warn("etag invalid", e)
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
