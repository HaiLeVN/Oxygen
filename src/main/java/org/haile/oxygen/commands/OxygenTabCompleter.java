package org.haile.oxygen.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.haile.oxygen.Oxygen;
import org.haile.oxygen.managers.OxygenPlayerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OxygenTabCompleter implements TabCompleter {

    private final Oxygen plugin;
    private final OxygenPlayerManager oxygenManager;

    public OxygenTabCompleter(Oxygen plugin, OxygenPlayerManager oxygenManager) {
        this.plugin = plugin;
        this.oxygenManager = oxygenManager;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        // Check if sender has permission
        if (!sender.hasPermission("oxygen.admin")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument: sub-commands
            List<String> subCommands = Arrays.asList("get", "set", "add", "reload");

            // Filter based on what user has typed
            String input = args[0].toLowerCase();
            completions = subCommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());

        } else if (args.length == 2) {
            // Second argument: player names (for get, set, add commands)
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("get") || subCommand.equals("set") || subCommand.equals("add")) {
                String input = args[1].toLowerCase();

                // Get online player names
                completions = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
            }

        } else if (args.length == 3) {
            // Third argument: values (for set, add commands)
            String subCommand = args[0].toLowerCase();
            String playerName = args[1];

            if (subCommand.equals("set")) {
                // Suggest common oxygen values for set command
                List<String> suggestions = Arrays.asList("0", "25", "50", "75", "100");
                String input = args[2];

                completions = suggestions.stream()
                        .filter(value -> value.startsWith(input))
                        .collect(Collectors.toList());

            } else if (subCommand.equals("add")) {
                // Advanced suggestions based on current oxygen level
                Player targetPlayer = Bukkit.getPlayer(playerName);
                List<String> suggestions;

                if (targetPlayer != null && targetPlayer.isOnline()) {
                    int currentOxygen = oxygenManager.getOxygen(targetPlayer);
                    int maxOxygen = oxygenManager.getMaxOxygen();

                    // Create smart suggestions based on current oxygen
                    suggestions = new ArrayList<>();

                    // Add negative values (to reduce oxygen)
                    if (currentOxygen > 10) suggestions.add("-10");
                    if (currentOxygen > 25) suggestions.add("-25");
                    if (currentOxygen > 50) suggestions.add("-50");
                    if (currentOxygen > 5) suggestions.add("-5");
                    if (currentOxygen > 1) suggestions.add("-1");

                    // Add positive values (to increase oxygen)
                    if (currentOxygen < maxOxygen) suggestions.add("1");
                    if (currentOxygen < maxOxygen - 5) suggestions.add("5");
                    if (currentOxygen < maxOxygen - 10) suggestions.add("10");
                    if (currentOxygen < maxOxygen - 25) suggestions.add("25");
                    if (currentOxygen < maxOxygen - 50) suggestions.add("50");

                    // Add value to fill to max
                    int toMax = maxOxygen - currentOxygen;
                    if (toMax > 0 && toMax != 1 && toMax != 5 && toMax != 10 && toMax != 25 && toMax != 50) {
                        suggestions.add(String.valueOf(toMax));
                    }

                } else {
                    // Default suggestions if player not found
                    suggestions = Arrays.asList("-50", "-25", "-10", "-5", "-1", "1", "5", "10", "25", "50");
                }

                String input = args[2];
                completions = suggestions.stream()
                        .filter(value -> value.startsWith(input))
                        .sorted() // Sort suggestions
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}