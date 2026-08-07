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

import controllers.actions.*
import models.TaxYearSelection.{CurrentYear, getTaxYear}
import models.{NpsDataFormats, Rates}
import pages.{CitizensDetailsAddress, NpsData, SummarySubscriptionsPage, YourEmployerPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import services.{ClaimAmountService, TaiService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PSubsUtil.*
import views.html.ConfirmationCurrentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmationCurrentController @Inject() (
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: ConfirmationCurrentView,
    taiService: TaiService,
    claimAmountService: ClaimAmountService
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = identify.andThen(getData).andThen(requireData).async { request =>
    import models.PSubsByYear.pSubsByYearFormats

    given Request[AnyContent] = request

    val getNpsAmountForCY: Option[Int] = request.userAnswers
      .get(NpsData)(using NpsDataFormats.npsDataFormatsFormats)
      .flatMap(_.get(getTaxYear(CurrentYear)))

    (
      request.userAnswers.get(SummarySubscriptionsPage).flatMap(_.get(getTaxYear(CurrentYear))),
      request.userAnswers.get(CitizensDetailsAddress),
      request.userAnswers.get(YourEmployerPage)
    ) match {
      case (Some(psubs), address, employerCorrect) =>
        taiService
          .taxCodeRecords(request.nino, getTaxYear(CurrentYear))
          .map { result =>
            val claimAmount                      = claimAmountMinusDeductions(psubs)
            val claimAmountsAndRates: Seq[Rates] = claimAmountService.getRates(result, claimAmount)

            Ok(
              view(
                claimAmountsAndRates,
                claimAmount,
                address,
                employerCorrect,
                hasClaimIncreased(getNpsAmountForCY, claimAmount),
                getNpsAmountForCY.getOrElse(0)
              )
            )
          }
          .recoverWith { case e =>
            logger.error(
              s"[ConfirmationCurrentAndPreviousYearsController][taiConnector.taiTaxCodeRecord] Call failed $e",
              e
            )
            Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad))
          }

      case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
    }
  }

}
