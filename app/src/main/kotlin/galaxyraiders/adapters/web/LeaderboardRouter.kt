package galaxyraiders.adapters.web

import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.EndpointGroup
import io.javalin.http.Context
import galaxyraiders.core.score.Registry
import galaxyraiders.core.score.Registrar
import galaxyraiders.core.score.RegistrarConfig
import java.io.File

class LeaderboardRouter : Router {
  data class LeaderboardDTO(val registries: ArrayList<Registry>) {
  }

  var dto: LeaderboardDTO? = null
    private set

  override val path = "/leaderboard"

  override val endpoints = EndpointGroup {
    get("/", ::getLeaderboard)
  }

  fun updateDto() {
    val registrar = Registrar()
    registrar.init()
    val filename = RegistrarConfig.leaderboardFilename
    var leaderboardFile = File(filename)
    if(!leaderboardFile.exists()) this.dto = null
    else this.dto = LeaderboardDTO(registrar.read(filename))
  }

  private fun getLeaderboard(ctx: Context) {
    updateDto()
    ctx.json(this.dto ?: "{}")
  }
}
