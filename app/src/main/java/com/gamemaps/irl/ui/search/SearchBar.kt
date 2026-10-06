package com.gamemaps.irl.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.components.hudSurface
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/**
 * Barre "Où aller ?" : le menu (Paramètres) à gauche, le champ, la croix pour effacer,
 * un indicateur pendant la recherche, puis [trailing] (pastille GPS).
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    isLoading: Boolean = false,
    onSubmit: () -> Unit = {},
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .hudSurface(HudShapes.Pill)
            .padding(start = 6.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Menu,
            contentDescription = "Paramètres",
            tint = CockpitColors.Text,
            modifier = Modifier.clip(HudShapes.Pill).clickable(onClick = onMenuClick).padding(12.dp).size(24.dp),
        )
        Spacer(Modifier.width(4.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Où aller ?", style = CockpitTypography.Street, color = CockpitColors.TextMuted)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = CockpitTypography.Street.copy(color = CockpitColors.Text),
                cursorBrush = SolidColor(CockpitColors.Accent),
                // Touche "Rechercher" du clavier : il se referme pour laisser voir les résultats.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Effacer",
                tint = CockpitColors.TextMuted,
                modifier = Modifier.clip(HudShapes.Pill).clickable { onQueryChange("") }.padding(8.dp).size(20.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = CockpitColors.Accent, strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
        }
        trailing()
    }
}
