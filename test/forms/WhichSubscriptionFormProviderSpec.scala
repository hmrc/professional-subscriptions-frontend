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

package forms

import forms.behaviours.StringFieldBehaviours
import generators.{Generators, ModelGenerators}
import models.ProfessionalBody
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.FormError

class WhichSubscriptionFormProviderSpec extends StringFieldBehaviours with ScalaCheckPropertyChecks with Generators with ModelGenerators {

  val requiredKey = "whichSubscription.error.required"
  val lengthKey = "whichSubscription.error.length"
  val maxLength = 999

  val professionalBodies = Seq(ProfessionalBody(stringsWithMaxLength(maxLength).sample.value, Nil, None), ProfessionalBody("otherProfessionalBody", Nil, None))
  val form = new WhichSubscriptionFormProvider()(professionalBodies)

  ".value" must {

    val fieldName = "subscription"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(professionalBodies.map(_.name))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "not bind values that are not valid from list of professional bodies" in {

      val invalidProfessionalBody = stringsWithMaxLength(maxLength)
        .suchThat(name => !professionalBodies.map(_.name).contains(name))
        .sample.value

      val result = form.bind(Map(fieldName -> invalidProfessionalBody)).apply(fieldName)
      result.errors shouldEqual Seq(FormError(fieldName, requiredKey))
    }
  }
}
