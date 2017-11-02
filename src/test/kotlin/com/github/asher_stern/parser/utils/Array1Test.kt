package com.github.asher_stern.parser.utils

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Created by Asher Stern on November-02 2017.
 */
class Array1Test
{
    @Test
    fun test()
    {
        val array = Array1.arrayOf(10) { it }
        assertEquals(10, array.size)
        for (i in 1..10)
        {
            assertEquals(i, array[i])
        }

        array[1] = 50
        assertEquals(50, array[1])
        for (i in 2..10)
        {
            assertEquals(i, array[i])
        }

        array[10] = 50
        assertEquals(50, array[1])
        assertEquals(50, array[10])
        for (i in 2..9)
        {
            assertEquals(i, array[i])
        }

        array[5] = 50
        assertEquals(50, array[5])

        try
        {
            array[0] = 50
            fail("Index 0 has not resulted with exception.")
        }
        catch (e: Exception) {}

        try
        {
            array[11] = 50
            fail("Index 11 has not resulted with exception.")
        }
        catch (e: Exception) {}
    }
}