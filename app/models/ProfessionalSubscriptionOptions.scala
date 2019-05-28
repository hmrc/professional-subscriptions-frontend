package models

sealed trait ProfessionalSubscriptionOptions

object ProfessionalSubscriptionOptions extends Enumerable.Implicits {

  case object PSNoYears extends WithName("freNoYears") with ProfessionalSubscriptionOptions
  case object PSSomeYears extends WithName("freAllYearsAllAmountsDifferentToClaimAmount") with ProfessionalSubscriptionOptions
  case object PSAllYearsAllAmountsSameAsClaimAmount extends WithName("freAllYearsAllAmountsSameAsClaimAmount") with ProfessionalSubscriptionOptions
  case object TechnicalDifficulties extends WithName("technicalDifficulties") with ProfessionalSubscriptionOptions

  val values: Seq[ProfessionalSubscriptionOptions] = Seq(
    PSNoYears,
    PSAllYearsAllAmountsSameAsClaimAmount,
    PSSomeYears,
    TechnicalDifficulties
  )

  implicit val enumerable: Enumerable[ProfessionalSubscriptionOptions] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
