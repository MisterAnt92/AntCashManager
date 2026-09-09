package com.antcashmanager.android.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ListExtensionsTest {
    private val list = listOf("a", "b", "c", "d")

    @Test
    fun moved_shouldShiftElementUp_whenTargetBeforeSource() {
        assertEquals(listOf("a", "c", "b", "d"), list.moved(2, 1))
    }

    @Test
    fun moved_shouldShiftElementDown_whenTargetAfterSource() {
        assertEquals(listOf("b", "c", "a", "d"), list.moved(0, 2))
    }

    @Test
    fun moved_shouldReturnSameInstance_whenIndexOutOfRangeOrEqual() {
        assertSame(list, list.moved(0, -1))
        assertSame(list, list.moved(3, 4))
        assertSame(list, list.moved(1, 1))
    }
}
