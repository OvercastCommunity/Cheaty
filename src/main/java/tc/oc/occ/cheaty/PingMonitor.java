package tc.oc.occ.cheaty;

import dev.pgm.community.assistance.Report;
import dev.pgm.community.events.PlayerReportEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tc.oc.occ.idly.Idly;

public class PingMonitor {

  private Map<UUID, Map<UUID, Long>> reportsByPlayer = new HashMap<>();

  private final BotConfig config;
  private final DiscordBot discord;

  public PingMonitor(BotConfig config, DiscordBot discord) {
    this.config = config;
    this.discord = discord;
  }

  private boolean isNotIdle(Player player) {
    Idly idlyPlugin = Idly.get();
    if (idlyPlugin == null) {
      return true;
    }

    return !idlyPlugin.getIdlyManager().isIdle(player, config.getStaffIdleMinutes() * 60);
  }

  private boolean isStaffOnline() {
    List<Player> staff =
        Bukkit.getOnlinePlayers().stream()
            .filter(p -> p.hasPermission(config.getStaffPermission()))
            .filter(p -> !p.hasPermission(config.getOffDutyPermission()))
            .filter(p -> isNotIdle(p))
            .collect(Collectors.toList());

    return !staff.isEmpty();
  }

  public void onReport(PlayerReportEvent event) {
    if (isStaffOnline()) return; // Don't log when staff are online and active

    Report report = event.getReport();
    UUID reportedId = report.getTargetId();
    UUID reporterId = report.getSenderId();
    long reportTime = System.currentTimeMillis();

    Map<UUID, Long> reports = reportsByPlayer.get(reportedId);
    if (reports == null) {
      reports = new HashMap<>();
      reportsByPlayer.put(reportedId, reports);
    }

    reports.put(reporterId, reportTime);

    checkReports(report, reportedId, reportTime);
  }

  private void checkReports(Report report, UUID reportedId, long reportTime) {
    Map<UUID, Long> reports = reportsByPlayer.get(reportedId);
    if (reports != null) {
      int numReports = 0;
      for (long time : reports.values()) {
        if ((reportTime - time) <= (config.getReportWindowMinutes() * 60 * 1000)) {
          numReports++;
        }
      }

      if (numReports >= config.getReportThreshold()) {
        discord.sendReportPing(report, numReports);
        reportsByPlayer.remove(reportedId); // remove the reports after the ping is sent
      }
    }
  }
}
