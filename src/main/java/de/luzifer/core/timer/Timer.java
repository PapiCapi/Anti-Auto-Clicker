package de.luzifer.core.timer;

import de.luzifer.core.Core;
import de.luzifer.core.api.check.Check;
import de.luzifer.core.api.manager.CheckManager;
import de.luzifer.core.api.player.User;
import de.luzifer.core.api.profile.storage.DataContainer;
import de.luzifer.core.utils.Variables;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Timer implements Runnable {

    HashMap<UUID, DataContainer> containerHashMap = new HashMap<>();

    public void run() {

        for(Player all : Bukkit.getOnlinePlayers()) {

            User user = User.get(all.getUniqueId());

            if(user.isFrozen())
                Variables.PUNISHED.forEach(var -> all.sendMessage(Core.prefix + var.replace("&", "§")));

            user.getClicksAverageList().add(user.getClicks());
            user.getClicksAverageCheckList().add(user.getAverage());

            sendActionBar(user);

            dataContainer(user);

            for(Check check : CheckManager.getChecks())
                check.execute(user);

            cleanUp(user);
        }
        Core.deleteLogs();
    }

    private void sendActionBar(User user) {
        if(user.getChecked() != null) {

            String message1 = "§4§l" + user.getChecked().getName();
            String message2;

            if(Variables.allowedClicks - user.getChecked().getClicks() <= 8) {
                if(!(Variables.allowedClicks - user.getChecked().getClicks() <= 0)) {
                    message2 = " §e§l-> §cClicks : §c§l" + user.getChecked().getClicks() + " §6Average : §6§l" + user.getChecked().getAverage();
                } else {
                    message2 = " §e§l-> §cClicks : §4§l" + user.getChecked().getClicks()+ " §6Average : §6§l" + user.getChecked().getAverage();
                }
            } else {
                message2 = " §e§l-> §cClicks : §a§l" + user.getChecked().getClicks()+ " §6Average : §6§l" + user.getChecked().getAverage();
            }

            message2 = message2 + " §6VL: §e" + user.getChecked().getViolations();

            if(Core.lowTPS) {
                message2 = " §e§l-> §c§lCannot be checked -> §4§lLowTPS";
            }

            if(Variables.pingChecker) {
                if(user.getChecked().getPing() >= Variables.highestAllowedPing) {
                    message2 = " §e§l-> §c§lCannot be checked -> §4§lPing §8(§4" + user.getChecked().getPing() + "§8)";
                }
            }

            if(Variables.bypass) {
                if(user.getChecked().isBypassed()) {
                    message2 = " §e§l-> §c§lCannot be checked -> §4§lBypassed";
                }
            }

            Core.sendActionBar(user.getPlayer(), message1 + message2);
        }
    }

    private void cleanUp(User user) {

        if(Variables.sanctionateAtViolations > 0) {
            if(user.getViolations() >= Variables.sanctionateAtViolations) {
                user.sanction(false);
                user.clearViolations();
            }
        }

        if(user.clearViolations != 60*Variables.clearVLMinutes) {
            user.clearViolations++;
        } else {
            user.clearViolations();
            user.clearViolations = 0;
        }

        if(user.isRestricted()) {
            user.setRestricted(false);
        }

        if(user.getClicksAverageCheckList().size() >= Variables.clickAverageOfSeconds) {
            user.getClicksAverageCheckList().remove(0);
        }

        if(user.getClicksAverageList().size() >= Variables.clickAverageOfSeconds) {
            user.getClicksAverageList().remove(0);
        }
        user.setClicks(0);
    }

    private void dataContainer(User user) {
        if(!containerHashMap.containsKey(user.getPlayer().getUniqueId())) {
            containerHashMap.put(user.getPlayer().getUniqueId(), new DataContainer(user, Variables.storeAsManyData));
        }

        if(!containerHashMap.get(user.getPlayer().getUniqueId()).isFinish()) {
            DataContainer dataContainer = containerHashMap.get(user.getPlayer().getUniqueId());
            if(Variables.doNotStoreNothing) {
                if(user.getClicks() != 0) {
                    dataContainer.collectData();
                }
            } else {
                dataContainer.collectData();
            }
            containerHashMap.put(user.getPlayer().getUniqueId(), dataContainer);
        } else {
            containerHashMap.remove(user.getPlayer().getUniqueId());
        }

        if(!user.getProfile().getDataContainers().isEmpty()) {
            user.getProfile().checkForContainer();
        }
    }
}
