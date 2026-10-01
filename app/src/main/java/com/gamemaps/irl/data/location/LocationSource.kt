package com.gamemaps.irl.data.location

import kotlinx.coroutines.flow.Flow

/** Source brute de positions. Interface pour pouvoir la remplacer en test (ou par un GPS simulé). */
fun interface LocationSource {
    fun fixes(): Flow<GpsFix>
}
