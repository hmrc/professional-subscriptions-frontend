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
import forms.EmployerContributionFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{EmployerContributionPage, ProfessionalBodies}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ProfessionalBodiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EmployerContributionView

import scala.concurrent.{ExecutionContext, Future}

class EmployerContributionController @Inject()(
                                                sessionRepository: SessionRepository,
                                                navigator: Navigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: EmployerContributionFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: EmployerContributionView,
                                                professionalBodiesService: ProfessionalBodiesService
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(EmployerContributionPage(year, index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, year, index))
  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, year, index))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EmployerContributionPage(year, index), value))
            _ <- sessionRepository.set(updatedAnswers)
            updateAnswersWithPsubs <- Future.fromTry(updatedAnswers.set(ProfessionalBodies, professionalBodiesService.professionalBodies))
          } yield Redirect(navigator.nextPage(EmployerContributionPage(year, index), mode, updateAnswersWithPsubs))
        }
      )
  }
}
