package tc.oc.occ.cheaty.anticheat;

import ac.grim.grimac.player.GrimPlayer;
import java.util.function.BooleanSupplier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;

public class GrimParticipationListener implements Listener {

  private final Plugin plugin;
  private final GrimManager grimManager;

  public GrimParticipationListener(Plugin plugin, GrimManager grimManager) {
    this.plugin = plugin;
    this.grimManager = grimManager;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    new ResolverTask(
            () -> {
              if (!player.isOnline()) return true;
              if (player.spigot().getCollidesWithEntities()) return true;

              GrimPlayer grimPlayer = grimManager.getGrimPlayer(player);
              if (grimPlayer == null) return false;

              grimManager.setPlayerBypass(player, true);
              return true;
            })
        .runTaskTimer(plugin, 1L, 5L);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onParticipantSpawn(ParticipantSpawnEvent event) {
    grimManager.setPlayerBypass(event.getPlayer().getBukkit(), false);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onParticipantDespawn(ParticipantDespawnEvent event) {
    grimManager.setPlayerBypass(event.getPlayer().getBukkit(), true);
  }

  public static class ResolverTask extends BukkitRunnable {

    private final BooleanSupplier task;
    private int attempts = 0;

    public ResolverTask(BooleanSupplier task) {
      this.task = task;
    }

    @Override
    public void run() {
      if (task.getAsBoolean() || attempts++ >= 20) this.cancel();
    }
  }
}
