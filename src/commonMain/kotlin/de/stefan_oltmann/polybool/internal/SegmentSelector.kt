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

    private fun select(segments: List<Segment>, selection: IntArray): List<Segment> {

        val result: MutableList<Segment> = ArrayList<Segment>()

        for (seg in segments) {
            val index =
                (if (seg.myFill!!.above == true) 8 else 0) +
                    (if (seg.myFill!!.below == true) 4 else 0) +
                    (if (seg.otherFill != null && seg.otherFill!!.above == true) 2 else 0) +
                    (if (seg.otherFill != null && seg.otherFill!!.below == true) 1 else 0)

            if (selection[index] != 0) {
                /* copy the segment to the results, while also calculating the fill status */
                result.add(
                    Segment(
                        seg.start,
                        seg.end,
                        SegmentFill(
                            selection[index] == 1,
                            selection[index] == 2
                        )
                    )
                )
            }
        }

        return result
    }


    fun union(segments: List<Segment>): List<Segment> {
        /*
         above1 below1 above2 below2    Keep?               Value
            0      0      0      0   =>   no                  0
            0      0      0      1   =>   yes filled below    2
            0      0      1      0   =>   yes filled above    1
            0      0      1      1   =>   no                  0
            0      1      0      0   =>   yes filled below    2
            0      1      0      1   =>   yes filled below    2
            0      1      1      0   =>   no                  0
            0      1      1      1   =>   no                  0
            1      0      0      0   =>   yes filled above    1
            1      0      0      1   =>   no                  0
            1      0      1      0   =>   yes filled above    1
            1      0      1      1   =>   no                  0
            1      1      0      0   =>   no                  0
            1      1      0      1   =>   no                  0
            1      1      1      0   =>   no                  0
            1      1      1      1   =>   no                  0
        */
        return select(
            segments, intArrayOf(
                0, 2, 1, 0,
                2, 2, 0, 0,
                1, 0, 1, 0,
                0, 0, 0, 0
            )
        )
    }


    fun intersect(segments: List<Segment>): List<Segment> {
        /*
         above1 below1 above2 below2    Keep?               Value
            0      0      0      0   =>   no                  0
            0      0      0      1   =>   no                  0
            0      0      1      0   =>   no                  0
            0      0      1      1   =>   no                  0
            0      1      0      0   =>   no                  0
            0      1      0      1   =>   yes filled below    2
            0      1      1      0   =>   no                  0
            0      1      1      1   =>   yes filled below    2
            1      0      0      0   =>   no                  0
            1      0      0      1   =>   no                  0
            1      0      1      0   =>   yes filled above    1
            1      0      1      1   =>   yes filled above    1
            1      1      0      0   =>   no                  0
            1      1      0      1   =>   yes filled below    2
            1      1      1      0   =>   yes filled above    1
            1      1      1      1   =>   no                  0
        */
        return select(
            segments, intArrayOf(
                0, 0, 0, 0,
                0, 2, 0, 2,
                0, 0, 1, 1,
                0, 2, 1, 0
            )
        )
    }


    fun difference(segments: List<Segment>): List<Segment> { /* primary - secondary */
        // above1 below1 above2 below2    Keep?               Value
        //    0      0      0      0   =>   no                  0
        //    0      0      0      1   =>   no                  0
        //    0      0      1      0   =>   no                  0
        //    0      0      1      1   =>   no                  0
        //    0      1      0      0   =>   yes filled below    2
        //    0      1      0      1   =>   no                  0
        //    0      1      1      0   =>   yes filled below    2
        //    0      1      1      1   =>   no                  0
        //    1      0      0      0   =>   yes filled above    1
        //    1      0      0      1   =>   yes filled above    1
        //    1      0      1      0   =>   no                  0
        //    1      0      1      1   =>   no                  0
        //    1      1      0      0   =>   no                  0
        //    1      1      0      1   =>   yes filled above    1
        //    1      1      1      0   =>   yes filled below    2
        //    1      1      1      1   =>   no                  0
        return select(
            segments, intArrayOf(
                0, 0, 0, 0,
                2, 0, 2, 0,
                1, 1, 0, 0,
                0, 1, 2, 0
            )
        )
    }


    fun differenceRev(segments: List<Segment>): List<Segment> { // secondary - primary
        // above1 below1 above2 below2    Keep?               Value
        //    0      0      0      0   =>   no                  0
        //    0      0      0      1   =>   yes filled below    2
        //    0      0      1      0   =>   yes filled above    1
        //    0      0      1      1   =>   no                  0
        //    0      1      0      0   =>   no                  0
        //    0      1      0      1   =>   no                  0
        //    0      1      1      0   =>   yes filled above    1
        //    0      1      1      1   =>   yes filled above    1
        //    1      0      0      0   =>   no                  0
        //    1      0      0      1   =>   yes filled below    2
        //    1      0      1      0   =>   no                  0
        //    1      0      1      1   =>   yes filled below    2
        //    1      1      0      0   =>   no                  0
        //    1      1      0      1   =>   no                  0
        //    1      1      1      0   =>   no                  0
        //    1      1      1      1   =>   no                  0
        return select(
            segments, intArrayOf(
                0, 2, 1, 0,
                0, 0, 1, 1,
                0, 2, 0, 2,
                0, 0, 0, 0
            )
        )
    }


    fun xor(segments: List<Segment>): List<Segment> { // primary ^ secondary
        // above1 below1 above2 below2    Keep?               Value
        //    0      0      0      0   =>   no                  0
        //    0      0      0      1   =>   yes filled below    2
        //    0      0      1      0   =>   yes filled above    1
        //    0      0      1      1   =>   no                  0
        //    0      1      0      0   =>   yes filled below    2
        //    0      1      0      1   =>   no                  0
        //    0      1      1      0   =>   no                  0
        //    0      1      1      1   =>   yes filled above    1
        //    1      0      0      0   =>   yes filled above    1
        //    1      0      0      1   =>   no                  0
        //    1      0      1      0   =>   no                  0
        //    1      0      1      1   =>   yes filled below    2
        //    1      1      0      0   =>   no                  0
        //    1      1      0      1   =>   yes filled above    1
        //    1      1      1      0   =>   yes filled below    2
        //    1      1      1      1   =>   no                  0
        return select(
            segments, intArrayOf(
                0, 2, 1, 0,
                2, 0, 0, 1,
                1, 0, 0, 2,
                0, 1, 2, 0
            )
        )
    }
}
