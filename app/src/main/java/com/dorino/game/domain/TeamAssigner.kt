package com.dorino.game.domain

import com.dorino.game.data.model.GameMode
import com.dorino.game.data.model.Team

/**
 * تخصیص تیم به‌صورت کاملاً پویا و مستقل از تعداد بازیکنان.
 * هیچ مقدار تعداد بازیکن به‌صورت Hardcode در این کلاس وجود ندارد.
 */
object TeamAssigner {

    data class TeamPalette(val colorHex: String, val shapeIndex: Int)

    private val palette = listOf(
        TeamPalette("#FF6B6B", 0), // دایره
        TeamPalette("#4DD0E5", 1), // مربع
        TeamPalette("#FFD166", 2), // مثلث
        TeamPalette("#81C784", 3), // لوزی
        TeamPalette("#BA68C8", 4), // شش‌ضلعی
        TeamPalette("#FFAB40", 5), // ستاره
        TeamPalette("#64B5F6", 0),
        TeamPalette("#F06292", 1),
        TeamPalette("#A1887F", 2),
        TeamPalette("#90A4AE", 3)
    )

    /**
     * برای هر بازیکن (بر اساس Index در چیدمان دایره‌ای صفر-پایه)، شناسه تیم را برمی‌گرداند.
     */
    fun teamIdForSeatIndex(mode: GameMode, seatIndex: Int, playerCount: Int): Int {
        return when (mode) {
            GameMode.TEAM_BATTLE -> seatIndex % 2
            GameMode.PAIR_TEAMS -> seatIndex % (playerCount / 2)
        }
    }

    fun buildTeams(mode: GameMode, playerCount: Int): List<Team> {
        val teamCount = when (mode) {
            GameMode.TEAM_BATTLE -> 2
            GameMode.PAIR_TEAMS -> playerCount / 2
        }
        return (0 until teamCount).map { teamId ->
            val playerIds = (0 until playerCount).filter { seatIndex ->
                teamIdForSeatIndex(mode, seatIndex, playerCount) == teamId
            }
            val paletteEntry = palette[teamId % palette.size]
            val name = when (mode) {
                GameMode.TEAM_BATTLE -> if (teamId == 0) "تیم قرمز" else "تیم آبی"
                GameMode.PAIR_TEAMS -> "تیم ${teamId + 1}"
            }
            Team(
                id = teamId,
                name = name,
                colorHex = paletteEntry.colorHex,
                shapeIndex = paletteEntry.shapeIndex,
                playerIds = playerIds
            )
        }
    }

    /** جایگاه دقیقاً روبه‌رو در حالت تیم‌های دونفره. */
    fun oppositeSeatIndex(seatIndex: Int, playerCount: Int): Int =
        (seatIndex + playerCount / 2) % playerCount
}
