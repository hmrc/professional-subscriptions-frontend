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
import config.FrontendAppConfig
import connectors.ProfessionalBodiesConnector
import models.{ProfessionalBody, SubmissionValidationException}
import play.api.Environment
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ProfessionalBodiesService @Inject()(
                                           professionalBodiesConnector: ProfessionalBodiesConnector,
                                           environment: Environment,
                                           frontendAppConfig: FrontendAppConfig
                                         ) {

  def subscriptions()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ProfessionalBody]] = {
    professionalBodiesConnector.getProfessionalBodies().map {
      _.json.validate[Seq[ProfessionalBody]] match {
        case JsSuccess(value, _) => value
        case JsError(errors) => throw new Exception(s"failed to get bodies: $errors")
      }
    }
  }

  def professionalBodies(resourceLocation: String = frontendAppConfig.professionalBodiesList): Future[Seq[ProfessionalBody]] = {
    environment.resourceAsStream(resourceLocation) match {
      case Some(inputStream) =>
        Json.parse(inputStream).validate[Seq[ProfessionalBody]] match {
          case JsSuccess(value, _) => Future.successful(value)
          case JsError(errors) => Future.failed(new Exception(s"failed to parse bodies: $errors"))
        }
      case _ => Future.failed(new Exception(s"failed to load bodies"))
    }
  }

  def validateYearInRange(psubNames: Seq[String], year: Int)(implicit ec: ExecutionContext): Future[Boolean] = {
    professionalBodies().map {
      allBodies =>
        psubNames.forall {
          name =>
            allBodies.filter(_.name == name).map {
              pBody => pBody.startYear.forall(_ <= year)
            }.headOption.getOrElse(true)
        }
    }.map {
      case true => true
      case false => throw SubmissionValidationException("Year out of range")
    }
  }
}
