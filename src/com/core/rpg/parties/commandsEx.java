package com.core.rpg.parties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: MayoDwarf
 * Date: 6/29/14
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class commandsEx implements CommandExecutor {
    Main m;
    public commandsEx(Main m) {
        this.m = m;
    }
    private HashMap<UUID, String> teamInvited = new HashMap<UUID, String>();
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, final String[] args) {
        switch (args.length) {
            case 0:
                //Help menu
                if(cmd.getName().equalsIgnoreCase("p") || cmd.getName().equalsIgnoreCase("party")) {
                    if(sender instanceof Player) {
                        Player p = (Player) sender;
                        p.sendMessage(ChatColor.GOLD+"-------------------- rpgParties --------------------");
                        p.sendMessage(ChatColor.GRAY+"-- \"party\" can also be shortened to \"p\" --\n" +
                                "./party - commands\n" +
                                "./party create <name> - Create a new party with the specified name\n" +
                                "./party invite <player> - Invite the specified player to your team\n" +
                                "./party disband - Disband your party\n" +
                                "./party leave - Leave your party\n" +
                                "./party info - Display your party info\n" +
                                "./party accept - Accept a party invite\n" +
                                "./party decline - Decline a party invite");
                        p.sendMessage(ChatColor.GOLD+"---------------------------------------------------");
                    }
                }
                break;
            case 1:
                // Decline, Leave, Disband, Accept
                if(cmd.getName().equalsIgnoreCase("p") || cmd.getName().equalsIgnoreCase("party")) {
                    if(sender instanceof Player) {
                        Player p = (Player) sender;
                        if(args[0].equalsIgnoreCase("leave")) {
                            if(m.hasParty(p)) {
                                if(!m.isLeader(p, m.getParty(p))) {
                                    p.sendMessage(ChatColor.GOLD+"You have left the party "+ChatColor.RED+m.getParty(p)+ChatColor.GOLD+"!");
                            m.removeMember(p, m.getParty(p));
                                } else {
                                    p.sendMessage(ChatColor.RED+"You can not leave a party you are leader of! Disband it if you would like!");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED+"You can only leave a party if you are in a party!");
                            }
                        } else
                            if(args[0].equalsIgnoreCase("disband")) {
                                //disband team
                                if(m.isLeader(p, m.getParty(p))) {
                                p.sendMessage(ChatColor.GOLD+"You have disbanded your party!");
                                m.disband(m.getParty(p));
                                } else {
                                    p.sendMessage(ChatColor.RED+"Only the party leader can disband the party!");
                                }
                            } else
                                if(args[0].equalsIgnoreCase("decline")) {
                                    //decline invite
                                    if(teamInvited.containsKey(p.getUniqueId())) {
                                        p.sendMessage(ChatColor.GOLD+"You have declined the party invite from "+ChatColor.RED+teamInvited.get(p.getUniqueId())+ChatColor.GOLD+"!");
                                        m.getLeader(teamInvited.get(p.getUniqueId())).sendMessage(ChatColor.GOLD+""+p.getName()+ChatColor.RED+" has declined your party invite!");
                                        teamInvited.remove(p.getUniqueId());
                                    } else {
                                        p.sendMessage(ChatColor.RED+"You have no pending party invites!");
                                    }
                                } else
                                    if(args[0].equalsIgnoreCase("accept")) {
                                        //accept invite
                                        if(teamInvited.containsKey(p.getUniqueId())) {
                                            if(!m.hasParty(p)) {
                                            m.addMember(p, teamInvited.get(p.getUniqueId()));
                                            teamInvited.remove(p.getUniqueId());
                                            } else {
                                                p.sendMessage(ChatColor.RED+"You can not join another party if you are already in a party!");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED+"You have no pending party invites!");
                                        }
                                    } else
                                        if(args[0].equalsIgnoreCase("info")) {
                                            //Return their team info
                                            if(m.hasParty(p)) {
                                                m.partyInfo(p, m.getParty(p));
                                            } else {
                                                p.sendMessage(ChatColor.RED+"You can only run this command if you are in a party!");
                                            }
                                        }
                    }
                }
                break;
            case 2:
                // Invite, Create
                if(cmd.getName().equalsIgnoreCase("p") || cmd.getName().equalsIgnoreCase("party")) {
                    if(sender instanceof Player) {
                        final Player p = (Player) sender;
                    if(args[0].equalsIgnoreCase("create")) {
                        if(!m.hasParty(p)) {
                            if(!m.partyExist(args[1])) {
                        m.makeParty(args[1], p);
                        p.sendMessage(ChatColor.GOLD+"You created the party "+ChatColor.RED+""+args[1]+ChatColor.GOLD+"!");
                                } else {
                                p.sendMessage(ChatColor.RED+"That party name already exists! Please think of another name!");
                                }
                            } else {
                            p.sendMessage(ChatColor.RED+"You already have a party! Leave it to create a new party!");
                            }
                        } else
                        if(args[0].equalsIgnoreCase("invite")) {
                            if(!args[1].equals(p.getName())) {
                            if(m.hasParty(p)) {
                            if(m.isLeader(p, m.getParty(p))) {
                            if(Bukkit.getPlayer(args[1]) !=null) {
                            if(!teamInvited.containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
                                teamInvited.put(Bukkit.getPlayer(args[1]).getUniqueId(), m.getParty(p));
                                Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GOLD + "" + m.getParty(p) + ChatColor.GRAY + " has invited you to join their party! Party invite expires in 30 seconds!");
                                p.sendMessage(ChatColor.GOLD+"You have invited "+ChatColor.RED+""+args[1]+ChatColor.GOLD+" to your party!");
                                Bukkit.getScheduler().scheduleSyncDelayedTask(m, new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if(teamInvited.containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
                                        teamInvited.remove(Bukkit.getPlayer(args[1]).getUniqueId());
                                        Bukkit.getPlayer(args[1]).sendMessage(ChatColor.RED+"Your party invite to "+ChatColor.GOLD+""+m.getParty(p)+ChatColor.RED+" has expired!");
                                        }
                                    }
                                }, 20*30);
                            } else {
                                p.sendMessage(ChatColor.RED+"This player already has a pending invite!");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED+"This player is not currently online!");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED+"Only the leader can invite players to the party!");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED+"You do not have a party!");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED+"You can not invite yourself to your party!");
                            }
                        }
                    }
                }
                break;
        }
        return true;
    }
}
