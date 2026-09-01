package com.dorino.game.domain

import com.dorino.game.data.model.GameMode
import com.dorino.game.data.model.GameSettings
import com.dorino.game.data.model.GameState
import com.dorino.game.data.model.GameStatus
import com.dorino.game.data.model.Player
import com.dorino.game.data.model.SurvivorCheckpoint
import com.dorino.game.data.model.SurvivorCheckpointType
import com.dorino.game.data.model.Team
import com.dorino.game.data.model.TurnStyle
import com.dorino.game.data.words.WordRepository
import java.util.UUID

data class GameResult(
    val teams: List<Team>,
    val winnerTeams: List<Team>,
    val bestPlayer: Player?,
    val totalCorrect: Int,
    val totalPass: Int,
    val totalSavedTimeSeconds: Int,
    val durationSeconds: Long,
    val wasSurvivorMode: Boolean
)

/**
 * منطق خالص بازی: بدون وابستگی به Android UI یا Coroutine.
 * تمام قوانین مربوط به نوبت، امتیاز و پایان بازی اینجا متمرکز شده‌اند.
 *
 * حالت سرویوایور (فقط شیوه‌ی «دست‌به‌دست» + تایمر محدود):
 * هر تیم یک «موجودی زمان» کلی برابر با مدت تایمر انتخاب‌شده دارد که هرگز ریست نمی‌شود.
 * وقتی موجودی یک تیم صفر شود، آن تیم حذف می‌شود، بازی استپ می‌شود و با دکمه‌ی «ادامه»
 * نوبت به تیمِ زنده‌ی بعدی می‌رسد. همچنین در آستانه‌های زمانیِ متناسب با تعداد دور،
 * بازی استپ می‌شود و با «ادامه» همان بازیکن با کلمه‌ی جدید ادامه می‌دهد. وقتی فقط یک تیم
 * زنده بماند، همان تیم برنده‌ی بازی است.
 */
object GameEngine {

    fun createGame(
        mode: GameMode,
        playerNames: List<String>,
        settings: GameSettings
    ): GameState {
        val playerCount = playerNames.size
        val teams = TeamAssigner.buildTeams(mode, playerCount)
        val players = playerNames.mapIndexed { index, name ->
            Player(
                id = index,
                name = name,
                teamId = TeamAssigner.teamIdForSeatIndex(mode, index, playerCount),
                position = index,
                isActive = index == 0
            )
        }
        return GameState(
            id = UUID.randomUUID().toString(),
            mode = mode,
            players = players,
            teams = teams,
            currentPlayerIndex = 0,
            round = 1,
            totalRounds = settings.roundCount,
            status = GameStatus.TURN_TRANSITION,
            startedAtEpochMillis = System.currentTimeMillis(),
            settings = settings
        )
    }

    /** انتخاب یک کلمه‌ی تصادفیِ استفاده‌نشده. اگر همه استفاده شده باشند، مجموعه Reset می‌شود. */
    private fun pickWord(state: GameState): Pair<String?, Set<String>> {
        val pool = WordRepository.wordsForFilters(
            state.settings.selectedCategories,
            state.settings.selectedDifficulty,
            state.settings.customWords
        )
        if (pool.isEmpty()) return null to state.usedWords
        val available = pool.filter { it.text !in state.usedWords }
        return if (available.isNotEmpty()) {
            val chosen = available.random()
            chosen.text to (state.usedWords + chosen.text)
        } else {
            val chosen = pool.random()
            chosen.text to setOf(chosen.text)
        }
    }

    fun startTurn(state: GameState): GameState {
        val (word, usedWords) = pickWord(state)
        return state.copy(
            currentWord = word,
            usedWords = usedWords,
            timeRemainingSeconds = state.settings.timerDurationSeconds,
            status = GameStatus.IN_PROGRESS
        )
    }

    /**
     * تیک هر ثانیه‌ی تایمر. در حالت سرویوایور، این تابع علاوه بر شمارش معکوس مسئول
     * تشخیص حذف تیم و پیشرفت شماره‌ی دور بر اساس آستانه‌های زمانی است. هرکدام از این دو
     * رویداد که رخ دهد، بازی استپ می‌شود (وضعیت ROUND_SUMMARY) تا با «ادامه» ازسر گرفته شود.
     * وقتی فقط یک تیم زنده بماند، بازی بی‌درنگ با برد همان تیم تمام می‌شود.
     */
    fun tickTimer(state: GameState): GameState {
        if (state.settings.timerDurationSeconds == 0) return state
        val bank = state.settings.timerDurationSeconds

        val newTime = (state.timeRemainingSeconds - 1).coerceAtLeast(0)
        val activeTeamId = state.currentPlayer?.teamId
        val updatedTeams = if (activeTeamId != null) {
            state.teams.map {
                if (it.id == activeTeamId) it.copy(activeTimeSeconds = (it.activeTimeSeconds + 1).coerceAtMost(bank)) else it
            }
        } else state.teams

        val working = state.copy(timeRemainingSeconds = newTime, teams = updatedTeams)

        if (!working.isSurvivorMode) return working

        val aliveTeamIds = updatedTeams.filter { it.activeTimeSeconds < bank }.map { it.id }.toSet()

        // فقط یک تیم زنده مانده: بازی همین‌جا با برد همان تیم تمام می‌شود.
        if (aliveTeamIds.size <= 1) {
            return working.copy(
                status = GameStatus.FINISHED,
                currentWord = null,
                survivorCheckpoint = null,
                finishedAtEpochMillis = System.currentTimeMillis()
            )
        }

        // تیمِ در حال بازی همین الان حذف شد: بازی استپ می‌شود تا با «ادامه» به تیم بعدی برسد.
        if (activeTeamId != null && activeTeamId !in aliveTeamIds) {
            val eliminatedTeamName = state.teams.firstOrNull { it.id == activeTeamId }?.name
            return working.copy(
                status = GameStatus.ROUND_SUMMARY,
                currentWord = null,
                survivorCheckpoint = SurvivorCheckpoint(SurvivorCheckpointType.TEAM_ELIMINATED, eliminatedTeamName)
            )
        }

        // آستانه‌ی زمانیِ پایانِ دور: بازی استپ می‌شود تا با «ادامه»، همان بازیکن با کلمه‌ی جدید ادامه دهد.
        if (working.round < working.totalRounds) {
            val thresholdSeconds = bank * (working.totalRounds - working.round) / working.totalRounds
            val teamsAboveThreshold = working.teams.count { (bank - it.activeTimeSeconds) > thresholdSeconds }
            if (teamsAboveThreshold <= 1) {
                return working.copy(
                    round = working.round + 1,
                    status = GameStatus.ROUND_SUMMARY,
                    currentWord = null,
                    survivorCheckpoint = SurvivorCheckpoint(SurvivorCheckpointType.ROUND_ADVANCED)
                )
            }
        }

        return working
    }

    /**
     * ازسرگیریِ بازی پس از یک چک‌پوینتِ سرویوایور (چه حذف تیم، چه پایان دور).
     * برای پایانِ دور: همان بازیکن با کلمه‌ی جدید ادامه می‌دهد.
     * برای حذف تیم: نوبت به نفرِ زنده‌ی بعدی می‌رسد.
     */
    fun resumeFromCheckpoint(state: GameState): GameState {
        val checkpoint = state.survivorCheckpoint ?: return state
        val cleared = state.copy(survivorCheckpoint = null, status = GameStatus.IN_PROGRESS)
        return when (checkpoint.type) {
            SurvivorCheckpointType.ROUND_ADVANCED -> {
                val (word, usedWords) = pickWord(cleared)
                cleared.copy(currentWord = word, usedWords = usedWords)
            }
            SurvivorCheckpointType.TEAM_ELIMINATED -> {
                val aliveTeamIds = cleared.aliveTeams.map { it.id }.toSet()
                passToNextAliveSeamlessly(cleared, aliveTeamIds)
            }
        }
    }

    fun markCorrect(state: GameState): GameState {
        val player = state.currentPlayer ?: return state
        val updatedPlayers = state.players.map {
            if (it.id == player.id) it.copy(correctCount = it.correctCount + 1) else it
        }
        val updatedTeams = state.teams.map {
            if (it.id == player.teamId) it.copy(score = it.score + 1, correctCount = it.correctCount + 1) else it
        }
        val scored = state.copy(players = updatedPlayers, teams = updatedTeams)

        return when (state.settings.turnStyle) {
            TurnStyle.RALLY -> {
                val (word, usedWords) = pickWord(scored)
                scored.copy(currentWord = word, usedWords = usedWords)
            }
            TurnStyle.ROTATING -> {
                passToNextPlayerSeamlessly(scored)
            }
        }
    }

    fun markPass(state: GameState): GameState {
        val player = state.currentPlayer ?: return state
        val updatedPlayers = state.players.map {
            if (it.id == player.id) it.copy(passCount = it.passCount + 1) else it
        }
        val updatedTeams = state.teams.map {
            if (it.id == player.teamId) it.copy(passCount = it.passCount + 1) else it
        }
        val (word, usedWords) = pickWord(state)
        return state.copy(
            players = updatedPlayers,
            teams = updatedTeams,
            currentWord = word,
            usedWords = usedWords
        )
    }

    private data class Advance(
        val players: List<Player>,
        val nextIndex: Int,
        val nextRound: Int,
        val finished: Boolean
    )

    private fun advanceToNextPlayer(state: GameState): Advance {
        val n = state.players.size
        var nextIndex = (state.currentPlayerIndex + 1) % n

        if (state.isSurvivorMode) {
            val aliveTeamIds = state.aliveTeams.map { it.id }.toSet()
            var guard = 0
            while (aliveTeamIds.isNotEmpty() && state.players[nextIndex].teamId !in aliveTeamIds && guard < n) {
                nextIndex = (nextIndex + 1) % n
                guard += 1
            }
            val playersActivated = state.players.mapIndexed { index, p -> p.copy(isActive = index == nextIndex) }
            return Advance(players = playersActivated, nextIndex = nextIndex, nextRound = state.round, finished = false)
        }

        val wrappedAround = nextIndex == 0
        val nextRound = if (wrappedAround) state.round + 1 else state.round
        val playersActivated = state.players.mapIndexed { index, p -> p.copy(isActive = index == nextIndex) }
        val finished = nextRound > state.totalRounds
        return Advance(
            players = playersActivated,
            nextIndex = nextIndex,
            nextRound = nextRound.coerceAtMost(state.totalRounds),
            finished = finished
        )
    }

    private fun passToNextPlayerSeamlessly(state: GameState): GameState {
        val advance = advanceToNextPlayer(state)
        if (advance.finished) {
            return state.copy(
                players = advance.players,
                currentPlayerIndex = advance.nextIndex,
                round = advance.nextRound,
                currentWord = null,
                status = GameStatus.FINISHED,
                finishedAtEpochMillis = System.currentTimeMillis()
            )
        }
        val (word, usedWords) = pickWord(state)
        return state.copy(
            players = advance.players,
            currentPlayerIndex = advance.nextIndex,
            round = advance.nextRound,
            currentWord = word,
            usedWords = usedWords
        )
    }

    private fun passToNextAliveSeamlessly(state: GameState, aliveTeamIds: Set<Int>): GameState {
        val n = state.players.size
        if (n == 0) return state
        var idx = state.currentPlayerIndex
        var guard = 0
        while (guard < n) {
            idx = (idx + 1) % n
            if (state.players[idx].teamId in aliveTeamIds) {
                val playersActivated = state.players.mapIndexed { i, p -> p.copy(isActive = i == idx) }
                val (word, usedWords) = pickWord(state)
                return state.copy(
                    players = playersActivated,
                    currentPlayerIndex = idx,
                    currentWord = word,
                    usedWords = usedWords
                )
            }
            guard += 1
        }
        return state
    }

    fun endTurn(state: GameState): GameState {
        if (state.isSurvivorMode) {
            return passToNextPlayerSeamlessly(state)
        }

        val currentTeamId = state.currentPlayer?.teamId
        val leftover = state.timeRemainingSeconds
        val teamsAfterSave = if (currentTeamId != null && leftover > 0) {
            state.teams.map {
                if (it.id == currentTeamId) it.copy(savedTimeSeconds = it.savedTimeSeconds + leftover) else it
            }
        } else state.teams

        val advance = advanceToNextPlayer(state)
        return state.copy(
            teams = teamsAfterSave,
            players = advance.players,
            currentPlayerIndex = advance.nextIndex,
            round = advance.nextRound,
            currentWord = null,
            status = if (advance.finished) GameStatus.FINISHED else GameStatus.TURN_TRANSITION,
            finishedAtEpochMillis = if (advance.finished) System.currentTimeMillis() else null
        )
    }

    fun computeResult(state: GameState): GameResult {
        val winners = if (state.isSurvivorMode) {
            state.aliveTeams.ifEmpty { state.teams }
        } else {
            val maxScore = state.teams.maxOfOrNull { it.score } ?: 0
            state.teams.filter { it.score == maxScore }
        }
        val bestPlayer = state.players.maxByOrNull { it.correctCount }
        val totalCorrect = state.players.sumOf { it.correctCount }
        val totalPass = state.players.sumOf { it.passCount }
        val totalSaved = state.teams.sumOf { it.savedTimeSeconds }
        val duration = ((state.finishedAtEpochMillis ?: System.currentTimeMillis()) - state.startedAtEpochMillis) / 1000
        return GameResult(
            teams = state.teams.sortedByDescending { it.score },
            winnerTeams = winners,
            bestPlayer = bestPlayer,
            totalCorrect = totalCorrect,
            totalPass = totalPass,
            totalSavedTimeSeconds = totalSaved,
            durationSeconds = duration,
            wasSurvivorMode = state.isSurvivorMode
        )
    }
}
