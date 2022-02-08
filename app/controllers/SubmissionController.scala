/*
 * Copyright 2022 HM Revenue & Customs
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
import models.{NormalMode, PSubsByYear, UserAnswers}
import models.auditing.{AuditData, AuditSubmissionData}
import models.auditing.AuditEventType._
import navigation.Navigator
import pages._
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class SubmissionController @Inject()(
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      val controllerComponents: MessagesControllerComponents,
                                      auditConnector: AuditConnector,
                                      submissionService: SubmissionService,
                                      navigator: Navigator
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with Logging {

  def submission: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      getAuditData(request.userAnswers) match {
        case Some(auditData) => {
          val result = submissionService.submitPSub(request.nino, auditData.subscriptions)

          auditAndRedirect(result, AuditData(request.nino, auditData), request.userAnswers)
        }
        case _ =>
          Future.successful(Redirect(SessionExpiredController.onPageLoad()))
      }
  }

  private def getAuditData(userAnswers: UserAnswers): Option[AuditSubmissionData] =
    (for {
      npsData <- userAnswers.get(NpsData)(models.NpsDataFormats.npsDataFormatsFormats)
      subscriptions <- userAnswers.get(SummarySubscriptionsPage)(models.PSubsByYear.pSubsByYearFormats)
    } yield {

      val areSubscriptionsEmpty: Boolean = PSubsByYear(subscriptions).isValid

      if (areSubscriptionsEmpty) {

        Some(AuditSubmissionData(
          npsData = npsData,
          amountsAlreadyInCode = userAnswers.get(AmountsAlreadyInCodePage),
          subscriptions = subscriptions.filter(_._2.nonEmpty),
          yourEmployersNames = userAnswers.get(YourEmployersNames),
          yourEmployer = userAnswers.get(YourEmployerPage),
          address = userAnswers.get(CitizensDetailsAddress)
        ))
      } else {
        None
      }
    }).flatten

  private def auditAndRedirect(result: Future[Unit],
                               auditData: AuditData,
                               userAnswers: UserAnswers
                              )(implicit hc: HeaderCarrier): Future[Result] = {
    result.map {
      _ =>
        auditConnector.sendExplicitAudit(UpdateProfessionalSubscriptions.toString, auditData)
        Redirect(navigator.nextPage(Submission, NormalMode, userAnswers))
    }.recover {
      case e =>
        logger.warn("[SubmissionController] submission failed", e)
        auditConnector.sendExplicitAudit(UpdateProfessionalSubscriptionsFailure.toString, auditData)
        Redirect(TechnicalDifficultiesController.onPageLoad())
    }
  }

}
