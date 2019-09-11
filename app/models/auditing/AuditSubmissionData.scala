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

package models.auditing

import models.{Address, PSub}
import play.api.libs.json.{JsValue, Json, Writes}

sealed trait AuditSubmissionData {
  val subscriptions: Map[Int, Seq[PSub]]
}

object AuditSubmissionData {
  def apply(npsData: Map[Int, Int],
            amountsAlreadyInCode: Option[Boolean],
            subscriptions: Map[Int, Seq[PSub]],
            yourEmployersNames: Option[Seq[String]],
            yourEmployer: Option[Boolean],
            address: Option[Address]): AuditSubmissionData = (yourEmployersNames, yourEmployer) match {
      case (Some(employersNames), Some(yourEmp)) =>
        ContainsCurrentYearUserData(
          npsData,
          amountsAlreadyInCode,
          subscriptions,
          employersNames,
          yourEmp,
          address
        )
      case _ =>
        PreviousYearsUserData(
          npsData,
          amountsAlreadyInCode,
          subscriptions,
          address
        )
    }

  import models.PSubsByYear.pSubsByYearFormats
  import models.NpsDataFormats.npsDataFormatsFormats

  implicit val writes: Writes[AuditSubmissionData] = new Writes[AuditSubmissionData] {
    override def writes(o: AuditSubmissionData): JsValue = o match {
      case x: ContainsCurrentYearUserData => Json.toJson(x)(ContainsCurrentYearUserData.writes)
      case x: PreviousYearsUserData => Json.toJson(x)(PreviousYearsUserData.writes)
    }
  }
}

case class ContainsCurrentYearUserData(
    npsData: Map[Int, Int],
    amountsAlreadyInCode: Option[Boolean],
    subscriptions: Map[Int, Seq[PSub]],
    yourEmployersNames: Seq[String],
    yourEmployer: Boolean,
    address: Option[Address]
) extends AuditSubmissionData

object ContainsCurrentYearUserData {
  implicit val writes: Writes[ContainsCurrentYearUserData] = Json.writes[ContainsCurrentYearUserData]
}

case class PreviousYearsUserData(
    npsData: Map[Int, Int],
    amountsAlreadyInCode: Option[Boolean],
    subscriptions: Map[Int, Seq[PSub]],
    address: Option[Address]
)  extends AuditSubmissionData

object PreviousYearsUserData {
  implicit val writes: Writes[PreviousYearsUserData] = Json.writes[PreviousYearsUserData]
}
