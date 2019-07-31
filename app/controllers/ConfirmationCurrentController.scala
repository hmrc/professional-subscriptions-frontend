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
import controllers.routes.TechnicalDifficultiesController
import javax.inject.Inject
import models.Rates
import models.TaxYearSelection.{CurrentYear, CurrentYearMinus1, getTaxYear}
import pages.{AmountsYouNeedToChangePage, SummarySubscriptionsPage, YourAddressPage, YourEmployerPage}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ClaimAmountService, TaiService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.PSubsUtil._
import views.html.ConfirmationCurrentView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmationCurrentController @Inject()(
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: ConfirmationCurrentView,
                                                       sessionRepository: SessionRepository,
                                                       taiService: TaiService,
                                                       claimAmountService: ClaimAmountService,
                                                       frontendAppConfig: FrontendAppConfig
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      import models.PSubsByYear.formats
      (
        request.userAnswers.get(SummarySubscriptionsPage).flatMap(_.get(getTaxYear(CurrentYear))),
        request.userAnswers.get(YourAddressPage),
        request.userAnswers.get(YourEmployerPage)
      ) match {
        case (Some(psubs), addressCorrect, employerCorrect) =>
          taiService.taxCodeRecords(request.nino, getTaxYear(CurrentYear)).map {
            result =>
              val claimAmount = claimAmountMinusDeductions(psubs)
              val claimAmountsAndRates: Seq[Rates] = claimAmountService.getRates(result, claimAmount)

              sessionRepository.remove(request.internalId)

              Ok(view(
                claimAmountsAndRates,
                claimAmount,
                addressCorrect,
                employerCorrect,
                frontendAppConfig.updateAddressInfoUrl,
                frontendAppConfig.updateEmployerInfoUrl
              ))

          }.recoverWith {
            case e =>
              Logger.error(s"[ConfirmationCurrentAndPreviousYearsController][taiConnector.taiTaxCodeRecord] Call failed $e", e)
              Future.successful(Redirect(TechnicalDifficultiesController.onPageLoad()))
          }

        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }
}