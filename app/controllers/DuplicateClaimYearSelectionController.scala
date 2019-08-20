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
import controllers.routes.{SessionExpiredController, TechnicalDifficultiesController}
import forms.DuplicateClaimYearSelectionFormProvider
import javax.inject.Inject
import models.PSubsByYear._
import models.TaxYearSelection._
import models.{Enumerable, Mode, PSub, PSubsByYear, TaxYearSelection, UserAnswers}
import navigation.Navigator
import pages.{DuplicateClaimYearSelectionPage, PSubPage, SummarySubscriptionsPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ProfessionalBodiesService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.DuplicateClaimYearSelectionView

import scala.concurrent.{ExecutionContext, Future}

class DuplicateClaimYearSelectionController @Inject()(
                                                       sessionRepository: SessionRepository,
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

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      request.userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.formats) match {
        case Some(psubsByYear: Map[Int, Seq[PSub]]) =>

          professionalBodiesService.professionalBodies().flatMap {
            professionalBodies =>
              val createDuplicateCheckBox = createDuplicateCheckbox(psubsByYear, professionalBodies, year, index)

              Future.successful(Ok(view(form, mode, createDuplicateCheckBox, year, index)))
          }.recover {
            case _ => Redirect(TechnicalDifficultiesController.onPageLoad())
          }

        case _ =>
          Future.successful(Redirect(SessionExpiredController.onPageLoad()))
      }
  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[Seq[TaxYearSelection]]) => {
          request.userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.formats) match {
            case Some(psubsByYear: Map[Int, Seq[PSub]]) =>
              professionalBodiesService.professionalBodies().flatMap {
                professionalBodies =>
                  val createDuplicateCheckBox = createDuplicateCheckbox(psubsByYear, professionalBodies, year, index)

                  Future.successful(BadRequest(view(formWithErrors, mode, createDuplicateCheckBox, year, index)))
              }.recover {
                case _ => Redirect(TechnicalDifficultiesController.onPageLoad())
              }
            case _ =>
              Future.successful(Redirect(SessionExpiredController.onPageLoad()))
          }
        },
        value => {

          request.userAnswers.get(SummarySubscriptionsPage).map {
            allPsubs =>

              val getDuplicatePsubYear: Option[Seq[PSub]] = allPsubs.get(year.toInt)

              getDuplicatePsubYear match {
                case Some(psubs) =>

                  val ua = value.foldLeft(request.userAnswers)(
                    (userAnswers: UserAnswers, taxYearSelection) => {

                      val getPsubsForYear: Option[Seq[PSub]] = allPsubs.get(getTaxYear(taxYearSelection))
                      val getNextIndex: Int = getPsubsForYear.map(_.length).getOrElse(0)

                      userAnswers.set(PSubPage(getTaxYear(taxYearSelection).toString, getNextIndex), psubs(index))
                        .getOrElse(userAnswers)
                    })

                  sessionRepository.set(ua)
                  Future.successful(Redirect(navigator.nextPage(DuplicateClaimYearSelectionPage, mode, ua)))
                case _ =>
                  Future.successful(Redirect(SessionExpiredController.onPageLoad()))
              }
          }.getOrElse {
            Future.successful(Redirect(SessionExpiredController.onPageLoad()))
          }
        }
      )
  }
}
