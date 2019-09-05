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
import controllers.routes.SessionExpiredController
import javax.inject.Inject
import models.TaxYearSelection
import models.TaxYearSelection._
import pages.SummarySubscriptionsPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html._

import scala.concurrent.ExecutionContext

class HowYouWillGetYourExpensesController @Inject()(
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     currentView: HowYouWillGetYourExpensesCurrentView,
                                                     previousView: HowYouWillGetYourExpensesPreviousView,
                                                     currentAndPreviousYearView: HowYouWillGetYourExpensesCurrentAndPreviousYearView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      import models.PSubsByYear._

      val getTaxYearSelection: Option[Seq[TaxYearSelection]] = request.userAnswers.get(SummarySubscriptionsPage).map(orderTaxYears)
      val redirectUrl = controllers.routes.SubmissionController.submission().url

      getTaxYearSelection match {
        case Some(seqTaxYearSelection) if seqTaxYearSelection.contains(CurrentYear) && seqTaxYearSelection.length > 1 =>
          Ok(currentAndPreviousYearView(redirectUrl, containsCurrentYearMinus1(seqTaxYearSelection)))
        case Some(seqTaxYearSelection) if seqTaxYearSelection.contains(CurrentYear) =>
          Ok(currentView(redirectUrl))
        case Some(seqTaxYearSelection) =>
          Ok(previousView(redirectUrl, containsCurrentYearMinus1(seqTaxYearSelection)))
        case _ =>
          Redirect(SessionExpiredController.onPageLoad())
      }
  }

  private def containsCurrentYearMinus1(taxYearSelections: Seq[TaxYearSelection]): Boolean = {
    taxYearSelections.contains(CurrentYearMinus1)
  }
}
