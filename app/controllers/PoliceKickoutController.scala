/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{ClaimUnsuccessful, Mode}
import navigation.Navigator
import pages.{MergedJourneyFlag, PoliceKickoutPage, SavePSubs}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PSubsUtil.remove
import views.html.PoliceKickoutView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class PoliceKickoutController @Inject()(
                                         sessionService: SessionService,
                                         appConfig: FrontendAppConfig,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: PoliceKickoutView,
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {
  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Ok(view(mergedJourney = request.userAnswers.isMergedJourney, mode, year, index))

  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val updatedAnswers = Try(remove(request.userAnswers, year, index)) match {
        case Success(value) =>
          for {
            userAnswers <- Future.fromTry(request.userAnswers.set(SavePSubs(year), value))
            _ <- sessionService.set(userAnswers)
          } yield userAnswers
        case _ => Future.successful(request.userAnswers)
      }

      updatedAnswers.map(userAnswers =>
        userAnswers.get(MergedJourneyFlag) match {
          case Some(true) => Redirect(appConfig.mergedJourneyContinueUrl(ClaimUnsuccessful))
          case _ => Redirect(navigator.nextPage(PoliceKickoutPage, mode, userAnswers).url)
        })
  }
}
