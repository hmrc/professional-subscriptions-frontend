/*
 * Copyright 2021 HM Revenue & Customs
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

package views

import controllers.routes
import models.NormalMode
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.SelfAssessmentClaimView

class SelfAssessmentClaimViewSpec extends ViewBehaviours {

  "SelfAssessmentClaim view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[SelfAssessmentClaimView]

    val applyView = view.apply(routes.SummarySubscriptionsController.onPageLoad(NormalMode).url)(fakeRequest, messages)

    behave like normalPage(applyView, "selfAssessmentClaim")

    behave like pageWithBackLink(applyView)

    "have correct content" in {
      val doc = asDocument(applyView)

      val selfAssessmentLink = Html(s"""<a id="self-assessment-link" href="${frontendAppConfig.selfAssessmentUrl}">${messages("selfAssessmentClaim.link1")}</a>""")

      val summaryLink = Html(s"""<a id="summary-link" href="${routes.SummarySubscriptionsController.onPageLoad(NormalMode).url}">${messages("selfAssessmentClaim.link2")}</a>""")

      assertContainsMessages(doc, messages("selfAssessmentClaim.para1"))

      assertContainsMessages(doc, Html(messages("selfAssessmentClaim.para2", selfAssessmentLink)).toString)
      doc.getElementById("self-assessment-link").attr("href") mustBe frontendAppConfig.selfAssessmentUrl

      assertContainsMessages(doc, Html(messages("selfAssessmentClaim.para3", summaryLink)).toString)
      doc.getElementById("summary-link").attr("href") mustBe routes.SummarySubscriptionsController.onPageLoad(NormalMode).url
    }
  }
}
