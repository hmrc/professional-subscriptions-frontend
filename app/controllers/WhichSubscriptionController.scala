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
import forms.WhichSubscriptionFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.WhichSubscriptionPage
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ProfessionalBodiesService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.PSubsUtil._
import views.html.WhichSubscriptionView

import scala.concurrent.{ExecutionContext, Future}

class WhichSubscriptionController @Inject()(
                                             sessionRepository: SessionRepository,
                                             navigator: Navigator,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: WhichSubscriptionFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: WhichSubscriptionView,
                                             professionalBodiesService: ProfessionalBodiesService
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhichSubscriptionPage(year, index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      professionalBodiesService.professionalBodies().map(
        subscriptions =>
          Ok(view(preparedForm, mode, subscriptions, year, index))
      ).recoverWith {
        case e =>
          Logger.warn(s"[WhichSubscriptionController.onPageLoad][professionalBodiesService.localSubscriptions] failed to load subscriptions: $e")
          Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
      }
  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          professionalBodiesService.professionalBodies().map {
            subscriptions => BadRequest(view(formWithErrors, mode, subscriptions, year, index))
          }.recoverWith {
            case e =>
              Logger.warn(s"[WhichSubscriptionController.onSubmit][professionalBodiesService.localSubscriptions] failed to load subscriptions: $e")
              Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
          },
        value =>
          Future.fromTry(request.userAnswers.set(WhichSubscriptionPage(year, index), value)).flatMap {
            userAnswers =>
              val duplicateSubscription = isDuplicate(userAnswers, year)
              val yearInRange = professionalBodiesService.validateYearInRange(Seq(value), year.toInt).recover{ case _ => false}

              if (duplicateSubscription) {
                Future.successful(Redirect(routes.DuplicateSubscriptionController.onPageLoad(mode)))
              } else {
                yearInRange.flatMap {
                  case true =>  sessionRepository.set(userAnswers).map { _ =>
                    Redirect(navigator.nextPage(WhichSubscriptionPage(year, index), mode, userAnswers))
                  }
                  case false => Future.successful(Redirect(routes.CannotClaimYearSpecificController.onPageLoad(mode, value, year)))
                }
              }
          }
      )
  }
}
