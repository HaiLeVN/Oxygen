package org.haile.oxygen.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Utility class for text formatting
 */
public class FormatUtils {
    /**
     * Format và thay thế các placeholder với MiniMessage
     * Thay thế {oxygen}/{max} trước, sau đó parse với MiniMessage
     *
     * @param text Text with placeholders
     * @param oxygen Current oxygen value
     * @param max Maximum oxygen value
     * @return Formatted Component with replaced placeholders
     */
    public static Component formatOxygenComponent(String text, int oxygen, int max) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Thay thế placeholder trước khi đưa vào MiniMessage
        String processedText = text
                .replace("{oxygen}", String.valueOf(oxygen))
                .replace("{max}", String.valueOf(max));

        // Parse với MiniMessage để xử lý màu sắc và định dạng
        return MiniMessage.miniMessage().deserialize(processedText);
    }

    /**
     * Format và thay thế các placeholder trong text
     * Sử dụng cho tương thích ngược với các định dạng cũ
     *
     * @param text Text with placeholders
     * @param oxygen Current oxygen value
     * @param max Maximum oxygen value
     * @return Formatted text with placeholders replaced
     * @deprecated Use formatOxygenComponent for new code
     */
    @Deprecated
    public static String formatOxygenText(String text, int oxygen, int max) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.replace("{oxygen}", String.valueOf(oxygen))
                .replace("{max}", String.valueOf(max));
    }
}