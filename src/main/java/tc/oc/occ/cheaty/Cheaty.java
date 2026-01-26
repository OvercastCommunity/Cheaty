package tc.oc.occ.cheaty;

import co.aikar.commands.BukkitCommandManager;
import org.bukkit.plugin.java.JavaPlugin;
import tc.oc.occ.cheaty.commands.AdminCommands;
import tc.oc.occ.cheaty.commands.BotCommands;
import tc.oc.occ.cheaty.commands.CheatNotifyCommand;

public class Cheaty extends JavaPlugin {

  private DiscordBot bot;
  private BotConfig config;
  private BukkitCommandManager commands;

  @Override
  public void onEnable() {
    this.saveDefaultConfig();
    this.reloadConfig();

    this.config = new BotConfig(getConfig());
    this.bot = new DiscordBot(config, getLogger());

    this.setupCommands();
    this.registerListeners();
  }

  public void setupCommands() {
    this.commands = new BukkitCommandManager(this);
    commands.registerDependency(DiscordBot.class, bot);
    commands.registerDependency(BotConfig.class, config);
    commands.registerCommand(new BotCommands());
    commands.registerCommand(new AdminCommands());
    commands.registerCommand(new CheatNotifyCommand());
  }

  private void registerListeners() {
    this.getServer().getPluginManager().registerEvents(new BotListener(bot), this);
  }

  public void reloadBotConfig() {
    this.reloadConfig();
    config.reload(getConfig());
  }
}
