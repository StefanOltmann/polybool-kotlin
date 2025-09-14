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
import de.stefan_oltmann.polybool.internal.LinkedList.TransitionResult
import de.stefan_oltmann.polybool.models.Segment
import de.stefan_oltmann.polybool.models.Segment.SegmentFill

internal abstract class AbstractIntersecter internal constructor(
    private val selfIntersection: Boolean,
    protected val epsilon: Epsilon
) {

    protected class IntersecterContent {
        var isStart: Boolean = false
        lateinit var point: DoubleArray
        lateinit var segment: Segment
        var primary: Boolean = false
        var other: LinkedList<IntersecterContent>? = null
        var status: LinkedList<LinkedList<IntersecterContent>?>? = null
    }

    private val eventRoot: LinkedList<IntersecterContent> = LinkedList.create<IntersecterContent>()

    protected fun segmentCopy(start: DoubleArray, end: DoubleArray, segment: Segment): Segment =
        Segment(start, end, SegmentFill(segment.myFill.above, segment.myFill.below))

    private fun eventCompare(
        p1isStart: Boolean, p1x1: DoubleArray, p1x2: DoubleArray,
        p2isStart: Boolean, p2x1: DoubleArray, p2x2: DoubleArray
    ): Int {

        /* compare the selected points first */
        val comparisonResult = this.epsilon.pointsCompare(p1x1, p2x1)

        if (comparisonResult != 0)
            return comparisonResult

        // the selected points are the same
        if (this.epsilon.pointsSame(p1x2, p2x2)) // if the non-selected points are the same too...
            return 0 // then the segments are equal

        if (p1isStart != p2isStart)  // if one is a start and the other isn't...
            return if (p1isStart) 1 else -1 // favor the one that isn't the start

        // otherwise, we'll have to calculate which one is below the other manually
        return if (
            epsilon.pointAboveOrOnLine(
                point = p1x2,
                left = if (p2isStart) p2x1 else p2x2, // order matters
                right = if (p2isStart) p2x2 else p2x1
            )
        ) 1 else -1
    }

    private fun eventAdd(
        event: LinkedList<IntersecterContent>,
        otherPt: DoubleArray
    ) {

        this.eventRoot.insertBefore(event) { here: LinkedList<IntersecterContent> ->

            // should ev be inserted before here?
            val comp = this.eventCompare(
                event.getContent().isStart,
                event.getContent().point,
                otherPt,
                here.getContent().isStart,
                here.getContent().point,
                here.getContent().other!!.getContent().point
            )

            comp < 0
        }
    }

    private fun eventAddSegmentStart(
        segment: Segment,
        primary: Boolean
    ): LinkedList<IntersecterContent> {

        val content = IntersecterContent()

        content.isStart = true
        content.point = segment.start
        content.segment = segment
        content.primary = primary

        val eventStart: LinkedList<IntersecterContent> = LinkedList.node(content)

        this.eventAdd(eventStart, segment.end)

        return eventStart
    }

    private fun eventAddSegmentEnd(
        eventStart: LinkedList<IntersecterContent>,
        segment: Segment,
        primary: Boolean
    ) {

        val content = IntersecterContent()

        content.isStart = false
        content.point = segment.end
        content.segment = segment
        content.primary = primary
        content.other = eventStart

        val eventEnd: LinkedList<IntersecterContent> = LinkedList.node(content)

        eventStart.getContent().other = eventEnd

        this.eventAdd(eventEnd, eventStart.getContent().point)
    }

    protected fun eventAddSegment(
        segment: Segment,
        primary: Boolean
    ): LinkedList<IntersecterContent> {

        val eventStart = this.eventAddSegmentStart(segment, primary)

        this.eventAddSegmentEnd(eventStart, segment, primary)

        return eventStart
    }

    private fun eventUpdateEnd(
        ev: LinkedList<IntersecterContent>,
        end: DoubleArray
    ) {

        // slides an end backwards
        //   (start)------------(end)    to:
        //   (start)---(end)

        ev.getContent().other!!.remove()
        ev.getContent().segment.end = end
        ev.getContent().other!!.getContent().point = end

        this.eventAdd(ev.getContent().other!!, ev.getContent().point)
    }

    private fun eventDivide(
        ev: LinkedList<IntersecterContent>,
        pt: DoubleArray
    ): LinkedList<IntersecterContent> {

        val ns = this.segmentCopy(
            start = pt,
            end = ev.getContent().segment.end,
            segment = ev.getContent().segment
        )

        this.eventUpdateEnd(ev, pt)

        return this.eventAddSegment(ns, ev.getContent().primary)
    }

    protected fun baseCalculate(
        primaryPolyInverted: Boolean,
        secondaryPolyInverted: Boolean
    ): List<Segment> {

        // if selfIntersection is true then there is no secondary polygon, so that isn't used

        //
        // status logic
        //

        val statusRoot: LinkedList<LinkedList<IntersecterContent>?> = LinkedList.create()

        fun statusCompare(
            ev1: LinkedList<IntersecterContent>,
            ev2: LinkedList<IntersecterContent>
        ): Int {

            val a1 = ev1.getContent().segment.start
            val a2 = ev1.getContent().segment.end
            val b1 = ev2.getContent().segment.start
            val b2 = ev2.getContent().segment.end

            if (this.epsilon.pointsCollinear(a1, b1, b2)) {

                if (this.epsilon.pointsCollinear(a2, b1, b2))
                    return 1

                return if (this.epsilon.pointAboveOrOnLine(a2, b1, b2)) 1 else -1
            }

            return if (this.epsilon.pointAboveOrOnLine(a1, b1, b2)) 1 else -1
        }

        val statusFindSurrounding: (LinkedList<IntersecterContent>?) -> TransitionResult<LinkedList<IntersecterContent>?> =
            { ev: LinkedList<IntersecterContent>? ->
                statusRoot.findTransition { here: LinkedList<LinkedList<IntersecterContent>?> ->

                    val comp: Int = statusCompare(ev!!, here.getContent()!!)

                    comp > 0
                }
            }

        fun checkIntersection(
            ev1: LinkedList<IntersecterContent>,
            ev2: LinkedList<IntersecterContent>
        ): LinkedList<IntersecterContent>? {

            // returns the segment equal to ev1, or false if nothing equal
            val firstSegment = ev1.getContent().segment
            val secondSegment = ev2.getContent().segment

            val a1 = firstSegment.start
            val a2 = firstSegment.end
            val b1 = secondSegment.start
            val b2 = secondSegment.end

            val intersectionResult =
                this.epsilon.linesIntersect(a1, a2, b1, b2)

            if (intersectionResult == null) {

                // segments are parallel or coincident
                // if points aren't collinear, then the segments are parallel, so no intersections

                if (!this.epsilon.pointsCollinear(a1, a2, b1))
                    return null

                // otherwise, segments are on top of each other somehow (aka coincident)
                if (this.epsilon.pointsSame(a1, b2) || this.epsilon.pointsSame(a2, b1))
                    return null // segments touch at endpoints... no intersection

                val a1EquB1 = this.epsilon.pointsSame(a1, b1)
                val a2EquB2 = this.epsilon.pointsSame(a2, b2)

                if (a1EquB1 && a2EquB2)
                    return ev2 // segments are exactly equal

                val a1Between = !a1EquB1 && this.epsilon.pointBetween(a1, b1, b2)
                val a2Between = !a2EquB2 && this.epsilon.pointBetween(a2, b1, b2)

                if (a1EquB1) {

                    if (a2Between) {
                        //  (a1)---(a2)
                        //  (b1)----------(b2)
                        this.eventDivide(ev2, a2)
                    } else {
                        //  (a1)----------(a2)
                        //  (b1)---(b2)
                        this.eventDivide(ev1, b2)
                    }

                    return ev2

                } else if (a1Between) {

                    if (!a2EquB2) {

                        // make a2 equal to b2
                        if (a2Between) {
                            //         (a1)---(a2)
                            //  (b1)-----------------(b2)
                            this.eventDivide(ev2, a2)
                        } else {
                            //         (a1)----------(a2)
                            //  (b1)----------(b2)
                            this.eventDivide(ev1, b2)
                        }
                    }

                    //         (a1)---(a2)
                    //  (b1)----------(b2)
                    this.eventDivide(ev2, a1)
                }

            } else {

                // otherwise, lines intersect at i.pt, which may or may not be between the endpoints

                // is A divided between its endpoints? (exclusive)

                if (intersectionResult.alongA == 0) {

                    when {
                        intersectionResult.alongB == -1  // yes, at exactly b1
                            -> this.eventDivide(ev1, b1)

                        intersectionResult.alongB == 0  // yes, somewhere between B's endpoints
                            -> this.eventDivide(ev1, intersectionResult.points)

                        intersectionResult.alongB == 1  // yes, at exactly b2
                            -> this.eventDivide(ev1, b2)
                    }
                }

                // is B divided between its endpoints? (exclusive)
                if (intersectionResult.alongB == 0) {

                    when {
                        intersectionResult.alongA == -1  // yes, at exactly a1
                            -> this.eventDivide(ev2, a1)

                        intersectionResult.alongA == 0  // yes, somewhere between A's endpoints (exclusive)
                            -> this.eventDivide(ev2, intersectionResult.points)

                        intersectionResult.alongA == 1  // yes, at exactly a2
                            -> this.eventDivide(ev2, a2)
                    }
                }
            }

            return null
        }

        //
        // main event loop
        //
        val segments: MutableList<Segment> = ArrayList()

        while (!this.eventRoot.isEmpty()) {

            val ev = this.eventRoot.getHead()

            if (ev.getContent().isStart) {

                val surrounding = statusFindSurrounding(ev)

                val above = surrounding.before?.getContent()

                val below = surrounding.after?.getContent()

                fun checkBothIntersections(): LinkedList<IntersecterContent>? {

                    if (above != null) {

                        val eve = checkIntersection(ev, above)

                        if (eve != null)
                            return eve
                    }

                    if (below != null)
                        return checkIntersection(ev, below)

                    return null
                }

                val eve = checkBothIntersections()

                if (eve != null) {

                    // ev and eve are equal
                    // we'll keep eve and throw away ev

                    // merge ev.seg's fill information into eve.seg

                    if (this.selfIntersection) {

                        val toggle: Boolean // are we a toggling edge?

                        if (ev.getContent().segment.myFill.below == null)
                            toggle = true
                        else
                            toggle =
                                ev.getContent().segment.myFill.above != ev.getContent().segment.myFill.below

                        // merge two segments that belong to the same polygon
                        // think of this as sandwiching two segments together, where `eve.seg` is
                        // the bottom -- this will cause the above fill flag to toggle
                        if (toggle)
                            eve.getContent().segment.myFill.above =
                                !(eve.getContent().segment.myFill.above ?: false)

                    } else {

                        // merge two segments that belong to different polygons
                        // each segment has distinct knowledge, so no special logic is needed
                        // note that this can only happen once per segment in this phase, because we
                        // are guaranteed that all self-intersections are gone
                        eve.getContent().segment.otherFill = ev.getContent().segment.myFill
                    }

                    ev.getContent().other!!.remove()
                    ev.remove()
                }

                if (this.eventRoot.getHead() !== ev) {
                    // something was inserted before us in the event queue, so loop back around and
                    // process it before continuing
                    continue
                }

                //
                // calculate fill flags
                //
                if (this.selfIntersection) {

                    val toggle: Boolean // are we a toggling edge?

                    if (ev.getContent().segment.myFill.below == null)  // if we are a new segment...
                        toggle = true // then we toggle
                    else  // we are a segment that has previous knowledge from a division
                        toggle =
                            ev.getContent().segment.myFill.above != ev.getContent().segment.myFill.below // calculate toggle

                    // next, calculate whether we are filled below us
                    if (below == null) { // if nothing is below us...
                        // we are filled below us if the polygon is inverted
                        ev.getContent().segment.myFill.below = primaryPolyInverted
                    } else {
                        // otherwise, we know the answer -- it's the same if whatever is below
                        // us is filled above it
                        ev.getContent().segment.myFill.below = below.getContent().segment.myFill.above
                    }

                    // since now we know if we're filled below us, we can calculate whether
                    // we're filled above us by applying toggle to whatever is below us
                    if (toggle)
                        ev.getContent().segment.myFill.above = !(ev.getContent().segment.myFill.below ?: false)
                    else
                        ev.getContent().segment.myFill.above = ev.getContent().segment.myFill.below
                } else {

                    // now we fill in any missing transition information, since we are all-knowing
                    // at this point

                    if (ev.getContent().segment.otherFill == null) {

                        // if we don't have other information, then we need to figure out if we're
                        // inside the other polygon
                        val inside: Boolean

                        if (below == null) {

                            // if nothing is below us, then we're inside if the other polygon is inverted
                            inside = if (ev.getContent().primary)
                                secondaryPolyInverted
                            else
                                primaryPolyInverted

                        } else { // otherwise, something is below us

                            // so copy the below segment's other polygon's above
                            inside = if (ev.getContent().primary == below.getContent().primary)
                                below.getContent().segment.otherFill!!.above ?: false
                            else
                                below.getContent().segment.myFill.above ?: false

                        }

                        ev.getContent().segment.otherFill = SegmentFill(inside, inside)
                    }
                }

                // insert the status and remember it for later removal
                ev.getContent().other!!.getContent().status =
                    surrounding.insert(LinkedList.node(ev))

            } else {

                val stNullable = ev.getContent().status
                    ?: error("PolyBool: Zero-length segment detected; your epsilon is probably too small or too large")

                val st: LinkedList<LinkedList<IntersecterContent>?> = stNullable

                // removing the status will create two new adjacent edges, so we'll need to check
                // for those
                val prev = st.getPrev()
                val next = st.getNext()

                if (statusRoot.exists(prev) && statusRoot.exists(next))
                    checkIntersection(prev!!.getContent()!!, next!!.getContent()!!)

                // remove the status
                st.remove()

                // if we've reached this point, we've calculated everything there is to know, so
                // save the segment for reporting
                if (!ev.getContent().primary) {

                    // make sure `seg.myFill` actually points to the primary polygon though
                    val s = ev.getContent().segment.myFill

                    ev.getContent().segment.myFill = ev.getContent().segment.otherFill!!

                    ev.getContent().segment.otherFill = s
                }

                segments.add(ev.getContent().segment)
            }

            // remove the event and continue
            this.eventRoot.getHead().remove()
        }

        return segments
    }
}
