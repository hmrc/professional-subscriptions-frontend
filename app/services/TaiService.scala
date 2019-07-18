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
import models.{ETag, Employment, EmploymentExpense, TaxYearSelection}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class TaiService @Inject()(taiConnector: TaiConnector,
                           citizenDetailsConnector: CitizenDetailsConnector) {

  def getEmployments(nino: String, taxYearSelection: TaxYearSelection)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Employment]] = {

    val taxYear = TaxYearSelection.getTaxYear(taxYearSelection).toString

    taiConnector.getEmployments(nino, taxYear)
  }

  def getPsubAmount(taxYearSelection: Seq[TaxYearSelection], nino: String)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Map[Int, Seq[EmploymentExpense]]] = {

    val taxYears: Seq[Int] = taxYearSelection.map(TaxYearSelection.getTaxYear)

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

  def updatePsubAmount(nino: String, year: Int, grossAmount: Int)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    citizenDetailsConnector.getEtag(nino).flatMap {
      response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[ETag] match {
              case JsSuccess(body, _) =>
                taiConnector.updateProfessionalSubscriptionAmount(nino, year, body.etag, grossAmount)
              case JsError(e) =>
                Logger.warn(s"[TaiService.updatePsubAmount][CitizenDetailsConnector.getEtag] failed to parse Json to ETag: $e")
                Future.successful(response)
            }
          case _ =>
            Future.successful(response)
        }
    }
  }
}
