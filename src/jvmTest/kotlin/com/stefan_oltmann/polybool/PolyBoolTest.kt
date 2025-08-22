/*
 * Copyright (c) 2025 Stefan Oltmann (@StefanOltmann)
 *
 * https://github.com/StefanOltmann/polybool-kotlin
 *
 * This file is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.stefan_oltmann.polybool

import de.stefan_oltmann.polybool.Epsilon
import de.stefan_oltmann.polybool.PolyBool
import de.stefan_oltmann.polybool.models.Polygon
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class PolyBoolTest {

    @Test
    fun testUnion() =
        runTest(
            expectedTestData = getTestData("paths_union.txt"),
            transform = { originalPolygon ->

                PolyBool.union(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )

    /*
     * FIXME Improve test data so that something actually intersets.
     */
    @Test
    fun testIntersect() =
        runTest(
            expectedTestData = getTestData("paths_intersect.txt"),
            transform = { originalPolygon ->

                PolyBool.intersect(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )

    @Test
    fun testDifference() =
        runTest(
            expectedTestData = getTestData("paths_difference.txt"),
            transform = { originalPolygon ->

                PolyBool.difference(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )

    /*
     * FIXME Improve test data so that something shows actually a difference here.
     */
    @Test
    fun testDifferenceRev() =
        runTest(
            expectedTestData = getTestData("paths_difference_rev.txt"),
            transform = { originalPolygon ->

                PolyBool.differenceRev(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )

    @Test
    fun testXor() =
        runTest(
            expectedTestData = getTestData("paths_xor.txt"),
            transform = { originalPolygon ->

                PolyBool.xor(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )

    fun runTest(
        expectedTestData: List<String>,
        transform: (Polygon) -> Polygon
    ) {

        val originalTestData = getOriginalTestData()

        assertEquals(10000, originalTestData.size)
        assertEquals(10000, expectedTestData.size)

        for ((index, line) in originalTestData.withIndex()) {

            val originalPolygon = parsePolygon(line)

            val transformedPolygon: Polygon = transform(originalPolygon)

            val actualOptimizedLine = serializePolygon(transformedPolygon)

            val expectedOptimizedLine = expectedTestData[index]

            assertEquals(
                expected = expectedOptimizedLine,
                actual = actualOptimizedLine,
                message = "Line $index does not match."
            )
        }
    }

    @Ignore("Only used to create new test data.")
    fun testCreateTestData() {

        createTestData(
            fileName = "build/paths_union.txt",
            transform = { originalPolygon ->

                PolyBool.union(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )

        createTestData(
            fileName = "build/paths_intersect.txt",
            transform = { originalPolygon ->

                PolyBool.intersect(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )

        createTestData(
            fileName = "build/paths_difference.txt",
            transform = { originalPolygon ->

                PolyBool.difference(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )

        createTestData(
            fileName = "build/paths_difference_rev.txt",
            transform = { originalPolygon ->

                PolyBool.differenceRev(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )

        createTestData(
            fileName = "build/paths_xor.txt",
            transform = { originalPolygon ->

                PolyBool.xor(
                    Epsilon(),
                    originalPolygon,
                    Polygon()
                )
            }
        )
    }

    private fun createTestData(
        fileName: String,
        transform: (Polygon) -> Polygon
    ) {

        val originalTestData = getOriginalTestData()

        assertEquals(10000, originalTestData.size)

        val lines = mutableListOf<String>()

        for (line in originalTestData) {

            val originalPolygon = parsePolygon(line)

            val transformedPolygon: Polygon = transform(originalPolygon)

            lines.add(serializePolygon(transformedPolygon))
        }

        val outFile = File(fileName)
        outFile.parentFile?.mkdirs()
        outFile.writeText(lines.joinToString("\n"))
    }

    private fun serializePolygon(polygon: Polygon): String =
        buildString {

            var firstEntry = true

            for (points in polygon.regions) {

                if (!firstEntry)
                    append(';')

                firstEntry = false

                for ((pointIndex, point) in points.withIndex()) {

                    if (pointIndex > 0)
                        append(" ")

                    append("${point[0].toInt()},${point[1].toInt()}")
                }
            }
        }

    private fun parsePolygon(line: String): Polygon {

        val pointsLists = line.split(';')

        val regionPointsList = mutableListOf<List<DoubleArray>>()

        for (pointsString in pointsLists) {

            val regionPoints = mutableListOf<DoubleArray>()

            for (pair in pointsString.split(' ')) {

                val pairSplit = pair.split(',')

                val x = pairSplit[0].toInt()
                val y = pairSplit[1].toInt()

                regionPoints.add(arrayOf(x.toDouble(), y.toDouble()).toDoubleArray())
            }

            regionPointsList.add(regionPoints)
        }

        return Polygon(regionPointsList)
    }

    private fun getOriginalTestData(): List<String> =
        getTestData("paths_original.txt")

    private fun getTestData(fileName: String): List<String> =
        PolyBoolTest::class.java.getResourceAsStream(fileName)!!
            .readBytes().decodeToString().split("\n")

}
