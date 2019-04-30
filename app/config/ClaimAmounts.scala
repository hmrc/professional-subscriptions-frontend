package config

import javax.inject.Singleton

sealed trait ClaimAmounts

@Singleton
object ClaimAmounts{

  lazy val maxClaimAmount: Int = 2500

}
