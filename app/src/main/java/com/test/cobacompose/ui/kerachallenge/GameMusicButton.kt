package com.test.cobacompose.ui.kerachallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.test.cobacompose.R
import com.test.cobacompose.data.settings.AppSettingsRepository
import com.test.cobacompose.ui.sound.GameMusicEffect

/**
 * Memutar musik latar game dan menampilkan tombol bulat kecil di kanan atas untuk menyalakan/mematikannya.
 * Gayanya sama dengan [KeraChallengeBackButton] (sejajar dengannya), hanya lebih kecil.
 * Pilihan disimpan di [AppSettingsRepository], jadi berlaku untuk Susun Game dan TTS sekaligus.
 */
@Composable
internal fun GameMusicButton(
    modifier: Modifier = Modifier,
    style: KeraBackButtonStyle = KeraBackButtonStyle.PURPLE
) {
    GameMusicEffect()

    val settings = AppSettingsRepository.getInstance(LocalContext.current)
    val enabled by settings.gameMusic.collectAsState()

    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(end = 16.dp, top = 16.dp)
            .size(40.dp)
            .shadow(elevation = 5.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(style.light, style.base, style.dark)))
            .border(1.5.dp, style.dark.copy(alpha = 0.5f), CircleShape)
            .toggleable(value = enabled, role = Role.Switch, onValueChange = settings::setGameMusic),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(if (enabled) R.drawable.ic_music_note else R.drawable.ic_music_off),
            contentDescription = stringResource(if (enabled) R.string.kc_music_off else R.string.kc_music_on),
            tint = style.arrow,
            modifier = Modifier.size(22.dp)
        )
    }
}
