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

package connectors

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import javax.inject.Singleton
import models._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class TaiConnectorImpl @Inject()(appConfig: FrontendAppConfig, httpClient: HttpClient) extends TaiConnector {

  override def getEmployments(nino: String, taxYear: String)
                             (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[Seq[Employment]] = {

    val taiEmploymentsUrl = s"${appConfig.taiHost}/tai/$nino/employments/years/$taxYear"

    httpClient.GET[Seq[Employment]](taiEmploymentsUrl)
  }

  override def getProfessionalSubscriptionAmount(nino: String, taxYear: Int)
                                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[EmploymentExpense]] = {

    val taiProfessionalExpensesUrl: String = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/expenses/employee-expenses/57"

    httpClient.GET[Seq[EmploymentExpense]](taiProfessionalExpensesUrl)
  }

  override def updateProfessionalSubscriptionAmount(nino: String, taxYear: Int, version: Int, grossAmount: Int)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    val taiUrl: String = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/expenses/flat-rate-expenses"

    val body: IabdEditDataRequest = IabdEditDataRequest(version, grossAmount)

    httpClient.POST[IabdEditDataRequest, HttpResponse](taiUrl, body)
  }

  override def taiTaxAccountSummary(nino: String, taxYear: Int)
                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    val taiUrl: String = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/summary"

    httpClient.GET[HttpResponse](taiUrl)
  }

  override def getTaxCodeRecord(nino: String, taxYear: Int)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TaxCodeRecord]] = {

    val taiTaxCodeIncomes = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/income/tax-code-incomes"

    httpClient.GET[Seq[TaxCodeRecord]](taiTaxCodeIncomes)
  }

}

@ImplementedBy(classOf[TaiConnectorImpl])
trait TaiConnector {
  def getEmployments(nino: String, taxYear: String)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Employment]]

  def getProfessionalSubscriptionAmount(nino: String, taxYear: Int)
                                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[EmploymentExpense]]

  def getTaxCodeRecord(nino: String, taxYear: Int)
                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TaxCodeRecord]]

  def taiTaxAccountSummary(nino: String, year: Int)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]

  def updateProfessionalSubscriptionAmount(nino: String, year: Int, version: Int, grossAmount: Int)
                                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]
}

