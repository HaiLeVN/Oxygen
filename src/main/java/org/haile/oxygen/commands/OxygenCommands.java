package org.haile.oxygen.commands;

import org.bukkit.potion.PotionEffectType;
import org.haile.oxygen.Oxygen;
import org.haile.oxygen.managers.OxygenPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OxygenCommands implements CommandExecutor {
    private final OxygenPlayerManager oxygenManager;
    private final Oxygen plugin;

    public OxygenCommands(Oxygen plugin, OxygenPlayerManager oxygenManager) {
        this.plugin = plugin;
        this.oxygenManager = oxygenManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("oxygen.admin")) {
            sender.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cSử dụng: /oxygen <get|set|add|reload> [player] [value]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Handle reload command - FIX: Use plugin.reloadPlugin() instead of manual reload
        if (subCommand.equals("reload")) {
            sender.sendMessage("§eĐang tải lại cấu hình plugin...");

            try {
                // Use the proper reload method from main plugin class
                plugin.reloadPlugin();
                sender.sendMessage("§aĐã tải lại cấu hình và dữ liệu người chơi thành công!");
            } catch (Exception e) {
                sender.sendMessage("§cLỗi khi tải lại cấu hình: " + e.getMessage());
                plugin.getLogger().severe("Error during plugin reload: " + e.getMessage());
                e.printStackTrace();
            }

            return true;
        }

        if (subCommand.equals("get")) {
            if (args.length != 2) {
                sender.sendMessage("§cSử dụng: /oxygen get <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cNgười chơi " + args[1] + " không online!");
                return true;
            }

            int oxygen = oxygenManager.getOxygen(target);
            int maxOxygen = oxygenManager.getMaxOxygen();
            sender.sendMessage("§aOxygen của " + target.getName() + ": " + oxygen + "/" + maxOxygen);
            return true;
        }

        if (subCommand.equals("set")) {
            if (args.length != 3) {
                sender.sendMessage("§cSử dụng: /oxygen set <player> <value>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cNgười chơi " + args[1] + " không online!");
                return true;
            }

            int value;
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cGiá trị oxygen phải là một số!");
                return true;
            }

            int maxOxygen = oxygenManager.getMaxOxygen();
            if (value < 0 || value > maxOxygen) {
                sender.sendMessage("§cGiá trị oxygen phải từ 0 đến " + maxOxygen + "!");
                return true;
            }

            oxygenManager.setOxygen(target, value);
            sender.sendMessage("§aĐã đặt oxygen của " + target.getName() + " thành " + value + "/" + maxOxygen);
            return true;
        }

        // NEW: Add command
        if (subCommand.equals("add")) {
            if (args.length != 3) {
                sender.sendMessage("§cSử dụng: /oxygen add <player> <value>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cNgười chơi " + args[1] + " không online!");
                return true;
            }

            int addValue;
            try {
                addValue = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cGiá trị oxygen phải là một số!");
                return true;
            }

            // Get current oxygen level
            int currentOxygen = oxygenManager.getOxygen(target);
            int maxOxygen = oxygenManager.getMaxOxygen();

            // Calculate new oxygen level (capped at max)
            int newOxygen = Math.min(maxOxygen, currentOxygen + addValue);

            // Handle negative values (subtract oxygen)
            if (addValue < 0) {
                newOxygen = Math.max(0, currentOxygen + addValue);
            }

            // Set the new oxygen level
            oxygenManager.setOxygen(target, newOxygen);

            // Send confirmation message
            if (addValue > 0) {
                sender.sendMessage("§aĐã thêm " + addValue + " oxygen cho " + target.getName() +
                        ". Oxygen hiện tại: " + newOxygen + "/" + maxOxygen);
            } else {
                sender.sendMessage("§aĐã trừ " + Math.abs(addValue) + " oxygen của " + target.getName() +
                        ". Oxygen hiện tại: " + newOxygen + "/" + maxOxygen);
            }

            // Notify target player
            if (addValue > 0) {
                target.sendMessage("§aBạn đã được thêm " + addValue + " oxygen!");
            } else {
                target.sendMessage("§cBạn đã bị trừ " + Math.abs(addValue) + " oxygen!");
            }

            return true;
        }

        sender.sendMessage("§cSử dụng: /oxygen <get|set|add|reload> [player] [value]");
        return true;
    }
}