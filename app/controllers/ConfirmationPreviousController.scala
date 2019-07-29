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

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import models.TaxYearSelection.CurrentYearMinus1
import pages.{AmountsYouNeedToChangePage, YourAddressPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ClaimAmountService, TaiService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.ConfirmationPreviousView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmationPreviousController @Inject()(
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: ConfirmationPreviousView,
                                                       sessionRepository: SessionRepository,
                                                       taiService: TaiService,
                                                       claimAmountService: ClaimAmountService,
                                                       frontendAppConfig: FrontendAppConfig
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (
        request.userAnswers.get(AmountsYouNeedToChangePage),
        request.userAnswers.get(YourAddressPage)
      ) match {
        case (Some(taxYears), addressCorrect) =>
          val currentYearMinus1Claim: Boolean = taxYears.contains(CurrentYearMinus1)
          sessionRepository.remove(request.internalId)

          Future.successful(Ok(view(
            currentYearMinus1Claim,
            addressCorrect,
            frontendAppConfig.updateAddressInfoUrl
          )))
        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }
}
