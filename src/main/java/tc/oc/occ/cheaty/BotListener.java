package tc.oc.occ.cheaty;

import dev.pgm.community.events.PlayerReportEvent;
import net.climaxmc.autokiller.events.AutoKillCheatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.occ.cheaty.DiscordBot.RelayType;

public class BotListener implements Listener {

  private final DiscordBot bot;
  private final BotConfig config;

  public BotListener(DiscordBot bot, BotConfig config) {
    this.bot = bot;
    this.config = config;
  }

  @EventHandler
  public void onPlayerReport(PlayerReportEvent event) {
    bot.sendReport(event);
  }

  @EventHandler
  public void onAutoKillerViolation(AutoKillCheatEvent event) {
    bot.sendRelay(event.getAlert(), RelayType.AUTOKILL);
  }
}
