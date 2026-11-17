package uz.tikoncha_parent.presentation.assistant

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Rule
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.ai_ask_protection
import tikoncha_parents.composeapp.generated.resources.ai_ask_requests
import tikoncha_parents.composeapp.generated.resources.ai_ask_rules
import tikoncha_parents.composeapp.generated.resources.ai_ask_screen_time
import tikoncha_parents.composeapp.generated.resources.ai_ask_tasks
import tikoncha_parents.composeapp.generated.resources.ai_ask_where
import tikoncha_parents.composeapp.generated.resources.ai_glance_requests_sub
import tikoncha_parents.composeapp.generated.resources.ai_glance_rules_sub
import tikoncha_parents.composeapp.generated.resources.ai_glance_tasks_sub
import tikoncha_parents.composeapp.generated.resources.ai_glance_title
import tikoncha_parents.composeapp.generated.resources.ai_glance_where
import tikoncha_parents.composeapp.generated.resources.ai_nav_open
import tikoncha_parents.composeapp.generated.resources.ai_stop
import tikoncha_parents.composeapp.generated.resources.ai_voice_hint
import tikoncha_parents.composeapp.generated.resources.ai_voice_listening
import tikoncha_parents.composeapp.generated.resources.ai_voice_mic
import tikoncha_parents.composeapp.generated.resources.ai_voice_missed
import tikoncha_parents.composeapp.generated.resources.ai_voice_understanding
import tikoncha_parents.composeapp.generated.resources.bugun_sarfladi
import tikoncha_parents.composeapp.generated.resources.cheklovlar
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.hammasi_joyida
import tikoncha_parents.composeapp.generated.resources.himoya
import tikoncha_parents.composeapp.generated.resources.live_watch
import tikoncha_parents.composeapp.generated.resources.mode_default_title
import tikoncha_parents.composeapp.generated.resources.mode_gujanak_title
import tikoncha_parents.composeapp.generated.resources.mode_qalqon_title
import tikoncha_parents.composeapp.generated.resources.obuna
import tikoncha_parents.composeapp.generated.resources.profil
import tikoncha_parents.composeapp.generated.resources.sorovlar
import tikoncha_parents.composeapp.generated.resources.statistika
import tikoncha_parents.composeapp.generated.resources.suhbat
import tikoncha_parents.composeapp.generated.resources.ta_ruxsat_ochirilgan
import tikoncha_parents.composeapp.generated.resources.tangachalar
import tikoncha_parents.composeapp.generated.resources.topshiriqlar
import tikoncha_parents.composeapp.generated.resources.xarita
import uz.tikoncha_parent.domain.model.ai.NavScreen
import uz.tikoncha_parent.presentation.assistant.voice.isReducedMotion
import uz.tikoncha_parent.presentation.statistic.durationStringCompact
import uz.tikoncha_parent.ui.rememberHapticSource
import uz.tikoncha_parent.ui.theme.AppColors
import uz.tikoncha_parent.ui.theme.AppTypography
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ── ovoz ──

/** Asosiy kiritiş — katta yumaloq mikrofon (≥72dp) va uning ostida bir qator holat matni. */
private val MicSize = 76.dp

@Composable
internal fun VoiceDock(
    phase: VoicePhase,
    streaming: Boolean,
    missed: Boolean,
    level: StateFlow<Float>,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val caption = when {
        phase == VoicePhase.LISTENING -> stringResource(Res.string.ai_voice_listening)
        phase == VoicePhase.UNDERSTANDING -> stringResource(Res.string.ai_voice_understanding)
        streaming -> stringResource(Res.string.ai_stop)
        missed -> stringResource(Res.string.ai_voice_missed)
        else -> stringResource(Res.string.ai_voice_hint)
    }
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MicButton(phase = phase, streaming = streaming, level = level, onTap = onTap)
        Spacer(Modifier.height(6.dp))
        Text(
            text = caption,
            style = AppTypography.titleSmMedium,
            color = if (missed && phase == VoicePhase.IDLE && !streaming) AppColors.text.accentWarning else AppColors.text.secondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun MicButton(phase: VoicePhase, streaming: Boolean, level: StateFlow<Float>, onTap: () -> Unit) {
    val reduce = remember { isReducedMotion() }
    val listening = phase == VoicePhase.LISTENING
    val description = stringResource(if (listening || streaming) Res.string.ai_stop else Res.string.ai_voice_mic)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(MicSize + 8.dp)) {
        if (listening) {
            // Ovoz balandligiga qarab kengayadigan halqa + sekin "nafas" halqasi
            val raw by level.collectAsState()
            val l by animateFloatAsState(raw, tween(90), label = "level")
            val t = rememberInfiniteTransition(label = "pulse")
            val pulse by t.animateFloat(0f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "p")
            Box(
                Modifier
                    .size(MicSize)
                    .graphicsLayer {
                        val s = 1.06f + 0.40f * l + if (reduce) 0f else 0.06f * pulse
                        scaleX = s
                        scaleY = s
                    }
                    .clip(CircleShape)
                    .background(AppColors.action.primary.copy(alpha = 0.22f))
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(MicSize)
                .clip(CircleShape)
                .background(AppColors.action.primary)
                .clickable(
                    enabled = phase != VoicePhase.UNDERSTANDING,
                    interactionSource = rememberHapticSource(),
                    indication = null,
                    onClick = onTap,
                )
                .semantics {
                    role = Role.Button
                    contentDescription = description
                },
        ) {
            when {
                phase == VoicePhase.UNDERSTANDING -> CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    strokeWidth = 3.dp,
                    color = OnAccent,
                )
                listening || streaming -> Box(
                    Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(OnAccent)
                )
                else -> Icon(Icons.Rounded.Mic, contentDescription = null, tint = OnAccent, modifier = Modifier.size(36.dp))
            }
        }
    }
}

// ── "Bugun qisqaça" ──

/**
 * Tanlangan farzand haqida 6 ta katta, oddiy karta (2 ustun): qayerda, telefonda qança, himoya, sörovlar,
 * vazifalar, qoidalar. Bosilsa — yordamçiga tabiiy savol beriladi.
 */
@OptIn(ExperimentalTime::class)
@Composable
internal fun GlanceGrid(glance: AssistantGlance, childName: String, onAsk: (String) -> Unit, modifier: Modifier = Modifier) {
    val dash = "—"
    val where = glance.locationAtMs?.let { at ->
        agoText(((Clock.System.now().toEpochMilliseconds() - at) / 60_000L).toInt().coerceAtLeast(0))
    } ?: dash
    val usage = glance.usage?.let { durationStringCompact(it) } ?: dash
    val mode = when (glance.protectionMode?.uppercase()) {
        "STRICT" -> stringResource(Res.string.mode_qalqon_title)
        "GUJANAK" -> stringResource(Res.string.mode_gujanak_title)
        "DEFAULT" -> stringResource(Res.string.mode_default_title)
        else -> dash
    }
    val off = glance.permissionsOff
    val protectionSub = when {
        off == null -> null
        off > 0 -> pluralStringResource(Res.plurals.ta_ruxsat_ochirilgan, off, off)
        else -> stringResource(Res.string.hammasi_joyida)
    }
    val pending = glance.pendingRequests

    val askWhere = stringResource(Res.string.ai_ask_where, childName)
    val askTime = stringResource(Res.string.ai_ask_screen_time, childName)
    val askProtection = stringResource(Res.string.ai_ask_protection)
    val askRequests = stringResource(Res.string.ai_ask_requests)
    val askTasks = stringResource(Res.string.ai_ask_tasks)
    val askRules = stringResource(Res.string.ai_ask_rules)

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(Res.string.ai_glance_title),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
        )
        GlanceRow(
            { GlanceCard(Icons.Rounded.LocationOn, stringResource(Res.string.ai_glance_where), where, null, false, it) { onAsk(askWhere) } },
            { GlanceCard(Icons.Rounded.Smartphone, stringResource(Res.string.bugun_sarfladi), usage, null, false, it) { onAsk(askTime) } },
        )
        GlanceRow(
            {
                GlanceCard(
                    if (off != null && off > 0) Icons.Rounded.GppMaybe else Icons.Rounded.VerifiedUser,
                    stringResource(Res.string.himoya), mode, protectionSub, off != null && off > 0, it,
                ) { onAsk(askProtection) }
            },
            {
                GlanceCard(
                    Icons.Rounded.MarkEmailUnread, stringResource(Res.string.sorovlar), pending?.toString() ?: dash,
                    stringResource(Res.string.ai_glance_requests_sub), pending != null && pending > 0, it,
                ) { onAsk(askRequests) }
            },
        )
        GlanceRow(
            {
                GlanceCard(
                    Icons.Rounded.TaskAlt, stringResource(Res.string.topshiriqlar), glance.activeTasks?.toString() ?: dash,
                    stringResource(Res.string.ai_glance_tasks_sub), false, it,
                ) { onAsk(askTasks) }
            },
            {
                GlanceCard(
                    Icons.AutoMirrored.Rounded.Rule, stringResource(Res.string.cheklovlar), glance.rulesOn?.toString() ?: dash,
                    stringResource(Res.string.ai_glance_rules_sub), false, it,
                ) { onAsk(askRules) }
            },
        )
    }
}

/** Ikki karta yonma-yon, balandligi teng (katta şriftda ham). */
@Composable
private fun GlanceRow(start: @Composable (Modifier) -> Unit, end: @Composable (Modifier) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        start(Modifier.weight(1f).fillMaxHeight())
        end(Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun GlanceCard(
    icon: ImageVector,
    label: String,
    value: String,
    sub: String?,
    warning: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .heightIn(min = 104.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(AppColors.bg.surface)
            .clickable(interactionSource = rememberHapticSource(), indication = null, onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (warning) AppColors.icon.accentWarning else AppColors.icon.accentPrimary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(label, style = AppTypography.bodyMdMedium, color = AppColors.text.tertiary, maxLines = 2)
        }
        Spacer(Modifier.height(8.dp))
        Text(value, style = AppTypography.titleLgSemiBold, color = AppColors.text.primary)
        if (sub != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                sub,
                style = AppTypography.bodyMdRegular,
                color = if (warning) AppColors.text.accentWarning else AppColors.text.secondary,
            )
        }
    }
}

// ── "Oçiş: …" ──

internal fun navLabel(screen: NavScreen): StringResource = when (screen) {
    NavScreen.MAP -> Res.string.xarita
    NavScreen.PROTECTION -> Res.string.himoya
    NavScreen.RULES -> Res.string.cheklovlar
    NavScreen.TASKS -> Res.string.topshiriqlar
    NavScreen.ADD_CHILD -> Res.string.farzand_qo_shish
    NavScreen.SUBSCRIPTION -> Res.string.obuna
    NavScreen.COINS -> Res.string.tangachalar
    NavScreen.LIVE -> Res.string.live_watch
    NavScreen.STATS -> Res.string.statistika
    NavScreen.CHAT -> Res.string.suhbat
    NavScreen.PROFILE -> Res.string.profil
}

private fun navIcon(screen: NavScreen): ImageVector = when (screen) {
    NavScreen.MAP -> Icons.Rounded.LocationOn
    NavScreen.PROTECTION -> Icons.Rounded.VerifiedUser
    NavScreen.RULES -> Icons.AutoMirrored.Rounded.Rule
    NavScreen.TASKS -> Icons.Rounded.TaskAlt
    NavScreen.ADD_CHILD -> Icons.Rounded.PersonAdd
    NavScreen.SUBSCRIPTION -> Icons.Rounded.WorkspacePremium
    NavScreen.COINS -> Icons.Rounded.MonetizationOn
    NavScreen.LIVE -> Icons.Rounded.Videocam
    NavScreen.STATS -> Icons.Rounded.Analytics
    NavScreen.CHAT -> Icons.Rounded.ChatBubble
    NavScreen.PROFILE -> Icons.Rounded.Person
}

/** Katta tonal tugma: "Oçiş: Xarita". */
@Composable
internal fun NavCardView(screen: NavScreen, onOpen: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(AppColors.bg.surface)
            .clickable(interactionSource = rememberHapticSource(), indication = null, onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.bg.secondary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(navIcon(screen), null, tint = AppColors.icon.accentPrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            stringResource(Res.string.ai_nav_open, stringResource(navLabel(screen))),
            style = AppTypography.titleMdSemiBold,
            color = AppColors.text.accentEmphasis,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.Rounded.ChevronRight, null, tint = AppColors.icon.secondary)
    }
}
