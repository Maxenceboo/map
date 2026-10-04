package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Panneau européen de limitation : fond blanc, bordure rouge, chiffre noir.
 * [estimated] : la limitation est déduite du type de route ; la bordure est grise et le chiffre précédé de "~".
 */
@Composable
fun SpeedLimitSign(limitKmh: Int, modifier: Modifier = Modifier, estimated: Boolean = false) {
    Box(
        modifier = modifier
            .size(46.dp)
            .shadow(4.dp, CircleShape)
            .background(Color.White, CircleShape)
            .border(5.dp, if (estimated) SIGN_GREY else SIGN_RED, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (estimated) "~$limitKmh" else limitKmh.toString(),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = if (limitKmh >= 100 || estimated) 13.sp else 17.sp,
        )
    }
}

/** Rouge des panneaux routiers. */
private val SIGN_RED = Color(0xFFD9121A)

/** Bordure d'une limitation estimée. */
private val SIGN_GREY = Color(0xFF8A929C)
