package net.intari.AndroidToolbox

import java.util.concurrent.atomic.AtomicReference

import kotlin.reflect.KProperty

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 05.11.2017.
 * Helper to make val usable on Android with onCreate
 */
class WriteOnceVal<T>() {
    companion object {
        val NULL_MASK = Any()
    }
    val valueRef = AtomicReference<T?>()

    fun get(thisRef: Any?, prop: KProperty<*>): T {
        val localValue = valueRef.get()
        return when (localValue) {
            null -> throw IllegalStateException("not yet initialized")
            //NULL_MASK -> null
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
