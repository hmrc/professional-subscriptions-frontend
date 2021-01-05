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

import javax.inject.Inject
import forms.mappings.Mappings
import models.ProfessionalBody
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

class WhichSubscriptionFormProvider @Inject() extends Mappings {

  private def duplicateProfessionalBodiesConstraint(allProfessionalBodies: Seq[ProfessionalBody]): Constraint[String] =
    Constraint {
      name =>
        allProfessionalBodies
          .map(_.name)
          .find(_ == name)
          .map(_ => Valid)
          .getOrElse(Invalid("whichSubscription.error.required"))
    }

  def apply(allProfessionalBodies: Seq[ProfessionalBody]): Form[String] =
    Form(
      "subscription" ->
        text("whichSubscription.error.required")
          .verifying(duplicateProfessionalBodiesConstraint(allProfessionalBodies))
    )
}
