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

package base

import com.github.tototoshi.play2.scalate.Scalate
import config.FrontendAppConfig
import controllers.actions._
import models.NpsDataFormats.npsDataFormatsFormats
import models.TaxYearSelection._
import models._
import models.auditing._
import navigation.Navigator
import org.scalatest.TryValues
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import pages._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.partials.FormPartialRetriever
import utils.{MockPsFormPartialRetriever, MockScalate}

trait SpecBase extends PlaySpec with GuiceOneAppPerSuite with TryValues {

  val userAnswersId = "id"
  val navigator = new Navigator

  lazy val fakeNino = "AB123456A"
  lazy val taxYear: String = getTaxYear(CurrentYear).toString
  lazy val index = 0
  lazy val taxYearInt: Int = getTaxYear(CurrentYear)
  lazy val psubWithEmployerContribution: PSub = PSub(nameOfProfessionalBody = "psub", amount = 10, employerContributed = true, employerContributionAmount = Some(5))
  lazy val psubWithoutEmployerContribution: PSub = PSub(nameOfProfessionalBody = "psub", amount = 10, employerContributed = false, employerContributionAmount = None)

  lazy val validAddress = Address(
    Some("6 Howsell Road"),
    Some("Llanddew"),
    Some("Line 3"),
    Some("Line 4"),
    Some("Line 5"),
    Some("DN16 3FB"),
    Some("GREAT BRITAIN")
  )

  lazy val validAddressJson: JsValue = Json.parse(
    s"""
       |{
       |  "address":{
       |    "line1":"6 Howsell Road",
       |    "line2":"Llanddew",
       |    "line3":"Line 3",
       |    "line4":"Line 4",
       |    "line5":"Line 5",
       |    "postcode":"DN16 3FB",
       |    "country":"GREAT BRITAIN"
       |  }
       |}
     """.stripMargin
  )

  lazy val emptyAddressJson: JsValue = Json.parse(
    s"""
       |{
       |  "address":{
       |    "line1":"",
       |    "line2":"",
       |    "line3":"",
       |    "line4":"",
       |    "line5":"",
       |    "postcode":"",
       |    "country":""
       |  }
       |}
     """.stripMargin
  )

  val validEmploymentJson: JsValue = Json.parse(
    """{
      | "data" : {
      |   "employments" : [{
      |    "name": "HMRC Longbenton",
      |    "startDate": "2018-06-27"
      |    }]
      |  }
      |}""".stripMargin
  )

  val validTaxCodeRecordJson: JsValue = Json.parse(
    """
      |{
      |  "data" : [ {
      |    "componentType" : "EmploymentIncome",
      |    "employmentId" : 1,
      |    "amount" : 1100,
      |    "description" : "EmploymentIncome",
      |    "taxCode" : "1150L",
      |    "name" : "Employer1",
      |    "basisOperation" : "Week1Month1BasisOperation",
      |    "status" : "Live",
      |    "inYearAdjustment" : 0
      |  }, {
      |    "componentType" : "EmploymentIncome",
      |    "employmentId" : 2,
      |    "amount" : 0,
      |    "description" : "EmploymentIncome",
      |    "taxCode" : "1100L",
      |    "name" : "Employer2",
      |    "basisOperation" : "OtherBasisOperation",
      |    "status" : "PotentiallyCeased",
      |    "inYearAdjustment" : 321.12
      |  }, {
      |    "componentType" : "EmploymentIncome",
      |    "employmentId" : 2,
      |    "amount" : 0,
      |    "description" : "EmploymentIncome",
      |    "taxCode" : "1100L",
      |    "name" : "Employer2",
      |    "basisOperation" : "OtherBasisOperation",
      |    "status" : "Ceased",
      |    "inYearAdjustment" : 321.12
      |  } ],
      |  "links" : [ ]
      |}
    """.stripMargin
  )

  lazy val etag: Int = 123

  lazy val validEtagJson: JsValue = Json.parse(
    s"""
       |{
       |   "etag":"$etag"
       |}
    """.stripMargin)

  lazy val invalidEtagJson: JsValue = Json.parse(
    s"""
       |{
       |   "etag":""
       |}
    """.stripMargin)

  def userAnswersCurrent: UserAnswers = emptyUserAnswers
    .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
    .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 200).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
    .set(NpsData, Map(
      getTaxYear(CurrentYear) -> 300
    )).success.value
    .set(YourEmployerPage, true).success.value
    .set(CitizensDetailsAddress, validAddress).success.value
    .set(YourEmployersNames, Seq.empty[String]).success.value
    .set(CitizensDetailsAddress, validAddress).success.value

  val dataToAuditCurrent = ContainsCurrentYearUserData(
    previouslyClaimedAmountsFromNPS = Map(getTaxYear(CurrentYear) -> 300),
    hasUserChangedClaimedAmount = Some(true),
    subscriptions = Map(getTaxYear(CurrentYear) -> Seq(PSub("Arable Research Institute Association", 1000, true, Some(200)))),
    yourEmployersNames = Nil,
    yourEmployer = true,
    userCurrentCitizensDetailsAddress = Some(validAddress)
  )

  def userAnswersCurrentAndPrevious: UserAnswers = emptyUserAnswers
    .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
    .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 200).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
    .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), "100 Women in Finance").success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), 50).success.value
    .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus1).toString, index), 25).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), true).success.value
    .set(NpsData, Map(
      getTaxYear(CurrentYear) -> 300,
      getTaxYear(CurrentYearMinus1) -> 0
    )).success.value
    .set(YourEmployerPage, true).success.value
    .set(CitizensDetailsAddress, validAddress).success.value
    .set(YourEmployersNames, Seq.empty[String]).success.value

  val dataToAuditCurrentAndPrevious = ContainsCurrentYearUserData(
    previouslyClaimedAmountsFromNPS = Map(getTaxYear(CurrentYear) -> 300, getTaxYear(CurrentYearMinus1) -> 0),
    hasUserChangedClaimedAmount = Some(true),
    subscriptions = Map(
      getTaxYear(CurrentYear) -> Seq(PSub("Arable Research Institute Association", 1000, true, Some(200))),
      getTaxYear(CurrentYearMinus1) -> Seq(PSub("100 Women in Finance", 50, true, Some(25)))
    ),
    yourEmployersNames = Nil,
    yourEmployer = true,
    userCurrentCitizensDetailsAddress = Some(validAddress)
  )

  def userAnswersPrevious: UserAnswers = emptyUserAnswers
    .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus1).toString, index), "100 Women in Finance").success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus1).toString, index), 50).success.value
    .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus1).toString, index), 25).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYearMinus1).toString, index), true).success.value
    .set(NpsData, Map(
      getTaxYear(CurrentYearMinus1) -> 300
    )).success.value
    .set(CitizensDetailsAddress, validAddress).success.value
    .set(CitizensDetailsAddress, validAddress).success.value

  val dataToAuditPrevious = PreviousYearsUserData(
    previouslyClaimedAmountsFromNPS = Map(getTaxYear(CurrentYearMinus1) -> 300),
    hasUserChangedClaimedAmount = Some(true),
    subscriptions = Map(getTaxYear(CurrentYearMinus1) -> Seq(PSub("100 Women in Finance", 50, true, Some(25)))),
    userCurrentCitizensDetailsAddress = Some(validAddress)
  )

  val userYearsAnswersCYMinus2 = emptyUserAnswers
    .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus2).toString, index), "100 Women in Finance").success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus2).toString, index), 50).success.value
    .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus2).toString, index), 25).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYearMinus2).toString, index), true).success.value
    .set(NpsData, Map(
      getTaxYear(CurrentYearMinus2) -> 300
    )).success.value
    .set(YourAddressPage, true).success.value

  def userAnswersCurrentAndPreviousYears: UserAnswers = emptyUserAnswers
    .set(WhichSubscriptionPage(getTaxYear(CurrentYear).toString, index), "Arable Research Institute Association").success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYear).toString, index), 1000).success.value
    .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYear).toString, index), 200).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYear).toString, index), true).success.value
    .set(NpsData, Map(
      getTaxYear(CurrentYear) -> 300
    )).success.value
    .set(YourEmployerPage, true).success.value
    .set(YourAddressPage, true).success.value
    .set(WhichSubscriptionPage(getTaxYear(CurrentYearMinus2).toString, index), "100 Women in Finance").success.value
    .set(SubscriptionAmountPage(getTaxYear(CurrentYearMinus2).toString, index), 50).success.value
    .set(ExpensesEmployerPaidPage(getTaxYear(CurrentYearMinus2).toString, index), 25).success.value
    .set(EmployerContributionPage(getTaxYear(CurrentYearMinus2).toString, index), true).success.value
    .set(NpsData, Map(
      getTaxYear(CurrentYearMinus2) -> 300
    )).success.value
    .set(YourEmployerPage, true).success.value
    .set(YourAddressPage, true).success.value

  lazy val taiEmployment: Seq[Employment] = Seq(Employment("HMRC Longbenton"))

  def emptyUserAnswers = UserAnswers(userAnswersId, Json.obj())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest = FakeRequest("", "")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[Scalate].to[MockScalate],
        bind[FormPartialRetriever].to[MockPsFormPartialRetriever]
      )
}
