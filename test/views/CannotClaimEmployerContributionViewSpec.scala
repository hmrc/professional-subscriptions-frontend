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

package views

import models.NormalMode
import views.behaviours.{NewViewBehaviours, ViewBehaviours}
import views.html.CannotClaimEmployerContributionView

class CannotClaimEmployerContributionViewSpec extends NewViewBehaviours {

  "CannotClaimEmployerContribution view" must {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    val view = application.injector.instanceOf[CannotClaimEmployerContributionView]

    val applyView = view.apply(NormalMode, taxYear, index)(fakeRequest, messages)

    behave like normalPage(applyView, "cannotClaimEmployerContribution")

    behave like pageWithBackLink(applyView)

    "have correct content" in {
      val doc = asDocument(applyView)

      assertContainsMessages(doc, messages("cannotClaimEmployerContribution.para1"))

      doc.getElementsByClass("govuk-button").text() mustBe messages("cannotClaimEmployerContribution.button")
    }
  }
}
