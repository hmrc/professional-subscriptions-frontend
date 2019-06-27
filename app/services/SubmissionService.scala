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

import connectors.TaiConnector
import javax.inject.Inject
import models.{PSub, TaxYearSelection}
import models.TaxYearSelection._
import org.joda.time.LocalDate
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.time.TaxYear

import scala.concurrent.{ExecutionContext, Future}

class SubmissionService @Inject()(
                                   taiService: TaiService,
                                   taiConnector: TaiConnector
                                 ) {

  def getTaxYearsToUpdate(nino: String, taxYears: Seq[TaxYearSelection], currentDate: LocalDate = LocalDate.now)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TaxYearSelection]] = {

    if (taxYears.contains(CurrentYear) && (currentDate.getMonthOfYear < 4 || (currentDate.getMonthOfYear == 4 && currentDate.getDayOfMonth < 6))) {
      taiConnector.taiTaxAccountSummary(nino, TaxYear.current.currentYear + 1).map {
        _.status match {
          case 200 =>
            taxYears :+ NextYear
          case _ =>
            taxYears
        }
      }.recoverWith {
        case e: Exception =>
          Logger.warn(s"[SubmissionService][getTaxYearsToUpdate] ${e.getMessage}")
          Future.successful(taxYears)
      }
    } else {
      Future.successful(taxYears)
    }
  }

  def submitPSub(nino: String, taxYears: Seq[TaxYearSelection], claimAmount: Int)
               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[HttpResponse]] = {

    getTaxYearsToUpdate(nino, taxYears).flatMap {
      claimYears =>
        futureSequence(claimYears) {
          taxYearSelection =>
            val taxYear = TaxYearSelection.getTaxYear(taxYearSelection)
            taiService.updatePsubAmount(nino, taxYear, claimAmount)
        }
    }
  }

  private def futureSequence[I, O](inputs: Seq[I])(flatMapFunction: I => Future[O])
                                  (implicit ec: ExecutionContext): Future[Seq[O]] =
    inputs.foldLeft(Future.successful(Seq.empty[O]))(
      (previousFutureResult, nextInput) =>
        for {
          futureSeq <- previousFutureResult
          future <- flatMapFunction(nextInput)
        } yield futureSeq :+ future
    )
}
