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
import models.{NpsDataFormats, PSub}
import models.TaxYearSelection._
import models.auditing.AuditData
import models.auditing.AuditEventType.{UpdateProfessionalSubscriptionsFailure, UpdateProfessionalSubscriptionsSuccess}
import navigation.Navigator
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
                                            auditConnector: AuditConnector,
                                            navigator: Navigator
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      import models.PSubsByYear._

      val cyaHelper = new CheckYourAnswersHelper(request.userAnswers)

      request.userAnswers.get(SummarySubscriptionsPage) match {
        case Some(psubsByYears) =>

          val taxYearSelection: Seq[AnswerSection] = Seq(AnswerSection(
            headingKey = Some("checkYourAnswers.taxYearsClaiming"),
            headingClasses = Some("visually-hidden"),
            subheadingKey = None,
            rows = Seq(
              cyaHelper.taxYearSelection,
              cyaHelper.amountsAlreadyInCode,
              cyaHelper.reEnterAmounts
            ).flatten
          ))

          val subscriptions: Seq[AnswerSection] = {
            NpsDataFormats.sort(psubsByYears).zipWithIndex.flatMap {
              case (psubByYear, yearIndex) =>
                psubByYear._2.zipWithIndex.map {
                  case (psub, subsIndex) =>
                    val taxYear = psubByYear._1

                    AnswerSection(
                      headingKey = if (yearIndex == 0 && subsIndex == 0) Some("checkYourAnswers.yourSubscriptions") else None,
                      headingClasses = None,
                      subheadingKey = if (subsIndex == 0) Some(s"taxYearSelection.${getTaxYearPeriod(taxYear)}") else None,
                      rows = Seq(
                        cyaHelper.whichSubscription(taxYear.toString, subsIndex, psub),
                        cyaHelper.subscriptionAmount(taxYear.toString, subsIndex, psub),
                        cyaHelper.employerContribution(taxYear.toString, subsIndex, psub),
                        cyaHelper.expensesEmployerPaid(taxYear.toString, subsIndex, psub)
                      ).flatten,
                      messageArgs = Seq(taxYear.toString, (taxYear + 1).toString): _*
                    )
                }
            }
          }

          val personalData: Seq[AnswerSection] = Seq(AnswerSection(
            headingKey = Some("checkYourAnswers.yourDetails"),
            headingClasses = None,
            subheadingKey = None,
            rows = Seq(
              cyaHelper.yourEmployer,
              cyaHelper.yourAddress
            ).flatten
          ))

          Ok(view(taxYearSelection ++ subscriptions ++ personalData))

        case _ => Redirect(SessionExpiredController.onPageLoad())
      }
  }

//  def acceptAndClaim(): Action[AnyContent] = (identify andThen getData andThen requireData) {
//    implicit request =>
//      navigator.nextPage()
//  }
}
