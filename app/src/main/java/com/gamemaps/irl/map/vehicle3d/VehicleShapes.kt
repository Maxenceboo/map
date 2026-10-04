package com.gamemaps.irl.map.vehicle3d

/** Formes de base pour assembler un véhicule (équivalent des Box / primitives Three.js de la version WebGL). */
object VehicleShapes {

    /** Boîte centrée en ([centerX], [centerZ]), de [width] (gauche-droite) sur [length] (avant-arrière). */
    fun box(centerX: Double, centerZ: Double, width: Double, length: Double, base: Double, top: Double, role: PartRole): VehiclePart {
        val halfW = width / 2
        val halfL = length / 2
        return VehiclePart(
            footprint = listOf(
                GroundPoint(centerX - halfW, centerZ - halfL),
                GroundPoint(centerX + halfW, centerZ - halfL),
                GroundPoint(centerX + halfW, centerZ + halfL),
                GroundPoint(centerX - halfW, centerZ + halfL),
            ),
            baseMeters = base,
            topMeters = top,
            role = role,
        )
    }

    /**
     * Carrosserie à nez effilé : rectangle de [width] dont l'avant se resserre à [noseWidth]
     * sur les [noseLength] derniers mètres. S'étend de [rearZ] à [frontZ].
     */
    fun taperedBody(width: Double, noseWidth: Double, rearZ: Double, frontZ: Double, noseLength: Double, base: Double, top: Double, role: PartRole): VehiclePart {
        val halfW = width / 2
        val halfNose = noseWidth / 2
        return VehiclePart(
            footprint = listOf(
                GroundPoint(-halfW, rearZ),
                GroundPoint(halfW, rearZ),
                GroundPoint(halfW, frontZ - noseLength),
                GroundPoint(halfNose, frontZ),
                GroundPoint(-halfNose, frontZ),
                GroundPoint(-halfW, frontZ - noseLength),
            ),
            baseMeters = base,
            topMeters = top,
            role = role,
        )
    }

    /** Les mêmes pièces à gauche et à droite : [build] reçoit +1 (droite) puis -1 (gauche). */
    fun mirrored(build: (side: Double) -> VehiclePart): List<VehiclePart> = listOf(build(1.0), build(-1.0))

    /** Quatre roues : deux essieux en [frontZ] et [rearZ], écartées de [trackWidth]. */
    fun wheels(trackWidth: Double, frontZ: Double, rearZ: Double, tireWidth: Double, diameter: Double): List<VehiclePart> =
        listOf(frontZ, rearZ).flatMap { axleZ ->
            mirrored { side -> box(side * trackWidth / 2, axleZ, tireWidth, diameter, 0.0, diameter, PartRole.TIRE) }
        }
}
