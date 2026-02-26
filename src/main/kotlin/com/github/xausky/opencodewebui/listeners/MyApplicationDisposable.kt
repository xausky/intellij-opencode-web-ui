package com.github.xausky.opencodewebui.listeners

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.Disposable

class MyApplicationDisposable : Disposable {

    override fun dispose() {
        thisLogger().info("Application is exiting, stopping OpenCode server")
        com.github.xausky.opencodewebui.toolWindow.MyToolWindowFactory.stopServer()
    }
}
