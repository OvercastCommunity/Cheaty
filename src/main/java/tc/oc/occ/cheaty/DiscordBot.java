package tc.oc.occ.cheaty;

import dev.pgm.community.assistance.Report;
import dev.pgm.community.events.PlayerReportEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import net.climaxmc.autokiller.events.AutoKillCheatEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DiscordBot {

  private final HttpClient httpClient;
  private final BotConfig config;
  private final Logger logger;
  private final PingMonitor pings;

  public DiscordBot(BotConfig config, Logger logger) {
    this.httpClient = HttpClient.newHttpClient();
    this.config = config;
    this.logger = logger;
    this.pings = new PingMonitor(config, this);
  }

  public BotConfig getConfig() {
    return config;
  }

  private String getUsername(UUID playerId) {
    Player player = Bukkit.getPlayer(playerId);
    return player != null ? player.getName() : null;
  }

  private void sendMessage(String message, boolean report) {
    if (!config.isEnabled()) return;

    String webhookUrl = report ? config.getReportsWebhookUrl() : config.getAnticheatWebhookUrl();
    if (webhookUrl == null || webhookUrl.isEmpty()) {
      logger.warning("Webhook URL not configured for " + (report ? "reports" : "anticheat"));
      return;
    }

    sendWebhook(webhookUrl, format(message));
  }

  private void sendWebhook(String webhookUrl, String content) {
    String json = "{\"content\":\"" + escapeJson(content) + "\",\"username\":\"Cheaty\"}";

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    httpClient
        .sendAsync(request, HttpResponse.BodyHandlers.discarding())
        .thenAccept(
            response -> {
              int statusCode = response.statusCode();
              if (statusCode != 204 && statusCode != 200) {
                logger.warning("Webhook failed with code: " + statusCode);
              }
            })
        .exceptionally(
            e -> {
              logger.warning("Failed to send webhook: " + e.getMessage());
              return null;
            });
  }

  private String escapeJson(String text) {
    return text.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
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

  public void sendRelay(String message, RelayType type) {
    if (!config.isRelayCommandEnabled()) return;
    String formatted = config.getRelayFormat().replace("%message%", message);
    sendMessage(getPrefix(type) + formatted, false);
  }

  public void sendAutoKiller(AutoKillCheatEvent event) {
    if (!config.isAutoKillerEnabled()) return;
    String formatted = config.getRelayFormat().replace("%message%", event.getAlert());
    sendMessage(getPrefix(RelayType.AUTOKILL) + formatted, false);
  }

  public void sendReportPing(Report report, int numReports) {
    if (!config.isEnabled()) return;

    String webhookUrl = config.getReportsWebhookUrl();
    if (webhookUrl == null || webhookUrl.isEmpty()) {
      logger.warning("Reports webhook URL not configured");
      return;
    }

    List<String> pingRoles = config.getDiscordPingRoles();

    // Build role mentions using raw format
    StringBuilder mentions = new StringBuilder();
    for (String roleId : pingRoles) {
      if (roleId != null && !roleId.isEmpty()) {
        mentions.append("<@&").append(roleId).append("> ");
      }
    }

    // Format ping message
    String pingMessage =
        mentions.toString()
            + "\n`"
            + getUsername(report.getTargetId())
            + "` has been reported by **"
            + numReports
            + " different player"
            + (numReports != 1 ? "s" : "")
            + "** within the last "
            + config.getReportWindowMinutes()
            + " minute"
            + (config.getReportWindowMinutes() != 1 ? "s" : "");

    sendWebhook(webhookUrl, pingMessage);
  }

  private String format(String text) {
    text = ChatColor.translateAlternateColorCodes('&', text);
    text = ChatColor.stripColor(text);
    text = text.replace("@", "");
    text = text.replace("_", "\\_");
    text = text.replace("*", "\\*");
    return text.trim();
  }

  public String getPrefix(RelayType type) {
    return switch (type) {
      case AUTOKILL -> config.getAutoKillPrefix();
      case MATRIX -> config.getMatrixPrefix();
      default -> config.getCommandPrefix();
    };
  }

  public static enum RelayType {
    AUTOKILL,
    MATRIX,
    COMMAND;
  }
}
