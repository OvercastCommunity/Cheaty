package tc.oc.occ.cheaty;

import dev.pgm.community.assistance.Report;
import dev.pgm.community.events.PlayerReportEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;

public class DiscordBot {

  private DiscordApi api;
  private final BotConfig config;
  private final Logger logger;
  private final PingMonitor pings;

  public DiscordBot(BotConfig config, Logger logger) {
    this.config = config;
    this.logger = logger;
    this.pings = new PingMonitor(config, this);
    reload();
  }

  public BotConfig getConfig() {
    return config;
  }

  private void setAPI(DiscordApi api) {
    this.api = api;
  }

  public void reload() {
    if (this.api != null && !config.isEnabled()) {
      disable();
    } else if (this.api == null && config.isEnabled()) {
      enable();
    }
  }

  private void enable() {
    if (config.isEnabled()) {
      logger.info("Enabling DiscordBot...");
      new DiscordApiBuilder()
          .setToken(config.getToken())
          .setWaitForServersOnStartup(false)
          .setWaitForUsersOnStartup(false)
          .login()
          .thenAcceptAsync(
              api -> {
                setAPI(api);
                api.setMessageCacheSize(1, 60 * 60);
                api.addServerBecomesAvailableListener(
                    listener -> {
                      logger.info(listener.getServer().getName() + " is now available");
                    });
                logger.info("Cheaty has connected to Discord!");
              });
    }
  }

  public void disable() {
    if (this.api != null) {
      this.api.disconnect();
    }
    this.api = null;
  }

  private Optional<Server> getDiscordServer() {
    return api.getServerById(config.getDiscordServerId());
  }

  private Optional<ServerChannel> getChannel(String id) {
    Server server = getDiscordServer().orElse(null);
    if (server != null) {
      return server.getChannelById(id);
    }
    return Optional.empty();
  }

  private void sendMessage(String message, boolean report) {
    if (api != null) {
      api.getServerById(config.getDiscordServerId())
          .ifPresent(
              server -> {
                server
                    .getChannelById(
                        report ? config.getReportChannel() : config.getAntiCheatChannel())
                    .ifPresent(
                        channel -> {
                          channel
                              .asTextChannel()
                              .ifPresent(
                                  text -> {
                                    text.sendMessage(format(message));
                                  });
                        });
              });
    }
  }

  public void sendReport(PlayerReportEvent event) {
    if (!config.isReportsEnabled()) return;
    Report report = event.getReport();
    String reporter = getUsername(report.getSenderId());
    String reported = getUsername(report.getTargetId());
    String reason = report.getReason();
    String formatted =
        config
            .getReportFormat()
            .replace("%reporter%", reporter)
            .replace("%reported%", reported)
            .replace("%reason%", reason);
    sendMessage(config.getReportPrefix() + formatted, true);

    if (config.isPingEnabled()) {
      pings.onReport(event);
    }
  }

  private String getUsername(UUID playerId) {
    Player player = Bukkit.getPlayer(playerId);
    return player != null ? player.getName() : null;
  }

  public void sendRelay(String message, RelayType type) {
    if (!config.isRelayCommandEnabled()) return;
    String formatted = config.getRelayFormat().replace("%message%", message);
    sendMessage(getPrefix(type) + formatted, false);
  }

  public void sendReportPing(Report report, int numReports) {
    List<String> pingRoles = config.getDiscordPingRoles();

    Server server = getDiscordServer().orElse(null);

    if (server == null) return;

    // Convert role ids to objects
    List<Role> discordRoles =
        pingRoles.stream()
            .map(
                roleId -> {
                  Role role = server.getRoleById(roleId).orElse(null);
                  if (role == null) {
                    logger.warning("Could not find role '" + roleId + "'");
                  }
                  return role;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    // Format ping message
    String pingMessage =
        "`"
            + getUsername(report.getTargetId())
            + "` has been reported by **"
            + numReports
            + " different player"
            + (numReports != 1 ? "s" : "")
            + "** within the last "
            + config.getReportWindowMinutes()
            + " minute"
            + (config.getReportWindowMinutes() != 1 ? "s" : "");

    // Send message here
    TextChannel channel = (TextChannel) api.getChannelById(config.getReportChannel()).orElse(null);
    if (channel == null) {
      logger.warning("Could not find report channel (" + config.getReportChannel() + ")");
      return;
    }

    MessageBuilder builder = new MessageBuilder();
    for (Role role : discordRoles) {
      builder.append(role.getMentionTag());
    }
    builder.appendNewLine();
    builder.append(pingMessage);
    builder.send(channel);
  }

  private String format(String text) {
    text = ChatColor.translateAlternateColorCodes('&', text);
    text = ChatColor.stripColor(text);
    text = text.replace("@", "");
    return text.trim();
  }

  public String getPrefix(RelayType type) {
    switch (type) {
      case AUTOKILL:
        return config.getAutoKillPrefix();
      case MATRIX:
        return config.getMatrixPrefix();
      default:
        return config.getCommandPrefix();
    }
  }

  public static enum RelayType {
    AUTOKILL,
    MATRIX,
    COMMAND;
  }
}
