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
import models.TaxYearSelection._
import models.{PSub, SubmissionValidationException, TaxYearSelection}
import org.joda.time.LocalDate
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.time.TaxYear
import utils.PSubsUtil._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

class SubmissionService @Inject()(
                                   taiService: TaiService,
                                   taiConnector: TaiConnector,
                                   professionalBodiesService: ProfessionalBodiesService
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
          Logger.warn(s"[SubmissionService][getTaxYearsToUpdate]: ${e.getMessage}")
          Future.successful(taxYears)
      }
    } else {
      Future.successful(taxYears)
    }
  }

  def submitPSub(nino: String, taxYears: Seq[TaxYearSelection], subscriptions: Map[Int, Seq[PSub]])
                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[HttpResponse]] = {

    getTaxYearsToUpdate(nino, taxYears).flatMap {
      claimYears => {
        Future.sequence(subscriptions.map {
          case (year, psubs) =>
            if (isDuplicateInSeqPsubs(psubs)) Future.failed(SubmissionValidationException("Duplicate Psubs"))
            else professionalBodiesService.validateYearInRange(psubs.map(_.name), year)
        }).flatMap[Seq[HttpResponse]] { _ =>
          val psubsToUpdate: Seq[(Int, Int)] = for {
            year <- claimYears
            psubs <- subscriptions.get(getTaxYear(year)).filter(_.nonEmpty)
          } yield getTaxYear(year) -> claimAmountMinusDeductions(psubs)

          taiService.updatePsubAmount(nino, psubsToUpdate)
        }
      }
    }
  }
}
