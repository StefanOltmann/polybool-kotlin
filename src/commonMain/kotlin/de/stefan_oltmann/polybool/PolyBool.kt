/*
 * Copyright (c) 2021 Davide Menegatti (@menecats)
 * Copyright (c) 2025 Stefan Oltmann (@StefanOltmann)
 *
 * https://github.com/StefanOltmann/polybool-kotlin
 *
 * This file is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package de.stefan_oltmann.polybool

import de.stefan_oltmann.polybool.internal.NonSelfIntersecter
import de.stefan_oltmann.polybool.internal.SegmentChainer
import de.stefan_oltmann.polybool.internal.SegmentSelector
import de.stefan_oltmann.polybool.internal.SelfIntersecter
import de.stefan_oltmann.polybool.models.Polygon
import de.stefan_oltmann.polybool.models.Segment

public object PolyBool {

    private data class Segments(
        val segments: List<Segment>,
        val inverted: Boolean
    )

    private data class Combined(
        val combined: List<Segment>,
        val inverted1: Boolean,
        val inverted2: Boolean
    )

    /* Core API */

    private fun segments(
        epsilon: Epsilon,
        polygon: Polygon
    ): Segments {

        val selfIntersecter = SelfIntersecter(epsilon)

        for (region in polygon.regions)
            selfIntersecter.addRegion(region)

        return Segments(
            segments = selfIntersecter.calculate(polygon.isInverted),
            inverted = polygon.isInverted
        )
    }

    private fun combine(
        epsilon: Epsilon,
        firstPolygonSegments: Segments,
        secondPolygonSegments: Segments
    ): Combined =
        Combined(
            combined = NonSelfIntersecter(epsilon).calculate(
                segments1 = firstPolygonSegments.segments,
                inverted1 = firstPolygonSegments.inverted,
                segments2 = secondPolygonSegments.segments,
                inverted2 = secondPolygonSegments.inverted
            ),
            inverted1 = firstPolygonSegments.inverted,
            inverted2 = secondPolygonSegments.inverted
        )

    private fun selectUnion(combined: Combined): Segments =
        Segments(
            segments = SegmentSelector.union(segments = combined.combined),
            inverted = combined.inverted1 || combined.inverted2
        )

    private fun selectIntersect(combined: Combined): Segments =
        Segments(
            segments = SegmentSelector.intersect(segments = combined.combined),
            inverted = combined.inverted1 && combined.inverted2
        )

    private fun selectDifference(combined: Combined): Segments =
        Segments(
            segments = SegmentSelector.difference(segments = combined.combined),
            inverted = combined.inverted1 && !combined.inverted2
        )

    private fun selectDifferenceRev(combined: Combined): Segments =
        Segments(
            segments = SegmentSelector.differenceRev(segments = combined.combined),
            inverted = !combined.inverted1 && combined.inverted2
        )

    private fun selectXor(combined: Combined): Segments =
        Segments(
            segments = SegmentSelector.xor(segments = combined.combined),
            inverted = combined.inverted1 != combined.inverted2
        )

    private fun polygon(epsilon: Epsilon, segments: Segments): Polygon =
        Polygon(
            regions = SegmentChainer.chain(segments.segments, epsilon),
            isInverted = segments.inverted
        )

    /* Public API */

    private fun operate(
        epsilon: Epsilon,
        firstPolygon: Polygon,
        secondPolygon: Polygon,
        selector: (Combined) -> Segments
    ): Polygon {

        val firstPolygonSegments = segments(epsilon, firstPolygon)
        val secondPolygonSegments = segments(epsilon, secondPolygon)

        val combined = combine(epsilon, firstPolygonSegments, secondPolygonSegments)

        val combinedSegments = selector(combined)

        return polygon(epsilon, combinedSegments)
    }

    public fun union(epsilon: Epsilon, firstPolygon: Polygon, secondPolygon: Polygon): Polygon =
        operate(epsilon, firstPolygon, secondPolygon, selector = ::selectUnion)

    public fun intersect(epsilon: Epsilon, firstPolygon: Polygon, secondPolygon: Polygon): Polygon =
        operate(epsilon, firstPolygon, secondPolygon, selector = ::selectIntersect)

    public fun difference(epsilon: Epsilon, firstPolygon: Polygon, secondPolygon: Polygon): Polygon =
        operate(epsilon, firstPolygon, secondPolygon, selector = ::selectDifference)

    public fun differenceRev(epsilon: Epsilon, firstPolygon: Polygon, secondPolygon: Polygon): Polygon =
        operate(epsilon, firstPolygon, secondPolygon, selector = ::selectDifferenceRev)

    public fun xor(epsilon: Epsilon, firstPolygon: Polygon, secondPolygon: Polygon): Polygon =
        operate(epsilon, firstPolygon, secondPolygon, selector = ::selectXor)

}
