# 🌌 Oxygen Plugin

**A realistic oxygen system plugin for Minecraft servers with space/survival themes**

[![Spigot](https://img.shields.io/badge/Spigot-1.20+-orange.svg)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-red.svg)](https://openjdk.java.net/)

## 📖 Overview

The Oxygen Plugin adds a comprehensive oxygen management system to your Minecraft server, perfect for space stations, underwater bases, or survival scenarios. Players must manage their oxygen levels to survive in hostile environments, with customizable warnings, visual indicators, and region-based mechanics.

## ✨ Features

### 🫁 **Oxygen Management**
- **Real-time oxygen consumption** in specified worlds and game modes
- **Configurable oxygen levels** (default: 100 max)
- **Damage system** when oxygen depletes
- **Boss bar display** with dynamic colors and custom formatting
- **Persistent data storage** (file-based or MySQL)

### 🎯 **Smart Region System**
- **WorldGuard integration** for spaceship/safe zones
- **World-based oxygen mechanics** (whitelist/blacklist support)
- **Game mode restrictions** (Survival, Adventure, etc.)
- **Unlimited oxygen permission** for admins

### 🔔 **Advanced Warning System**
- **Multi-level warnings** (Medium, Low, Critical)
- **Custom sound support** (built-in Bukkit sounds + resource pack sounds)
- **MiniMessage formatting** with hex colors and styling
- **Configurable thresholds** and messages

### 🎨 **Visual Customization**
- **Boss bar with hex color support** using MiniMessage format
- **Dynamic color changes** based on oxygen levels
- **Multiple bar styles** (solid, segmented)
- **Fully customizable messages** and prefixes

### ⚙️ **Administration Tools**
- **Live config reload** without server restart
- **Player oxygen management** commands (get, set, add)
- **Permission-based access control**
- **Comprehensive tab completion**

## 🚀 Installation

1. **Download** the latest release from [Releases](../../releases)
2. **Place** the `.jar` file in your server's `plugins/` folder
3. **Install dependencies**: [WorldGuard](https://dev.bukkit.org/projects/worldguard)
4. **Restart** your server
5. **Configure** the plugin using `/oxygen reload`

## 🔧 Configuration

### Basic Setup

```yaml
# Oxygen mechanics
oxygen:
  max-level: 100
  decrease-rate: 100  # ticks (5 seconds)
  damage: 10
  gamemodes:
    - SURVIVAL
    - ADVENTURE
  worlds:
    - Space
    - Moon
  worlds-whitelist: true

# Visual display
display:
  bossbar:
    title: "<white><bold>Oxygen: <#3498db>{oxygen}</#3498db></bold></white><white>/<#3498db>{max}</#3498db></white>"
    colors:
      high: GREEN
      medium: YELLOW  
      low: RED
    style: SEGMENTED_10
    thresholds:
      medium: 60
      low: 30
```

### Warning System

```yaml
notifications:
  enabled: true
  warnings:
    critical:
      enabled: true
      oxygen-level: 10
      subtitle: "<dark_red><bold>OXYGEN CRITICAL!</bold></dark_red>"
      sound:
        enabled: true
        name: "custom.oxygen.alarm"  # Custom sound from resource pack
        volume: 1.5
        pitch: 0.3
```

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/oxygen get <player>` | Check player's oxygen level | `oxygen.admin` |
| `/oxygen set <player> <value>` | Set player's oxygen level | `oxygen.admin` |
| `/oxygen add <player> <value>` | Add/subtract oxygen | `oxygen.admin` |
| `/oxygen reload` | Reload plugin configuration | `oxygen.admin` |

## 🔑 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `oxygen.admin` | Access to all commands | `op` |
| `oxygen.unlimited` | Unlimited oxygen (bypasses system) | `op` |

## 🛠️ Advanced Features

### Custom Sounds
The plugin supports both built-in Bukkit sounds and custom sounds from resource packs:

```yaml
sound:
  name: "ENTITY_PLAYER_LEVELUP"  # Built-in Bukkit sound
  # OR
  name: "spaceship.oxygen.warning"  # Custom resource pack sound
```

### WorldGuard Integration
Create regions with "spaceship" in the name to provide safe zones:
```
/rg define spaceship_main
/rg define emergency_spaceship
```

### MiniMessage Support
Full MiniMessage formatting support for rich text:
```yaml
title: "<gradient:#ff0000:#0000ff><bold>Oxygen: {oxygen}</bold></gradient>"
subtitle: "<rainbow>Safe Zone Detected!</rainbow>"
```

## 🗄️ Storage Options

### File Storage (Default)
```yaml
storage:
  type: file
```

### MySQL Storage
```yaml
storage:
  type: mysql
  mysql:
    host: localhost
    port: 3306
    database: minecraft
    username: user
    password: pass
    table-prefix: oxygen_
```

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'Add amazing feature'`
4. **Push** to branch: `git push origin feature/amazing-feature`
5. **Open** a Pull Request

## 📋 Requirements

- **Minecraft**: 1.20+
- **Java**: 17+
- **Dependencies**: WorldGuard
- **Server Software**: Spigot, Paper, or compatible

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- **Developer**: [haile](https://github.com/HaiLeVN)
- **Inspired by**: Space exploration and survival gameplay
- **Special Thanks**: WorldGuard team for region API

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/HaiLeVN/Oxygen/issues)
- **Wiki**: NOT YET

---

<div align="center">

**⭐ Star this repository if you find it useful! ⭐**

Made with ❤️ for the Minecraft community

</div>
