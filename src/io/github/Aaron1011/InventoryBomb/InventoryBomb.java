package io.github.Aaron1011.InventoryBomb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.v1_7_R3.EntityTypes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.permissions.BroadcastPermissions;

import de.ntcomputer.minecraft.controllablemobs.*;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;

public final class InventoryBomb extends JavaPlugin implements Listener {
	
	HashMap<ItemStack, HashMap<String, Object>> bombs;
	ItemStack bombItem = new ItemStack(Material.BLAZE_ROD);

	@Override
	public void onDisable() {
		this.bombs = new HashMap<ItemStack, HashMap<String, Object>>();
	}

	@Override
	public void onEnable() {
		this.bombs = new HashMap<ItemStack, HashMap<String, Object>>();
		this.getServer().getPluginManager().registerEvents(this, this);
		ItemMeta meta = bombItem.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Bomb!");
		
		this.bombItem.setItemMeta(meta);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				for (Map.Entry<ItemStack, HashMap<String, Object>> entry : bombs.entrySet()) {				
					HashMap<String, Object> data = entry.getValue();
					int time = ((int) data.get("Timer")) - 1;
					
					ItemStack item = entry.getKey();
					Bukkit.broadcastMessage("Time: " + time);
					

					data.put("Timer", time);
					if (time <= 0) {
						Bukkit.broadcastMessage("Boom!");

						if (data.containsKey("Owner")) {
							Player player = (Player) data.get("Owner");
							player.sendMessage("Your bomb blew up!");
							player.damage(0.0);
						}
						if (bombs.containsKey(item)) {
							Bukkit.broadcastMessage("Yes!");
						}
						else {
							Bukkit.broadcastMessage("No!");
							Bukkit.broadcastMessage(bombs.keySet().toString());
						}
						bombs.remove(item);
						continue;

					}
					

					/*ItemMeta meta = item.getItemMeta();
					
					meta.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Bomb!" +  ChatColor.YELLOW + time +  ChatColor.RESET + " seconds");
					item.setItemMeta(meta);
					bombs.put(item, data);*/
				}
			}
		}, 0L, 20L);
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			ItemStack item = new ItemStack(bombItem);
			ItemMeta meta = item.getItemMeta();
			event.getDrops().add(item);
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("Timer", 5);
			/*meta.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Bomb!" +  ChatColor.YELLOW + "5" +  ChatColor.RESET + " seconds");
			item.setItemMeta(meta);*/
			

			bombs.put(item, map);
			Bukkit.broadcastMessage("Hello!");
		}
	}
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		ItemStack item = event.getItem().getItemStack();
		
		if (bombs.containsKey(item)) {
			bombs.get(item).put("Owner", event.getPlayer());
		}
	}
}