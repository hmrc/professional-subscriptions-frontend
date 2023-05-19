import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val bootstrapFrontendVersion = "7.15.0"
  val mongoPlayVersion = "1.2.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-28"            % mongoPlayVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc"            % "7.4.0-play-28",
    "uk.gov.hmrc"           %% "http-caching-client"           % "10.0.0-play-28",
    "uk.gov.hmrc"           %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-28"    % bootstrapFrontendVersion,
    "uk.gov.hmrc"           %% "play-partials"                 % "8.4.0-play-28",
    "uk.gov.hmrc"           %% "tax-year"                      % "3.2.0"
  )

  val test: Seq[ModuleID] = Seq(
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.35.10",
    "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "org.scalatestplus"       %% "scalatestplus-scalacheck"   % "3.1.0.0-RC2",
    "org.pegdown"             %  "pegdown"                    % "1.6.0",
    "org.jsoup"               %  "jsoup"                      % "1.16.1",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % mongoPlayVersion,
    "org.mockito"             %  "mockito-all"                % "1.10.19",
    "org.scalacheck"          %% "scalacheck"                 % "1.17.0",
    "com.github.tomakehurst"  %  "wiremock-standalone"        % "2.27.2",
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapFrontendVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
