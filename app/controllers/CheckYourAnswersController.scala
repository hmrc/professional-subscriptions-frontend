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

package controllers

import com.google.inject.Inject
import controllers.actions._
import controllers.routes._
import models.{NormalMode, NpsDataFormats}
import models.TaxYearSelection._
import navigation.Navigator
import pages._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import viewmodels.AnswerSection
import views.html.CheckYourAnswersView

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
            headingClasses = Some("govuk-visually-hidden"),
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
                      headingKey = None,
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

          Ok(view(taxYearSelection ++ subscriptions))

        case _ => Redirect(SessionExpiredController.onPageLoad)
      }
  }

  def acceptAndClaim(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CheckYourAnswersPage, NormalMode, request.userAnswers))
  }
}
