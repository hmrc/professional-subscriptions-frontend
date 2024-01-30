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
import forms.WhichSubscriptionFormProvider
import javax.inject.Inject
import models.{Mode, ProfessionalBody}
import navigation.Navigator
import pages.WhichSubscriptionPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import services.ProfessionalBodiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PSubsUtil._
import views.html.WhichSubscriptionView
import controllers.routes._

import scala.concurrent.{ExecutionContext, Future}

class WhichSubscriptionController @Inject()(
                                             sessionService: SessionService,
                                             navigator: Navigator,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: WhichSubscriptionFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: WhichSubscriptionView,
                                             professionalBodiesService: ProfessionalBodiesService
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhichSubscriptionPage(year, index)) match {
        case None => formProvider(Nil)
        case Some(value) => formProvider(Nil).fill(value)
      }

      Ok(view(preparedForm, mode, professionalBodiesService.professionalBodies, year, index))
  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val bodies: List[ProfessionalBody] = professionalBodiesService.professionalBodies

      formProvider(bodies).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, bodies, year, index))),
        selectedProfessionalBody =>
          Future.fromTry(request.userAnswers.set(WhichSubscriptionPage(year, index), selectedProfessionalBody)).flatMap {
            userAnswers =>
              val duplicateSubscription: Boolean = isDuplicate(userAnswers, year)
              val yearInRange: Boolean = professionalBodiesService.validateYearInRange(selectedProfessionalBody, year.toInt)

              if (duplicateSubscription) {
                Future.successful(Redirect(routes.DuplicateSubscriptionController.onPageLoad(mode)))
              } else if (yearInRange) {
                sessionService.set(userAnswers).map { _ =>
                  Redirect(navigator.nextPage(WhichSubscriptionPage(year, index), mode, userAnswers))
                }
              } else {
                bodies.find(_.name == selectedProfessionalBody) match {
                  case Some(ProfessionalBody(_, _, Some(startYear))) =>
                    Future.successful(Redirect(routes.CannotClaimYearSpecificController.onPageLoad(mode, selectedProfessionalBody, startYear)))
                  case _ =>
                    Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad))
                }
              }
          }
      )
  }
}
