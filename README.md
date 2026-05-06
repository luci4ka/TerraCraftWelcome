```markdown
# 🎉 Welcome Plugin v2.2

![Paper](https://img.shields.io/badge/Paper-1.21.4-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/License-MIT-green)
![bStats](https://img.shields.io/badge/bStats-metrics-yellow)

> ✨ Современный плагин для Paper 1.21.4 с кастомными оповещениями о входе, выходе, смерти игроков, босс-трекером, праздничными эффектами и поддержкой PlaceholderAPI.

---

## 🚀 Возможности

| Категория | Фичи |
|-----------|------|
| **Вход/выход** | MOTD с кликабельными ссылками, Title-сообщения, ActionBar, фейерверки для новичков, сообщения по пермишенам (default/vip/admin) |
| **Смерти** | PvP, мобы (все виды), окружение (лава, падение, взрыв), кастомные сообщения, Title при смерти, эффекты для убийцы |
| **Авто-сообщения** | Персонажи с эмоциями, категории сообщений (greetings/tips/fun), настраиваемый интервал |
| **Босс-трекер** | Любые мобы через конфиг, топ урона с позициями, награды (команды/предметы с чарами/деньги/опыт), Title уведомления |
| **Праздники** | Автоопределение по дате, кастомные эффекты и сообщения, 8+ встроенных праздников |
| **PlaceholderAPI** | `%welcome_last_join%`, `%welcome_deaths%`, `%welcome_last_death_cause%`, `%welcome_online_count%` |
| **GUI** | `/welcome gui` — настройка плагина через инвентарь |
| **Языки** | Русский, Английский, легко добавить новые |
| **Обновления** | Проверка через свой API + GitHub |

---

## 📥 Установка

1. Скачай `Welcome-2.2.jar` из [релизов](https://github.com/luci/Welcome/releases/latest)
2. Помести в папку `plugins/` твоего сервера Paper 1.21.4
3. **Опционально:** Установи [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) и/или [Vault](https://www.spigotmc.org/resources/vault.34315/)
4. Перезапусти сервер
5. Настрой `plugins/Welcome/config.yml` под себя

---

## 🎮 Команды

| Команда | Описание | Права |
|---------|----------|-------|
| `/welcome` | Показать приветствие | `welcome.use` |
| `/welcome help` | Справка по командам | `welcome.use` |
| `/welcome version` | Версия плагина | `welcome.use` |
| `/welcome reload` | Перезагрузка конфига | `welcome.admin` |
| `/welcome gui` | Меню настроек (GUI) | `welcome.admin` |
| `/welcome language <ru/en>` | Смена языка | `welcome.admin` |

---

## 🔌 PlaceholderAPI

После установки PlaceholderAPI плагин автоматически регистрирует плейсхолдеры:

| Плейсхолдер | Описание |
|-------------|----------|
| `%welcome_last_join%` | Последний вход игрока |
| `%welcome_deaths%` | Количество смертей |
| `%welcome_last_death_cause%` | Причина последней смерти |
| `%welcome_online_count%` | Количество онлайна |
| `%welcome_plugin_version%` | Версия плагина |

---

## ⚙️ Конфигурация

```yaml
# config.yml (основные настройки)
auto_messages:
  enabled: true
  interval_ticks: 6000  # 5 минут
  characters:
    - name: "Лисёнок"
      color: "<green>"
      prefix: "🦊"
      moods:
        happy: "😊"

settings:
  boss_fight:
    enabled: true
    max_top_players: 5
    rewards:
      place_1:
        items: ["DIAMOND_SWORD:1:sharpness:5"]
        money: 1000.0
        experience: 500

  holidays:
    enabled: false
    current: "auto"  # new_year, halloween, christmas...
```

Сообщения вынесены в отдельные языковые файлы: `languages/ru.yml`, `languages/en.yml`.

---

## 🏗️ Структура проекта

```
Welcome/
├── src/main/java/com/luci/welcome/
│   ├── Main.java                    # Точка входа
│   ├── api/WelcomeAPI.java          # Публичное API
│   ├── command/CommandManager.java  # Paper Brigadier команды
│   ├── config/                      # Конфигурации
│   │   ├── ConfigHandler.java
│   │   ├── AutoMessageConfig.java
│   │   ├── BossFightConfig.java
│   │   ├── HolidayConfig.java
│   │   ├── PlayerDeathConfig.java
│   │   └── PlayerJoinConfig.java
│   ├── hook/                        # Хуки
│   │   ├── HookManager.java
│   │   ├── PlaceholderAPIHook.java
│   │   ├── WelcomePlaceholderExpansion.java
│   │   └── VaultHook.java
│   ├── listener/                    # Слушатели событий
│   │   ├── BossFightListener.java
│   │   ├── PlayerDeathListener.java
│   │   └── PlayerJoinListener.java
│   ├── manager/                     # Менеджеры
│   │   ├── AdapterManager.java
│   │   ├── LanguageManager.java
│   │   └── UpdateManager.java
│   ├── task/                        # Периодические задачи
│   │   ├── AutoMessageTask.java
│   │   └── HolidayEffectTask.java
│   └── utils/LoggerUtil.java        # Утилиты
└── src/main/resources/
    ├── config.yml
    ├── paper-plugin.yml
    └── languages/
        ├── ru.yml
        └── en.yml
```

---

## 🛠️ Сборка

```bash
# Клонируй репозиторий
git clone https://github.com/luci/Welcome.git
cd Welcome

# Собери с Gradle
./gradlew clean build

# Плагин будет в build/libs/Welcome-2.2.jar
```

**Требования:**
- Java 21
- Gradle 8.8+

---

## 📊 bStats

[![](https://bstats.org/signatures/bukkit/Welcome.svg)](https://bstats.org/plugin/bukkit/Welcome)

---

## 🤝 Благодарности

- [PaperMC](https://papermc.io) — лучший серверный софт
- [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) — система плейсхолдеров
- [Vault](https://github.com/MilkBowl/Vault) — экономика и права

---

## 📄 Лицензия

MIT © [luci](https://github.com/luci)
