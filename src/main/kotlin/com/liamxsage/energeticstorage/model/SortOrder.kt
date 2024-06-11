package com.liamxsage.energeticstorage.model

import com.liamxsage.energeticstorage.sorting.AlphabeticalItemComparator
import com.liamxsage.energeticstorage.sorting.ItemAmountComparator

enum class SortOrder(val displayString: String, val comparator: Comparator<ESItem>) {
    ALPHABETICAL("Alphabetical", AlphabeticalItemComparator()),
    ALPHABETICAL_REVERSED("Alphabetical (Reversed)", AlphabeticalItemComparator(true)),
    AMOUNT("Amount", ItemAmountComparator()),
    AMOUNT_REVERSED("Amount (Reversed)", ItemAmountComparator(true))
}