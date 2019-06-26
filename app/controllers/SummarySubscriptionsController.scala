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
import models.TaxYearSelection._
import models.{Mode, PSub}
import navigation.Navigator
import pages.{SummarySubscriptionsPage, TaxYearSelectionPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.SummarySubscriptionsView

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext

class SummarySubscriptionsController @Inject()(
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

      request.userAnswers.get(TaxYearSelectionPage) match {
        case Some(taxYears) =>
          val subs: Map[Int, Seq[PSub]] = ListMap(taxYears.flatMap(
            taxYear =>
              request.userAnswers.get(SummarySubscriptionsPage) match {
                case Some(subscriptions) =>
                  if (subscriptions.keys.exists(_ == getTaxYear(taxYear).toString))
                    Map(getTaxYear(taxYear) -> subscriptions(getTaxYear(taxYear).toString))
                  else
                    Map(getTaxYear(taxYear) -> Seq.empty)
                case _ =>
                  Map(getTaxYear(taxYear) -> Seq.empty)
              }
          ).sortWith(_._1 > _._1):_*)

          Ok(view(subs, navigator.nextPage(SummarySubscriptionsPage, mode, request.userAnswers).url, mode))
        case _ =>
          Redirect(SessionExpiredController.onPageLoad())
      }

  }
}
