# 🎉 Welcome v2.2

[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue)](https://papermc.io)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/License-MIT-green)
[![bStats](https://img.shields.io/badge/bStats-metrics-yellow)](https://bstats.org/plugin/bukkit/Welcome)

> Современный плагин для Paper с уведомлениями о входе, выходе, смерти игроков, босс-трекером, авто-сообщениями и праздничными эффектами.

---

## 🚀 Возможности

| Категория | Функции |
|----------|--------|
| Вход/выход | MOTD, Title, ActionBar, фейерверки, группы (default/vip/admin) |
| Смерти | PvP, мобы, окружение (лава, падение, взрыв), кастомные сообщения |
| Авто-сообщения | Категории (greetings/tips/fun), персонажи, интервал |
| Босс-трекер | Топ урона, награды, любые мобы |
| Праздники | Авто по дате, эффекты и сообщения |
| PlaceholderAPI | Статистика игрока и сервера |
| GUI | `/welcome gui` |
| Языки | RU / EN |

---

## 📥 Установка

1. Скачайте `.jar` из релизов  
2. Поместите в `plugins/`  
3. (Опционально) установите PlaceholderAPI и Vault  
4. Перезапустите сервер  

---

## 🎮 Команды

| Команда | Описание | Права |
|--------|---------|------|
| `/welcome` | Приветствие | welcome.use |
| `/welcome reload` | Перезагрузка | welcome.admin |
| `/welcome gui` | GUI меню | welcome.admin |
| `/welcome language <ru|en>` | Смена языка | welcome.admin |

---

## 🔌 PlaceholderAPI

Доступны после установки:

- `%welcome_last_join%`
- `%welcome_deaths%`
- `%welcome_last_death_cause%`
- `%welcome_online_count%`
- `%welcome_plugin_version%`

---

## ⚙️ Конфигурация

```yaml
auto_messages:
  enabled: true
  interval_ticks: 6000 # 5 минут (20 тиков = 1 секунда)

settings:
  boss_fight:
    enabled: true
    rewards:
      place_1:
        items: ["MATERIAL:amount:enchant:level"]
        # пример: DIAMOND_SWORD:1:sharpness:5
```
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

[![](https://bstats.org/signatures/bukkit/Welcome_TerraCraft.svg)](https://bstats.org/plugin/bukkit/Welcome_TerraCraft/27073)

## 🤝 Благодарности

- [PaperMC](https://papermc.io) — лучший серверный софт
- [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) — система плейсхолдеров
- [Vault](https://github.com/MilkBowl/Vault) — экономика и права

---

## 📄 Лицензия

MIT © [luci](https://github.com/luci)
