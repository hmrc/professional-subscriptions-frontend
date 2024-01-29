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
import forms.ExpensesEmployerPaidFormProvider
import javax.inject.Inject
import models._
import navigation.Navigator
import pages.{ExpensesEmployerPaidPage, ProfessionalBodies, WhichSubscriptionPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import services.ProfessionalBodiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ExpensesEmployerPaidView

import scala.concurrent.{ExecutionContext, Future}

class ExpensesEmployerPaidController @Inject()(
                                                sessionService: SessionService,
                                                navigator: Navigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: ExpensesEmployerPaidFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: ExpensesEmployerPaidView,
                                                professionalBodiesService: ProfessionalBodiesService
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Int] = formProvider()

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ExpensesEmployerPaidPage(year, index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(WhichSubscriptionPage(year, index)) match {
        case Some(subscription) => Ok(view(preparedForm, mode, subscription, year, index))
        case None => Redirect(routes.SessionExpiredController.onPageLoad)
      }
  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          request.userAnswers.get(WhichSubscriptionPage(year, index)) match {
            case Some(subscription) => Future.successful(BadRequest(view(formWithErrors, mode, subscription, year, index)))
            case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
          },
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ExpensesEmployerPaidPage(year, index), value))
            _ <- sessionService.set(updatedAnswers)
            updateAnswersWithPsubs <- Future.fromTry(updatedAnswers.set(ProfessionalBodies, professionalBodiesService.professionalBodies))
          } yield Redirect(navigator.nextPage(ExpensesEmployerPaidPage(year, index), mode, updateAnswersWithPsubs))
        }
      )
  }
}
