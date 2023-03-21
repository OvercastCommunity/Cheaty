package tc.oc.occ.cheaty.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import tc.oc.occ.cheaty.Cheaty;

@CommandAlias("cheaty")
public class AdminCommands extends BaseCommand {

  @Dependency private Cheaty plugin;

  @Subcommand("reload")
  @Description("Reload cheaty config")
  @CommandPermission("cheaty.admin")
  public void reload(CommandSender sender) {
    plugin.reloadBotConfig();
    sender.sendMessage(color("&a&lReloaded config!"));
  }

  public static String color(String format, Object... args) {
    return ChatColor.translateAlternateColorCodes('&', String.format(format, args));
  }
}
