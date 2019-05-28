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
import connectors.TaiConnector
import models.ProfessionalSubscriptionOptions._
import models.{Employment, ProfessionalSubscriptionAmount, ProfessionalSubscriptionOptions, TaxYearSelection}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TaiService @Inject()(taiConnector: TaiConnector){

  def getEmployments(nino: String, taxYearSelection: TaxYearSelection)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Employment]] = {

    val taxYear = TaxYearSelection.getTaxYear(taxYearSelection).toString

    taiConnector.getEmployments(nino, taxYear)
  }

  def getPsubAmount(taxYearSelection: Seq[TaxYearSelection], nino: String)
                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ProfessionalSubscriptionAmount]] = {

    val taxYears: Seq[Int] = taxYearSelection.map(TaxYearSelection.getTaxYear)

    Future.sequence(taxYears map {
        taxYear =>
          taiConnector.getProfessionalSubscriptionAmount(nino, taxYear).map {
            psubAmount =>
              ProfessionalSubscriptionAmount(psubAmount.headOption, taxYear)
          }
      })
  }

  def psubResponse(taxYears: Seq[TaxYearSelection], nino: String, claimAmount: Int)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ProfessionalSubscriptionOptions] = {

    getPsubAmount(taxYears, nino).map {
      case freSeq if freSeq.forall(_.psubAmount.isEmpty) =>
        PSNoYears
      case freSeq if freSeq.exists(_.psubAmount.isEmpty) && freSeq.filterNot(_.psubAmount.isEmpty).forall(_.psubAmount.get.grossAmount == 0) =>
        PSNoYears
      case freSeq if freSeq.forall(_.psubAmount.isDefined) && freSeq.forall(_.psubAmount.get.grossAmount == 0) =>
        PSNoYears
      case freSeq if freSeq.forall(_.psubAmount.isDefined) && freSeq.forall(_.psubAmount.get.grossAmount == claimAmount) =>
        PSAllYearsAllAmountsSameAsClaimAmount
      case freSeq if freSeq.exists(_.psubAmount.isDefined) && freSeq.filterNot(_.psubAmount.isEmpty).exists(_.psubAmount.get.grossAmount > 0) =>
        PSSomeYears
      case _ =>
        TechnicalDifficulties
    }
  }
}
