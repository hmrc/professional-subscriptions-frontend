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
import pages.{SubscriptionAmountAndAnyDeductions, TaxYearSelectionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SubmissionService
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import viewmodels.AnswerSection
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView,
                                            submissionService: SubmissionService
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val checkYourAnswersHelper = new CheckYourAnswersHelper(request.userAnswers)

      val sections = Seq(AnswerSection(None, Seq(
        checkYourAnswersHelper.taxYearSelection,
        checkYourAnswersHelper.whichSubscription,
        checkYourAnswersHelper.subscriptionAmount,
        checkYourAnswersHelper.employerContribution,
        checkYourAnswersHelper.addAnotherSubscription,
        checkYourAnswersHelper.yourEmployer,
        checkYourAnswersHelper.yourAddress
      ).flatten))

      Ok(view(sections))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      (
        request.userAnswers.get(TaxYearSelectionPage),
        request.userAnswers.get(SubscriptionAmountAndAnyDeductions)
      ) match {
        case (Some(taxYears), Some(subscriptionAmount)) =>
          submissionService.submitPSub(request.nino, taxYears, subscriptionAmount).map(redirect)
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
