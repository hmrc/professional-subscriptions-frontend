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
import forms.TaxYearSelectionFormProvider
import javax.inject.Inject
import models.{Enumerable, Mode, TaxYearSelection}
import navigation.Navigator
import pages.{NpsDataSave, TaxYearSelectionPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TaiService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.TaxYearSelectionView

import scala.concurrent.{ExecutionContext, Future}

class TaxYearSelectionController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            sessionRepository: SessionRepository,
                                            navigator: Navigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: TaxYearSelectionFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: TaxYearSelectionView,
                                            taiService: TaiService
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  val form: Form[Seq[TaxYearSelection]] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(TaxYearSelectionPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[Seq[TaxYearSelection]]) =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          for {
            ua1          <- Future.fromTry(request.userAnswers.set(TaxYearSelectionPage, value))
            psubResponse <- taiService.getPsubAmount(value, request.nino)
            ua2          <- Future.fromTry(ua1.set(NpsDataSave, psubResponse))
            _            <- sessionRepository.set(ua2)
          } yield Redirect(navigator.nextPage(TaxYearSelectionPage, mode, ua2))
        }
      )
  }
}
