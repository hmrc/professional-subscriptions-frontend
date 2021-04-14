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
import controllers.routes.{SessionExpiredController, TechnicalDifficultiesController, UpdateYourEmployerInformationController}
import forms.YourEmployerFormProvider
import javax.inject.Inject
import models.Mode
import models.TaxYearSelection._
import navigation.Navigator
import pages.{YourEmployerPage, YourEmployersNames}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TaiService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.YourEmployerView

import scala.concurrent.{ExecutionContext, Future}

class YourEmployerController @Inject()(
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: YourEmployerFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: YourEmployerView,
                                        taiService: TaiService
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(YourEmployerPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
          val nino = request.nino

          taiService.getEmployments(nino, getTaxYear(CurrentYear)).flatMap {
            employments =>
              if (employments.nonEmpty) {
                val employersNames = employments.map(_.name)
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(YourEmployersNames, employersNames))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield {
                  Ok(view(preparedForm, mode, employersNames))
                }
              } else {
                Future.successful(Redirect(UpdateYourEmployerInformationController.onPageLoad()))
              }
          }.recoverWith {
            case e =>
              Logger.warn(s"[YourEmployerController.onPageLoad][taiService.getEmployments] failed: $e")
              Future.successful(Redirect(TechnicalDifficultiesController.onPageLoad()))
          }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(YourEmployersNames) match {
        case Some(employerNames) =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(view(formWithErrors, mode, employerNames))),

            value => {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(YourEmployerPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(YourEmployerPage, mode, updatedAnswers))
            }
          )
        case _ =>
          Future.successful(Redirect(SessionExpiredController.onPageLoad()))
      }
  }
}
