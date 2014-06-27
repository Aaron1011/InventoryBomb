package io.github.Aaron1011.InventoryBomb;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
	private InventoryBomb plugin;

	public EventListener(InventoryBomb plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			final Player player = (Player) event.getEntity();
			ItemStack item = plugin.createBomb(player, plugin.delay);
			
			event.getDrops().add(item);
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					for (Entity e: player.getNearbyEntities(4,4,4)) {
						if (e.getType() == EntityType.DROPPED_ITEM) {
							Item i = (Item) e;
							if (plugin.bombs.containsKey(i.getItemStack())) {
								ConcurrentHashMap<String, Object> data = plugin.bombs.get(i.getItemStack());
								plugin.bombs.remove(i.getItemStack());
								plugin.droppedBombs.put(i, data);
								i.setPickupDelay(0);
							}
						}
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Item item = event.getItemDrop();
		if (plugin.bombs.containsKey(item.getItemStack())) {
			ConcurrentHashMap<String, Object> data = plugin.bombs.get(item.getItemStack());
			plugin.bombs.remove(item.getItemStack());
			plugin.droppedBombs.put(item, data);
		}

	}
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		ItemStack item = event.getItem().getItemStack();
		
		event.setCancelled(true);
		event.getPlayer().getInventory().addItem(item);
		event.getItem().remove();
		
		
		
		if (plugin.droppedBombs.containsKey(event.getItem())) {
			plugin.getLogger().info("Picked up a bomb");
			ConcurrentHashMap<String, Object> data = plugin.droppedBombs.get(event.getItem());
			data.put("Owner", event.getPlayer());
			plugin.droppedBombs.remove(event.getItem());
			plugin.bombs.put(item, data);
		}
		
	}

}
