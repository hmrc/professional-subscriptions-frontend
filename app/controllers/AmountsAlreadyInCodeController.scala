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
import forms.AmountsAlreadyInCodeFormProvider
import javax.inject.Inject
import models.NpsDataFormats._
import models.{Mode, PSubsByYear, TaxYearSelection}
import navigation.Navigator
import pages.{AmountsAlreadyInCodePage, NpsData, SummarySubscriptionsPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AmountsAlreadyInCodeView

import scala.concurrent.{ExecutionContext, Future}

class AmountsAlreadyInCodeController @Inject() (
    sessionService: SessionService,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: AmountsAlreadyInCodeFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: AmountsAlreadyInCodeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = identify.andThen(getData).andThen(requireData) { implicit request =>
    val form: Form[Boolean] = formProvider(request.userAnswers)

    val preparedForm = request.userAnswers.get(AmountsAlreadyInCodePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    (
      request.userAnswers.get(NpsData),
      request.userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)
    ) match {
      case (Some(npsData), Some(psubsByYear)) =>
        val taxYears: Seq[TaxYearSelection] = PSubsByYear.orderTaxYears(psubsByYear)
        Ok(view(preparedForm, mode, taxYears, npsData))
      case _ =>
        Redirect(routes.SessionExpiredController.onPageLoad)
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    identify.andThen(getData).andThen(requireData).async { implicit request =>
      val form: Form[Boolean] = formProvider(request.userAnswers)

      (
        request.userAnswers.get(NpsData),
        request.userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats)
      ) match {
        case (Some(npsData), Some(psubsByYear)) =>
          form
            .bindFromRequest()
            .fold(
              (formWithErrors: Form[_]) => {
                val taxYears: Seq[TaxYearSelection] = PSubsByYear.orderTaxYears(psubsByYear)
                Future.successful(BadRequest(view(formWithErrors, mode, taxYears, npsData)))
              },
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AmountsAlreadyInCodePage, value))
                  _              <- sessionService.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AmountsAlreadyInCodePage, mode, updatedAnswers))
            )
        case _ =>
          Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
      }
    }

}
