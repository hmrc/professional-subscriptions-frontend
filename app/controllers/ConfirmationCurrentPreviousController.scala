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

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import models.TaxYearSelection._
import models.{NpsDataFormats, Rates}
import pages.{CitizensDetailsAddress, NpsData, SummarySubscriptionsPage, YourEmployerPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import services.{ClaimAmountService, TaiService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PSubsUtil._
import views.html.ConfirmationCurrentPreviousView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmationCurrentPreviousController @Inject()(identify: IdentifierAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      view: ConfirmationCurrentPreviousView,
                                                      taiService: TaiService,
                                                      claimAmountService: ClaimAmountService,
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      import models.PSubsByYear.pSubsByYearFormats

      val getNpsAmount: Option[Int] = request.userAnswers.get(NpsData)(NpsDataFormats.npsDataFormatsFormats)
        .flatMap(_.get(getTaxYear(CurrentYear)))

      (
        request.userAnswers.get(SummarySubscriptionsPage),
        request.userAnswers.get(CitizensDetailsAddress),
        request.userAnswers.get(YourEmployerPage)
      ) match {
        case (Some(psubsByYear), address, employerCorrect) =>
          taiService.taxCodeRecords(request.nino, getTaxYear(CurrentYear)).map {
            result =>
              val taxYears = psubsByYear.map(psubsByYear => getTaxYearPeriod(psubsByYear._1)).toSeq

              psubsByYear.get(getTaxYear(CurrentYear)) match {
                case Some(psubs) => {
                  val claimAmount: Int = claimAmountMinusDeductions(psubs)
                  val currentYearMinus1Claim: Boolean = taxYears.contains(CurrentYearMinus1)
                  val claimAmountsAndRates: Seq[Rates] = claimAmountService.getRates(result, claimAmount)

                  Ok(view(
                    claimAmountsAndRates,
                    claimAmount,
                    getNpsAmount.getOrElse(0),
                    currentYearMinus1Claim,
                    address,
                    employerCorrect,
                    hasClaimIncreased(getNpsAmount, claimAmount)
                  ))
                }
                case _ =>
                  Redirect(routes.SessionExpiredController.onPageLoad)
              }
          }.recoverWith {
            case e =>
              logger.error(s"[ConfirmationCurrentAndPreviousYearsController][taiConnector.taiTaxCodeRecord] Call failed $e", e)
              Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad))
          }

        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
      }
  }
}
