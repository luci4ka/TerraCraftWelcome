package ru.terracraft.welcome.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

@Configuration
public class MainConfig {

    @Comment("Версия конфига")
    public int configVersion = 1;

    @Comment("Язык по умолчанию (ru_ru, en_us)")
    public String language = "ru_ru";

    @Comment("Настройки входа")
    public JoinSection join = new JoinSection();

    @Comment("Настройки смерти")
    public DeathSection death = new DeathSection();

    @Comment("Настройки праздников")
    public HolidaySection holidays = new HolidaySection();

    @Comment("Настройки эффектов")
    public EffectSection effects = new EffectSection();

    // ═══════════════ Вложенные классы ═══════════════

    @Configuration
    public static class JoinSection {
        @Comment("Сообщения о входе")
        public boolean join = true;

        @Comment("Сообщения о выходе")
        public boolean quit = true;

        @Comment("MOTD при входе")
        public boolean motd = true;

        @Comment("Титул при входе")
        public boolean titles = true;

        @Comment("Action bar при входе")
        public boolean actionBar = true;

        @Comment("Фейерверк при первом входе")
        public boolean firstJoinFirework = false;
    }

    @Configuration
    public static class DeathSection {
        @Comment("Сообщения о смерти")
        public boolean death = true;

        @Comment("Сообщения при возрождении")
        public boolean respawn = true;

        @Comment("Титул при смерти")
        public boolean titles = true;
    }

    @Configuration
    public static class HolidaySection {
        @Comment("Включить праздники")
        public boolean enabled = false;

        @Comment("Текущий праздник (auto)")
        public String current = "auto";

        @Comment("Интервал в тиках")
        public int intervalTicks = 1200;

        @Comment("Громкость")
        public double volume = 1.0;

        @Comment("Pitch")
        public double pitch = 1.0;

        @Comment("Частиц")
        public int particleAmount = 10;
    }

    @Configuration
    public static class EffectSection {
        @Comment("Звуки включены")
        public boolean soundEnabled = true;

        @Comment("Громкость звука")
        public double soundVolume = 1.0;

        @Comment("Pitch звука")
        public double soundPitch = 1.0;

        @Comment("Частицы включены")
        public boolean particlesEnabled = true;

        @Comment("Количество частиц")
        public int particleAmount = 5;
    }
}