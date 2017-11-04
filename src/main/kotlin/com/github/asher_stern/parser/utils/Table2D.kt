package com.github.asher_stern.parser.utils

/**
 * Created by Asher Stern on November-02 2017.
 */
class Table2D<T1,T2,V>
{
    operator fun set(firstIndex: T1, secondIndex: T2, value: V?)
    {
        val m2 = map.computeIfAbsent(firstIndex) { mutableMapOf() }
        m2.put(secondIndex, value)
    }

    operator fun get(firstIndex: T1, secondIndex: T2): V?
    {
        return map.get(firstIndex)?.get(secondIndex)
    }

    operator fun get(firstIndex: T1): Map<T2, V?>?
    {
        return map[firstIndex]
    }

    val firstIndexes: Set<T1>
        get() = map.keys

    private val map: MutableMap<T1, MutableMap<T2, V?>> = mutableMapOf()
}