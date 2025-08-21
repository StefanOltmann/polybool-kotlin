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

internal class LinkedList<T> private constructor(
    private val root: Boolean,
    private val content: T?
) {

    class TransitionResult<T>(
        val before: LinkedList<T>?,
        val after: LinkedList<T>?,
        val insert: (LinkedList<T>) -> LinkedList<T>
    )

    private var prev: LinkedList<T>? = null
    private var next: LinkedList<T>? = null

    fun exists(node: LinkedList<T>?): Boolean =
        node != null && node !== this

    fun isEmpty(): Boolean =
        this.next == null

    fun getHead(): LinkedList<T> =
        this.next!!

    fun getPrev(): LinkedList<T>? =
        prev

    fun getNext(): LinkedList<T>? =
        next

    fun insertBefore(node: LinkedList<T>, check: (LinkedList<T>) -> Boolean) {

        var last: LinkedList<T> = this

        var here: LinkedList<T>? = this.next

        while (here != null && !here.root) {

            if (check(here)) {

                node.prev = here.prev
                node.next = here

                if (here.prev != null)
                    here.prev!!.next = node

                here.prev = node

                return
            }

            last = here
            here = here.next
        }

        last.next = node
        node.prev = last
        node.next = null
    }

    fun findTransition(check: (LinkedList<T>) -> Boolean): TransitionResult<T> {

        var prev: LinkedList<T> = this
        var here: LinkedList<T>? = this.next

        while (here != null) {

            if (check(here))
                break

            prev = here
            here = here.next
        }

        val finalPrev = prev
        val finalHere = here

        return TransitionResult(
            before = if (prev === this) null else prev,
            after = here,
            insert = { node: LinkedList<T> ->

                node.prev = finalPrev
                node.next = finalHere

                finalPrev.next = node

                if (finalHere != null)
                    finalHere.prev = node

                node
            }
        )
    }

    fun remove() {

        if (this.root)
            return

        if (this.prev != null)
            this.prev!!.next = this.next

        if (this.next != null)
            this.next!!.prev = this.prev

        this.prev = null
        this.next = null
    }

    fun getContent(): T {
        return requireNotNull(content) { "Should not be called on empty list." }
    }

    companion object {

        fun <T> create(): LinkedList<T> = LinkedList(true, null)

        fun <T> node(content: T): LinkedList<T> = LinkedList(false, content)
    }
}
