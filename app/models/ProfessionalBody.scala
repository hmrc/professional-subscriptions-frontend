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

package models

import play.api.libs.json._
import play.api.i18n.Messages

case class ProfessionalBody(name: String, synonyms: List[String], startYear: Option[Int]) {
  def toAutoCompleteJson(implicit messages: Messages): JsObject =
    Json.obj("displayName" -> name, "synonyms" -> synonyms)

  def toDisplayText(): String = {
    startYear.foldLeft(name){(psubName,year) => psubName + ", with effect from 6 April " + year}
  }
}

object ProfessionalBody {
  implicit val format: Format[ProfessionalBody] = Json.format[ProfessionalBody]

  implicit val listReads: Reads[Seq[ProfessionalBody]] =
    __.read(Reads.seq[ProfessionalBody])
}
