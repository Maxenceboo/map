package com.gamemaps.irl.data.location

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

/** Passe du vrai GPS à la position de démonstration (et inversement) dès que [useDemo] change. */
class SwitchableLocationSource(
    private val real: LocationSource,
    private val demo: LocationSource,
    private val useDemo: Flow<Boolean>,
) : LocationSource {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fixes(): Flow<GpsFix> = useDemo.flatMapLatest { if (it) demo.fixes() else real.fixes() }
}
