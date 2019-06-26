/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.IsYourDataCorrectFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{IsYourDataCorrectPage, NpsData, TaxYearSelectionPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.IsYourDataCorrectView

import scala.concurrent.{ExecutionContext, Future}

class IsYourDataCorrectController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             sessionRepository: SessionRepository,
                                             navigator: Navigator,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: IsYourDataCorrectFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: IsYourDataCorrectView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(IsYourDataCorrectPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      (request.userAnswers.get(NpsData), request.userAnswers.get(TaxYearSelectionPage)) match {
        case (Some(npsData), Some(taxYears)) =>
          Ok(view(preparedForm, mode, taxYears, npsData))
        case _ =>
          Redirect(SessionExpiredController.onPageLoad())
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (request.userAnswers.get(NpsData), request.userAnswers.get(TaxYearSelectionPage)) match {
        case (Some(npsData), Some(taxYears)) =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(view(formWithErrors, mode, taxYears, npsData))),

            value => {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(IsYourDataCorrectPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(IsYourDataCorrectPage, mode, updatedAnswers))
            }
          )
        case _ =>
          Future.successful(Redirect(SessionExpiredController.onPageLoad()))
      }
  }
}
