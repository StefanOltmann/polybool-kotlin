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
package de.stefan_oltmann.polybool.models

internal class Segment(
    var start: DoubleArray,
    var end: DoubleArray,
    var myFill: SegmentFill = SegmentFill()
) {

    var otherFill: SegmentFill? = null

    class SegmentFill(
        var above: Boolean? = null,
        var below: Boolean? = null
    )
}
