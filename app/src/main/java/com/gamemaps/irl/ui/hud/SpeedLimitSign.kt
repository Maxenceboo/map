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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamemaps.irl.ui.theme.CockpitColors

/** Panneau européen de limitation : fond blanc, bordure rouge, chiffre noir. */
@Composable
fun SpeedLimitSign(limitKmh: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(Color.White, CircleShape)
            .border(5.dp, CockpitColors.Danger, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = limitKmh.toString(),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = if (limitKmh >= 100) 15.sp else 18.sp,
        )
    }
}
