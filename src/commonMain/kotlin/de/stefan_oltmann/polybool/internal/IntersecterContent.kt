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

internal class IntersecterContent {
    var isStart: Boolean = false
    lateinit var point: DoubleArray
    lateinit var segment: Segment
    var primary: Boolean = false
    var other: LinkedList<IntersecterContent>? = null
    var status: LinkedList<LinkedList<IntersecterContent>?>? = null
}
