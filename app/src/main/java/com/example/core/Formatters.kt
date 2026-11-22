package com.example.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

object Formatters {

    private val STIR_REGEX = Regex("^[0-9]{9}$")
    private val UZ_PHONE_REGEX = Regex("^\\+?998[\\s-]?\\d{2}[\\s-]?\\d{3}[\\s-]?\\d{2}[\\s-]?\\d{2}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun formatUzs(
        amount: Long,
        compact: Boolean = false,
        includeSign: Boolean = false,
        currencySuffix: String = "so‘m"
    ): String {
        val sign = when {
            amount < 0 -> "-"
            includeSign && amount > 0 -> "+"
            else -> ""
        }
        val absVal = abs(amount)

        if (compact) {
            return when {
                absVal >= 1_000_000_000L -> {
                    val mlrd = absVal / 1_000_000_000.0
                    val formatted = String.format(Locale.US, "%.1f", mlrd).replace('.', ',')
                    "$sign$formatted mlrd $currencySuffix"
                }
                absVal >= 1_000_000L -> {
                    val mln = absVal / 1_000_000.0
                    val formatted = String.format(Locale.US, "%.1f", mln).replace('.', ',')
                    "$sign$formatted mln $currencySuffix"
                }
                else -> "$sign${groupThousands(absVal)} $currencySuffix"
            }
        }
        return "$sign${groupThousands(absVal)} $currencySuffix"
    }

    fun formatUsdFromUzs(amountUzs: Long, usdRate: Double = 12_850.0): String {
        val usd = (amountUzs / usdRate).roundToLong()
        return "$${groupThousands(abs(usd))}"
    }

    fun formatDeltaPercent(percent: Double): String {
        val arrow = if (percent >= 0) "▲" else "▼"
        val sign = if (percent >= 0) "+" else "-"
        val num = String.format(Locale.US, "%.1f", abs(percent)).replace('.', ',')
        return "$arrow $sign$num%"
    }

    fun formatPercent(percent: Double): String {
        val num = String.format(Locale.US, "%.1f", percent).replace('.', ',')
        return "$num%"
    }

    fun groupThousands(value: Long): String {
        val digits = abs(value).toString()
        val sb = StringBuilder()
        var count = 0
        for (i in digits.length - 1 downTo 0) {
            sb.append(digits[i])
            count++
            if (count % 3 == 0 && i != 0) {
                sb.append('\u00A0')
            }
        }
        return sb.reverse().toString()
    }

    fun formatDateDdMmYyyy(epochMs: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        return sdf.format(Date(epochMs))
    }

    fun isValidStir(stir: String): Boolean = STIR_REGEX.matches(stir.trim())

    fun isValidUzPhone(phone: String): Boolean = UZ_PHONE_REGEX.matches(phone.trim())

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())
}
