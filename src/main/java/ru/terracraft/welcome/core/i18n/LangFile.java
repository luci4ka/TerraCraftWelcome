package ru.terracraft.welcome.core.i18n;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Парсер Minecraft .lang файлов.
 *
 * Формат:
 *   key=value
 *   # комментарий
 *   key=value1\nvalue2\nvalue3   (многострочное значение через \n)
 *
 * Поддерживает:
 *   - списки через key.1, key.2, ...
 *   - escape: \n, \\, \=, \:
 *   - комментарии # в начале строки
 *   - пустые строки
 */
public class LangFile {

    // \n, \\, \=, \:, \t
    private static final Pattern ESCAPE = Pattern.compile("\\\\([n\\=:t\\\\])");

    // Оригинальные значения: ключ → значение (с escape, как в файле)
    private final Map<String, String> raw = new TreeMap<>();

    // Кэш списков: baseKey → List<значений>
    private final Map<String, List<String>> listCache = new HashMap<>();

    // ═══════════════ LOAD ═══════════════

    public static LangFile load(File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return load(in);
        }
    }

    public static LangFile load(InputStream in) throws IOException {
        LangFile lang = new LangFile();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // BOM на первой строке
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                // Пустая строка
                if (line.isEmpty()) continue;

                // Комментарий
                if (line.startsWith("#")) continue;

                // key=value
                int eq = line.indexOf('=');
                if (eq < 0) {
                    // строка без '=' — игнорируем (можно warning)
                    continue;
                }

                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1);   // без trim — пробелы важны

                if (key.isEmpty()) continue;

                lang.raw.put(key, value);
            }
        }

        lang.buildListCache();
        return lang;
    }

    // ═══════════════ PUBLIC API ═══════════════

    /**
     * Получить строку по ключу.
     * Не разворачивает escape — это делает render.
     */
    public String getString(String key) {
        if (key == null) return null;
        return raw.get(key);
    }

    /**
     * Получить список строк по базовому ключу.
     * Ищет key.1, key.2, ..., key.N.
     * Если key сам является строкой (без .N) — вернёт singleton-список.
     */
    public List<String> getStringList(String key) {
        if (key == null) return List.of();

        // Есть ли уже кэш
        List<String> cached = listCache.get(key);
        if (cached != null) return cached;

        // Прямое значение
        String direct = raw.get(key);
        if (direct != null) {
            return List.of(direct);
        }

        return List.of();
    }

    /**
     * Развернуть escape-последовательности: \n → LF, \\ → \, \= → =, \: → :
     */
    public static String unescape(String value) {
        if (value == null) return null;
        return ESCAPE.matcher(value).replaceAll(match -> {
            String g = match.group(1);
            return switch (g) {
                case "n" -> "\n";
                case "\\" -> "\\";
                case "=" -> "=";
                case ":" -> ":";
                case "t" -> "\t";
                default -> g;
            };
        });
    }

    public boolean has(String key) {
        return raw.containsKey(key);
    }

    public java.util.Set<String> keys() {
        return Collections.unmodifiableSet(raw.keySet());
    }

    // ═══════════════ LIST CACHE ═══════════════

    /**
     * Для каждого ключа вида "base.N" добавляем значение в listCache["base"].
     * Сортировка по N (числовая).
     */
    private void buildListCache() {
        // Собираем: base → (N → value)
        Map<String, TreeMap<Integer, String>> grouped = new HashMap<>();

        for (Map.Entry<String, String> e : raw.entrySet()) {
            String key = e.getKey();
            int lastDot = key.lastIndexOf('.');
            if (lastDot < 0) continue;

            String suffix = key.substring(lastDot + 1);
            if (!isNumeric(suffix)) continue;

            String base = key.substring(0, lastDot);
            int n = Integer.parseInt(suffix);

            grouped.computeIfAbsent(base, k -> new TreeMap<>()).put(n, e.getValue());
        }

        // Заполняем listCache
        for (Map.Entry<String, TreeMap<Integer, String>> e : grouped.entrySet()) {
            listCache.put(e.getKey(), new ArrayList<>(e.getValue().values()));
        }
    }

    private static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }
}