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

import kotlin.math.abs

public data class Epsilon(
    public val eps: Double = DEFAULT_EPS
) {

    internal fun pointAboveOrOnLine(
        point: DoubleArray,
        left: DoubleArray,
        right: DoubleArray
    ): Boolean {

        val ax = left[0]
        val ay = left[1]
        val bx = right[0]
        val by = right[1]
        val cx = point[0]
        val cy = point[1]

        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax) >= -eps
    }

    internal fun pointBetween(
        point: DoubleArray,
        left: DoubleArray,
        right: DoubleArray
    ): Boolean {

        /*
         * p must be collinear with left->right
         * returns false if p == left, p == right, or left == right
         */
        val dPyLy = point[1] - left[1]
        val dRxLx = right[0] - left[0]
        val dPxLx = point[0] - left[0]
        val dRyLy = right[1] - left[1]

        val dot = dPxLx * dRxLx + dPyLy * dRyLy

        if (dot < eps)
            return false

        val length = dRxLx * dRxLx + dRyLy * dRyLy

        return dot - length <= -eps
    }

    private fun pointsSameX(
        firstPoint: DoubleArray,
        secondPoint: DoubleArray
    ): Boolean =
        abs(firstPoint[0] - secondPoint[0]) < eps

    private fun pointsSameY(
        firstPoint: DoubleArray,
        secondPoint: DoubleArray
    ): Boolean =
        abs(firstPoint[1] - secondPoint[1]) < eps

    internal fun pointsSame(
        firstPoint: DoubleArray,
        secondPoint: DoubleArray
    ): Boolean =
        pointsSameX(firstPoint, secondPoint) && pointsSameY(firstPoint, secondPoint)

    internal fun pointsCompare(
        firstPoint: DoubleArray,
        secondPoint: DoubleArray
    ): Int {

        /* returns -1 if p1 is smaller, 1 if p2 is smaller, 0 if equal */
        if (pointsSameX(firstPoint, secondPoint))
            return if (pointsSameY(firstPoint, secondPoint))
                0
            else
                (if (firstPoint[1] < secondPoint[1]) -1 else 1)

        return if (firstPoint[0] < secondPoint[0]) -1 else 1
    }

    internal fun pointsCollinear(
        pt1: DoubleArray,
        pt2: DoubleArray,
        pt3: DoubleArray
    ): Boolean {

        /*
         * Does pt1->pt2->pt3 make a straight line?
         * Essentially, this is just checking to see if the slope(pt1->pt2) === slope(pt2->pt3)
         * If slopes are equal, then they must be collinear, because they share pt2
        */
        val dx1 = pt1[0] - pt2[0]
        val dy1 = pt1[1] - pt2[1]
        val dx2 = pt2[0] - pt3[0]
        val dy2 = pt2[1] - pt3[1]

        return abs(dx1 * dy2 - dx2 * dy1) < eps
    }

    internal fun linesIntersect(
        a0: DoubleArray,
        a1: DoubleArray,
        b0: DoubleArray,
        b1: DoubleArray
    ): EpsilonIntersectionResult? {

        /*
         returns false if the lines are coincident (e.g., parallel or on top of each other)

         returns an object if the lines intersect:
           {
             pt: [x, y],    where the intersection point is at
             alongA: where intersection point is along A,
             alongB: where intersection point is along B
           }

          alongA and alongB will each be one of: -2, -1, 0, 1, 2

          with the following meaning:

            -2   intersection point is before segment's first point
            -1   intersection point is directly on segment's first point
             0   intersection point is between segment's first and second points (exclusive)
             1   intersection point is directly on segment's second point
             2   intersection point is after segment's second point
        */
        val adx = a1[0] - a0[0]
        val ady = a1[1] - a0[1]
        val bdx = b1[0] - b0[0]
        val bdy = b1[1] - b0[1]

        val axb = adx * bdy - ady * bdx

        /* Return null when lines are coincident */
        if (abs(axb) < eps)
            return null

        val dx = a0[0] - b0[0]
        val dy = a0[1] - b0[1]

        val a = (bdx * dy - bdy * dx) / axb
        val b = (adx * dy - ady * dx) / axb

        return EpsilonIntersectionResult(
            alongA = when {
                a <= -eps -> -2
                a < eps -> -1
                a - 1 <= -eps -> 0
                a - 1 < eps -> 1
                else -> 2
            },
            alongB = when {
                b <= -eps -> -2
                b < eps -> -1
                b - 1 <= -eps -> 0
                b - 1 < eps -> 1
                else -> 2
            },
            points = doubleArrayOf(a0[0] + a * adx, a0[1] + a * ady)
        )
    }

    public companion object {

        private const val DEFAULT_EPS: Double = 1e-10

        public val default: Epsilon = Epsilon(DEFAULT_EPS)
    }
}
