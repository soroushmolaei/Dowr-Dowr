package com.dorino.game.domain

import com.dorino.game.data.model.GameMode
import com.dorino.game.data.model.GameSettings
import com.dorino.game.data.model.GameState
import com.dorino.game.data.model.GameStatus
import com.dorino.game.data.model.Player
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
    val durationSeconds: Long
)

/**
 * منطق خالص بازی: بدون وابستگی به Android UI یا Coroutine.
 * تمام قوانین مربوط به نوبت، امتیاز و پایان بازی اینجا متمرکز شده‌اند.
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
        val pool = WordRepository.wordsForFilters(state.settings.selectedCategories, state.settings.selectedDifficulties)
        if (pool.isEmpty()) return null to state.usedWords
        val available = pool.filter { it.text !in state.usedWords }
        return if (available.isNotEmpty()) {
            val chosen = available.random()
            chosen.text to (state.usedWords + chosen.text)
        } else {
            // همه کلمات استفاده شده‌اند؛ مجموعه بازنشانی می‌شود تا بازی متوقف نشود.
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

    fun tickTimer(state: GameState): GameState {
        if (state.settings.timerDurationSeconds == 0) return state
        val newTime = (state.timeRemainingSeconds - 1).coerceAtLeast(0)
        return state.copy(timeRemainingSeconds = newTime)
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
                // رالی: همان بازیکن با کلمه‌ی جدید ادامه می‌دهد تا زمان تمام شود.
                val (word, usedWords) = pickWord(scored)
                scored.copy(currentWord = word, usedWords = usedWords)
            }
            TurnStyle.ROTATING -> {
                // دست‌به‌دست: بلافاصله نوبت به نفر بعدی می‌رسد، بدون ریست شدن تایمر.
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

    /** نتیجه‌ی رفتن به بازیکن بعدی در دور میز، مستقل از این‌که تایمر ریست شود یا نه. */
    private data class Advance(
        val players: List<Player>,
        val nextIndex: Int,
        val nextRound: Int,
        val finished: Boolean
    )

    private fun advanceToNextPlayer(state: GameState): Advance {
        val playersDeactivated = state.players.map { it.copy(isActive = false) }
        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        val wrappedAround = nextIndex == 0
        val nextRound = if (wrappedAround) state.round + 1 else state.round
        val playersActivated = playersDeactivated.mapIndexed { index, p ->
            if (index == nextIndex) p.copy(isActive = true) else p
        }
        val finished = nextRound > state.totalRounds
        return Advance(
            players = playersActivated,
            nextIndex = nextIndex,
            nextRound = nextRound.coerceAtMost(state.totalRounds),
            finished = finished
        )
    }

    /**
     * دست‌به‌دست: نوبت فوراً به نفر بعد می‌رسد اما زمان باقی‌مانده دست‌نخورده باقی می‌ماند
     * و صفحه‌ی «آماده‌ام» نمایش داده نمی‌شود؛ بازی همچنان IN_PROGRESS باقی می‌ماند.
     */
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
            // status و timeRemainingSeconds عمداً دست‌نخورده می‌مانند.
        )
    }

    /**
     * پایان کامل نوبت (پایان زمان یا پایان دستی): زمان باقی‌مانده به‌عنوان زمان ذخیره‌شده
     * ثبت می‌شود، تایمر برای نفر بعد ریست خواهد شد و صفحه‌ی انتقال نوبت نمایش داده می‌شود.
     */
    fun endTurn(state: GameState): GameState {
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
        val maxScore = state.teams.maxOfOrNull { it.score } ?: 0
        val winners = state.teams.filter { it.score == maxScore }
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
            durationSeconds = duration
        )
    }
}
