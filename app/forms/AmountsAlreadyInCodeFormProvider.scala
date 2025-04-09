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

import javax.inject.Inject
import forms.mappings.Mappings
import models.UserAnswers
import pages.NpsData
import play.api.data.Form
import models.NpsDataFormats._

class AmountsAlreadyInCodeFormProvider @Inject() extends Mappings {

  def apply(userAnswers: UserAnswers): Form[Boolean] = {

    val errorMessageKey: String = userAnswers.get(NpsData) match {
      case Some(npsData) => if (npsData.size == 1) "single" else "multiple"
      case _             => ""
    }

    Form("value" -> boolean(s"amountsAlreadyInCode.error.required.$errorMessageKey"))
  }

}
