package org.haile.oxygen.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling color conversions
 * Uses modern Adventure API with MiniMessage format
 */
public class ColorUtils {
    // Pattern để chuyển đổi mã "&" legacy và mã hex
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&([0-9a-fk-orx])");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // MiniMessage instance with all tags
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.gradient())
                    .resolver(StandardTags.rainbow())
                    .resolver(StandardTags.reset())
                    .resolver(StandardTags.newline())
                    .build())
            .build();

    // LegacyComponentSerializer cho tương thích ngược
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    /**
     * Chuyển đổi chuỗi có mã màu sang Component (đã có màu sắc)
     * Hỗ trợ cả định dạng legacy (&c, &#RRGGBB) và MiniMessage (<color:red>, <#RRGGBB>)
     *
     * @param text Text to colorize
     * @return Adventure Component with colors
     */
    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Chuyển đổi định dạng legacy sang MiniMessage
        String miniMessageFormat = convertLegacyToMiniMessage(text);

        // Sử dụng MiniMessage để parse thành Component
        return MINI_MESSAGE.deserialize(miniMessageFormat);
    }

    /**
     * Chuyển đổi chuỗi có mã màu sang String đã có màu
     * Chỉ sử dụng khi cần String (cho tương thích ngược),
     * khuyên dùng toComponent thay thế
     *
     * @param text Text to colorize
     * @return Colorized text as string (for backward compatibility)
     * @deprecated Use toComponent instead when possible
     */
    @Deprecated
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Đối với tương thích ngược, vẫn sử dụng LegacySerializer
        return LEGACY_SERIALIZER.serialize(toComponent(text));
    }

    /**
     * Chuyển đổi định dạng màu legacy (&, &#) sang định dạng MiniMessage
     *
     * @param text Text with legacy color codes
     * @return Text with MiniMessage format
     */
    private static String convertLegacyToMiniMessage(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Chuyển đổi mã hex &#RRGGBB thành <#RRGGBB>
        Matcher legacyMatcher = getMatcher(text);
        StringBuilder legacyBuffer = new StringBuilder();

        while (legacyMatcher.find()) {
            String colorCode = legacyMatcher.group(1);
            String replacement = getLegacyColorReplacement(colorCode);
            legacyMatcher.appendReplacement(legacyBuffer, replacement);
        }
        legacyMatcher.appendTail(legacyBuffer);

        return legacyBuffer.toString();
    }

    private static @NotNull Matcher getMatcher(String text) {
        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        StringBuilder hexBuffer = new StringBuilder();

        while (hexMatcher.find()) {
            String hexCode = hexMatcher.group(1);
            hexMatcher.appendReplacement(hexBuffer, "<#" + hexCode + ">");
        }
        hexMatcher.appendTail(hexBuffer);
        String hexConverted = hexBuffer.toString();

        // Chuyển đổi mã màu legacy &c thành <color:red>
        return LEGACY_COLOR_PATTERN.matcher(hexConverted);
    }

    /**
     * Chuyển đổi mã màu legacy sang tương đương MiniMessage
     *
     * @param code Mã màu legacy (một ký tự)
     * @return Định dạng MiniMessage tương đương
     */
    private static String getLegacyColorReplacement(String code) {
        return switch (code.toLowerCase()) {
            case "0" -> "<black>";
            case "1" -> "<dark_blue>";
            case "2" -> "<dark_green>";
            case "3" -> "<dark_aqua>";
            case "4" -> "<dark_red>";
            case "5" -> "<dark_purple>";
            case "6" -> "<gold>";
            case "7" -> "<gray>";
            case "8" -> "<dark_gray>";
            case "9" -> "<blue>";
            case "a" -> "<green>";
            case "b" -> "<aqua>";
            case "c" -> "<red>";
            case "d" -> "<light_purple>";
            case "e" -> "<yellow>";
            case "f" -> "<white>";
            case "k" -> "<obfuscated>";
            case "l" -> "<bold>";
            case "m" -> "<strikethrough>";
            case "n" -> "<underlined>";
            case "o" -> "<italic>";
            case "r" -> "<reset>";
            case "x" -> ""; // &x đã được xử lý trong phần hex
            default -> "&" + code;
        };
    }

    /**
     * Lấy TextColor từ mã hex
     *
     * @param hexCode Hex color code (format: "#RRGGBB" hoặc "RRGGBB")
     * @return TextColor instance
     */
    public static TextColor fromHex(String hexCode) {
        if (hexCode.startsWith("#")) {
            return TextColor.fromHexString(hexCode);
        } else {
            return TextColor.fromHexString("#" + hexCode);
        }
    }

    /**
     * Chuyển đổi định dạng MiniMessage thành Component
     *
     * @param miniMessage Text in MiniMessage format
     * @return Adventure Component
     */
    public static Component fromMiniMessage(String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) {
            return Component.empty();
        }

        return MINI_MESSAGE.deserialize(miniMessage);
    }
}