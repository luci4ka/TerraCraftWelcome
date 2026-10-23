# 🎉 TerraCraftWelcome

[![Paper](https://img.shields.io/badge/Paper-1.21-blue)](https://papermc.io)
![Java](https://img.shields.io/badge/Java-25-orange)
![License](https://img.shields.io/badge/License-MIT-green)
[![bStats](https://img.shields.io/badge/bStats-metrics-yellow)](https://bstats.org/plugin/bukkit/Welcome_TerraCraft/27073)

> Современный плагин для Paper: уведомления о входе, выходе и смерти игроков,
> группы с наследованием, праздничные эффекты, i18n с локалью игрока,
> PlaceholderAPI, GUI настроек и публичный API.

---

## 🚀 Возможности

| Категория | Что умеет |
|----------|----------|
| **Вход/выход** | MOTD, Title, ActionBar, фейерверк при первом входе, счётчик «#N новичок» |
| **Группы** | Наследование (`inherits`), приоритеты, per-group join/quit сообщения |
| **Смерти** | PvP, мобы (ZOMBIE, SKELETON, CREEPER, SPIDER, WITCH, ENDERMAN, WARDEN), лава, падение, взрыв, утопление, огонь, пустота |
| **Праздники** | Новый год, Рождество, Валентин, Хэллоуин, Пасха, 8 Марта, 23 Февраля, 1 Апреля, День Победы — авто по дате или вручную |
| **Локализация** | Формат **YAML**, локаль игрока (клиент → `ru_ru` / `en_us`), fallback |
| **PlaceholderAPI** | `%welcome_last_join%`, `%welcome_deaths%`, `%welcome_last_death_cause%`, `%welcome_first_join_number%`, `%welcome_total_players%`, ссылки |
| **GUI** | `/welcome gui` — тумблеры звуков/частиц/праздников, смена языка |
| **API** | `WelcomeAPI` через `ServicesManager`, три события (`WelcomeJoinEvent`, `WelcomeDeathEvent`, `WelcomeHolidayEvent`) |
| **Интеграции** | PlaceholderAPI, Vault, LuckPerms, bStats, CommandAPI |
| **Reload** | `/welcome reload` — без перезапуска сервера |

---

## 📥 Установка

1. Скачай `Welcome-x.x.jar` из [релизов](../../releases)
2. Положи в `plugins/`
3. Установи **обязательно**: [CommandAPI](https://github.com/JorelAli/CommandAPI) (12.1.0+)
4. Опционально: [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI), [Vault](https://github.com/MilkBowl/Vault), [LuckPerms](https://luckperms.net/)
5. Перезапусти сервер

---

## 🎮 Команды

| Команда | Описание | Права |
|--------|---------|------|
| `/welcome` | Показать приветствие | `welcome.use` |
| `/welcome help` | Справка | — |
| `/welcome info` | Информация о плагине | — |
| `/welcome reload` | Перезагрузить конфиг, языки, группы | `welcome.admin` |
| `/welcome gui` | Открыть меню настроек | `welcome.admin` |

**Алиасы:** `welc`, `wlc`.

---

## 🔑 Права

| Право | Описание |
|------|---------|
| `welcome.use` | Доступ к `/welcome` |
| `welcome.admin` | Доступ к `reload`, `gui` |
| `welcome.group.<id>` | Принадлежность к группе (для выбора активной группы) |

Пример выдачи группы `overlord`:
```
/lp group overlord permission set welcome.group.overlord true
```

---

## 🔌 PlaceholderAPI

После установки PlaceholderAPI:

| Плейсхолдер | Описание |
|---|---|
| `%welcome_last_join%` | Когда последний раз заходил («Только что», «5 мин. назад», ...) |
| `%welcome_deaths%` | Количество смертей (онлайн) |
| `%welcome_last_death_cause%` | Причина последней смерти |
| `%welcome_first_join_number%` | Номер первого входа (`1`, `2`, ...) |
| `%welcome_first_join_number_with_hash%` | То же с `#` |
| `%welcome_total_players%` | Всего игроков за всё время |
| `%welcome_online_count%` | Онлайн сейчас |
| `%welcome_max_players%` | Максимум слотов |
| `%welcome_player_name%` | Имя игрока |
| `%welcome_plugin_version%` | Версия плагина |
| `%welcome_vk_url%` | Ссылка ВК (MiniMessage) |
| `%welcome_site_url%` | Ссылка на сайт |
| `%welcome_discord_url%` | Discord |
| `%welcome_telegram_url%` | Telegram |
| `%welcome_youtube_url%` | YouTube |

> ⚠️ Плейсхолдеры `%welcome_*%` возвращают **MiniMessage-строки** (кликабельные ссылки). Парсите их через MiniMessage или используйте в компонентах.

---

## ⚙️ Конфигурация

### `config.yml`

```yaml
configVersion: 1
language: ru_ru

join:
  join: true
  quit: true
  motd: true
  titles: true
  actionBar: true
  firstJoinFirework: false

death:
  death: true
  respawn: true
  titles: true

holidays:
  enabled: false
  current: auto
  intervalTicks: 1200
  volume: 1.0
  pitch: 1.0
  particleAmount: 10

effects:
  soundEnabled: true
  soundVolume: 1.0
  soundPitch: 1.0
  particlesEnabled: true
  particleAmount: 5
```

### `groups.yml`

```yaml
groups:
  default:
    priority: 0
    inherits: []

  deluxe:
    priority: 30
    inherits: [default]

  elite:
    priority: 40
    inherits: [deluxe, default]

  overlord:
    priority: 50
    inherits: [elite, deluxe, default]

```

Наследование: группа использует свои сообщения + сообщения всех `inherits`.

### `lang/ru_ru.yml` и `lang/en_us.yml`

Формат — **YAML** (нативные списки вместо `key.1/key.2`):

```yaml
motd:
  lines:
    - "<dark_purple>| <gold>Приветик, %player_name%!</gold></dark_purple>"
    - "<dark_purple>| <gray>Рады видеть тебя снова!</gray>"

join:
  first_join:
    - "<gold>🎉 Новичок #%number%!</gold>"
    - "<green>✨ %player_name% впервые у нас!</green>"

groups:
  overlord:
    join:
      - "<gold>👑 Владыка %player_name% снизошёл!</gold>"
```

Локаль определяется по клиенту игрока: `ru_ru`, `en_us`. Fallback — `config.language`.

---

## 📡 API

### Получение API

```java
WelcomeAPI api = Bukkit.getServicesManager().load(WelcomeAPI.class);
if (api != null) {
    api.sendWelcomeMessage(player);
}
```

### События

| Событие | Когда | Отменяемое |
|---|---|---|
| `WelcomeJoinEvent` | Перед обработкой входа | ✅ |
| `WelcomeDeathEvent` | Перед обработкой смерти | ✅ |
| `WelcomeHolidayEvent` | Перед применением праздника к игроку | ✅ |

```java
@EventHandler
public void onJoin(WelcomeJoinEvent event) {
    if (event.isFirstJoin()) {
        // первый вход
    }
    event.setCancelled(true); // отменить обработку
}
```

---

## 🛠️ Сборка

```bash
git clone https://github.com/luci/Welcome.git
cd Welcome
./gradlew clean build
# → build/libs/Welcome-<version>.jar
```

**Требования:**
- JDK 25
- Gradle 9.0+
- Paper 1.21+

---

## 🌍 Языки

Поддерживаются YAML-файлы локализации:
- `ru_ru` — русский
- `en_us` — английский

Локализация — **на локали клиента**, а не на языке плагина. Каждый игрок видит сообщения на своём языке (для join/quit/death/holidays).

Хочешь добавить язык? Положи `lang/<code>.yml` в `plugins/Welcome/lang/`.

---

## 📊 bStats

[![](https://bstats.org/signatures/bukkit/Welcome_TerraCraft.svg)](https://bstats.org/plugin/bukkit/Welcome_TerraCraft/27073)

---

## 🤝 Благодарности

- [PaperMC](https://papermc.io) — серверный софт
- [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) — плейсхолдеры
- [Vault](https://github.com/MilkBowl/Vault) — экономика
- [LuckPerms](https://luckperms.net/) — права и группы
- [CommandAPI](https://github.com/JorelAli/CommandAPI) — команды
- [ConfigLib](https://github.com/Exlll/ConfigLib) — конфиг
- [bStats](https://bstats.org) — метрики

---

## 📄 Лицензия

MIT © [LICENSE](https://github.com/luci4ka/LICENSE)