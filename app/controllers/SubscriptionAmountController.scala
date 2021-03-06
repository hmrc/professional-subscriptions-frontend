/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.routes.SessionExpiredController
import forms.SubscriptionAmountFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{SubscriptionAmountPage, WhichSubscriptionPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SubscriptionAmountView

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionAmountController @Inject()(
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: SubscriptionAmountFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: SubscriptionAmountView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(SubscriptionAmountPage(year, index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(WhichSubscriptionPage(year, index)) match {
        case Some(subscription) => Ok(view(preparedForm, mode, subscription, year, index))
        case _ => Redirect(routes.SessionExpiredController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          request.userAnswers.get(WhichSubscriptionPage(year, index)) match {
            case Some(subscription) =>
              Future.successful(BadRequest(view(formWithErrors, mode, subscription, year, index)))
            case _ =>
              Future.successful(Redirect(SessionExpiredController.onPageLoad()))
          },

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SubscriptionAmountPage(year, index), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SubscriptionAmountPage(year, index), mode, updatedAnswers))
        }
      )
  }
}
