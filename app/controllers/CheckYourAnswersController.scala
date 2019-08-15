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

import com.google.inject.Inject
import controllers.actions._
import controllers.routes._
import models.NpsDataFormats._
import models.{PSub, TaxYearSelection}
import models.TaxYearSelection._
import models.auditing.AuditData
import models.auditing.AuditEventType.{UpdateProfessionalSubscriptionsFailure, UpdateProfessionalSubscriptionsSuccess}
import pages.SummarySubscriptionsPage
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import viewmodels.AnswerSection
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersController @Inject()(
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView,
                                            submissionService: SubmissionService,
                                            auditConnector: AuditConnector
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      import models.PSubsByYear._

      val cyaHelper = new CheckYourAnswersHelper(request.userAnswers)

      request.userAnswers.get(SummarySubscriptionsPage) match {
        case Some(psubsByYears) =>

          val taxYearSelection: Seq[AnswerSection] = Seq(AnswerSection(
            headingKey = None,
            rows = Seq(
              cyaHelper.taxYearSelection,
              cyaHelper.amountsAlreadyInCode,
              cyaHelper.reEnterAmounts
            ).flatten
          ))

          val subscriptions: Seq[AnswerSection] = psubsByYears.flatMap {
            psubsByYear =>
              psubsByYear._2.zipWithIndex.map {
                case (psub, index) =>
                  val taxYear = psubsByYear._1
                  AnswerSection(
                    headingKey = if (index == 0) Some(s"taxYearSelection.${getTaxYearPeriod(taxYear)}") else None,
                    rows = Seq(
                      cyaHelper.whichSubscription(taxYear.toString, index, psub),
                      cyaHelper.subscriptionAmount(taxYear.toString, index, psub),
                      cyaHelper.employerContribution(taxYear.toString, index, psub),
                      cyaHelper.expensesEmployerPaid(taxYear.toString, index, psub)
                    ).flatten,
                    messageArgs = Seq(taxYear.toString, (taxYear + 1).toString): _*
                  )
              }
          }.toSeq

          val personalData: Seq[AnswerSection] = Seq(AnswerSection(
            headingKey = Some("checkYourAnswers.yourDetails"),
            rows = Seq(
              cyaHelper.yourEmployer,
              cyaHelper.yourAddress
            ).flatten
          ))

          Ok(view(taxYearSelection ++ subscriptions ++ personalData))

        case _ => Redirect(SessionExpiredController.onPageLoad())
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      import models.PSubsByYear.formats
      val dataToAudit = AuditData(nino = request.nino, userAnswers = request.userAnswers.data)

      request.userAnswers.get(SummarySubscriptionsPage) match {
        case Some(subscriptions) => {
          val taxYears = subscriptions.map(psubByYear => TaxYearSelection.getTaxYearPeriod(psubByYear._1)).toSeq
          val result = submissionService.submitPSub(request.nino, taxYears, subscriptions)

          auditAndRedirect(result, dataToAudit, subscriptions)
        }
        case _ =>
          Future.successful(Redirect(SessionExpiredController.onPageLoad()))
      }
  }

  private def auditAndRedirect(result: Future[Unit],
                               auditData: AuditData,
                               subscriptions: Map[Int, Seq[PSub]]
                              )(implicit hc: HeaderCarrier): Future[Result] = {
    result.map {
      _ =>
        auditConnector.sendExplicitAudit(UpdateProfessionalSubscriptionsSuccess.toString, auditData)

        subscriptions.filter(_._2.nonEmpty).keys.toSeq match {
          case years if years.contains(getTaxYear(CurrentYear)) && years.length == 1 =>
            Redirect(ConfirmationCurrentController.onPageLoad())
          case years if !years.contains(getTaxYear(CurrentYear)) =>
            Redirect(ConfirmationPreviousController.onPageLoad())
          case _ =>
            Redirect(ConfirmationCurrentPreviousController.onPageLoad())
        }
    }.recover {
      case e =>
        Logger.warn("[CYAController] submission failed", e)
        auditConnector.sendExplicitAudit(UpdateProfessionalSubscriptionsFailure.toString, auditData)
        Redirect(TechnicalDifficultiesController.onPageLoad())
    }
  }
}
