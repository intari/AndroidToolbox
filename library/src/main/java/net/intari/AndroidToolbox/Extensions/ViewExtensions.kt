package net.intari.AndroidToolbox.Extensions

import android.view.View

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 18.11.2017.
 */

var View.visible: Boolean
    get () = visibility ==View.VISIBLE
    set (value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }