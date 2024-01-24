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
import controllers.routes.SessionExpiredController
import forms.DuplicateClaimYearSelectionFormProvider
import javax.inject.Inject
import models.PSubsByYear._
import models.TaxYearSelection._
import models.{Enumerable, Mode, PSub, PSubsByYear, TaxYearSelection}
import navigation.Navigator
import pages.{DuplicateClaimYearSelectionPage, SummarySubscriptionsPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import services.ProfessionalBodiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PSubsUtil
import views.html.DuplicateClaimYearSelectionView

import scala.concurrent.{ExecutionContext, Future}

class DuplicateClaimYearSelectionController @Inject()(
                                                       sessionService: SessionService,
                                                       navigator: Navigator,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: DuplicateClaimYearSelectionFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: DuplicateClaimYearSelectionView,
                                                       professionalBodiesService: ProfessionalBodiesService
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  val form: Form[Seq[TaxYearSelection]] = formProvider()

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {

    implicit request =>

      val professionalBodies = professionalBodiesService.professionalBodies

      request.userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) match {
        case Some(psubsByYear: Map[Int, Seq[PSub]]) =>
          val createDuplicateCheckBox = createDuplicateCheckbox(psubsByYear, professionalBodies, year, index)

          if (createDuplicateCheckBox.checkboxOption.isEmpty) {
            Redirect(routes.SummarySubscriptionsController.onPageLoad(mode))
          } else {
            Ok(view(form, mode, createDuplicateCheckBox, year, index))
          }

        case _ =>
          Redirect(SessionExpiredController.onPageLoad)
      }

  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[Seq[TaxYearSelection]]) => {
          request.userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.pSubsByYearFormats) match {
            case Some(psubsByYear: Map[Int, Seq[PSub]]) =>

              val professionalBodies = professionalBodiesService.professionalBodies
              val createDuplicateCheckBox = createDuplicateCheckbox(psubsByYear, professionalBodies, year, index)

              Future.successful(BadRequest(view(formWithErrors, mode, createDuplicateCheckBox, year, index)))

            case _ =>
              Future.successful(Redirect(SessionExpiredController.onPageLoad))
          }
        },
        value => {
          request.userAnswers.get(SummarySubscriptionsPage).flatMap {
            allPsubs: Map[Int, Seq[PSub]] =>

              allPsubs.get(year.toInt) map {
                psubs =>
                  val psubToDuplicate: PSub = psubs(index)
                  val ua = PSubsUtil.duplicatePsubsUserAnswers(value, request.userAnswers, allPsubs, psubToDuplicate)
                  sessionService.set(ua).map(_ =>
                      Redirect(navigator.nextPage(DuplicateClaimYearSelectionPage, mode, ua))
                  )
              }
          }.getOrElse {
            Future.successful(Redirect(SessionExpiredController.onPageLoad))
          }
        }
      )
  }
}
