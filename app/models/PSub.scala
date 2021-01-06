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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Reads, Writes, __}

case class PSub(
                 nameOfProfessionalBody: String,
                 amount: Int,
                 employerContributed: Boolean,
                 employerContributionAmount: Option[Int]
               )

object PSub {
  implicit lazy val reads: Reads[PSub] = (
    ((__ \ "nameOfProfessionalBody").read[String] orElse (__ \ "name").read[String]) and
      (__ \ "amount").read[Int] and
      (__ \ "employerContributed").read[Boolean] and
      (__ \ "employerContributionAmount").readNullable[Int]
    ) (PSub.apply _)

  implicit lazy val writes: Writes[PSub] = Json.writes[PSub]
}
