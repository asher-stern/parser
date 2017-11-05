package com.github.asher_stern.parser.utils

import java.util.*

/**
 * Created by Asher Stern on November-02 2017.
 */
class Array1<T>(private val underlying: Array<T>)
{
    companion object
    {
        inline fun <reified T> arrayOf(size: Int, initializer: (Int) -> T): Array1<T>
        {
            if (size < 0)
            {
                throw RuntimeException("Negative size")
            }
            if (size == 0)
            {
                return Array1<T>(kotlin.emptyArray<T>())
            }

            val first = initializer(1)
            val underlying = kotlin.Array<T>(size) { first }
            for (i in 1..size)
            {
                underlying[i - 1] = initializer(i)
            }
            return Array1<T>(underlying)
        }

        inline fun <reified T> arrayOfNulls(size: Int): Array1<T?>
        {
            return Array1<T?>(kotlin.arrayOfNulls<T>(size))
        }
    }

    operator fun get(index: Int): T = underlying[index-1]

    operator fun set(index: Int, value: T)
    {
        underlying[index-1] = value
    }

    operator fun iterator(): Iterator<T>
    {
        return underlying.iterator()
    }

    override fun toString(): String = underlying.joinToString(", ", "[", "]")

    override fun equals(other: Any?): Boolean
    {
        if (other is Array1<*>)
        {
            return Arrays.equals(underlying, other.underlying)
        }
        else
        {
            return false
        }
    }

    override fun hashCode(): Int = Arrays.hashCode(underlying)

    val size: Int
        get() = underlying.size
}