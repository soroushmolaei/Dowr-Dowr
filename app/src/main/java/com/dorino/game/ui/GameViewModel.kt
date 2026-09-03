package com.dorino.game.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dorino.game.data.audio.SoundEffect
import com.dorino.game.data.audio.SoundManager
import com.dorino.game.data.model.GameHistoryEntry
import com.dorino.game.data.model.GameMode
import com.dorino.game.data.model.GameSettings
import com.dorino.game.data.model.GameState
import com.dorino.game.data.model.GameStatus
import com.dorino.game.data.model.SurvivorCheckpointType
import com.dorino.game.data.model.TurnStyle
import com.dorino.game.data.persistence.GameStateStore
import com.dorino.game.domain.GameEngine
import com.dorino.game.domain.GameResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** تنظیمات موقتِ در حال ساخت بازی، پیش از ایجاد GameState نهایی. */
data class GameSetupDraft(
    val mode: GameMode? = null,
    val playerCount: Int = 4,
    val playerNames: List<String> = emptyList()
)

class GameViewModel(
    context: Context
) : ViewModel() {

    private val store = GameStateStore(context)
    private val soundManager = SoundManager(context)

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _setupDraft = MutableStateFlow(GameSetupDraft())
    val setupDraft: StateFlow<GameSetupDraft> = _setupDraft.asStateFlow()

    private val _history = MutableStateFlow<List<GameHistoryEntry>>(emptyList())
    val history: StateFlow<List<GameHistoryEntry>> = _history.asStateFlow()

    private val _lastResult = MutableStateFlow<GameResult?>(null)
    val lastResult: StateFlow<GameResult?> = _lastResult.asStateFlow()

    private val _passCooldownRemaining = MutableStateFlow(0)
    val passCooldownRemaining: StateFlow<Int> = _passCooldownRemaining.asStateFlow()

    private var timerJob: Job? = null
    private var beepJob: Job? = null
    private var passCooldownJob: Job? = null

    /** حداقلِ فاصله‌ی بوق که تا الان در این بازیِ سرویوایور رسیده شده؛ فقط می‌تواند کوچک‌تر شود
     * (یعنی سرعت فقط زیاد می‌شود) تا استرس بازی هیچ‌وقت کم نشود. با هر بازی جدید ریست می‌شود. */
    private var survivorBeepFloorMs: Long = 1000L

    init {
        viewModelScope.launch {
            store.settingsFlow.collect { _settings.value = it }
        }
        viewModelScope.launch {
            store.savedGameFlow.collect { _gameState.value = it }
        }
        viewModelScope.launch {
            store.historyFlow.collect { _history.value = it }
        }
    }

    // ---------- تنظیمات ----------

    fun updateSettings(update: (GameSettings) -> GameSettings) {
        val candidate = update(_settings.value)
        // سرعتی به تایمر محدود و حداقل ۳ دور نیاز دارد؛ هر تغییری این محدودیت را رعایت می‌کند.
        val sanitized = if (candidate.turnStyle == TurnStyle.ROTATING) {
            candidate.copy(
                roundCount = candidate.roundCount.coerceAtLeast(3),
                timerDurationSeconds = if (candidate.timerDurationSeconds == 0) 60 else candidate.timerDurationSeconds
            )
        } else {
            candidate
        }
        _settings.value = sanitized
        viewModelScope.launch { store.saveSettings(sanitized) }
    }

    fun addCustomWord(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        updateSettings {
            if (it.customWords.any { existing -> existing.equals(trimmed, ignoreCase = true) }) it
            else it.copy(customWords = it.customWords + trimmed)
        }
    }

    fun removeCustomWord(text: String) {
        updateSettings { it.copy(customWords = it.customWords - text) }
    }

    // ---------- ساخت بازی ----------

    fun selectMode(mode: GameMode) {
        _setupDraft.value = _setupDraft.value.copy(mode = mode)
    }

    fun setPlayerCount(count: Int) {
        _setupDraft.value = _setupDraft.value.copy(playerCount = count)
    }

    fun setPlayerNames(names: List<String>) {
        _setupDraft.value = _setupDraft.value.copy(playerNames = names)
    }

    fun defaultPlayerNames(count: Int): List<String> = (1..count).map { "بازیکن $it" }

    fun startNewGame() {
        val draft = _setupDraft.value
        val mode = draft.mode ?: GameMode.TEAM_BATTLE
        val names = draft.playerNames.ifEmpty { defaultPlayerNames(draft.playerCount) }
        val state = GameEngine.createGame(mode, names, _settings.value)
        _gameState.value = state
        _setupDraft.value = GameSetupDraft()
        survivorBeepFloorMs = 1000L
        persist(state)
        soundManager.play(SoundEffect.START, _settings.value.soundEnabled)
    }

    // ---------- جریان نوبت ----------

    fun confirmReadyStartTurn() {
        val state = _gameState.value ?: return
        val updated = GameEngine.startTurn(state)
        _gameState.value = updated
        persist(updated)
        startPassCooldown()
        startTimerLoop()
        startBeepLoop()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        val duration = _gameState.value?.settings?.timerDurationSeconds ?: 0
        if (duration == 0) return // بدون محدودیت
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _gameState.value ?: break
                if (current.status != GameStatus.IN_PROGRESS) break

                val ticked = GameEngine.tickTimer(current)
                _gameState.value = ticked

                if (ticked.status == GameStatus.FINISHED) {
                    beepJob?.cancel()
                    resetPassCooldown()
                    persist(ticked)
                    onGameFinished(ticked)
                    break
                }

                if (ticked.status == GameStatus.ROUND_SUMMARY) {
                    // چک‌پوینتِ سرویوایور (حذف تیم یا پایان دور): بازی اینجا استپ می‌شود
                    // و فقط با فشردن «ادامه» (confirmSurvivorContinue) دوباره شروع خواهد شد.
                    beepJob?.cancel()
                    resetPassCooldown()
                    val eliminated = ticked.survivorCheckpoint?.type == SurvivorCheckpointType.TEAM_ELIMINATED
                    soundManager.play(
                        if (eliminated) SoundEffect.DEFEAT else SoundEffect.TURN_CHANGE,
                        _settings.value.soundEnabled
                    )
                    if (eliminated) soundManager.vibrate(_settings.value.vibrationEnabled, durationMs = 200L)
                    persist(ticked)
                    break
                }

                if (!ticked.isSurvivorMode && ticked.timeRemainingSeconds <= 0) {
                    finishTurn()
                    break
                }
            }
        }
    }

    /** ادامه‌ی بازی پس از یک چک‌پوینتِ سرویوایور (حذف تیم یا پایان دور). */
    fun confirmSurvivorContinue() {
        val state = _gameState.value ?: return
        if (state.status != GameStatus.ROUND_SUMMARY) return
        val updated = GameEngine.resumeFromCheckpoint(state)
        _gameState.value = updated
        persist(updated)
        startPassCooldown()
        startTimerLoop()
        startBeepLoop()
        soundManager.play(SoundEffect.START, _settings.value.soundEnabled)
    }

    /**
     * حلقه‌ی مستقلِ بوق برای ایجاد استرس: از ابتدا هر ثانیه یک تیک ملایم،
     * از ۱۵ ثانیه‌ی آخر با شتاب تصاعدی سریع‌تر می‌شود. بوق ممتد پایانی
     * توسط [finishTurn] (نه این حلقه) پخش می‌شود تا با پایان واقعی هم‌زمان باشد.
     * در حالت سرویوایور، «زمان باقی‌مانده» یعنی موجودیِ باقی‌مانده‌ی تیمی که همین الان بازی می‌کند؛
     * و ریتم هیچ‌وقت کندتر نمی‌شود، حتی وقتی نوبت به تیمی با موجودیِ بیشتر می‌رسد.
     */
    private fun startBeepLoop() {
        beepJob?.cancel()
        val duration = _gameState.value?.settings?.timerDurationSeconds ?: 0
        if (duration == 0) return // بدون محدودیت: بوق نداریم
        beepJob = viewModelScope.launch {
            while (true) {
                val current = _gameState.value ?: break
                if (current.status != GameStatus.IN_PROGRESS) break
                val remaining = beepUrgencyRemaining(current)
                if (remaining <= 0) break
                if (remaining <= URGENT_PHASE_SECONDS) {
                    soundManager.play(SoundEffect.TIMER_WARNING, _settings.value.soundEnabled)
                    soundManager.vibrate(_settings.value.vibrationEnabled, durationMs = 20L)
                } else {
                    soundManager.play(SoundEffect.TICK_SOFT, _settings.value.soundEnabled)
                }
                var interval = beepIntervalMs(remaining)
                if (current.isSurvivorMode) {
                    // فقط اجازه‌ی سریع‌تر شدن؛ هیچ‌وقت کندتر از رکوردِ قبلی نمی‌شود.
                    interval = minOf(interval, survivorBeepFloorMs)
                    survivorBeepFloorMs = interval
                }
                delay(interval)
            }
        }
    }

    /**
     * ملاکِ استرسِ بوق. در سرویوایور، به‌جای فقط زمانِ باقی‌مانده‌ی تیمِ در حال بازی، بر اساس
     * نزدیک‌ترین تیمِ زنده به حذف‌شدن حساب می‌شود — یعنی برای کل دور مشترک است، نه هر تیم.
     * این‌طوری وقتی نوبت دست‌به‌دست بین تیم‌ها می‌چرخد (حتی به تیمی با موجودیِ بیشتر)،
     * نه ریتم و نه نوع صدا هیچ‌وقت آروم‌تر به‌نظر نمی‌رسد.
     */
    private fun beepUrgencyRemaining(state: GameState): Int {
        if (!state.isSurvivorMode) return state.timeRemainingSeconds
        val bank = state.settings.timerDurationSeconds
        return state.aliveTeams.minOfOrNull { (bank - it.activeTimeSeconds).coerceAtLeast(0) } ?: 0
    }

    private fun beepIntervalMs(remainingSeconds: Int): Long {
        if (remainingSeconds > URGENT_PHASE_SECONDS) return 1000L
        // از ۱۰۰۰ میلی‌ثانیه در ثانیه‌ی ۱۵، با شتاب تصاعدی تا حدود ۱۱۰ میلی‌ثانیه نزدیک صفر.
        val t = (URGENT_PHASE_SECONDS - remainingSeconds).coerceIn(0, URGENT_PHASE_SECONDS) / URGENT_PHASE_SECONDS.toFloat()
        val eased = t * t
        return (1000 - eased * 890).toLong().coerceAtLeast(110L)
    }

    fun markCorrect() {
        val state = _gameState.value ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        val previousPlayerId = state.currentPlayer?.id
        val updated = GameEngine.markCorrect(state)
        _gameState.value = updated
        soundManager.play(SoundEffect.CORRECT, _settings.value.soundEnabled)
        soundManager.vibrate(_settings.value.vibrationEnabled)

        val playerChanged = updated.currentPlayer?.id != previousPlayerId

        when {
            updated.status == GameStatus.FINISHED -> {
                // در حالت دست‌به‌دست ممکن است بازی دقیقاً با یک پاسخ صحیح تمام شود.
                timerJob?.cancel()
                beepJob?.cancel()
                resetPassCooldown()
                persist(updated)
                onGameFinished(updated)
            }
            playerChanged -> {
                // دست‌به‌دست: نوبت عوض شد؛ کلمه‌ی جدید هم کول‌داون تازه‌ی خودش را می‌خواهد.
                soundManager.play(SoundEffect.TURN_CHANGE, _settings.value.soundEnabled)
                startPassCooldown()
                persist(updated)
            }
            else -> {
                // رالی: همان بازیکن با کلمه‌ی جدید ادامه می‌دهد؛ برای این کلمه‌ی جدید هم کول‌داون از نو شروع می‌شود.
                startPassCooldown()
            }
        }
    }

    /** فاصله‌ی زمانی اجباری بعد از «رد شو» تا از رد کردن پی‌درپی و بی‌رویه‌ی کلمات جلوگیری شود. */
    fun markPass() {
        val state = _gameState.value ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        if (_passCooldownRemaining.value > 0) return
        val updated = GameEngine.markPass(state)
        _gameState.value = updated
        soundManager.play(SoundEffect.PASS, _settings.value.soundEnabled)
        startPassCooldown()
    }

    private fun startPassCooldown() {
        passCooldownJob?.cancel()
        val cooldown = _settings.value.passCooldownSeconds
        if (cooldown <= 0) {
            _passCooldownRemaining.value = 0
            return
        }
        _passCooldownRemaining.value = cooldown
        passCooldownJob = viewModelScope.launch {
            var remaining = cooldown
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _passCooldownRemaining.value = remaining
            }
        }
    }

    private fun resetPassCooldown() {
        passCooldownJob?.cancel()
        _passCooldownRemaining.value = 0
    }

    fun finishTurnManually() {
        val state = _gameState.value ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        if (state.isSurvivorMode) {
            // سرویوایور: پایان دستی فقط یک پاسِ نرم است؛ ساعتِ کلیِ بازی متوقف نمی‌شود.
            val previousPlayerId = state.currentPlayer?.id
            val updated = GameEngine.endTurn(state)
            _gameState.value = updated
            if (updated.status == GameStatus.FINISHED) {
                timerJob?.cancel()
                beepJob?.cancel()
                resetPassCooldown()
                persist(updated)
                onGameFinished(updated)
            } else {
                if (updated.currentPlayer?.id != previousPlayerId) {
                    soundManager.play(SoundEffect.TURN_CHANGE, _settings.value.soundEnabled)
                    startPassCooldown()
                }
                persist(updated)
            }
        } else {
            finishTurn()
        }
    }

    private fun finishTurn() {
        timerJob?.cancel()
        beepJob?.cancel()
        resetPassCooldown()
        val state = _gameState.value ?: return
        val timedOut = state.settings.timerDurationSeconds != 0 && state.timeRemainingSeconds <= 0
        val updated = GameEngine.endTurn(state)
        _gameState.value = updated
        persist(updated)
        if (updated.status == GameStatus.FINISHED) {
            onGameFinished(updated)
        } else if (timedOut) {
            soundManager.play(SoundEffect.TIME_UP, _settings.value.soundEnabled)
            soundManager.vibrate(_settings.value.vibrationEnabled, durationMs = 180L)
        } else {
            soundManager.play(SoundEffect.TURN_CHANGE, _settings.value.soundEnabled)
        }
    }

    private fun onGameFinished(state: GameState) {
        val result = GameEngine.computeResult(state)
        _lastResult.value = result
        val hasWinner = result.winnerTeams.size == 1
        soundManager.play(
            if (hasWinner) SoundEffect.VICTORY else SoundEffect.DEFEAT,
            _settings.value.soundEnabled
        )
        viewModelScope.launch {
            val winnerName = result.winnerTeams.firstOrNull()?.name ?: "مساوی"
            store.addHistoryEntry(
                GameHistoryEntry(
                    id = state.id,
                    dateEpochMillis = System.currentTimeMillis(),
                    mode = state.mode,
                    playerCount = state.players.size,
                    winnerName = winnerName,
                    winnerScore = result.winnerTeams.firstOrNull()?.score ?: 0,
                    durationSeconds = result.durationSeconds
                )
            )
        }
    }

    fun clearFinishedGame() {
        timerJob?.cancel()
        beepJob?.cancel()
        passCooldownJob?.cancel()
        _gameState.value = null
        _lastResult.value = null
        viewModelScope.launch { store.clearGameState() }
    }

    /** آماده‌سازی یک بازی نیمه‌تمام برای ادامه (مثلاً پس از بستن برنامه در وسط بازی). */
    fun prepareResume() {
        val state = _gameState.value ?: return
        when (state.status) {
            GameStatus.IN_PROGRESS -> {
                timerJob?.cancel()
                beepJob?.cancel()
                passCooldownJob?.cancel()
                val resumed = state.copy(status = GameStatus.TURN_TRANSITION, currentWord = null)
                _gameState.value = resumed
                persist(resumed)
            }
            GameStatus.ROUND_SUMMARY -> {
                // یک چک‌پوینتِ سرویوایور نیمه‌کاره بود (حذف تیم یا پایان دور)؛
                // ابتدا طبق قانونش حل می‌شود، بعد مثل یک انتقال نوبتِ عادی نشان داده می‌شود.
                timerJob?.cancel()
                beepJob?.cancel()
                passCooldownJob?.cancel()
                val resolved = GameEngine.resumeFromCheckpoint(state)
                    .copy(status = GameStatus.TURN_TRANSITION, currentWord = null)
                _gameState.value = resolved
                persist(resolved)
            }
            else -> {}
        }
    }

    /** شروع مجدد بازی با همان بازیکنان و همان حالت بازیِ تمام‌شده. */
    fun playAgainSamePlayers() {
        val finished = _gameState.value ?: return
        val names = finished.players.sortedBy { it.position }.map { it.name }
        val newState = GameEngine.createGame(finished.mode, names, _settings.value)
        // عمداً lastResult را اینجا null نمی‌کنیم: چون صفحه‌ی نتیجه هنوز موقتاً روی صفحه است،
        // خالی کردنش دقیقاً همین‌جا باعث می‌شد قبل از تکمیل ناوبری، آن صفحه با state خالی
        // رندر دوباره شود و به‌اشتباه به صفحه‌ی اصلی هدایت کند. با پایان بازیِ بعدی خودش تازه می‌شود.
        _gameState.value = newState
        survivorBeepFloorMs = 1000L
        persist(newState)
        soundManager.play(SoundEffect.START, _settings.value.soundEnabled)
    }

    fun playClickSound() {
        soundManager.play(SoundEffect.CLICK, _settings.value.soundEnabled)
    }

    private fun persist(state: GameState) {
        viewModelScope.launch { store.saveGameState(state) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        beepJob?.cancel()
        passCooldownJob?.cancel()
        soundManager.release()
    }

    companion object {
        private const val URGENT_PHASE_SECONDS = 15

        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GameViewModel(context.applicationContext) as T
                }
            }
    }
}
