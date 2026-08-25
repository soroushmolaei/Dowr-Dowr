package com.dorino.game.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val id: String,
    val mode: GameMode,
    val players: List<Player> = emptyList(),
    val teams: List<Team> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val currentWord: String? = null,
    val usedWords: Set<String> = emptySet(),
    val timeRemainingSeconds: Int = 0,
    val round: Int = 1,
    val totalRounds: Int = 3,
    val status: GameStatus = GameStatus.NOT_STARTED,
    val startedAtEpochMillis: Long = 0L,
    val finishedAtEpochMillis: Long? = null,
    val settings: GameSettings = GameSettings()
) {
    val currentPlayer: Player?
        get() = players.getOrNull(currentPlayerIndex)

    val currentTeam: Team?
        get() = currentPlayer?.let { p -> teams.firstOrNull { it.id == p.teamId } }

    fun teamOf(playerId: Int): Team? =
        players.firstOrNull { it.id == playerId }?.let { p -> teams.firstOrNull { it.id == p.teamId } }
}

@Serializable
data class GameHistoryEntry(
    val id: String,
    val dateEpochMillis: Long,
    val mode: GameMode,
    val playerCount: Int,
    val winnerName: String,
    val winnerScore: Int,
    val durationSeconds: Long
)
