package tc.oc.occ.cheaty.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import tc.oc.occ.cheaty.DiscordBot;
import tc.oc.occ.cheaty.DiscordBot.RelayType;

@CommandAlias("cheaty")
public class BotCommands extends BaseCommand {

  @Dependency private DiscordBot bot;

  @Subcommand("relay")
  @Syntax("[message] - Message to relay to discord")
  @CommandPermission("cheaty.bot")
  public void relay(CommandSender sender, String message) {
    bot.sendRelay(message, RelayType.COMMAND);
  }
}
