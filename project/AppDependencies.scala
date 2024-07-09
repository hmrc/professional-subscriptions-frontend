import sbt._

object AppDependencies {

  val bootstrapVersion = "8.6.0"
  val mongoVersion = "2.1.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"                     % mongoVersion,
    "uk.gov.hmrc"           %% "play-conditional-form-mapping-play-30"  % "3.0.0",
    "uk.gov.hmrc"           %% "tax-year"                               % "5.0.0",
    "uk.gov.hmrc"           %% "sca-wrapper-play-30"                    % "1.9.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-30"                % mongoVersion,
    "uk.gov.hmrc"           %% "bootstrap-test-play-30"                 % bootstrapVersion,
    "org.scalatestplus"     %% "scalacheck-1-17"                        % "3.2.18.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
