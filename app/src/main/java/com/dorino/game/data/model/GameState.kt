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
    val survivorCheckpoint: SurvivorCheckpoint? = null,
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

    /**
     * حالت سرویوایور فقط در شیوه‌ی «دست‌به‌دست» و با تایمر محدود معنا دارد؛
     * چون بدون سقف زمانی، تیمی هرگز زمانش تمام نمی‌شود.
     */
    val isSurvivorMode: Boolean
        get() = settings.turnStyle == TurnStyle.ROTATING && settings.timerDurationSeconds > 0

    fun isTeamEliminated(team: Team): Boolean =
        isSurvivorMode && team.activeTimeSeconds >= settings.timerDurationSeconds

    val aliveTeams: List<Team>
        get() = if (isSurvivorMode) teams.filterNot { isTeamEliminated(it) } else teams

    /** عددی که باید روی حلقه‌ی تایمر و شتاب بوق نمایش داده شود. */
    val displayTimeRemaining: Int
        get() = if (isSurvivorMode) {
            (settings.timerDurationSeconds - (currentTeam?.activeTimeSeconds ?: 0)).coerceAtLeast(0)
        } else {
            timeRemainingSeconds
        }
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
