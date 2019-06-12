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
import controllers.routes._
import javax.inject.Inject
import models.{Mode, SummaryData}
import models.TaxYearSelection._
import navigation.Navigator
import pages.{NpsData, SummarySubscriptionsPage, TaxYearSelectionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.SummarySubscriptionsView

import scala.concurrent.ExecutionContext

class SummarySubscriptionsController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: SummarySubscriptionsView,
                                                navigator: Navigator,
                                                sessionRepository: SessionRepository
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      (
        request.userAnswers.get(TaxYearSelectionPage),
        request.userAnswers.get(SummarySubscriptionsPage),
        request.userAnswers.get(NpsData)
      ) match {
        case (Some(taxYears), None, Some(npsData)) =>
          val subs = taxYears.flatMap(
            taxYear =>
              Map(getTaxYear(taxYear) -> SummaryData(Seq.empty, npsData(getTaxYear(taxYear).toString)))
          ).toMap

          Ok(view(subs, navigator.nextPage(SummarySubscriptionsPage, mode, request.userAnswers).url, mode))
        case (Some(taxYears), Some(subscriptions), Some(npsData)) =>
          val subs = taxYears.flatMap(
            taxYear => {
              if (subscriptions.keys.exists(_ == getTaxYear(taxYear).toString))
                Map(getTaxYear(taxYear) -> SummaryData(subscriptions(getTaxYear(taxYear).toString), npsData(getTaxYear(taxYear).toString)))
              else
                Map(getTaxYear(taxYear) -> SummaryData(Seq.empty, npsData(getTaxYear(taxYear).toString)))
            }
          ).toMap

          Ok(view(subs, navigator.nextPage(SummarySubscriptionsPage, mode, request.userAnswers).url, mode))
        case _ =>
          Redirect(SessionExpiredController.onPageLoad())
      }

  }
}
