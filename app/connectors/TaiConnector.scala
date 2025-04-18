/*
 * Copyright 2023 HM Revenue & Customs
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
import models._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, readRaw}

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaiConnector @Inject() (appConfig: FrontendAppConfig, httpClient: HttpClientV2) {

  def getEmployments(
      nino: String,
      taxYear: Int
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Employment]] = {

    val taiUrl = s"${appConfig.taiHost}/tai/$nino/employments/years/$taxYear"

    httpClient
      .get(url"$taiUrl")
      .execute[Seq[Employment]]
      .flatMap(response => Future.successful(response))
      .recover { case _ => Seq.empty }
  }

  def getProfessionalSubscriptionAmount(
      nino: String,
      taxYear: Int
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Int]] = {

    val taiUrl: String = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/expenses/employee-expenses/57"

    httpClient
      .get(url"$taiUrl")
      .execute[Seq[EmploymentExpense]]
      .map(_.headOption.map(_.grossAmount))
  }

  def updateProfessionalSubscriptionAmount(nino: String, taxYear: Int, version: Int, grossAmount: Int)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext
  ): Future[Unit] = {

    val taiUrl: String = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/expenses/employee-expenses/57"

    val body: IabdEditDataRequest = IabdEditDataRequest(version, grossAmount)
    httpClient
      .post(url"$taiUrl")
      .withBody(Json.toJson(body))
      .execute[HttpResponse]
      .map(response =>
        response.status match {
          case code if isSuccessful(code) => ()
          case NOT_FOUND                  => throw new NotFoundException(response.body)
          case code                       => throw UpstreamErrorResponse.apply(response.body, code)
        }
      )
  }

  def isYearAvailable(nino: String, taxYear: Int)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {

    val taiUrl: String = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/summary"
    httpClient
      .get(url"$taiUrl")
      .execute[HttpResponse]
      .map(response => isSuccessful(response.status))
      .recover { case _ => false }
  }

  def getTaxCodeRecords(
      nino: String,
      taxYear: Int
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TaxCodeRecord]] = {

    val taiUrl = s"${appConfig.taiHost}/tai/$nino/tax-account/$taxYear/income/tax-code-incomes"

    httpClient
      .get(url"$taiUrl")
      .execute[Seq[TaxCodeRecord]]
      .recover { case _ => Seq.empty }
  }

}
