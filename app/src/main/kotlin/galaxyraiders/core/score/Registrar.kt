package galaxyraiders.core.score

import galaxyraiders.Config
import java.util.UUID
import java.time.LocalDateTime
import com.fasterxml.jackson.module.kotlin.*
import java.io.File

data class Registry(
  var runId: String,
  var timestamp: String,
  var score: Double,
  var count: Int) {
}

object RegistrarConfig {
  private val config = Config(prefix = "GR__CORE__SCORE__")
  val scoreboardFilename = config.get<String>("SCOREBOARD__FILENAME")
  val leaderboardFilename = config.get<String>("LEADERBOARD__FILENAME")
}

class Registrar() {
  val mapper = jacksonObjectMapper()

  fun init() {
    var scoreboard = File(RegistrarConfig.scoreboardFilename)
    var leaderboard = File(RegistrarConfig.leaderboardFilename)
    if(!scoreboard.exists() || !leaderboard.exists()) {
      scoreboard.writeText("[]")
      leaderboard.writeText("[]")
    }
  }

  fun read(filename: String) : ArrayList<Registry> {
    var registryList: List<Registry> = this.mapper.readValue(File(filename))
    return ArrayList(registryList)
  }

  fun write(filename: String, registries: ArrayList<Registry>) {
    this.mapper.writerWithDefaultPrettyPrinter().writeValue(
      File(filename),
      registries
    )
  }

  fun updateLeaderboard() {
    var registries = read(RegistrarConfig.scoreboardFilename)
    var sortedRegistries = registries.sortedWith(compareBy({ it.score }))
    sortedRegistries = sortedRegistries.reversed()
    val lastLeader = Math.min(sortedRegistries.size - 1, 2)
    var leaderRegistries = sortedRegistries.slice(0..lastLeader)
    write(RegistrarConfig.leaderboardFilename, ArrayList(leaderRegistries))
  }

  fun incrementScore(runId: String, score: Double) {
    var registries = read(RegistrarConfig.scoreboardFilename)

    registries.forEach { 
      if (it.runId == runId) {
        it.count += 1
        it.score += score
      }
    }

    write(RegistrarConfig.scoreboardFilename, registries)

    this.updateLeaderboard()
  }

  fun insertScore(): String {
    val runId = UUID.randomUUID().toString()
    val timestamp = LocalDateTime.now().toString()

    var registries = read(RegistrarConfig.scoreboardFilename)
    registries.add(Registry(runId, timestamp, 0.0, 0))
    write(RegistrarConfig.scoreboardFilename, registries)

    return runId
  }
}
