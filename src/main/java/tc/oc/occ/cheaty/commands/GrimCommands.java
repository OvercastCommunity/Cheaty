package tc.oc.occ.cheaty.commands;

import ac.grim.grimac.player.GrimPlayer;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.occ.cheaty.Cheaty;
import tc.oc.occ.cheaty.anticheat.GrimManager;

@CommandAlias("cheaty")
public class GrimCommands extends BaseCommand {

  private final GrimManager grimManager;
  @Dependency private Cheaty plugin;

  public GrimCommands(GrimManager grimManager) {
    this.grimManager = grimManager;
  }

  @Subcommand("grim status")
  @Description("Check the bypass status of player")
  @Syntax("[player]")
  @CommandPermission("cheaty.admin")
  public void status(CommandSender sender, @Optional OnlinePlayer player) {
    Player target = getTargetPlayer(sender, player);

    GrimPlayer grimPlayer = grimManager.getGrimPlayer(target);
    if (grimPlayer == null)
      throw new InvalidCommandArgument("Unable to resolve target as grim player");

    boolean status = grimPlayer.disableGrim;

    sender.sendMessage(
        target.getDisplayName() + " Status: " + ((status) ? "Except" : "Not Exempt"));
  }

  @Subcommand("grim toggle")
  @Description("Toggles bypass status of player")
  @Syntax("[player]")
  @CommandPermission("cheaty.admin")
  public void toggle(CommandSender sender, @Optional OnlinePlayer player) {
    Player target = getTargetPlayer(sender, player);
    boolean toggleStatus = grimManager.togglePlayerBypass(target);

    sender.sendMessage(
        target.getDisplayName() + " Status: " + ((toggleStatus) ? "Except" : "Not Exempt"));
  }

  private Player getTargetPlayer(CommandSender sender, OnlinePlayer onlinePlayer) {
    if (onlinePlayer != null) return onlinePlayer.getPlayer();

    if (sender instanceof Player) return ((Player) sender).getPlayer();

    throw new InvalidCommandArgument("Unable to resolve target player");
  }
}
