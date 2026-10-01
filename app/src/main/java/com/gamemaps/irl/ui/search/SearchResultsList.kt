package com.gamemaps.irl.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Liste verticale des résultats, séparateurs fins, flèche "›" à droite (règle §2.3). */
@Composable
fun SearchResultsList(results: List<Place>, onSelect: (Place) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .background(CockpitColors.Panel, shape)
            .border(1.dp, CockpitColors.Border, shape),
    ) {
        items(results, key = { it.id }) { place ->
            PlaceRow(place, onClick = { onSelect(place) })
            HorizontalDivider(color = CockpitColors.Border, thickness = 1.dp)
        }
    }
}

@Composable
private fun PlaceRow(place: Place, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(place.name, style = CockpitTypography.Street, color = CockpitColors.Text)
            if (place.subtitle.isNotBlank()) {
                Text(place.subtitle, style = CockpitTypography.Caption, color = CockpitColors.TextMuted)
            }
        }
        Text("›", style = CockpitTypography.Street, color = CockpitColors.TextMuted)
    }
}
