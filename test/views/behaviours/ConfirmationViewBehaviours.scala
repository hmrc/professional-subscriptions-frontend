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

package views.behaviours

import play.twirl.api.HtmlFormat
import views.NewViewSpecBase

trait ConfirmationViewBehaviours extends NewViewSpecBase {

  def normalPage(view: HtmlFormat.Appendable, messageKeyPrefix: String, messageKeySuffix: Option[String] = None): Unit =

    "behave like a normal page" when {

      "rendered" must {

        "have the correct banner title" in {

          val doc = asDocument(view)
          assertRenderedByCssSelector(doc, "div.govuk-header__content")
        }

        "display the correct browser title" in {

          val doc = asDocument(view)
          assertEqualsMessage(
            doc = doc,
            cssSelector = "title",
            expectedMessageKey =
              if (messageKeySuffix.isEmpty)
                s"${messages(s"$messageKeyPrefix.title")} - ${messages("service.name")} - ${messages("site.gov.uk")}"
              else
                s"${messages(s"$messageKeyPrefix.title.${messageKeySuffix.get}")} - ${messages("service.name")} - ${messages("site.gov.uk")}"
          )
        }

        "display the correct page title" in {

          val doc = asDocument(view)
          if (messageKeySuffix.isEmpty) assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading")
          else assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading.${messageKeySuffix.get}")
        }

        "display language toggles" in {

          val doc = asDocument(view)
          assertRenderedByCssSelector(doc, ".hmrc-language-select")
        }
      }
    }

  def pageWithBackLink(view: HtmlFormat.Appendable): Unit =

    "behave like a page with a back link" must {

      "have a back link" in {

        val doc = asDocument(view)
        assertRenderedById(doc, "back-link")
      }
    }

  def pageWithBodyText(view: HtmlFormat.Appendable, messageKey: String*): Unit =

    "behave like a page with body text" must {

      "display content" in {
        val doc = asDocument(view)
        for (key <- messageKey)
          assertContainsMessages(doc, key)
      }
    }

}
