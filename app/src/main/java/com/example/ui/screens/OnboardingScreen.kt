package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.data.IndustryType
import com.example.data.TaxRegime
import com.example.ui.components.BalansLineBeam
import com.example.ui.components.BalansLogoMark
import com.example.ui.components.SolidCard
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalBalansPalette

@Composable
fun OnboardingScreen(
    isSaving: Boolean,
    onComplete: (
        companyName: String,
        stir: String,
        city: String,
        taxRegime: TaxRegime,
        industry: IndustryType,
        seedDemo: Boolean
    ) -> Unit
) {
    val palette = LocalBalansPalette.current
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var step by remember { mutableIntStateOf(1) }
    var companyName by remember { mutableStateOf("Oltin Vodiy Tekstil MCHJ") }
    var stir by remember { mutableStateOf("305412890") }
    var city by remember { mutableStateOf("Farg‘ona") }
    var taxRegime by remember { mutableStateOf(TaxRegime.QQS) }
    var industry by remember { mutableStateOf(IndustryType.ISHLAB_CHIQARISH) }
    var seedDemo by remember { mutableStateOf(true) }

    val stirValid = Formatters.isValidStir(stir)
    val step1Valid = companyName.trim().length >= 2 && stirValid && city.trim().length >= 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BalansLogoMark(size = 38.dp)
                Column {
                    Text(
                        text = strings.onboardingTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "$step / 5",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.emeraldText
                    )
                }
            }

            BalansLineBeam(modifier = Modifier.fillMaxWidth())

            SolidCard(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    1 -> {
                        Text(
                            text = if (lang == AppLanguage.RU) "1. Реквизиты компании" else "1. Kompaniya rekvizitlari",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text(strings.companyNameLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_company_name")
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = stir,
                            onValueChange = { input -> stir = input.filter { it.isDigit() }.take(9) },
                            label = { Text(strings.stirLabel) },
                            isError = stir.isNotEmpty() && !stirValid,
                            supportingText = {
                                if (stir.isNotEmpty() && !stirValid) {
                                    Text(strings.stirError, color = palette.danger)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_stir")
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text(strings.cityLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_city")
                        )
                    }

                    2 -> {
                        Text(
                            text = "2. ${strings.taxRegimeLabel}",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        TaxRegime.entries.forEach { regime ->
                            val selected = taxRegime == regime
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { taxRegime = regime },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) palette.emerald.copy(alpha = 0.14f) else palette.elevated,
                                border = BorderStroke(1.dp, if (selected) palette.emerald else palette.border)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.RU) regime.labelRu else regime.labelUz,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textPrimary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    3 -> {
                        Text(
                            text = "3. ${strings.industryLabel}",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        IndustryType.entries.forEach { ind ->
                            val selected = industry == ind
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { industry = ind },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) palette.emerald.copy(alpha = 0.14f) else palette.elevated,
                                border = BorderStroke(1.dp, if (selected) palette.emerald else palette.border)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.RU) ind.labelRu else ind.labelUz,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textPrimary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    4 -> {
                        Text(
                            text = if (lang == AppLanguage.RU) "4. Импорт и стартовые данные" else "4. Boshlang‘ich ma’lumotlar (Import)",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        listOf(true to strings.demoSeedOption, false to strings.cleanSlateOption).forEach { (isDemo, label) ->
                            val selected = seedDemo == isDemo
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { seedDemo = isDemo },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) palette.emerald.copy(alpha = 0.14f) else palette.elevated,
                                border = BorderStroke(1.dp, if (selected) palette.emerald else palette.border)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = palette.textPrimary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    5 -> {
                        Text(
                            text = if (lang == AppLanguage.RU) "5. Готово к запуску" else "5. Ishga tushirishga tayyor",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "$companyName · STIR $stir · $city",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.emeraldText
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (lang == AppLanguage.RU) {
                                "Включён 30-дневный пробный период Premium. Вы сможете переключать роли (RBAC) и приглашать сотрудников в настройках."
                            } else {
                                "30 kunlik bepul Premium sinov davri faollashtiriladi. Sozlamalarda 9 xil xodim rolini tekshirishingiz mumkin."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text(strings.prevStep)
                    }
                }
                Button(
                    onClick = {
                        if (step < 5) {
                            step++
                        } else {
                            onComplete(companyName, stir, city, taxRegime, industry, seedDemo)
                        }
                    },
                    enabled = !isSaving && (step != 1 || step1Valid),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.emerald,
                        contentColor = palette.onEmerald
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("onboarding_next_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = palette.onEmerald, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (step < 5) strings.nextStep else strings.launchCockpit,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
