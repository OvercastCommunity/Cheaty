package tc.oc.occ.cheaty.anticheat;

import ac.grim.grimac.player.GrimPlayer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
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
              if (isPlayerCollidable(player)) return true;

              GrimPlayer grimPlayer = grimManager.getGrimPlayer(player);
              if (grimPlayer == null) return false;

              grimManager.setPlayerBypass(player, true);
              return true;
            })
        .runTaskTimer(plugin, 1L, 5L);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onParticipantSpawn(ParticipantSpawnEvent event) {
    Player bukkitPlayer = event.getPlayer().getBukkit();
    if (bukkitPlayer == null) return;

    grimManager.setPlayerBypass(bukkitPlayer, false);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onParticipantDespawn(ParticipantDespawnEvent event) {
    Player bukkitPlayer = event.getPlayer().getBukkit();
    if (bukkitPlayer == null) return;

    grimManager.setPlayerBypass(bukkitPlayer, true);
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

  private static final boolean IS_SPORTPAPER =
      Bukkit.getServer().getVersion().contains("SportPaper");
  private static final MethodHandle IS_COLLIDABLE = getIsCollidable();

  private static MethodHandle getIsCollidable() {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    if (IS_SPORTPAPER) {
      try {
        return lookup.findVirtual(
            Player.Spigot.class, "getCollidesWithEntities", MethodType.methodType(boolean.class));
      } catch (NoSuchMethodException | IllegalAccessException ignored) {
      }
    }

    try {
      return lookup.findVirtual(Player.class, "isCollidable", MethodType.methodType(boolean.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      return null;
    }
  }

  private boolean isPlayerCollidable(Player player) {
    if (IS_COLLIDABLE == null) {
      return true;
    }

    try {
      Object target = IS_SPORTPAPER ? player.spigot() : player;
      return (boolean) IS_COLLIDABLE.invoke(target);
    } catch (Throwable e) {
      return true;
    }
  }
}
