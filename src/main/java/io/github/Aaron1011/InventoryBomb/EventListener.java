package io.github.Aaron1011.InventoryBomb;

import java.util.concurrent.ConcurrentHashMap;

import net.milkbowl.vault.economy.EconomyResponse;

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

			if (plugin.economyEnabled) {
				if (player.hasPermission("InventoryBomb.skipCharge")) {
					player.sendMessage("Giving you a bomb for free!");
				}
				else {
					//boolean once = plugin.getConfig().getBoolean("bomb.cost.once");
					double cost = plugin.getConfig().getDouble("bomb.cost.price");
					plugin.getLogger().info(String.format("Charging %s %s for dropping a bomb", player.getName(), cost));
					if (!plugin.econ.has(player, cost)) {
						plugin.getLogger().severe(String.format("Player %s can't afford to pay for a bomb! Cancelling", player.getName()));
						player.sendMessage("You can't afford a bomb!");
						return;
					}
					EconomyResponse response = plugin.econ.withdrawPlayer(player, cost);
					switch (response.type) {
						case FAILURE:
							plugin.getLogger().severe(String.format("Charging player %s failed - %s", player.getName(), response.errorMessage));
							break;
						case NOT_IMPLEMENTED:
							plugin.getLogger().severe(String.format("Charging player %s failed - economy plugin doesn't support withdrawing money"));
							break;
						case SUCCESS:
							plugin.getLogger().info(String.format("Successfully charged player %s %s", player.getName(), cost));
							break;
					}
				}
			}
			
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
