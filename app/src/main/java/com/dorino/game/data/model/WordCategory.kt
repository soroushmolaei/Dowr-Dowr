package com.dorino.game.data.model

import com.dorino.game.R
import kotlinx.serialization.Serializable

@Serializable
enum class WordCategory(val labelRes: Int) {
    GENERAL(R.string.category_general),
    MOVIES(R.string.category_movies),
    CELEBRITIES(R.string.category_celebrities),
    FOOD(R.string.category_food),
    ANIMALS(R.string.category_animals),
    PLACES(R.string.category_places),
    JOBS(R.string.category_jobs),
    OBJECTS(R.string.category_objects),
    HOBBIES(R.string.category_hobbies),
    TECHNOLOGY(R.string.category_technology),
    SPORTS(R.string.category_sports),
    IRANIAN(R.string.category_iranian),
    HARD(R.string.category_hard),
    EASY(R.string.category_easy),
    FUNNY(R.string.category_funny)
}

/** درجه سختی مستقل از دسته‌بندی؛ هر کلمه در هر دسته‌ای می‌تواند هرکدام از این سطوح را داشته باشد. */
@Serializable
enum class Difficulty(val labelRes: Int) {
    EASY(R.string.difficulty_easy),
    MEDIUM(R.string.difficulty_medium),
    HARD(R.string.difficulty_hard)
}

@Serializable
data class Word(
    val text: String,
    val category: WordCategory,
    val difficulty: Difficulty = Difficulty.MEDIUM
)
