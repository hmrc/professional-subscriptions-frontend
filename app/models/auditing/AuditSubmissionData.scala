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

  implicit val writes: Writes[AuditSubmissionData] = new Writes[AuditSubmissionData] {
    override def writes(o: AuditSubmissionData): JsValue = o match {
      case x: ContainsCurrentYearUserData => ContainsCurrentYearUserData.writes.writes(x)
      case x: PreviousYearsUserData => PreviousYearsUserData.writes.writes(x)
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
  implicit val writes: Writes[ContainsCurrentYearUserData] = Json.writes[ContainsCurrentYearUserData]
}

case class PreviousYearsUserData(
    previouslyClaimedAmountsFromNPS: Map[Int, Int],
    hasUserChangedClaimedAmount: Option[Boolean],
    subscriptions: Map[Int, Seq[PSub]],
    userCurrentCitizensDetailsAddress: Option[Address]
)  extends AuditSubmissionData

object PreviousYearsUserData {
  implicit val writes: Writes[PreviousYearsUserData] = Json.writes[PreviousYearsUserData]
}
