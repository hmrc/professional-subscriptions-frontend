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
import models.TaxYearSelection.{CurrentYear, getTaxYear}
import models.{ClaimCompleteCurrent, ClaimCompleteCurrentPrevious, ClaimCompletePrevious, PSubsByYear}
import pages.SummarySubscriptionsPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ConfirmationMergedJourneyView

import javax.inject.Inject
import scala.concurrent.Future

class ConfirmationMergedJourneyController @Inject() (
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: ConfirmationMergedJourneyView,
    appConfig: FrontendAppConfig
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = identify.andThen(getData).andThen(requireData).async { implicit request =>
    val claims = request.userAnswers
      .get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)
      .map(_.filter(_._2.nonEmpty).keys.toSeq)

    claims match {
      case Some(years) if request.userAnswers.isMergedJourney =>
        val claimCompleteStatus = years match {
          case years if years.forall(_ == getTaxYear(CurrentYear)) =>
            ClaimCompleteCurrent
          case years if !years.contains(getTaxYear(CurrentYear)) =>
            ClaimCompletePrevious
          case _ =>
            ClaimCompleteCurrentPrevious
        }
        Future.successful(Ok(view(appConfig.mergedJourneyContinueUrl(claimCompleteStatus))))
      case _ =>
        Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
    }
  }

}
