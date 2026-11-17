package uz.tikoncha_parent.presentation.tutorial_clips

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Ovozsiz, cheksiz takrorlanadigan qisqa video (ilova ichidagi fayl yoki URL). Ekrandan çiqqanda pleyer yopiladi. */
@Composable
expect fun LoopingClip(uri: String, modifier: Modifier = Modifier)
