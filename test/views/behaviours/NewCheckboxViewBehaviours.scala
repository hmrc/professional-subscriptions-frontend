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

import play.api.data.{Form, FormError}
import play.twirl.api.HtmlFormat
import viewmodels.RadioCheckboxOption

trait NewCheckboxViewBehaviours[A] extends NewViewBehaviours {

  def checkboxPage(form: Form[Seq[A]],
                   createView: Form[Seq[A]] => HtmlFormat.Appendable,
                   messageKeyPrefix: String,
                   options: Seq[RadioCheckboxOption],
                   fieldKey: String = "value",
                   legend: Option[String] = None): Unit = {

    def getCheckboxId(i: Int): String = {
      val suffix = if(i > 0) s"-${ i + 1 }" else ""
      s"value$suffix"
    }

    "behave like a checkbox page" must {
      "contain a legend for the question" in {
        val doc = asDocument(createView(form))
        val legends = doc.getElementsByTag("legend")
        legends.size mustBe 1
        legends.first.text contains legend.getOrElse(messages(s"$messageKeyPrefix.heading"))
      }

      "contain an input for the value" in {
        val doc = asDocument(createView(form))
        for {
          (_, i) <- options.zipWithIndex
        } yield {
          assertRenderedById(doc, getCheckboxId(i))
        }
      }

      "contain a label for each input" in {
        val doc = asDocument(createView(form))
        for {
          (option, i) <- options.zipWithIndex
        } yield {
          val id = getCheckboxId(i)
          doc.select(s"label[for=$id]").text contains option.message.html.toString()
        }
      }

      "have no values checked when rendered with no form" in {
        val doc = asDocument(createView(form))
        for {
          (_, i) <- options.zipWithIndex
        } yield {
          assert(!doc.getElementById(getCheckboxId(i)).hasAttr("checked"))
        }
      }

      options.zipWithIndex.foreach {
        case (checkboxOption, i) =>
          s"have correct value checked when value `${checkboxOption.value}` is given" in {
            val data: Map[String, String] =
              Map(s"$fieldKey[$i]" -> checkboxOption.value)

            val doc = asDocument(createView(form.bind(data)))
            assert(doc.getElementById(getCheckboxId(i)).hasAttr("checked"), s"${getCheckboxId(i)} is not checked")

            options.zipWithIndex.foreach {
              case (option, j) =>
                if (option != checkboxOption) {
                  assert(!doc.getElementById(getCheckboxId(j)).hasAttr("checked"), s"${getCheckboxId(j)} is checked")
                }
            }
          }
      }

      "not render an error summary" in {
        val doc = asDocument(createView(form))
        assertNotRenderedById(doc, "error-summary-heading")
      }


      "show error in the title" in {
        val doc = asDocument(createView(form.withError(FormError(fieldKey, "error.invalid"))))
        doc.title.contains("Error: ") mustBe true
      }

      "show an error summary" in {
        val doc = asDocument(createView(form.withError(FormError(fieldKey, "error.invalid"))))
        assertRenderedByCssSelector(doc, ".govuk-error-summary__title")
      }

      "show an error in the value field's label" in {
        val doc = asDocument(createView(form.withError(FormError(fieldKey, "error.invalid"))))
        val errorSpan = doc.getElementsByClass("govuk-error-message").first
        errorSpan.text mustBe s"Error: ${messages("error.invalid")}"
      }
    }
  }
}
