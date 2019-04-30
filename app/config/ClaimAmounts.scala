package config

import javax.inject.Singleton

sealed trait ClaimAmounts

@Singleton
object ClaimAmount{

  lazy val maxClaimAmount: Int = 2500

}
