package com.app.mytipsjob.utils

import android.util.Base64

class KeyUtils {
    companion object {
        fun getServerCliId(): String {
            return String(Base64.decode("MTAyOTEyNDY3MTgwNy04cWtqNDhpNml2NXQzcm9oZGxla29lYXZjNzVwZmE5Yi5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbQ==", Base64.DEFAULT))
        }
    }
}