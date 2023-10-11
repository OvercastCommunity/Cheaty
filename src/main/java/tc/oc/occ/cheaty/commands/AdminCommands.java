package tc.oc.occ.cheaty.commands;

import static net.kyori.adventure.text.Component.text;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import tc.oc.occ.cheaty.Cheaty;
import tc.oc.pgm.util.Audience;

@CommandAlias("cheaty")
public class AdminCommands extends BaseCommand {

  @Dependency private Cheaty plugin;

  @Subcommand("reload")
  @Description("Reload cheaty config")
  @CommandPermission("cheaty.admin")
  public void reload(CommandSender sender) {
    plugin.reloadBotConfig();
    Audience.get(sender)
        .sendMessage(text("Reloaded config!", NamedTextColor.GREEN, TextDecoration.BOLD));
  }
}
