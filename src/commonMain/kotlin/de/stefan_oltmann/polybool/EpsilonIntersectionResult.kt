/*
 * Copyright (c) 2016 Sean Connelly (@velipso)
 * Copyright (c) 2021 Davide Menegatti (@menecats)
 * Copyright (c) 2025 Stefan Oltmann (@StefanOltmann)
 *
 * https://github.com/StefanOltmann/polybool-kotlin
 *
 * This file is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package de.stefan_oltmann.polybool

internal data class EpsilonIntersectionResult(
    val alongA: Int,
    val alongB: Int,
    val points: DoubleArray
) {

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (other == null || this::class != other::class)
            return false

        other as EpsilonIntersectionResult

        if (alongA != other.alongA)
            return false

        if (alongB != other.alongB)
            return false

        if (!points.contentEquals(other.points))
            return false

        return true
    }

    override fun hashCode(): Int {

        var result = alongA
        result = 31 * result + alongB
        result = 31 * result + points.contentHashCode()
        return result
    }
}
