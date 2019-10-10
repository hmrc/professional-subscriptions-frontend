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

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  lazy val serviceTitle = "Professional Subscriptions - GOV.UK"

  private val contactHost = configuration.get[Service]("microservice.services.contact-frontend").baseUrl
  private val contactFormServiceIdentifier = "professionalSubsriptionsFrontend"

  val assetsPath: String = configuration.get[String]("assets.url") + configuration.get[String]("assets.version") + "/"
  val govukTemplatePath: String = "/templates/mustache/production/"
  val analyticsToken: String = configuration.get[String](s"google-analytics.token")
  val analyticsHost: String = configuration.get[String](s"google-analytics.host")
  val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"
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
  lazy val authorisedCallback: String = configuration.get[String]("identity-verification-uplift.authorised-callback.url")
  lazy val unauthorisedCallback: String = configuration.get[String]("identity-verification-uplift.unauthorised-callback.url")

  lazy val updateAddressInfoUrl: String = configuration.get[String]("urls.updateAddressInfo")
  lazy val updateEmployerInfoUrl: String = configuration.get[String]("urls.updateEmployerInfo")

  lazy val languageTranslationEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val englishBasicRate: Int = configuration.get[Int]("tax-percentage.englishBasicTaxRate")
  lazy val englishHigherRate: Int = configuration.get[Int]("tax-percentage.englishHigherTaxRate")
  lazy val scottishStarterRate: Int = configuration.get[Int]("scottish-tax-percentage.scottishStartTaxRate")
  lazy val scottishBasicRate: Int = configuration.get[Int]("scottish-tax-percentage.scottishBasicTaxRate")
  lazy val scottishIntermediateRate: Int = configuration.get[Int]("scottish-tax-percentage.scottishIntermediateTaxRate")

  val accessibilityStatementUrl: String = configuration.get[String]("accessibilityStatement.govAccessibilityStatementUrl")
  val abilityNettUrl: String = configuration.get[String]("accessibilityStatement.abilityNetUrl")
  val w3StandardsUrl: String = configuration.get[String]("accessibilityStatement.w3StandardsUrl")
  val equalityAdvisoryServiceUrl: String = configuration.get[String]("accessibilityStatement.equalityAdvisoryServiceUrl")
  val equalityNIUrl: String = configuration.get[String]("accessibilityStatement.equalityNIUrl")
  val dealingHmrcAdditionalNeedsUrl: String = configuration.get[String]("accessibilityStatement.dealingHmrcAdditionalNeedsUrl")
  val dacUrl: String = configuration.get[String]("accessibilityStatement.dacUrl")
  val contactAccessibilityUrl = configuration.get[String]("accessibilityStatement.contactAccessibilityUrl")
  val contactUsPhoneNumber = configuration.get[String]("accessibilityStatement.contactUsPhoneNumber")
  val accessibilityStatementLastTested: String = configuration.get[String]("accessibilityStatement.lastTested")
  val accessibilityStatementFirstPublished: String = configuration.get[String]("accessibilityStatement.firstPublished")
  val accessibilityStatementEnabled: Boolean = configuration.get[Boolean]("accessibilityStatement.enabled")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

}
