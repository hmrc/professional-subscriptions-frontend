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
import controllers.routes._
import forms.RemoveSubscriptionFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{PSubPage, RemoveSubscriptionPage, SavePSubs}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.PSubsUtil._
import views.html.RemoveSubscriptionView

import scala.concurrent.{ExecutionContext, Future}

class RemoveSubscriptionController @Inject()(
                                              sessionRepository: SessionRepository,
                                              navigator: Navigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: RemoveSubscriptionFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: RemoveSubscriptionView
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(PSubPage(year, index)) match {
        case Some(subscription) =>
          Ok(view(form, mode, year, index, subscription.nameOfProfessionalBody))
        case _ =>
          Redirect(SessionExpiredController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(PSubPage(year, index)) match {
        case Some(subscription) =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(view(formWithErrors, mode, year, index, subscription.nameOfProfessionalBody))),

            value =>
              Future.fromTry(request.userAnswers.set(RemoveSubscriptionPage, value)) flatMap (ua1 =>
                if (value)
                  Future.fromTry(ua1.set(SavePSubs(year), remove(ua1, year, index))) flatMap (ua2 =>
                    sessionRepository.set(ua2) map (_ => Redirect(navigator.nextPage(RemoveSubscriptionPage, mode, ua2)))
                  )
                else
                  sessionRepository.set(ua1) map (_ => Redirect(navigator.nextPage(RemoveSubscriptionPage, mode, ua1)))
              )
          )
        case _ =>
          Future.successful(Redirect(SessionExpiredController.onPageLoad()))
      }
  }
}
