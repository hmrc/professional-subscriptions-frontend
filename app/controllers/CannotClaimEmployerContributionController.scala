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

import controllers.actions._
import javax.inject.Inject
import models.{Mode, PSub}
import navigation.Navigator
import pages.{CannotClaimEmployerContributionPage, SavePSubs}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PSubsUtil._
import views.html.CannotClaimEmployerContributionView

import scala.concurrent.{ExecutionContext, Future}

class CannotClaimEmployerContributionController @Inject()(
                                                           identify: IdentifierAction,
                                                           getData: DataRetrievalAction,
                                                           requireData: DataRequiredAction,
                                                           val controllerComponents: MessagesControllerComponents,
                                                           view: CannotClaimEmployerContributionView,
                                                           navigator: Navigator,
                                                           sessionService: SessionService
                                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Ok(view(mode, year, index))
  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val psubs: Seq[PSub] = remove(request.userAnswers, year, index)

      for {
        userAnswers <- Future.fromTry(request.userAnswers.set(SavePSubs(year), psubs))
        _ <- sessionService.set(userAnswers)
      } yield {
        Redirect(navigator.nextPage(CannotClaimEmployerContributionPage(year, index), mode, userAnswers).url)
      }
  }
}
