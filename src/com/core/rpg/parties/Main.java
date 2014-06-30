package com.core.rpg.parties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: MayoDwarf
 * Date: 6/29/14
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class Main extends JavaPlugin implements Listener {
    YamlConfiguration yCon;
    File f = new File(getDataFolder(), "parties.yml");
    commandsEx cX;
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        cX = new commandsEx(this);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        yCon = YamlConfiguration.loadConfiguration(f);
        getCommand("party").setExecutor(cX);
        getCommand("p").setExecutor(cX);
        if(!yCon.contains("Parties")) {
        yCon.set("Parties", new ArrayList<String>());
        }
        try {
            yCon.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDisable() {}
    @EventHandler
    public void onExperience(PlayerExpChangeEvent e) {
        if(this.hasParty(e.getPlayer())) {
        this.setPartyXP(this.getParty(e.getPlayer()), e.getAmount()+e.getPlayer().getLevel());
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if(this.hasParty(p)) {
        p.setLevel(this.getPartyXP(this.getParty(p)));
        }
    }
    public void scoreboardPlayer(Player p) {      //TODO WIP - Work In Progress
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective(""+p.getDisplayName(), "dummy");
        obj.setDisplayName(""+this.getParty(p));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    public void partyInfo(Player p, String party) {
        String manager = yCon.getString("Party." + party + ".ManagerName");
        List<String> members = (List<String>) yCon.getList("Party."+party+".MembersName");
        String name = yCon.getString("Party."+party+".Name");
        p.sendMessage(ChatColor.GREEN+""+ChatColor.BOLD+""+name);
        p.sendMessage(ChatColor.DARK_PURPLE+""+ChatColor.BOLD+"     Manager:");
        p.sendMessage(ChatColor.DARK_PURPLE+"     "+manager);
        p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"Members:");
        if(members.size() >= 1) {
        for(String strings : members) {
            if(Bukkit.getPlayer(strings) !=null) {
                p.sendMessage(ChatColor.GREEN+""+strings);
            } else {
                p.sendMessage(ChatColor.RED+""+strings);
                }
            }
        } else {
        p.sendMessage(ChatColor.YELLOW+"NONE");
        }
    }
    public Player getLeader(String party) {
        return Bukkit.getPlayer(UUID.fromString(yCon.getString("Party."+party+".Manager")));
    }
    public void makeParty(String name, Player p) {
        yCon.set("Party."+name+".Manager", p.getUniqueId().toString());
        yCon.set("Party."+name+".ManagerName", p.getName());
        yCon.set("Party."+name+".Members", new ArrayList<UUID>());
        yCon.set("Party."+name+".MembersName", new ArrayList<String>());
        yCon.set("Party."+name+".Name", name);
        yCon.set("Party."+name+".XP", 0);
        List<String> list = (List<String>) yCon.getList("Parties", new ArrayList<String>());
        list.add(name);
        yCon.set("Parties", list);
        try {
            yCon.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disband(String name) {
        yCon.set("Party."+name+".Manager", null);
        yCon.set("Party."+name+".ManagerName", null);
        yCon.set("Party."+name+".Members", null);
        yCon.set("Party."+name+".MembersName", null);
        yCon.set("Party."+name+".Name", name);
        yCon.set("Party."+name+".XP", null);
        yCon.set("Party."+name, null);
        List<String> list = (List<String>) yCon.getList("Parties", new ArrayList<String>());
        list.remove(name);
        yCon.set("Parties", list);
        try {
            yCon.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean partyExist(String name) {
        if(yCon.getList("Parties").contains(name))
            return true;
        else
            return false;
    }

    public void addMember(Player p, String party) {
        List<String> list = (List<String>) yCon.getList("Party."+party+".Members");
        if(getConfig().getInt("PartyLimit") > list.size()) {
        List<String> listString = (List<String>) yCon.getList("Party."+party+".MembersName");
        list.add(p.getUniqueId().toString());
        listString.add(p.getName());
        yCon.set("Party."+party+".Members", list);
        yCon.set("Party."+party+".MembersName", listString);
            p.sendMessage(ChatColor.GOLD+"You have joined the party "+ChatColor.RED+""+party+ChatColor.GOLD+"!");
            getLeader(party).sendMessage(ChatColor.RED+""+p.getName()+ChatColor.GOLD+" has accepted your party invite!");
        try {
            yCon.save(f);
        } catch (IOException e) {
            e.printStackTrace();
            }
        } else {
            p.sendMessage(ChatColor.RED+"The team has the maximum amount of players for a party!");
        }
    }

    public boolean hasParty(Player p) {
        for(String parties : (List<String>) yCon.getList("Parties")) {
            if(yCon.getList("Party."+parties+".Members").contains(p.getUniqueId().toString()) || yCon.getString("Party."+parties+".Manager").equals(p.getUniqueId().toString())) {
                return true;
            }
        }
        return false;
    }

    public String getParty(Player p) {
        for(String parties : (List<String>) yCon.getList("Parties")) {
            if(yCon.getList("Party."+parties+".Members").contains(p.getUniqueId().toString()) || yCon.getString("Party."+parties+".Manager").equals(p.getUniqueId().toString())) {
                return parties;
            }
        }
        return null;
    }

    public void removeMember(Player p, String party) {
        List<String> list = (List<String>) yCon.getList("Party."+party+".Members");
        List<String> listString = (List<String>) yCon.getList("Party."+party+".MembersName");
        list.remove(p.getUniqueId().toString());
        listString.remove(p.getName());
        yCon.set("Party."+party+".Members", list);
        yCon.set("Party."+party+".MembersName", listString);
        try {
            yCon.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLeader(Player p, String party) {
        if(yCon.getString("Party."+party+".Manager").equals(p.getUniqueId().toString())) {
            return true;
        } else {
            return false;
        }
    }

    public void setPartyXP(String party, int xp) {
        yCon.set("Party."+party+".XP", xp);
        try {
            yCon.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getPartyXP(String party) {
        return yCon.getInt("Party." + party + ".XP");
    }
}
