package com.liamxsage.energeticstorage.sorting

import com.liamxsage.energeticstorage.model.ESItem

/**
 * A comparator that compares ItemStacks based on their display names in alphabetical order.
 *
 * This class implements the Comparator interface to provide a custom comparison logic for ItemStacks.
 * The comparison is case-sensitive and treats null display names as empty strings.
 */
class ItemAmountComparator(private val descending: Boolean = false) : Comparator<ESItem> {
    override fun compare(o1: ESItem, o2: ESItem): Int {
        val compared = o1.amount.compareTo(o2.amount)
        return if (descending) -compared else compared
    }
}