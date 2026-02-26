# opencode-web-ui

![Build](https://github.com/xausky/opencode-web-ui/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## 简介

OpenCodeWeb 是一款 JetBrains IDE（IntelliJ IDEA、PyCharm、WebStorm 等）插件，为 OpenCode 提供便捷的 Web UI 集成。

<!-- Plugin description -->
> **注意：** 这是一款 OpenCode 的非官方插件，由社区开发者维护，与 OpenCode 官方无关。

### 功能特性

- 🚀 **自动启动服务** - 点击侧边栏图标，自动检查并启动 OpenCode 服务器
- 🔄 **智能监控** - 定时检测服务器状态，自动重启异常的服务
- 🛡️ **自动清理** - IDE 退出时自动停止 OpenCode 服务，释放资源
- 🎨 **侧边栏集成** - 在 IDE 右侧显示插件图标，点击即可访问 OpenCode Web UI
- 📁 **项目同步** - 自动加载当前项目的 Web 界面

### 使用场景

- 需要在 JetBrains IDE 中使用 OpenCode Web UI
- 需要自动管理 OpenCode 服务器的开发者
- 希望在编码过程中快速查看 AI 助手界面的用户
<!-- Plugin description end -->

## 安装

- 使用 IDE 内置插件系统：

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>搜索 "open code web"</kbd> >
  <kbd>Install</kbd>

- 使用 JetBrains Marketplace：

  访问 [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) 进行安装

  也可以从 JetBrains Marketplace 下载[最新版本](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions)，然后使用
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>从磁盘安装插件...</kbd>

- 手动安装：

  下载[最新版本](https://github.com/xausky/opencode-web-ui/releases/latest)，然后使用
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>从磁盘安装插件...</kbd>

## 使用说明

1. **确保已安装 OpenCode CLI 工具**  
   插件需要在系统 PATH 中找到 `opencode` 命令。如未安装，请访问 OpenCode 官方文档进行安装。

2. **打开插件侧边栏**  
   在 IDE 右侧边栏找到 "OpenCodeWeb" 图标并点击。

3. **自动启动服务**  
   插件会自动检查 127.0.0.1:4096 端口，如果服务未运行，将自动执行：
   ```
   opencode serve --hostname 127.0.0.1 --port 4096
   ```

4. **访问 Web UI**  
   服务启动成功后，侧边栏将自动加载当前项目的 OpenCode Web 界面。

## 配置

插件默认使用以下配置：
- 主机: 127.0.0.1
- 端口: 4096

如需修改，请确保 OpenCode CLI 使用相同的端口和主机启动。

## 故障排除

**问题：插件无法找到 opencode 命令**

- 确保 OpenCode CLI 已正确安装
- 确保 opencode 可执行文件在系统的 PATH 环境变量中
- 在终端中运行 `opencode --version` 验证安装

**问题：服务启动失败**

- 检查端口 4096 是否已被其他程序占用
- 查看 IDE 日志窗口中的错误信息
- 手动运行启动命令排查问题

## 开发

项目基于 [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template) 开发。

## 许可证

本项目采用 MIT 许可证。

## 免责声明

本插件是 OpenCode 的非官方社区插件，与 OpenCode 官方没有任何关联。使用本插件产生的任何问题，请通过 GitHub Issues 反馈。

---

> 💡 如果你觉得这个插件有帮助，欢迎给个 Star 🌟
