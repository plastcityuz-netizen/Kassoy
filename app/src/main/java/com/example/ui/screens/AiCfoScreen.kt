package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.data.AiChatMessage
import com.example.data.UiState
import com.example.data.WhatIfScenarioResult
import com.example.ui.BalansCockpitState
import com.example.ui.components.BalansLineBeam
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricDeltaBadge
import com.example.ui.components.SolidCard
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalBalansPalette
import com.example.ui.theme.TabularNumberStyle

@Composable
fun AiCfoScreen(
    state: BalansCockpitState,
    messages: List<AiChatMessage>,
    chatState: UiState<Unit>,
    morningBriefState: UiState<List<String>>?,
    whatIfState: UiState<WhatIfScenarioResult>?,
    onAskQuestion: (String) -> Unit,
    onLoadMorningBrief: () -> Unit,
    onRunWhatIf: (String) -> Unit
) {
    val palette = LocalBalansPalette.current
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var questionInput by remember { mutableStateOf("") }
    var whatIfInput by remember {
        mutableStateOf(
            if (lang == AppLanguage.RU) "Если поднять цены на 5% и снизить брак до 0,8%?"
            else "Agar sotuv narxini 5% oshirsam va brakni 0,8% ga tushirsam?"
        )
    }

    val quickPrompts = if (lang == AppLanguage.RU) {
        listOf(
            "Почему выросли расходы?",
            "Какой запас ликвидности у компании?",
            "Кому отправить напоминание о долге?"
        )
    } else {
        listOf(
            "Bu oy xarajatlar tarkibi qanday?",
            "Likvidlik va kassa holatini baholang",
            "Qaysi qarzdorlardan pulni tezroq undirish kerak?"
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // AI CFO Header & Safety Rule
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentBorder = palette.violet.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, null, tint = palette.violet)
                            Text(
                                text = "AI CFO Copilot",
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.textPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.emerald.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "Gemini 3.8 Flash",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.emeraldText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    BalansLineBeam(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Shield, null, tint = palette.emeraldText, modifier = Modifier.size(16.dp))
                        Text(
                            text = strings.aiDisclaimer,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    }
                }
            }

            // Morning Brief (3 priorities)
            item {
                SolidCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.aiMorningBriefTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onLoadMorningBrief,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("load_morning_brief_btn")
                        ) {
                            Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (lang == AppLanguage.RU) "Обновить" else "Yangilash")
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    when (morningBriefState) {
                        null -> {
                            Text(
                                text = if (lang == AppLanguage.RU) {
                                    "Нажмите «Обновить», чтобы Gemini AI сформировал 3 главных приоритета на основе текущих цифр."
                                } else {
                                    "Joriy kompaniya raqamlari asosida eng muhim 3 ta vazifani olish uchun “Yangilash” tugmasini bosing."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted
                            )
                        }
                        is UiState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text(strings.loadingLabel, style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                            }
                        }
                        is UiState.Error -> {
                            ErrorStateCard(
                                message = morningBriefState.message,
                                retryLabel = strings.retryLabel,
                                onRetry = onLoadMorningBrief
                            )
                        }
                        is UiState.Success -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                morningBriefState.data.forEachIndexed { idx, point ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = palette.elevated,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${idx + 1}. $point",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = palette.textPrimary,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // "What if...?" Structured Scenario Simulator
            item {
                SolidCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Bolt, null, tint = palette.cyan)
                        Text(
                            text = strings.aiWhatIfTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = whatIfInput,
                        onValueChange = { whatIfInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("what_if_input"),
                        maxLines = 2
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onRunWhatIf(whatIfInput) },
                        enabled = whatIfInput.isNotBlank() && whatIfState !is UiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.emerald,
                            contentColor = palette.onEmerald
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("run_what_if_btn")
                    ) {
                        Text(strings.aiWhatIfRun, fontWeight = FontWeight.Bold)
                    }
                    when (whatIfState) {
                        is UiState.Loading -> {
                            Spacer(Modifier.height(10.dp))
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                        is UiState.Error -> {
                            Spacer(Modifier.height(10.dp))
                            ErrorStateCard(
                                message = whatIfState.message,
                                retryLabel = strings.retryLabel,
                                onRetry = { onRunWhatIf(whatIfInput) }
                            )
                        }
                        is UiState.Success -> {
                            val res = whatIfState.data
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = palette.elevated,
                                border = BorderStroke(1.dp, palette.emerald.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(res.scenarioName, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                        MetricDeltaBadge(res.profitDeltaPercent)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Tushum: ${Formatters.formatUzs(res.projectedRevenueUzs, compact = true)}", style = MaterialTheme.typography.bodySmall.merge(TabularNumberStyle), color = palette.textMuted)
                                        Text("Foyda: ${Formatters.formatUzs(res.projectedProfitUzs, compact = true)}", style = MaterialTheme.typography.bodySmall.merge(TabularNumberStyle), color = palette.emeraldText)
                                    }
                                    Text(res.recommendation, style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
                                }
                            }
                        }
                        null -> {}
                    }
                }
            }

            // Quick Prompt Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickPrompts) { prompt ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = palette.elevated,
                            border = BorderStroke(1.dp, palette.border),
                            modifier = Modifier.clickable { onAskQuestion(prompt) }
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Chat History
            items(messages, key = { it.id }) { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (msg.isUser) palette.emerald.copy(alpha = 0.16f) else palette.surface,
                        border = BorderStroke(1.dp, if (msg.isUser) palette.emerald.copy(alpha = 0.4f) else palette.border),
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (msg.isUser) state.activeRole.label(lang) else "AI CFO",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (msg.isUser) palette.emeraldText else palette.violet
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (msg.text.isBlank() && msg.isStreaming) "..." else msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textPrimary
                            )
                        }
                    }
                }
            }

            if (chatState is UiState.Error) {
                item {
                    ErrorStateCard(
                        message = chatState.message,
                        retryLabel = strings.retryLabel,
                        onRetry = {
                            val lastUser = messages.lastOrNull { it.isUser }?.text
                            if (!lastUser.isNullOrBlank()) onAskQuestion(lastUser)
                        }
                    )
                }
            }
        }

        // Chat Input Bar
        Surface(
            color = palette.surface,
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    placeholder = { Text(strings.aiChatPlaceholder, maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("ai_chat_input")
                )
                IconButton(
                    onClick = {
                        val q = questionInput.trim()
                        if (q.isNotEmpty()) {
                            questionInput = ""
                            onAskQuestion(q)
                        }
                    },
                    enabled = questionInput.isNotBlank() && chatState !is UiState.Loading,
                    modifier = Modifier
                        .size(48.dp)
                        .background(palette.emerald, RoundedCornerShape(12.dp))
                        .testTag("ai_chat_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Yuborish",
                        tint = palette.onEmerald
                    )
                }
            }
        }
    }
}
