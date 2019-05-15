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
import javax.inject.Inject
import models.{EnglishRate, ScottishRate}
import pages.{EmployerContributionPage, ExpensesEmployerPaidPage, SubscriptionAmountAndAnyDeductions, SubscriptionAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.ClaimAmountService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.ClaimAmountView

import scala.concurrent.{ExecutionContext, Future}

class ClaimAmountController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ClaimAmountView,
                                       claimAmountService: ClaimAmountService,
                                       sessionRepository: SessionRepository

                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {



  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (request.userAnswers.get(SubscriptionAmountPage),
       request.userAnswers.get(EmployerContributionPage),
       request.userAnswers.get(ExpensesEmployerPaidPage)) match {

        case (Some(subscriptionAmount), employerContribution, expensesEmployerPaid) =>

          val claimAmountAndAnyDeductions = claimAmountService.calculateClaimAmount(
            employerContribution, expensesEmployerPaid, subscriptionAmount)

          for {
            saveSubscriptionAmountAndAnyDeductions <- Future.fromTry(request.userAnswers.set(SubscriptionAmountAndAnyDeductions, claimAmountAndAnyDeductions))
            _ <- sessionRepository.set(saveSubscriptionAmountAndAnyDeductions)
          } yield {

            val englishRate: EnglishRate = claimAmountService.englishRate(claimAmountAndAnyDeductions)
            val scottishRate: ScottishRate = claimAmountService.scottishRate(claimAmountAndAnyDeductions)

            Ok(view(
              claimAmountAndAnyDeductions,
              subscriptionAmount,
              expensesEmployerPaid,
              employerContribution,
              englishRate,
              scottishRate
            ))
          }

        case _ =>
          Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }
}
