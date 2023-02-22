import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-28"            % "0.74.0",
    "uk.gov.hmrc"           %% "play-frontend-hmrc"            % "6.3.0-play-28",
    "uk.gov.hmrc"           %% "http-caching-client"           % "9.6.0-play-28",
    "uk.gov.hmrc"           %% "play-conditional-form-mapping" % "1.11.0-play-28",
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-28"    % "5.24.0",
    "uk.gov.hmrc"           %% "play-partials"                 % "8.3.0-play-28",
    "org.scalatra.scalate"  %% "play-scalate"                  % "0.6.0",
    "org.scalatra.scalate"  %% "scalate-core"                  % "1.9.8",
    "uk.gov.hmrc"           %% "tax-year"                      % "1.3.0"
  )

  val test: Seq[ModuleID] = Seq(
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.35.10",
    "org.scalatestplus"       %% "scalatestplus-mockito"      % "1.0.0-M2",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "org.scalatestplus"       %% "scalatestplus-scalacheck"   % "3.1.0.0-RC2",
    "org.pegdown"             %  "pegdown"                    % "1.6.0",
    "org.jsoup"               %  "jsoup"                      % "1.13.1",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % "0.74.0",
    "org.mockito"             %  "mockito-all"                % "1.10.19",
    "org.scalacheck"          %% "scalacheck"                 % "1.15.2",
    "com.github.tomakehurst"  %  "wiremock-standalone"        % "2.26.3"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
