.. AndroidToolbox documentation master file, created by
   sphinx-quickstart on Sat Nov 18 14:12:40 2017.
 
Welcome to AndroidToolbox's documentation!
==========================================

Tools I usually use in my android apps

Usage:
====

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'net.intari:AndroidToolbox:{latest version}'
}
```


Usage
====

Kotlin helpers
===

Write-once read-only properties:
```kotlin
   private val glSurfaceViewDelegate = WriteOnceVal<GLSurfaceView>()
   ...
   glSurfaceViewDelegate.writeOnce(MyGLSurfaceView(this))
   // use glSurfaceViewDelegate as regular glSurfaceView      
```

Logging extensions (using CustomLogger)
```kotlin
   "HelloWorld".logInfo("message")
   this.logException(ex,"message to go with exception")
   42.logVerbose("The Answer")
   "20!8".logError("Replace and press any key")
   1984.logWarning("Big brother watches you")
   2000.logInfo("error2k")
   2038.logVerbose("error2038")
   "amazon".logDebug("debug aws code")
```

View extensions
```kotlin
  
```

Android helpers 
===
BackgroundExecutionManager

Utils

Android base classes
===
BaseActivity

BaseActivityWithGNSSSupport

BaseViewModel

BaseActivityWithViewModelSupport

GNSSLocationService


Android 3D Graphics helpers
====
TextureHelper

TextResourceReader

MatrixHelper

ShaderHelper

Geometry

VertexArray

TODO: add more descriptions

TODO: note about libs we import (a lot of Rx ones) and requirements (Java 8) 
