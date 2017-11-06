package net.intari.AndroidToolbox

import java.util.concurrent.atomic.AtomicReference

import kotlin.reflect.KProperty

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 05.11.2017.
 * Helper to make val usable on Android with onCreate
 * Experiment code https://try.kotlinlang.org/#/UserProjects/vl2h3qj2abq4b96bl3112kh2ji/ls3kpvnct8b90rs6pf2p6cbq8i
 * See also https://habrahabr.ru/company/JetBrains/blog/183444/
 */

class WriteOnceVal<T>() {
    companion object {
        val NULL_MASK = Any()
    }
    val valueRef = AtomicReference<T?>()


    operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
        val localValue = valueRef.get()
        return when (localValue) {
            null -> throw IllegalStateException("not yet initialized")
            //NULL_MASK -> null
            NULL_MASK -> throw IllegalStateException("initialized with null")
            else -> localValue as T
        }
    }

    fun get(): T {
        val localValue = valueRef.get()
        return when (localValue) {
            null -> throw IllegalStateException("not yet initialized")
            //NULL_MASK -> null
            NULL_MASK -> throw IllegalStateException("initialized with null")
            else -> localValue as T
        }
    }

    fun writeOnce(value : T) {
        val localValue = if (value == null) NULL_MASK as T else value
        if (!valueRef.compareAndSet(null, localValue)) {
            throw IllegalStateException("already initialized")
        }

    }
}
