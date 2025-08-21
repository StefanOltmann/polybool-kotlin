/*
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

internal class NonSelfIntersecter(
    epsilon: Epsilon
) : AbstractIntersecter(
    selfIntersection = false,
    epsilon = epsilon
) {

    fun calculate(
        segments1: List<Segment>,
        inverted1: Boolean,
        segments2: List<Segment>,
        inverted2: Boolean
    ): List<Segment> {

        /*
         * segmentsX come from the self-intersection API, or this API
         * invertedX is whether we treat that list of segments as an inverted polygon or not
         * returns segments that can be used for further operations
         */

        for (segment in segments1)
            this.eventAddSegment(this.segmentCopy(segment.start, segment.end, segment), true)

        for (segment in segments2)
            this.eventAddSegment(this.segmentCopy(segment.start, segment.end, segment), false)

        return this.baseCalculate(inverted1, inverted2)
    }
}
