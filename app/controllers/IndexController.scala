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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import navigation.Navigator
import pages.MergedJourneyFlag
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(val controllerComponents: MessagesControllerComponents,
                                identify: IdentifierAction,
                                getData: DataRetrievalAction,
                                sessionService: SessionService,
                                navigator: Navigator,
                                appConfig: FrontendAppConfig
                               )(implicit executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad(isMergedJourney: Boolean = false): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    if (request.userAnswers.isEmpty || request.userAnswers.exists(_.isMergedJourney != isMergedJourney)) {
      sessionService.set(UserAnswers(
        request.internalId,
        Json.obj(
          MergedJourneyFlag.toString -> (isMergedJourney && appConfig.mergedJourneyEnabled)
        )
      )).map(_ => Redirect(navigator.firstPage()))
    } else {
      Future.successful(Redirect(navigator.firstPage()))
    }
  }
}
