package com.example.data

import android.util.Log
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class AiCfoRepository {

    private companion object {
        const val TAG = "AiCfoRepository"
        const val MODEL_NAME = "gemini-3.8-flash"
        const val TIMEOUT_MS = 60_000L
    }

    private val chatModel by lazy {
        Firebase.ai.generativeModel(MODEL_NAME)
    }

    private val whatIfSchema = Schema.obj(
        properties = mapOf(
            "scenarioName" to Schema.string("Short scenario title"),
            "projectedRevenueUzs" to Schema.integer("Projected monthly revenue in UZS"),
            "projectedExpenseUzs" to Schema.integer("Projected monthly expense in UZS"),
            "projectedProfitUzs" to Schema.integer("Projected net profit in UZS"),
            "profitDeltaPercent" to Schema.double("Expected profit percentage change"),
            "recommendation" to Schema.string("Concise, actionable CFO recommendation")
        )
    )

    private val whatIfModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = whatIfSchema
            }
        )
    }

    private val briefSchema = Schema.obj(
        properties = mapOf(
            "items" to Schema.array(
                items = Schema.string(),
                description = "Exactly 3 concise, prioritized morning CFO action points grounded in numbers"
            )
        )
    )

    private val briefModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = briefSchema
            }
        )
    }

    fun streamCfoAnswer(
        question: String,
        company: Company?,
        metrics: DashboardMetrics,
        language: AppLanguage
    ): Flow<String> = flow {
        val langInstruction = if (language == AppLanguage.RU) {
            "Respond in clear, professional Russian for an Uzbekistan business owner."
        } else {
            "Respond in clear, respectful Uzbek (Latin script: o‘, g‘, ’) for an Uzbekistan business owner."
        }

        val contextBlock = buildCompanyContext(company, metrics)
        val prompt = """
            You are BALANS AI CFO — a senior financial advisor for Uzbekistan businesses.
            $langInstruction
            Rules:
            - Cite exact UZS figures from the company data below.
            - Always include source reasoning (which figure or ratio drives your advice).
            - NEVER claim you transferred money or edited accounting entries automatically; always state that user confirmation is required.
            - Keep the answer structured, concise, and practical (max 150 words).

            Company financial snapshot:
            $contextBlock

            User question:
            $question
        """.trimIndent()

        val accumulated = StringBuilder()
        try {
            withTimeout(TIMEOUT_MS) {
                chatModel.generateContentStream(prompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        accumulated.append(chunkText)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("AI CFO javobi bo‘sh qaytdi.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI CFO stream timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI CFO stream failed", e)
            throw e
        }
    }

    suspend fun generateMorningBrief(
        company: Company?,
        metrics: DashboardMetrics,
        language: AppLanguage
    ): Result<List<String>> {
        val langInstruction = if (language == AppLanguage.RU) {
            "Output 3 items in Russian."
        } else {
            "Output 3 items in Uzbek (Latin script)."
        }
        val prompt = """
            Generate today's 3-point Morning Brief for the business owner based strictly on this snapshot.
            $langInstruction
            ${buildCompanyContext(company, metrics)}
        """.trimIndent()

        return try {
            val response = withTimeout(TIMEOUT_MS) {
                briefModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Empty brief response")
            val json = JSONObject(jsonText)
            val arr = json.getJSONArray("items")
            val list = (0 until arr.length()).map { arr.getString(it) }
            Result.success(list)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Morning brief timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Morning brief failed", e)
            Result.failure(e)
        }
    }

    suspend fun runWhatIfScenario(
        scenarioInput: String,
        company: Company?,
        metrics: DashboardMetrics,
        language: AppLanguage
    ): Result<WhatIfScenarioResult> {
        val langInstruction = if (language == AppLanguage.RU) {
            "Use Russian for scenarioName and recommendation."
        } else {
            "Use Uzbek (Latin script) for scenarioName and recommendation."
        }
        val prompt = """
            Run a financial "What if...?" simulation for this Uzbekistan company.
            $langInstruction
            ${buildCompanyContext(company, metrics)}
            Scenario requested by owner: "$scenarioInput"
            Compute realistic projectedRevenueUzs, projectedExpenseUzs, projectedProfitUzs, profitDeltaPercent, and recommendation.
        """.trimIndent()

        return try {
            val response = withTimeout(TIMEOUT_MS) {
                whatIfModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Empty what-if response")
            val json = JSONObject(jsonText)
            Result.success(
                WhatIfScenarioResult(
                    scenarioName = json.getString("scenarioName"),
                    projectedRevenueUzs = json.getLong("projectedRevenueUzs"),
                    projectedExpenseUzs = json.getLong("projectedExpenseUzs"),
                    projectedProfitUzs = json.getLong("projectedProfitUzs"),
                    profitDeltaPercent = json.getDouble("profitDeltaPercent"),
                    recommendation = json.getString("recommendation")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "What-if simulation timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "What-if simulation failed", e)
            Result.failure(e)
        }
    }

    private fun buildCompanyContext(company: Company?, metrics: DashboardMetrics): String {
        val topExpenseText = metrics.expenseTop5.joinToString("; ") {
            "${it.category}: ${Formatters.formatUzs(it.amountUzs)} (${Formatters.formatPercent(it.sharePercent)})"
        }
        return """
            - Company: ${company?.name ?: "BALANS Demo"} (STIR: ${company?.stir ?: "000000000"}, Regime: ${company?.taxRegime ?: "QQS"}, City: ${company?.city ?: "Toshkent"})
            - Revenue: ${Formatters.formatUzs(metrics.totalRevenueUzs)}
            - Expenses: ${Formatters.formatUzs(metrics.totalExpenseUzs)}
            - Net Profit: ${Formatters.formatUzs(metrics.netProfitUzs)}
            - Cash & Bank: ${Formatters.formatUzs(metrics.cashAndBankUzs)}
            - Receivables (Debitor): ${Formatters.formatUzs(metrics.receivableUzs)}
            - Payables (Kreditor): ${Formatters.formatUzs(metrics.payableUzs)}
            - Inventory Value: ${Formatters.formatUzs(metrics.inventoryValueUzs)}
            - Balans Score: ${metrics.balansScore.overallScore}/100 (Liquidity ${metrics.balansScore.liquidityScore}, Profitability ${metrics.balansScore.profitabilityScore}, Debt ${metrics.balansScore.debtHealthScore}, Inventory ${metrics.balansScore.inventoryTurnScore})
            - Top Expenses: $topExpenseText
        """.trimIndent()
    }
}
