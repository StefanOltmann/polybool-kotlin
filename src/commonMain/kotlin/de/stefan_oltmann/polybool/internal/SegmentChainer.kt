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

internal object SegmentChainer {

    private data class SegmentChainerMatch(
        var index: Int = 0,
        var matchesHead: Boolean = false,
        var matchesPt1: Boolean = false
    )

    fun chain(
        segments: List<Segment>,
        epsilon: Epsilon
    ): List<List<DoubleArray>> {

        val chains = mutableListOf<MutableList<DoubleArray>>()
        val regions = mutableListOf<MutableList<DoubleArray>>()

        for (segment in segments) {

            val pt1 = segment.start
            val pt2 = segment.end

            if (epsilon.pointsSame(pt1, pt2))
                error("PolyBool: Warning: Zero-length segment detected; your epsilon is probably too small or too large")

            val firstMatch = SegmentChainerMatch()
            val secondMatch = SegmentChainerMatch()

            var nextMatch: SegmentChainerMatch? = firstMatch

            fun setMatch(
                index: Int,
                matchesHead: Boolean,
                matchesPt1: Boolean
            ): Boolean {

                // return true if we've matched twice
                val nm = nextMatch ?: return true

                nm.index = index
                nm.matchesHead = matchesHead
                nm.matchesPt1 = matchesPt1

                nextMatch = if (nm === firstMatch)
                    secondMatch
                else
                    null

                return nextMatch == null // we've matched twice, we're done here
            }

            for (index in 0 until chains.size) {

                val chain = chains[index]
                val head = chain[0]
                val tail = chain[chain.size - 1]

                when {

                    epsilon.pointsSame(head, pt1) ->
                        if (setMatch(index, true, true))
                            break

                    epsilon.pointsSame(head, pt2) ->
                        if (setMatch(index, true, false))
                            break

                    epsilon.pointsSame(tail, pt1) ->
                        if (setMatch(index, false, true))
                            break

                    epsilon.pointsSame(tail, pt2) ->
                        if (setMatch(index, false, false))
                            break
                }
            }

            if (nextMatch === firstMatch) {

                val newChain = mutableListOf(pt1, pt2)

                // we didn't match anything, so create a new chain
                chains.add(newChain)

                continue
            }

            if (nextMatch === secondMatch) {

                // we matched a single chain
                // add the other point to the appropriate end, and check to see if we've closed the chain into a loop

                val index = firstMatch.index
                val pt = if (firstMatch.matchesPt1) pt2 else pt1 // if we matched pt1, then we add pt2, etc
                val addToHead = firstMatch.matchesHead // if we matched at head, then add to the head

                val chain = chains[index]
                var grow = if (addToHead) chain[0] else chain[chain.size - 1]
                val grow2 = if (addToHead) chain[1] else chain[chain.size - 2]
                val oppo = if (addToHead) chain[chain.size - 1] else chain[0]
                val oppo2 = if (addToHead) chain[chain.size - 2] else chain[1]

                if (epsilon.pointsCollinear(grow2, grow, pt)) {
                    // grow isn't needed because it's directly between grow2 and pt:
                    // grow2 ---grow---> pt
                    if (addToHead) {
                        chain.removeAt(0)
                    } else {
                        chain.removeAt(chain.size - 1)
                    }
                    grow = grow2 // old grow is gone... new grow is what grow2 was
                }

                if (epsilon.pointsSame(oppo, pt)) {

                    // we're closing the loop, so remove chain from chains
                    chains.removeAt(index)

                    if (epsilon.pointsCollinear(oppo2, oppo, grow)) {

                        // oppo isn't needed because it's directly between oppo2 and grow:
                        // oppo2 ---oppo--->grow
                        if (addToHead)
                            chain.removeAt(chain.size - 1)
                        else
                            chain.removeAt(0)
                    }

                    // we have a closed chain!
                    regions.add(chain)

                    continue
                }

                // not closing a loop, so just add it to the appropriate side
                if (addToHead)
                    chain.add(0, pt)
                else
                    chain.add(pt)

                continue
            }

            // otherwise, we matched two chains, so we need to combine those chains together

            fun reverseChain(index: Int) {
                chains[index].reverse()
            }

            fun appendChain(index1: Int, index2: Int) {

                // index1 gets index2 appended to it, and index2 is removed
                val chain1 = chains[index1]
                val chain2 = chains[index2]
                var tail = chain1[chain1.size - 1]
                val tail2 = chain1[chain1.size - 2]
                val head = chain2[0]
                val head2 = chain2[1]

                if (epsilon.pointsCollinear(tail2, tail, head)) {
                    // tail isn't needed because it's directly between tail2 and head
                    // tail2 ---tail---> head
                    chain1.removeAt(chain1.size - 1)
                    tail = tail2 // old tail is gone... new tail is what tail2 was
                }

                if (epsilon.pointsCollinear(tail, head, head2)) {
                    // head isn't needed because it's directly between tail and head2
                    // tail ---head---> head2
                    chain2.removeAt(0)
                }

                val concatenated = ArrayList<DoubleArray>(chain1.size + chain2.size)

                concatenated.addAll(chain1)
                concatenated.addAll(chain2)
                chains[index1] = concatenated
                chains.removeAt(index2)
            }

            val firstMatchIndex = firstMatch.index
            val secondMatchIndex = secondMatch.index

            val reverseF =
                chains[firstMatchIndex].size < chains[secondMatchIndex].size // reverse the shorter chain, if needed

            if (firstMatch.matchesHead) {

                if (secondMatch.matchesHead) {

                    if (reverseF) {

                        // <<<< F <<<< --- >>>> S >>>>
                        reverseChain(firstMatchIndex)

                        // >>>> F >>>> --- >>>> S >>>>
                        appendChain(firstMatchIndex, secondMatchIndex)

                    } else {

                        // <<<< F <<<< --- >>>> S >>>>
                        reverseChain(secondMatchIndex)

                        // <<<< F <<<< --- <<<< S <<<<   logically same as:
                        // >>>> S >>>> --- >>>> F >>>>
                        appendChain(secondMatchIndex, firstMatchIndex)
                    }

                } else {

                    // <<<< F <<<< --- <<<< S <<<<   logically same as:
                    // >>>> S >>>> --- >>>> F >>>>
                    appendChain(secondMatchIndex, firstMatchIndex)
                }

            } else {

                if (secondMatch.matchesHead) {

                    // >>>> F >>>> --- >>>> S >>>>
                    appendChain(firstMatchIndex, secondMatchIndex)

                } else {

                    if (reverseF) {

                        // >>>> F >>>> --- <<<< S <<<<
                        reverseChain(firstMatchIndex)

                        // <<<< F <<<< --- <<<< S <<<<   logically same as:
                        // >>>> S >>>> --- >>>> F >>>>
                        appendChain(secondMatchIndex, firstMatchIndex)

                    } else {

                        // >>>> F >>>> --- <<<< S <<<<
                        reverseChain(secondMatchIndex)

                        // >>>> F >>>> --- >>>> S >>>>
                        appendChain(firstMatchIndex, secondMatchIndex)
                    }
                }
            }
        }

        return regions
    }
}
