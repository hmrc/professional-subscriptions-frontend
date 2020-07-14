/*
 * Copyright 2020 HM Revenue & Customs
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

case class ProfessionalBody(name: String, synonyms: List[String], startYear: Option[Int]) {
  def toAutoCompleteJson: JsObject =
    Json.obj("displayName" -> name, "synonyms" -> synonyms)

  def toDisplayText: String = {
    if (startYear.isDefined) name + ", with effect from 6 April " + startYear.get else name
  }

  def validateStartYear(year: Int): Boolean = {
    startYear match {
      case Some(startYear) => startYear <= year
      case _ => true
    }
  }
}

object ProfessionalBody {
  implicit val format: Format[ProfessionalBody] = Json.format[ProfessionalBody]

  implicit val listReads: Reads[Seq[ProfessionalBody]] =
    __.read(Reads.seq[ProfessionalBody])
}
