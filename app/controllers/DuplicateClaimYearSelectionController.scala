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
import models.{Enumerable, Mode, PSub, TaxYearSelection, UserAnswers}
import navigation.Navigator
import pages.{DuplicateClaimYearSelectionPage, PSubPage, SavePSubs, SummarySubscriptionsPage, TaxYearSelectionPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
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
                                                       view: DuplicateClaimYearSelectionView
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  val form: Form[Seq[TaxYearSelection]] = formProvider()

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(DuplicateClaimYearSelectionPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(TaxYearSelectionPage) match {
        case Some(taxYearSelection) =>
          val filteredTaxYearSelection = TaxYearSelection.filterTaxYearSelection(taxYearSelection, year)
          Ok(view(preparedForm, mode, TaxYearSelection.getTaxYearCheckboxOptions(filteredTaxYearSelection), year, index))
        case None =>
          Redirect(SessionExpiredController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[Seq[TaxYearSelection]]) => {
          request.userAnswers.get(TaxYearSelectionPage) match {
            case Some(taxYearSelection) =>
              val filteredTaxYearSelection = TaxYearSelection.filterTaxYearSelection(taxYearSelection, year)
              Future.successful(BadRequest(view(formWithErrors, mode, TaxYearSelection.getTaxYearCheckboxOptions(filteredTaxYearSelection), year, index)))
            case _ =>
              Future.successful(Redirect(SessionExpiredController.onPageLoad()))
          }
        },
        value => {

          request.userAnswers.get(SummarySubscriptionsPage).map {
            allPsubs =>

              val getDuplicatePsubYear: Option[Seq[PSub]] = allPsubs.get(year.toInt)

              getDuplicatePsubYear match {
                case Some(psubs) if psubs.nonEmpty =>

                  val psubToDuplicate: PSub = psubs(index)

                  val ua = value.foldLeft(request.userAnswers)(
                    (userAnswers: UserAnswers, taxYearSelection) => {

                      val getPsubsForYear: Option[Seq[PSub]] = allPsubs.get(TaxYearSelection.getTaxYear(taxYearSelection))
                      val getNextIndex: Int = getPsubsForYear.map(_.length).getOrElse(0)

                      userAnswers.set(PSubPage(TaxYearSelection.getTaxYear(taxYearSelection).toString, getNextIndex), psubToDuplicate)
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
