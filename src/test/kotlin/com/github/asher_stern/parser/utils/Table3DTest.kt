package com.github.asher_stern.parser.utils

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Created by Asher Stern on November-02 2017.
 */
class Table3DTest
{
    @Test
    fun test()
    {
        val table = Table3D<Int, Int, Int, Int>()
        assertNull(table[1,2,3])

        table[1,2,3] = 4
        assertEquals(4, table[1,2,3])

        table[1,2,3] = null
        assertNull(table[1,2,3])

        table[1,2,3] = 4
        assertNull(table[1,2,4])
        assertNull(table[0,2,3])
        assertNull(table[1,2,2])

        table[10,2,3] = 5
        assertNull(table[9,2,3])
        assertNull(table[10,2,2])
        assertEquals(4, table[1,2,3])
        assertEquals(5, table[10,2,3])

        val t_10_2 = table[10,2]
        assertNotNull(t_10_2)
        assertEquals(setOf<Int>(3), t_10_2!!.keys)
        val t_1_2 = table[1,2]
        assertNotNull(t_1_2)
        assertEquals(setOf<Int>(3), t_1_2!!.keys)
        assertNullOrEmpty(table[10,3])
    }
}

private fun assertNullOrEmpty(m: Map<*,*>?)
{
    if (m != null)
    {
        assert(m.isEmpty())
    }
}