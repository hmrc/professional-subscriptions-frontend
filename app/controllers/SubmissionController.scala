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

package controllers

import controllers.actions._
import controllers.routes._
import javax.inject.Inject
import models.{NormalMode, PSub, UserAnswers}
import models.TaxYearSelection._
import models.auditing.AuditData
import models.auditing.AuditEventType._
import navigation.Navigator
import pages.{DuplicateClaimForOtherYearsPage, Submission, SummarySubscriptionsPage}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class SubmissionController @Inject()(
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      val controllerComponents: MessagesControllerComponents,
                                      auditConnector: AuditConnector,
                                      submissionService: SubmissionService,
                                      navigator: Navigator
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController {

  def submission: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val dataToAudit: AuditData = AuditData(nino = request.nino, userAnswers = request.userAnswers.data)

      import models.PSubsByYear.formats

      request.userAnswers.get(SummarySubscriptionsPage) match {
        case Some(subscriptions) => {
          val result = submissionService.submitPSub(request.nino, subscriptions)

          auditAndRedirect(result, dataToAudit, request.userAnswers)
        }
        case _ =>
          Future.successful(Redirect(SessionExpiredController.onPageLoad()))
      }
  }

  private def auditAndRedirect(result: Future[Unit],
                               auditData: AuditData,
                               userAnswers: UserAnswers
                              )(implicit hc: HeaderCarrier): Future[Result] = {
    result.map {
      _ =>
        auditConnector.sendExplicitAudit(UpdateProfessionalSubscriptionsSuccess.toString, auditData)
        Redirect(navigator.nextPage(Submission, NormalMode, userAnswers))
    }.recover {
      case e =>
        Logger.warn("[SubmissionController] submission failed", e)
        auditConnector.sendExplicitAudit(UpdateProfessionalSubscriptionsFailure.toString, auditData)
        Redirect(TechnicalDifficultiesController.onPageLoad())
    }
  }

}
