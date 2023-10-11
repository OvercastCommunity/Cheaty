package tc.oc.occ.cheaty.anticheat;

import ac.grim.grimac.GrimAbstractAPI;
import ac.grim.grimac.GrimUser;
import ac.grim.grimac.player.GrimPlayer;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class GrimManager {

  private GrimAbstractAPI api;

  public GrimManager() {
    GrimAbstractAPI api = null;
    RegisteredServiceProvider<GrimAbstractAPI> provider =
        Bukkit.getServicesManager().getRegistration(GrimAbstractAPI.class);
    if (provider != null) api = provider.getProvider();
    if (api == null) return;

    this.api = api;
  }

  public boolean setPlayerBypass(Player player, boolean shouldBypass) {
    GrimUser grimUser = api.getGrimUser(player);
    if (!(grimUser instanceof GrimPlayer)) return false;

    GrimPlayer grimPlayer = (GrimPlayer) grimUser;
    grimPlayer.disableGrim = shouldBypass;

    return true;
  }

  public boolean togglePlayerBypass(Player player) {
    GrimPlayer grimPlayer = getGrimPlayer(player);
    if (grimPlayer == null) return true;

    grimPlayer.disableGrim = !grimPlayer.disableGrim;

    return grimPlayer.disableGrim;
  }

  public @Nullable GrimPlayer getGrimPlayer(Player player) {
    GrimUser grimUser = api.getGrimUser(player);
    if (!(grimUser instanceof GrimPlayer)) return null;

    return (GrimPlayer) grimUser;
  }
}
