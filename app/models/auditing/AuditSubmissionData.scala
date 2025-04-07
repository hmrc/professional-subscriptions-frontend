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

package models.auditing

import models.{Address, PSub}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Json, Writes, __}

sealed trait AuditSubmissionData {
  val subscriptions: Map[Int, Seq[PSub]]
}

object AuditSubmissionData {

  def apply(
      npsData: Map[Int, Int],
      amountsAlreadyInCode: Option[Boolean],
      subscriptions: Map[Int, Seq[PSub]],
      yourEmployersNames: Option[Seq[String]],
      yourEmployer: Option[Boolean],
      address: Option[Address]
  ): AuditSubmissionData = (yourEmployersNames, yourEmployer) match {
    case (Some(employersNames), Some(yourEmp)) =>
      ContainsCurrentYearUserData(
        previouslyClaimedAmountsFromNPS = npsData,
        hasUserChangedClaimedAmount = amountsAlreadyInCode,
        subscriptions = subscriptions,
        yourEmployersNames = employersNames,
        yourEmployer = yourEmp,
        userCurrentCitizensDetailsAddress = address
      )
    case _ =>
      PreviousYearsUserData(
        previouslyClaimedAmountsFromNPS = npsData,
        hasUserChangedClaimedAmount = amountsAlreadyInCode,
        subscriptions = subscriptions,
        userCurrentCitizensDetailsAddress = address
      )
  }

  implicit val writes: Writes[AuditSubmissionData] = new Writes[AuditSubmissionData] {
    override def writes(o: AuditSubmissionData): JsValue =
      o match {
        case x: ContainsCurrentYearUserData => Json.toJson(x)(ContainsCurrentYearUserData.writes)
        case x: PreviousYearsUserData       => Json.toJson(x)(PreviousYearsUserData.writes)
      }
  }

}

case class ContainsCurrentYearUserData(
    previouslyClaimedAmountsFromNPS: Map[Int, Int],
    hasUserChangedClaimedAmount: Option[Boolean],
    subscriptions: Map[Int, Seq[PSub]],
    yourEmployersNames: Seq[String],
    yourEmployer: Boolean,
    userCurrentCitizensDetailsAddress: Option[Address]
) extends AuditSubmissionData

object ContainsCurrentYearUserData {

  // imports required for the writes below
  import models.PSubsByYear.pSubsByYearFormats
  import models.NpsDataFormats.npsDataFormatsFormats

  implicit lazy val writesAddress: Writes[Address] =
    (__ \ "line1")
      .writeNullable[String]
      .and((__ \ "line2").writeNullable[String])
      .and((__ \ "line3").writeNullable[String])
      .and((__ \ "line4").writeNullable[String])
      .and((__ \ "line5").writeNullable[String])
      .and((__ \ "postcode").writeNullable[String])
      .and((__ \ "country").writeNullable[String])(unlift(Address.unapply))

  implicit val writes: Writes[ContainsCurrentYearUserData] = Json.writes[ContainsCurrentYearUserData]
}

case class PreviousYearsUserData(
    previouslyClaimedAmountsFromNPS: Map[Int, Int],
    hasUserChangedClaimedAmount: Option[Boolean],
    subscriptions: Map[Int, Seq[PSub]],
    userCurrentCitizensDetailsAddress: Option[Address]
) extends AuditSubmissionData

object PreviousYearsUserData {

  // imports required for the writes below
  import models.PSubsByYear.pSubsByYearFormats
  import models.NpsDataFormats.npsDataFormatsFormats

  implicit lazy val writesAddress: Writes[Address] =
    (__ \ "line1")
      .writeNullable[String]
      .and((__ \ "line2").writeNullable[String])
      .and((__ \ "line3").writeNullable[String])
      .and((__ \ "line4").writeNullable[String])
      .and((__ \ "line5").writeNullable[String])
      .and((__ \ "postcode").writeNullable[String])
      .and((__ \ "country").writeNullable[String])(unlift(Address.unapply))

  implicit val writes: Writes[PreviousYearsUserData] = Json.writes[PreviousYearsUserData]
}
