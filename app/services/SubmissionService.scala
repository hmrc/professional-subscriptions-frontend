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
import models.{PSub, SubmissionValidationException}
import org.joda.time.LocalDate
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.time.TaxYear
import utils.PSubsUtil._

import scala.concurrent.{ExecutionContext, Future}

class SubmissionService @Inject()(
                                   taiService: TaiService,
                                   taiConnector: TaiConnector,
                                   professionalBodiesService: ProfessionalBodiesService
                                 ) {

  private def getSubscriptionsToUpdate(nino: String, subscriptions: Map[Int, Seq[PSub]], currentDate: LocalDate)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Map[Int, Seq[PSub]]] = {
    subscriptions.get(TaxYear.current.startYear).map { currentYearsSubscriptions =>
      if (nextTaxYearIsApproaching(currentDate)) {
        taiConnector.isYearAvailable(nino, TaxYear.current.forwards(1).startYear).map {
          case true => subscriptions + (TaxYear.current.forwards(1).startYear -> currentYearsSubscriptions)
          case false => subscriptions
        }
      } else {Future.successful(subscriptions)}
    }.getOrElse(Future.successful(subscriptions))
  }

  private def nextTaxYearIsApproaching(currentDate: LocalDate) = {
    currentDate.getMonthOfYear < 4 || (currentDate.getMonthOfYear == 4 && currentDate.getDayOfMonth < 6)
  }

  def submitPSub(nino: String, subscriptions: Map[Int, Seq[PSub]], currentDate: LocalDate = LocalDate.now)
                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    getSubscriptionsToUpdate(nino, subscriptions, currentDate).flatMap {
      subscriptionsToUpdate => {
        Future.sequence(subscriptionsToUpdate.map {
          case (year, psubs) =>
            if (isDuplicateInSeqPsubs(psubs)) {Future.failed(SubmissionValidationException("Duplicate Psubs"))}
            else {professionalBodiesService.validateYearInRange(psubs.map(_.name), year)}
        }).flatMap[Unit] { _ =>
          val amountsToSubmit : Seq[(Int, Int)] = subscriptionsToUpdate.filterNot(_._2.isEmpty).map {
            case (year, subscriptionsForYear) => (year, claimAmountMinusDeductions(subscriptionsForYear))
          }.toSeq

          taiService.updatePsubAmount(nino, amountsToSubmit)
        }
      }
    }
  }
}
