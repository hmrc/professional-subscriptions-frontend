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

package views

import controllers.routes
import views.behaviours.ViewBehaviours
import views.html.SelfAssessmentClaimView

class SelfAssessmentClaimViewSpec extends ViewBehaviours {

  "SelfAssessmentClaim view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[SelfAssessmentClaimView]

    val applyView = view.apply(frontendAppConfig.selfAssessmentUrl, routes.SummarySubscriptionsController.onPageLoad().url)(fakeRequest, messages)

    behave like normalPage(applyView, "selfAssessmentClaim")

    behave like pageWithBackLink(applyView)

    "have correct content" in {
      val doc = asDocument(applyView)

      assertContainsMessages(doc, messages("selfAssessmentClaim.para1"))

      assertContainsMessages(doc, messages("selfAssessmentClaim.para2"))
      assertContainsMessages(doc, messages("selfAssessmentClaim.link1"))
      doc.getElementById("self-assessment-link").attr("href") mustBe frontendAppConfig.selfAssessmentUrl

      assertContainsMessages(doc, messages("selfAssessmentClaim.para3"))
      assertContainsMessages(doc, messages("selfAssessmentClaim.link2"))
      doc.getElementById("summary-link").attr("href") mustBe routes.SummarySubscriptionsController.onPageLoad().url
    }
  }
}
