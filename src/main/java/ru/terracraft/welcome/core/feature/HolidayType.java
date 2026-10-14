package ru.terracraft.welcome.core.feature;

import org.bukkit.Particle;
import org.bukkit.Sound;

/**
 * Известные праздники.
 * Каждый праздник знает свои звук, частицу и путь в lang/*.yml.
 */
public enum HolidayType {

    NEW_YEAR(
            "new_year",
            "holidays.new_year.messages",
            Sound.BLOCK_NOTE_BLOCK_BELL,
            Particle.SNOWFLAKE
    ),
    CHRISTMAS(
            "christmas",
            "holidays.christmas.messages",
            Sound.BLOCK_NOTE_BLOCK_CHIME,
            Particle.SNOWFLAKE
    ),
    VALENTINE(
            "valentine",
            "holidays.valentine.messages",
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            Particle.HEART
    ),
    HALLOWEEN(
            "halloween",
            "holidays.halloween.messages",
            Sound.ENTITY_WITCH_CELEBRATE,
            Particle.WITCH
    ),
    EASTER(
            "easter",
            "holidays.easter.messages",
            Sound.ENTITY_CHICKEN_EGG,
            Particle.HAPPY_VILLAGER
    ),
    WOMENS_DAY(
            "womens_day",
            "holidays.womens_day.messages",
            Sound.ENTITY_PLAYER_LEVELUP,
            Particle.HEART
    ),
    DEFENDER_DAY(
            "defender_day",
            "holidays.defender_day.messages",
            Sound.ENTITY_PLAYER_LEVELUP,
            Particle.CRIT
    ),
    APRIL_FOOLS(
            "april_fools",
            "holidays.april_fools.messages",
            Sound.ENTITY_VILLAGER_AMBIENT,
            Particle.CLOUD
    ),
    VICTORY_DAY(
            "victory_day",
            "holidays.victory_day.messages",
            Sound.ENTITY_PLAYER_LEVELUP,
            Particle.FIREWORK
    ),
    DEFAULT(
            "default",
            "holidays.default.messages",
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            Particle.HAPPY_VILLAGER
    );

    private final String key;
    private final String messagePath;
    private final Sound sound;
    private final Particle particle;

    HolidayType(String key, String messagePath, Sound sound, Particle particle) {
        this.key = key;
        this.messagePath = messagePath;
        this.sound = sound;
        this.particle = particle;
    }

    public String getKey() {
        return key;
    }

    public String getMessagePath() {
        return messagePath;
    }

    public Sound getSound() {
        return sound;
    }

    public Particle getParticle() {
        return particle;
    }

    /**
     * Парсит ключ в HolidayType.
     * Возвращает DEFAULT, если ключ неизвестен.
     */
    public static HolidayType fromKey(String key) {
        if (key == null || key.isBlank()) return DEFAULT;
        for (HolidayType type : values()) {
            if (type.key.equalsIgnoreCase(key)) {
                return type;
            }
        }
        return DEFAULT;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }
}