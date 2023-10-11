package tc.oc.occ.cheaty.anticheat;

import co.aikar.commands.BukkitCommandManager;
import tc.oc.occ.cheaty.Cheaty;
import tc.oc.occ.cheaty.commands.GrimCommands;

public class GrimUtil {

  public static GrimManager createManager() {
    try {
      Class.forName("ac.grim.grimac.GrimAC");
      return new GrimManager();
    } catch (ClassNotFoundException ignored) {
      return null;
    }
  }

  public static void createCommands(GrimManager grimManager, BukkitCommandManager commands) {
    if (grimManager == null) return;

    commands.registerCommand(new GrimCommands(grimManager));
  }

  public static void registerListeners(GrimManager grimManager, Cheaty plugin) {
    if (grimManager == null) return;

    plugin
        .getServer()
        .getPluginManager()
        .registerEvents(new GrimParticipationListener(plugin, grimManager), plugin);
  }
}
