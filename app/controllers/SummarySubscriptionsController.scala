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
import javax.inject.Inject
import models.{Mode, NpsDataFormats, PSubsByYear}
import navigation.Navigator
import pages.{NpsData, SummarySubscriptionsPage}
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

      request.userAnswers.get(SummarySubscriptionsPage)(PSubsByYear.formats) match {
        case Some(psubsByYears) => {

          val npsData: Map[Int, Int] = ListMap(psubsByYears.flatMap(
            psubByYear =>
              request.userAnswers.get(NpsData)(NpsDataFormats.formats) match {
                case Some(npsData) =>
                  Map(psubByYear._1 -> npsData.getOrElse(psubByYear._1, 0))
                case _ =>
                  Map(psubByYear._1 -> 0)
              }
          ).toSeq.sortWith(_._1 > _._1):_*)

          val orderedPsubs = ListMap(psubsByYears.toSeq.sortWith(_._1 > _._1):_*)

          Ok(view(orderedPsubs, npsData, navigator.nextPage(SummarySubscriptionsPage, mode, request.userAnswers).url, mode))
        }
        case _ =>
          Redirect(routes.SessionExpiredController.onPageLoad())
      }

  }
}
