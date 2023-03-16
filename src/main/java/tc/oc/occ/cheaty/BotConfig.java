package tc.oc.occ.cheaty;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.Configuration;

public class BotConfig {

  private boolean enabled;

  private String discordToken;
  private String discordServerId;

  private String serverName;

  private boolean reportsEnabled;
  private boolean relayCommandEnabled;

  private String reportFormat;
  private String relayFormat;

  private String reportPrefix;
  private String autoKillPrefix;
  private String matrixPrefix;
  private String commandPrefix;

  private String reportChannel;
  private String cheatChannel;

  private boolean morpheusEnabled;
  private String morpheusPermission;
  private Component morpheusPrefix;

  public BotConfig(Configuration config) {
    reload(config);
  }

  public void reload(Configuration config) {
    this.enabled = config.getBoolean("enabled");
    this.discordToken = config.getString("token");
    this.discordServerId = config.getString("server");

    String serverEnv = System.getenv("SERVER_NAME");
    this.serverName =
        (serverEnv != null && !serverEnv.isEmpty()) ? serverEnv : config.getString("server-name");

    this.reportsEnabled = config.getBoolean("types.reports");
    this.relayCommandEnabled = config.getBoolean("types.relay-command");

    this.reportFormat = config.getString("report-format");
    this.relayFormat = config.getString("relay-format");

    this.reportPrefix = config.getString("prefix.report");
    this.autoKillPrefix = config.getString("prefix.autokill");
    this.matrixPrefix = config.getString("prefix.matrix");
    this.commandPrefix = config.getString("prefix.command");

    this.reportChannel = config.getString("channels.reports");
    this.cheatChannel = config.getString("channels.anticheat");

    this.morpheusEnabled = config.getBoolean("morpheus.enabled");
    this.morpheusPermission = config.getString("morpheus.permission-node");
    this.morpheusPrefix =
        LegacyComponentSerializer.legacyAmpersand()
            .deserialize(config.getString("morpheus.alert-prefix"));
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getToken() {
    return discordToken;
  }

  public String getDiscordServerId() {
    return discordServerId;
  }

  public String getServerName() {
    return serverName;
  }

  public boolean isReportsEnabled() {
    return reportsEnabled;
  }

  public boolean isRelayCommandEnabled() {
    return relayCommandEnabled;
  }

  public String getReportFormat() {
    return replaceServerName(reportFormat);
  }

  public String getRelayFormat() {
    return replaceServerName(relayFormat);
  }

  private String replaceServerName(String format) {
    return format.replace("%server%", getServerName());
  }

  public String getReportPrefix() {
    return reportPrefix;
  }

  public String getAutoKillPrefix() {
    return autoKillPrefix;
  }

  public String getMatrixPrefix() {
    return matrixPrefix;
  }

  public String getCommandPrefix() {
    return commandPrefix;
  }

  public String getReportChannel() {
    return reportChannel;
  }

  public String getAntiCheatChannel() {
    return cheatChannel;
  }

  public boolean isMorpheusEnabled() {
    return morpheusEnabled;
  }

  public String getMorpheusPermission() {
    return morpheusPermission;
  }

  public Component getMorpheusPrefix() {
    return morpheusPrefix;
  }
}
