package net.intari.AndroidToolbox.Extensions

import android.content.Context
import android.widget.Toast

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 05.11.2017.
 * Kotlin extensions for common Android classes
 */
fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Context.toast(message: Int) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

