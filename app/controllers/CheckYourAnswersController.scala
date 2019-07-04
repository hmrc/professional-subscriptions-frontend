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
import models.TaxYearSelection._
import pages.{AmountsYouNeedToChangePage, SummarySubscriptionsPage, TaxYearSelectionPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SubmissionService
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.{CheckYourAnswersHelper, PSubsUtil}
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
                                            pSubsUtil: PSubsUtil
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      import models.PSubsByYear._

      val cyaHelper = new CheckYourAnswersHelper(request.userAnswers)

      (
        request.userAnswers.get(TaxYearSelectionPage),
        request.userAnswers.get(SummarySubscriptionsPage)
      ) match {
        case (Some(taxYears), Some(subs)) =>

          val taxYearSelection: Seq[AnswerSection] = Seq(AnswerSection(
            headingKey = None,
            rows = Seq(
              cyaHelper.taxYearSelection,
              cyaHelper.amountsAlreadyInCode,
              cyaHelper.amountsYouNeedToChange
            ).flatten
          ))

          val subscriptions: Seq[AnswerSection] = taxYears.flatMap {
            taxYear =>
              sort(subs).toMap.filterKeys(_ == getTaxYear(taxYear)).flatMap(
                _._2.zipWithIndex.map {
                  case (psub, index) =>
                    AnswerSection(
                      headingKey = if (index == 0) Some(s"taxYearSelection.${getTaxYearPeriod(getTaxYear(taxYear))}") else None,
                      rows = Seq(
                        cyaHelper.whichSubscription(getTaxYear(taxYear).toString, index, psub),
                        cyaHelper.subscriptionAmount(getTaxYear(taxYear).toString, index, psub),
                        cyaHelper.employerContribution(getTaxYear(taxYear).toString, index, psub),
                        cyaHelper.expensesEmployerPaid(getTaxYear(taxYear).toString, index, psub)
                      ).flatten,
                      messageArgs = Seq(getTaxYear(taxYear).toString, (getTaxYear(taxYear) + 1).toString): _*
                    )
                }
              )
          }

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
      (
        request.userAnswers.get(AmountsYouNeedToChangePage),
        request.userAnswers.get(SummarySubscriptionsPage)
      ) match {
        case (Some(taxYears), Some(psubs)) =>
          submissionService.submitPSub(request.nino, taxYears, psubs).map(redirect)
        case _ =>
          Future.successful(Redirect(SessionExpiredController.onPageLoad()))
      }
  }

  def redirect(result: Seq[HttpResponse]): Result = {
    if (result.nonEmpty && result.forall(_.status == 204))
      Redirect(ConfirmationController.onPageLoad())
    else
      Redirect(TechnicalDifficultiesController.onPageLoad())
  }
}
