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

package views.html.templates

import config.FrontendAppConfig
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.hmrcstandardpage.ServiceURLs
import uk.gov.hmrc.sca.services.WrapperService
import views.html.playComponents.{AdditionalScript, HeadBlock}

import javax.inject.Inject

trait LayoutProvider {

  // noinspection ScalaStyle
  def apply(
      pageTitle: String,
      showBackLink: Boolean = true,
      timeout: Boolean = true,
      showSignOut: Boolean = false,
      scripts: Option[Html] = None,
      stylesheets: Option[Html] = None
  )(contentBlock: Html)(
      implicit request: RequestHeader,
      messages: Messages
  ): HtmlFormat.Appendable

}

class OldLayoutProvider @Inject() (layout: views.html.templates.Layout) extends LayoutProvider {

  // noinspection ScalaStyle
  override def apply(
      pageTitle: String,
      showBackLink: Boolean,
      timeout: Boolean,
      showSignOut: Boolean,
      scripts: Option[Html],
      stylesheets: Option[Html]
  )(contentBlock: Html)(implicit request: RequestHeader, messages: Messages): HtmlFormat.Appendable =
    layout(pageTitle, showBackLink, timeout)(contentBlock)

}

class NewLayoutProvider @Inject() (
    wrapperService: WrapperService,
    additionalScript: AdditionalScript,
    headBlock: HeadBlock,
    appConfig: FrontendAppConfig
) extends LayoutProvider
    with Logging {

  // noinspection ScalaStyle
  override def apply(
      pageTitle: String,
      showBackLink: Boolean,
      timeout: Boolean,
      showSignOut: Boolean,
      scripts: Option[Html],
      stylesheets: Option[Html]
  )(contentBlock: Html)(implicit request: RequestHeader, messages: Messages): HtmlFormat.Appendable = {
    val hideAccountMenu = request.session.get("authToken").isEmpty

    wrapperService.standardScaLayout(
      disableSessionExpired = !timeout,
      content = contentBlock,
      pageTitle = Some(s"$pageTitle - ${messages("service.name")} - ${messages("site.gov.uk")}"),
      serviceURLs = ServiceURLs(
        signOutUrl = Some(controllers.routes.SignOutController.signOut.url),
        serviceUrl = Some(controllers.routes.IndexController.start.url)
      ),
      timeOutUrl = Some(controllers.routes.SignOutController.signOut.url),
      keepAliveUrl = controllers.routes.KeepAliveController.keepAlive.url,
      showBackLinkJS = showBackLink,
      scripts = scripts.toSeq :+ additionalScript(),
      styleSheets = stylesheets.toSeq :+ headBlock(),
      fullWidth = false,
      hideMenuBar = hideAccountMenu
    )(messages, request.withBody(()))
  }

}
