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
import models.{Mode, TaxYearSelection}
import navigation.Navigator
import pages.{SummarySubscriptionsPage, TaxYearSelectionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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
                                                navigator: Navigator
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode, year: String, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      println(s"\n\n\n\n ua: ${request.userAnswers}\n\n\n\n")

      request.userAnswers.get(TaxYearSelectionPage) match {
        case Some(taxYears) =>

          taxYears.foreach(taxYear => {

            println(s"\n\n${TaxYearSelection.getTaxYear(taxYear)}")
            println(s"\n\n\n\n${request.userAnswers.get(SummarySubscriptionsPage(TaxYearSelection.getTaxYear(taxYear).toString))}\n\n\n\n")
          })
      }

      println(s"\n\n\n\n${request.userAnswers.get(SummarySubscriptionsPage(year))}\n\n\n\n")

      Ok(view(navigator.nextPage(SummarySubscriptionsPage(year), mode, request.userAnswers).url))
  }
}
