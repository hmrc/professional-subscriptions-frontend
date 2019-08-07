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
import pages.{DuplicateClaimYearSelectionPage, SavePSubs, SummarySubscriptionsPage, TaxYearSelectionPage}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.DuplicateClaimYearSelectionView

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
          request.userAnswers.get(SummarySubscriptionsPage).get(year.toInt) match {
            case psubToDuplicate: Seq[PSub] =>

              def update(userAnswers: UserAnswers, taxYears: TaxYearSelection): UserAnswers = {
                userAnswers.set(SavePSubs(TaxYearSelection.getTaxYear(taxYears).toString), psubToDuplicate).get
              }

              val ua = value.foldLeft(request.userAnswers)(update)

              sessionRepository.set(ua)

              Future.successful(Redirect(navigator.nextPage(DuplicateClaimYearSelectionPage, mode, request.userAnswers)))
            case _ =>
              Future.successful(Redirect(SessionExpiredController.onPageLoad()))
          }
        }
      )
  }


  // the tail-recursive version of sum
  def sum(list: List[Int]): Int = {
    @tailrec
    def sumWithAccumulator(list: List[Int], currentSum: Int): Int = {
      list match {
        case Nil => {
          val stackTraceAsArray = Thread.currentThread.getStackTrace
          stackTraceAsArray.foreach(println)
          currentSum
        }
        case x :: xs => sumWithAccumulator(xs, currentSum + x)
      }
    }

    sumWithAccumulator(list, 0)
  }

}


