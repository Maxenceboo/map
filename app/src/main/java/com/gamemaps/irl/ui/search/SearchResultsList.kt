package com.gamemaps.irl.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.ui.components.HudShapes
import com.gamemaps.irl.ui.components.hudSurface
import com.gamemaps.irl.ui.theme.CockpitColors
import com.gamemaps.irl.ui.theme.CockpitTypography

/** Liste verticale des résultats (règle §2.3) : repère, nom, adresse, distance à droite. */
@Composable
fun SearchResultsList(results: List<Place>, near: LatLng?, onSelect: (Place) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
            .hudSurface(HudShapes.Card),
    ) {
        itemsIndexed(results, key = { _, place -> place.id }) { index, place ->
            if (index > 0) HorizontalDivider(color = CockpitColors.Border, thickness = 1.dp, modifier = Modifier.padding(start = 66.dp))
            PlaceRow(place, near, onClick = { onSelect(place) })
        }
    }
}

@Composable
private fun PlaceRow(place: Place, near: LatLng?, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(38.dp).background(CockpitColors.PanelRaised, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = CockpitColors.Accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(place.name, style = CockpitTypography.Street, color = CockpitColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (place.subtitle.isNotBlank()) {
                Text(place.subtitle, style = CockpitTypography.Caption, color = CockpitColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (near != null) {
            Text(
                DistanceFormatter.format(GeoMath.distanceMeters(near, place.position)),
                style = CockpitTypography.Caption,
                color = CockpitColors.TextMuted,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}
