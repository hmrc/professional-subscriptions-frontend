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

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import models.PSubsByYear
import models.TaxYearSelection._
import pages.{SummarySubscriptionsPage, CitizensDetailsAddress}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ClaimAmountService, TaiService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ConfirmationPreviousView

import scala.concurrent.Future

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
                                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (
        request.userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats),
        request.userAnswers.get(CitizensDetailsAddress)
      ) match {
        case (Some(psubsByYear), address) =>
          val taxYears = psubsByYear.map(psubsByYear => getTaxYearPeriod(psubsByYear._1)).toSeq
          val currentYearMinus1Claim: Boolean = taxYears.contains(CurrentYearMinus1)

          sessionRepository.remove(request.internalId)

          Future.successful(Ok(view(
            currentYearMinus1Claim,
            address,
            frontendAppConfig.updateAddressInfoUrl
          )))
        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
      }
  }
}
