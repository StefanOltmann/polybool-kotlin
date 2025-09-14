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
package de.stefan_oltmann.polybool.internal

import de.stefan_oltmann.polybool.Epsilon
import de.stefan_oltmann.polybool.models.Segment

internal class SelfIntersecter(epsilon: Epsilon) : AbstractIntersecter(
    selfIntersection = true,
    epsilon = epsilon
) {

    fun addRegion(region: List<DoubleArray>) {

        /*
         * Regions are a list of points:
         * [ [0, 0], [100, 0], [50, 100] ]
         * you can add multiple regions before calculating
         */
        var firstPoint: DoubleArray
        var secondPoint = region[region.size - 1]

        for (point in region) {

            firstPoint = secondPoint
            secondPoint = point

            val forward = this.epsilon.pointsCompare(firstPoint, secondPoint)

            if (forward == 0) // points are equal, so we have a zero-length segment
                continue // just skip it

            this.eventAddSegment(
                Segment(
                    start = if (forward < 0) firstPoint else secondPoint,
                    end = if (forward < 0) secondPoint else firstPoint
                ),
                primary = true
            )
        }
    }

    fun calculate(inverted: Boolean): List<Segment> =
        this.baseCalculate(
            primaryPolyInverted = inverted,
            secondaryPolyInverted = false
        )
}
