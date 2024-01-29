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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class FrontendAppConfig @Inject() (val configuration: Configuration) {

  lazy val scaWrapperEnabled = configuration.get[Boolean]("microservice.services.features.sca-wrapper")

  lazy val serviceTitle = "Claim for your work related professional subscriptions - GOV.UK"

  val professionalSubscriptionsFrontendUrl: String = configuration.get[String]("urls.logout")
  val signOutUrl: String = professionalSubscriptionsFrontendUrl + "/sign-out"

  val feedbackUrl: String = configuration.get[String]("urls.feedbackSurvey")
  val selfAssessmentUrl: String = configuration.get[String]("urls.selfAssessment")
  lazy val incomeTaxSummary: String = configuration.get[String]("incomeTaxSummary.url")
  lazy val keepAliveUrl: String = configuration.get[String]("urls.keepAlive")

  lazy val minCurrencyInput: Int = configuration.get[Int]("amounts.minCurrencyInput")
  lazy val maxCurrencyInput: Int = configuration.get[Int]("amounts.maxCurrencyInput")
  lazy val maxClaimAmount:Int = configuration.get[Int]("amounts.maxClaimAmount")

  lazy val p87ClaimByPostUrl: String = configuration.get[String]("p87.claimByPostUrl")
  lazy val claimOnlineUrl: String = configuration.get[String]("claimOnline.url")

  lazy val indexUrl: String = configuration.get[String]("urls.index")
  lazy val authUrl: String = configuration.get[Service]("auth").baseUrl
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val citizenDetailsHost: String = configuration.get[Service]("microservice.services.citizen-details").baseUrl
  lazy val taiHost: String = configuration.get[Service]("microservice.services.tai").baseUrl
  lazy val professionalBodiesUrl: String = configuration.get[Service]("microservice.services.professional-bodies").baseUrl
  lazy val contactHMRC: String = configuration.get[String]("contactHMRC.url")

  lazy val ivUpliftUrl: String = configuration.get[String]("identity-verification-uplift.host")
  lazy val ivCompletionUrl: String = configuration.get[String]("identity-verification-uplift.ivCompletion.url")
  lazy val ivFailureUrl: String = configuration.get[String]("identity-verification-uplift.ivFailure.url")

  lazy val updateAddressInfoUrl: String = configuration.get[String]("urls.updateAddressInfo")
  lazy val updateEmployerInfoUrl: String = configuration.get[String]("urls.updateEmployerInfo")

  lazy val languageTranslationEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val englishBasicRate: Int = configuration.get[Int]("tax-percentage.englishBasicTaxRate")
  lazy val englishHigherRate: Int = configuration.get[Int]("tax-percentage.englishHigherTaxRate")
  lazy val scottishStarterRate: Int = configuration.get[Int]("scottish-tax-percentage.scottishStartTaxRate")
  lazy val scottishBasicRate: Int = configuration.get[Int]("scottish-tax-percentage.scottishBasicTaxRate")
  lazy val scottishIntermediateRate: Int = configuration.get[Int]("scottish-tax-percentage.scottishIntermediateTaxRate")

  lazy val employeeExpensesHost: String = configuration.get[Service]("microservice.services.employee-expenses-frontend").baseUrl

  lazy val mergedJourneyEnabled: Boolean = configuration.getOptional[Boolean]("microservice.services.features.merged-journey").getOrElse(false)
}
