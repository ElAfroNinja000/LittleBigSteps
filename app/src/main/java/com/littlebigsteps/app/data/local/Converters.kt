package com.littlebigsteps.app.data.local

import androidx.room.TypeConverter
import com.littlebigsteps.app.domain.model.Badge
import com.littlebigsteps.app.domain.model.ChallengeLevel
import com.littlebigsteps.app.domain.model.Frequency
import com.littlebigsteps.app.domain.model.MediumType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Room ne connaît nativement ni kotlinx.datetime, ni les List<>/enums : ces
 * convertisseurs les rendent stockables sous forme primitive (Long, String).
 */
class Converters {

    // --- kotlinx.datetime ---

    @TypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilliseconds()

    @TypeConverter
    fun epochMillisToInstant(value: Long?): Instant? =
        value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    // --- listes de chaînes (tags) ---

    @TypeConverter
    fun stringListToString(value: List<String>?): String? =
        value?.joinToString(separator = ITEM_SEPARATOR)

    @TypeConverter
    fun stringToStringList(value: String?): List<String>? =
        value?.takeIf { it.isNotEmpty() }?.split(ITEM_SEPARATOR)

    // --- enums ---

    @TypeConverter
    fun mediumTypeToString(value: MediumType?): String? = value?.name

    @TypeConverter
    fun stringToMediumType(value: String?): MediumType? = value?.let { MediumType.valueOf(it) }

    @TypeConverter
    fun mediumTypeListToString(value: List<MediumType>?): String? =
        value?.joinToString(separator = ITEM_SEPARATOR) { it.name }

    @TypeConverter
    fun stringToMediumTypeList(value: String?): List<MediumType>? =
        value?.takeIf { it.isNotEmpty() }?.split(ITEM_SEPARATOR)?.map { MediumType.valueOf(it) }

    @TypeConverter
    fun challengeLevelToString(value: ChallengeLevel?): String? = value?.name

    @TypeConverter
    fun stringToChallengeLevel(value: String?): ChallengeLevel? =
        value?.let { ChallengeLevel.valueOf(it) }

    @TypeConverter
    fun frequencyToString(value: Frequency?): String? = value?.name

    @TypeConverter
    fun stringToFrequency(value: String?): Frequency? = value?.let { Frequency.valueOf(it) }

    @TypeConverter
    fun badgeToString(value: Badge?): String? = value?.name

    @TypeConverter
    fun stringToBadge(value: String?): Badge? = value?.let { Badge.valueOf(it) }

    private companion object {
        const val ITEM_SEPARATOR = "|"
    }
}
