/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import javax.inject.Singleton
import models._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaiConnector @Inject()(appConfig: FrontendAppConfig, httpClient: HttpClient) {

  def getEmployments(nino: String, taxYear: Int)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Employment]] = {

    val taiUrl = s"${appConfig.taiHost}/tai/$nino/employments/years/$taxYear"

    httpClient.GET[Seq[Employment]](taiUrl).recover{case _ => Seq.empty}
  }

  def getProfessionalSubscriptionAmount(nino: String, taxYear: Int)
                                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Int]] = {

    val taiUrl: String = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/expenses/employee-expenses/57"

    httpClient.GET[Seq[EmploymentExpense]](taiUrl)
      .map(_.headOption.map(_.grossAmount))
  }

  def updateProfessionalSubscriptionAmount(nino: String, taxYear: Int, version: Int, grossAmount: Int)
                                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    val taiUrl: String = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/expenses/employee-expenses/57"

    val body: IabdEditDataRequest = IabdEditDataRequest(version, grossAmount)

    httpClient.POST[IabdEditDataRequest, HttpResponse](taiUrl, body).map{ _ => ()}
  }

  def isYearAvailable(nino: String, taxYear: Int)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {

    val taiUrl: String = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/summary"

    httpClient.GET[HttpResponse](taiUrl).map{_ => true}.recover{case _ => false}
  }

  def getTaxCodeRecords(nino: String, taxYear: Int)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TaxCodeRecord]] = {

    val taiUrl = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/income/tax-code-incomes"

    httpClient.GET[Seq[TaxCodeRecord]](taiUrl).recover{case _ => Seq.empty}
  }

}
