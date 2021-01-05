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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logger
import play.api.mvc.Results._
import play.api.libs.json.Reads
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.OptionalRetrieval
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(AuthProviders(AuthProvider.Verify) or (AffinityGroup.Individual and ConfidenceLevel.L200))
      .retrieve(OptionalRetrieval("internalId", Reads.StringReads) and OptionalRetrieval("nino", Reads.StringReads)) {
        x =>
          val internalId = x.a.getOrElse(throw new UnauthorizedException("Unable to retrieve internalId"))
          val nino = x.b.getOrElse(throw new UnauthorizedException("Unable to retrieve nino"))

          block(IdentifierRequest(request, internalId, nino))
      }
  } recover {
    case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
    case _: InsufficientConfidenceLevel =>
      Redirect(s"${config.ivUpliftUrl}?origin=PSUBS&confidenceLevel=200" +
        s"&completionURL=${config.authorisedCallback}" +
        s"&failureURL=${config.unauthorisedCallback}")
    case _: AuthorisationException =>
      Redirect(routes.UnauthorisedController.onPageLoad())
    case e: Exception =>
      Logger.warn(s"[AuthenticatedIdentifierAction] failed: $e")
      Redirect(routes.TechnicalDifficultiesController.onPageLoad())
  }
}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]
