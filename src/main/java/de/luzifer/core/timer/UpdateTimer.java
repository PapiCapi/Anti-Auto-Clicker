package de.luzifer.core.timer;

import de.luzifer.core.Core;
import de.luzifer.core.utils.UpdateChecker;
import de.luzifer.core.utils.Variables;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UpdateTimer implements Runnable{

    private final Core core;
    public UpdateTimer(Core core) {
        this.core = core;
    }

    @Override
    public void run() {
        Bukkit.getScheduler().runTaskAsynchronously(core, () -> {
            UpdateChecker updateChecker = new UpdateChecker(core);
            if(!updateChecker.check()) {
                Bukkit.getScheduler().runTask(core, () -> {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        
                        if(!hasSubPermissions(player, "checkupdate"))
                            continue;
                        
                        player.sendMessage(Core.prefix + "§aAn update is available!");
                        player.sendMessage(Core.prefix + "§c" + Core.getInstance().getDescription().getVersion() + " §e-> §a" + updateChecker.getLatestVersion());
                    }
                });
            }
        });
    }
    
    private boolean hasSubPermissions(Player player, String perms) {
        return player.hasPermission(Variables.perms + "." + perms) || hasPermission(player);
    }
    
    private boolean hasPermission(Player player) {
        return player.hasPermission(Variables.perms + ".*") || player.isOp();
    }
}
