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

import de.stefan_oltmann.polybool.models.Segment
import de.stefan_oltmann.polybool.models.Segment.SegmentFill

internal object SegmentSelector {

    private val unionSelection = intArrayOf(
        0, 2, 1, 0,
        2, 2, 0, 0,
        1, 0, 1, 0,
        0, 0, 0, 0
    )

    private val intersectSelection = intArrayOf(
        0, 0, 0, 0,
        0, 2, 0, 2,
        0, 0, 1, 1,
        0, 2, 1, 0
    )

    private val differenceSelection = intArrayOf(
        0, 0, 0, 0,
        2, 0, 2, 0,
        1, 1, 0, 0,
        0, 1, 2, 0
    )

    private val differenceRevSelection = intArrayOf(
        0, 2, 1, 0,
        0, 0, 1, 1,
        0, 2, 0, 2,
        0, 0, 0, 0
    )

    private val xorSelection = intArrayOf(
        0, 2, 1, 0,
        2, 0, 0, 1,
        1, 0, 0, 2,
        0, 1, 2, 0
    )

    fun union(segments: List<Segment>): List<Segment> =
        select(segments, unionSelection)

    fun intersect(segments: List<Segment>): List<Segment> =
        select(segments, intersectSelection)

    fun difference(segments: List<Segment>): List<Segment> =
        select(segments, differenceSelection)

    fun differenceRev(segments: List<Segment>): List<Segment> =
        select(segments, differenceRevSelection)

    fun xor(segments: List<Segment>): List<Segment> =
        select(segments, xorSelection)

    private fun select(
        segments: List<Segment>,
        selection: IntArray
    ): List<Segment> {

        val result: MutableList<Segment> = ArrayList<Segment>(segments.size)

        for (segment in segments) {

            val otherFill = segment.otherFill

            val index =
                (if (segment.myFill.above == true) 8 else 0) +
                    (if (segment.myFill.below == true) 4 else 0) +
                    (if (otherFill != null && otherFill.above == true) 2 else 0) +
                    (if (otherFill != null && otherFill.below == true) 1 else 0)

            if (selection[index] != 0) {

                /* Copy the segment to the results, while also calculating the fill status */
                result.add(
                    Segment(
                        start = segment.start,
                        end = segment.end,
                        myFill = SegmentFill(
                            above = selection[index] == 1,
                            below = selection[index] == 2
                        )
                    )
                )
            }
        }

        return result
    }
}
