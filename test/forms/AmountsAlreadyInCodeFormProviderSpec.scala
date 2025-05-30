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

package forms

import forms.behaviours.BooleanFieldBehaviours
import models.UserAnswers
import models.TaxYearSelection._
import models.NpsDataFormats.npsDataFormatsFormats
import org.scalatest.TryValues
import pages.NpsData
import play.api.data.FormError
import play.api.libs.json.Json

class AmountsAlreadyInCodeFormProviderSpec extends BooleanFieldBehaviours with TryValues {

  val requiredKey = "amountsAlreadyInCode.error.required.single"
  val invalidKey  = "error.boolean"

  def emptyUserAnswers = UserAnswers("id", Json.obj())

  def ua: UserAnswers = emptyUserAnswers.set(NpsData, Map(getTaxYear(CurrentYear) -> 100)).success.value

  val form = new AmountsAlreadyInCodeFormProvider()(ua)

  ".value" must {

    val fieldName = "value"

    behave.like(
      booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )
    )

    behave.like(
      mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    )
  }

}
