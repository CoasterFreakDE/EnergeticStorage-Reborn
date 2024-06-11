package com.liamxsage.energeticstorage.sorting

import com.liamxsage.energeticstorage.model.ESItem
import dev.fruxz.stacked.extension.asPlainString

/**
 * A comparator that compares ItemStacks based on their display names in alphabetical order.
 *
 * This class implements the Comparator interface to provide a custom comparison logic for ItemStacks.
 * The comparison is case-sensitive and treats null display names as empty strings.
 */
class AlphabeticalItemComparator(private val descending: Boolean = false) : Comparator<ESItem> {
    override fun compare(o1: ESItem, o2: ESItem): Int {
        val compared = o1.itemStackAsSingle.itemMeta!!.displayName()?.asPlainString?.compareTo(
            o2.itemStackAsSingle.itemMeta!!.displayName()?.asPlainString ?: ""
        ) ?: 0
        return if (descending) -compared else compared
    }
}