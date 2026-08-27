package com.cometchat.uikit.compose.presentation.messagelist.ui

import com.cometchat.uikit.compose.presentation.shared.interfaces.DateTimeFormatterCallback
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ENG-38585 — property-based coverage of the date separator precedence.
 *
 * The example-based tests pin a handful of combinations; these pin the rules for arbitrary
 * inputs: a callback that answers always wins, a callback that declines always falls through,
 * `dateFormat` never reaches today or yesterday, and no input produces blank separator text.
 *
 * Layer 3 (PBT). Kotest Property with `checkAll` and `Arb` generators, no hardcoded values.
 *
 * Run: ./gradlew :chatuikit-compose:testDebugUnitTest --tests "*MessageListDateSeparatorPropertyTest"
 */
class MessageListDateSeparatorPropertyTest : StringSpec({

    fun daysAgo(days: Int): Long = Calendar.getInstance()
        .apply { add(Calendar.DAY_OF_YEAR, -days) }
        .timeInMillis / 1000

    val today = daysAgo(0)
    val yesterday = daysAgo(1)
    val older = daysAgo(30)

    fun answering(answer: String) = object : DateTimeFormatterCallback {
        override fun today(timestamp: Long) = answer
        override fun yesterday(timestamp: Long) = answer
        override fun otherDays(timestamp: Long) = answer
    }

    val declining = object : DateTimeFormatterCallback {}

    "a callback that answers wins for every bucket" {
        checkAll(Arb.string(1..20), Arb.string(1..10), Arb.string(1..10)) { answer, todayText, yesterdayText ->
            listOf(today, yesterday, older).forEach { timestamp ->
                resolveDateSeparatorText(
                    timestamp = timestamp,
                    dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.US),
                    dateTimeFormatter = answering(answer),
                    todayText = todayText,
                    yesterdayText = yesterdayText
                ) shouldBe answer
            }
        }
    }

    "a callback that declines falls through for every bucket" {
        checkAll(Arb.string(1..10), Arb.string(1..10)) { todayText, yesterdayText ->
            val pattern = SimpleDateFormat("dd.MM.yyyy", Locale.US)
            fun resolve(timestamp: Long) = resolveDateSeparatorText(
                timestamp = timestamp,
                dateFormat = pattern,
                dateTimeFormatter = declining,
                todayText = todayText,
                yesterdayText = yesterdayText
            )

            resolve(today) shouldBe todayText
            resolve(yesterday) shouldBe yesterdayText
            resolve(older) shouldBe pattern.format(java.util.Date(older * 1000))
        }
    }

    "dateFormat never reaches today or yesterday" {
        checkAll(Arb.string(1..10), Arb.string(1..10)) { todayText, yesterdayText ->
            val pattern = SimpleDateFormat("dd.MM.yyyy", Locale.US)
            fun resolve(timestamp: Long) = resolveDateSeparatorText(
                timestamp = timestamp,
                dateFormat = pattern,
                dateTimeFormatter = null,
                todayText = todayText,
                yesterdayText = yesterdayText
            )

            resolve(today) shouldBe todayText
            resolve(yesterday) shouldBe yesterdayText
        }
    }

    "no timestamp produces blank separator text" {
        checkAll(Arb.long(0L..4_102_444_800L)) { timestamp ->
            resolveDateSeparatorText(
                timestamp = timestamp,
                dateFormat = null,
                dateTimeFormatter = null,
                todayText = "Today",
                yesterdayText = "Yesterday"
            ).shouldNotBeBlank()
        }
    }

    "the bucket only depends on the day, never on the formatters" {
        checkAll(Arb.string(1..10)) { answer ->
            dateSeparatorBucketOf(today) shouldBe DateSeparatorBucket.TODAY
            dateSeparatorBucketOf(yesterday) shouldBe DateSeparatorBucket.YESTERDAY
            dateSeparatorBucketOf(older) shouldBe DateSeparatorBucket.OTHER
            answering(answer).today(0L) shouldBe answer
        }
    }
})
