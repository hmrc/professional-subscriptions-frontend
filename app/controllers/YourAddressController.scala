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

import connectors.CitizenDetailsConnector
import controllers.actions._
import controllers.routes.SessionExpiredController
import forms.YourAddressFormProvider
import javax.inject.Inject
import models.{Address, Mode}
import navigation.Navigator
import pages.{CitizensDetailsAddress, YourAddressPage}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.YourAddressView

import scala.concurrent.{ExecutionContext, Future}

class YourAddressController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: YourAddressFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         citizenDetailsConnector: CitizenDetailsConnector,
                                         view: YourAddressView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(YourAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }


      citizenDetailsConnector.getAddress(request.nino).flatMap {
        response =>
          response.status match {
            case OK =>
              Json.parse(response.body).validate[Address] match {
                case JsSuccess(address, _) =>
                  if (address.line1.exists(_.trim.nonEmpty) && address.postcode.exists(_.trim.nonEmpty)) {
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.set(CitizensDetailsAddress, address))
                      _ <- sessionRepository.set(updatedAnswers)
                    } yield Ok(view(preparedForm, mode, address))
                  } else {
                    Future.successful(Redirect(???))
                  }
              }
            case _ => Future.successful(Redirect(???))

          }
      }.recoverWith {
        case e =>
          Logger.error(s"[YourAddressController][citizenDetailsConnector.getAddress] failed $e", e)
          Future.successful(Redirect(???))
      }
  }

  def onSubmit(mode: Mode) = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(CitizensDetailsAddress) match {
        case Some(address) =>

          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(view(formWithErrors, mode, address))),

            value => {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(YourAddressPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(YourAddressPage, mode)(updatedAnswers))
            }
          )
        case _ => Future.successful(Redirect(SessionExpiredController.onPageLoad()))
      }
  }
}