package com.github.xausky.opencodewebui.toolWindow

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.content.ContentFactory
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MyToolWindowFactory : ToolWindowFactory {

    companion object {
        private var browserInstance: JBCefBrowser? = null
        private var serverRunning = false
        private var serverProcess: Process? = null
        private var checkScheduledFuture: ScheduledFuture<*>? = null
        private var currentProject: Project? = null
        private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "OpenCode-Server-Checker")
        }

        fun getBrowser(): JBCefBrowser? = browserInstance
        fun setBrowser(browser: JBCefBrowser) {
            browserInstance = browser
        }
        fun isServerRunning(): Boolean = serverRunning
        fun setServerRunning(running: Boolean) {
            serverRunning = running
        }
        fun getServerProcess(): Process? = serverProcess
        fun setServerProcess(process: Process?) {
            serverProcess = process
        }
        fun getProject(): Project? = currentProject
        fun setProject(project: Project) {
            currentProject = project
        }
        fun getCheckScheduledFuture(): ScheduledFuture<*>? = checkScheduledFuture
        fun setCheckScheduledFuture(future: ScheduledFuture<*>?) {
            checkScheduledFuture = future
        }
        fun getScheduler() = scheduler
        
        fun stopServer() {
            try {
                getCheckScheduledFuture()?.cancel(true)
                setCheckScheduledFuture(null)
                
                val process = getServerProcess()
                if (process?.isAlive == true) {
                    process.destroy()
                    thisLogger().info("OpenCode server stopped")
                }
                setServerRunning(false)
            } catch (e: Exception) {
                thisLogger().error("Error stopping OpenCode server: ${e.message}")
            }
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        setProject(project)
        val myToolWindow = MyToolWindow(toolWindow)
        ApplicationManager.getApplication().invokeLater {
            val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
            toolWindow.contentManager.addContent(content)
            myToolWindow.checkAndLoadContent()
            myToolWindow.startPeriodicCheck()
        }
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val project = toolWindow.project
        private val browser = JBCefBrowser()

        init {
            setBrowser(browser)
        }

        fun getContent() = browser.component

        fun checkAndLoadContent() {
            if (checkPortOpen("127.0.0.1", 4096)) {
                thisLogger().info("Port 4096 is already open")
                setServerRunning(true)
                loadProjectPage()
            } else {
                thisLogger().info("Port 4096 is not open, starting opencode serve...")
                startOpenCodeServer()
            }
        }

        private fun checkPortOpen(host: String, port: Int): Boolean {
            return try {
                val socket = java.net.Socket(host, port)
                socket.soTimeout = 2000
                socket.close()
                true
            } catch (e: Exception) {
                thisLogger().info("Port check failed: ${e.message}")
                false
            }
        }

        fun startPeriodicCheck() {
            if (getCheckScheduledFuture() != null) return
            
            val future = getScheduler().scheduleAtFixedRate(
                {
                    try {
                        ApplicationManager.getApplication().invokeAndWait {
                            checkServerHealth()
                        }
                    } catch (e: Exception) {
                        thisLogger().error("Error during periodic check: ${e.message}")
                    }
                },
                30L,
                30L,
                TimeUnit.SECONDS
            )
            setCheckScheduledFuture(future)
            thisLogger().info("Started periodic server health check")
        }

        private fun checkServerHealth() {
            if (!checkPortOpen("127.0.0.1", 4096)) {
                thisLogger().warn("Server is not responding, attempting to restart...")
                if (getServerProcess()?.isAlive == true) {
                    getServerProcess()?.destroy()
                }
                startOpenCodeServer()
            }
        }

        private fun startOpenCodeServer() {
            val task = object : Backgroundable(project, "Starting OpenCode Server", true) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        val process = ProcessBuilder()
                            .command("opencode", "serve", "--hostname", "127.0.0.1", "--port", "4096")
                            .redirectErrorStream(true)
                            .start()
                        
                        setServerProcess(process)

                        var maxAttempts = 30
                        val startTime = System.currentTimeMillis()
                        val timeout = 60000L

                        while (maxAttempts-- > 0 && (System.currentTimeMillis() - startTime) < timeout) {
                            if (process.isAlive && checkPortOpen("127.0.0.1", 4096)) {
                                break
                            }
                            Thread.sleep(1000)
                        }

                        if (checkPortOpen("127.0.0.1", 4096)) {
                            thisLogger().info("OpenCode server started successfully")
                            setServerRunning(true)
                            ApplicationManager.getApplication().invokeLater {
                                loadProjectPage()
                            }
                        } else {
                            thisLogger().error("Failed to start OpenCode server")
                            ApplicationManager.getApplication().invokeLater {
                                showErrorInBrowser()
                            }
                            setServerRunning(false)
                        }
                    } catch (e: Exception) {
                        thisLogger().error("Error starting OpenCode server: ${e.message}")
                        ApplicationManager.getApplication().invokeLater {
                            showErrorInBrowser()
                        }
                        setServerRunning(false)
                    }
                }
            }
            ProgressManager.getInstance().run(task)
        }

        private fun loadProjectPage() {
            val projectPath = project.basePath ?: return
            val encodedPath = Base64.getEncoder().encodeToString(projectPath.toByteArray(StandardCharsets.UTF_8))
            val url = "http://127.0.0.1:4096/$encodedPath"
            thisLogger().info("Loading page: $url")
            browser.loadURL(url)
        }

        private fun showErrorInBrowser() {
            val html = """
                <html>
                <body style="background-color: #2B2B2B; color: #A9B7C6; font-family: sans-serif; padding: 20px;">
                    <h2>Failed to start OpenCode server</h2>
                    <p>Please make sure 'opencode' is installed and available in your PATH.</p>
                    <p>Run the following command to start the server manually:</p>
                    <pre style="background: #3C3F41; padding: 10px; border-radius: 4px;">opencode serve --hostname 127.0.0.1 --port 4096</pre>
                </body>
                </html>
            """.trimIndent()
            browser.loadHTML(html)
        }
    }
}
