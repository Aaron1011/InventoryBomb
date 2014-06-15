package io.github.Aaron1011.InventoryBomb;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class InventoryBomb extends JavaPlugin implements Listener {
	
	HashMap<ItemStack, HashMap<String, Object>> bombs;
	HashMap<Item, HashMap<String, Object>> droppedBombs;
	ItemStack bombItem = new ItemStack(Material.BLAZE_ROD);

	@Override
	public void onDisable() {
		this.bombs = new HashMap<ItemStack, HashMap<String, Object>>();
		this.droppedBombs = new HashMap<Item, HashMap<String, Object>>();
	}

	@Override
	public void onEnable() {
		this.bombs = new HashMap<ItemStack, HashMap<String, Object>>();
		this.droppedBombs = new HashMap<Item, HashMap<String, Object>>();
		
		this.getServer().getPluginManager().registerEvents(this, this);
		ItemMeta meta = bombItem.getItemMeta();
		meta.setDisplayName(getName(5));
		
		this.bombItem.setItemMeta(meta);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				for (Map.Entry<ItemStack, HashMap<String, Object>> entry : bombs.entrySet()) {				
					HashMap<String, Object> data = entry.getValue();
					int time = ((int) data.get("Timer")) - 1;
					
					ItemStack item = entry.getKey();

					data.put("Timer", time);
					if (time <= 0) {
						Player player = (Player) data.get("Owner");
						player.playSound(player.getLocation(), Sound.EXPLODE, 1, 5);
						player.sendMessage("Your bomb blew up!");
						player.getInventory().remove(item);
						player.damage(0.0);

						if (!bombs.containsKey(item)) {
							getLogger().severe("Somehow, we've lost track of a bomb. This should NEVER happen - something is messed up");
						}
						bombs.remove(item);
						continue;
					}

					
					Player player = (Player) data.get("Owner");
					player.playSound(player.getLocation(), Sound.CLICK, 1, 5);

					
					ItemStack inInventory = player.getInventory().getItem(player.getInventory().first(item));
					if (inInventory == null) {
						getLogger().warning("A bomb is missing from the inventory it should be in");
						continue;
					}
					item = inInventory;
					
					ItemMeta meta = item.getItemMeta();
					
					bombs.remove(item);
					meta.setDisplayName(getName(time));

					item.setItemMeta(meta);
					bombs.put(item, data);
					
					if (item != null) {
						item.setItemMeta(meta);
					}
					


					
			}
			for (Map.Entry<Item, HashMap<String, Object>> entry : droppedBombs.entrySet()) {
				HashMap<String, Object> data = entry.getValue();
				int time = ((int) data.get("Timer")) - 1;
				
				Item bomb = entry.getKey();
				Bukkit.broadcastMessage("Time: " + time);
				
				
				

				data.put("Timer", time);
				if (time <= 0) {
					Bukkit.broadcastMessage("Boom!");

					bomb.getWorld().createExplosion(bomb.getLocation(), 1);
					bomb.remove();
					
					if (droppedBombs.containsKey(bomb)) {
						Bukkit.broadcastMessage("Yes! - dropped bomb");
					}
					else {
						Bukkit.broadcastMessage("No! - dropped bomb");
						Bukkit.broadcastMessage(bombs.keySet().toString());
					}
					droppedBombs.remove(bomb);
					continue;
				}
				
				ItemStack stack = bomb.getItemStack();
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(getName(time));
				stack.setItemMeta(meta);
				bomb.setItemStack(stack);
				bomb.setFireTicks(20);
				
			}

			}
			
		}, 0L, 20L);
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			final Player player = (Player) event.getEntity();
			ItemStack item = createBomb();
			
			event.getDrops().add(item);
			
			Bukkit.broadcastMessage("Hello!");
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					for (Entity e: player.getNearbyEntities(4,4,4)) {
						if (e.getType() == EntityType.DROPPED_ITEM) {
							Item i = (Item) e;
							if (bombs.containsKey(i.getItemStack())) {
								HashMap<String, Object> data = bombs.get(i.getItemStack());
								bombs.remove(i.getItemStack());
								droppedBombs.put(i, data);
								i.setPickupDelay(0);
							}
						}
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		ItemStack item = event.getItem().getItemStack();
		
		event.setCancelled(true);
		event.getPlayer().getInventory().addItem(item);
		event.getItem().remove();
		
		
		
		if (droppedBombs.containsKey(event.getItem())) {
			getLogger().info("Picked up a bomb");
			HashMap<String, Object> data = droppedBombs.get(event.getItem());
			data.put("Owner", event.getPlayer());
			droppedBombs.remove(event.getItem());
			bombs.put(item, data);
		}
		
		
		
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Item item = event.getItemDrop();
		if (bombs.containsKey(item.getItemStack())) {
			HashMap<String, Object> data = bombs.get(item.getItemStack());
			bombs.remove(item.getItemStack());
			droppedBombs.put(item, data);
		}

	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("giveBomb")) {
			if (args.length == 1) {
				@SuppressWarnings("deprecation")
				Player player = Bukkit.getPlayer(args[0]);
				player.getInventory().addItem(createBomb(player));
			}
			else {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					player.getInventory().addItem(createBomb(player));
				}
				else {
					sender.sendMessage("This command can only be run as a player");
				}
			}
			return true;
				
		}
		return false;
	}

	private ItemStack createBomb() {
		ItemStack item = new ItemStack(bombItem);
		ItemMeta meta = item.getItemMeta();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Timer", 10);
		meta.setDisplayName(getName(5));
		item.setItemMeta(meta);
		bombs.put(item, map);
		return item;
	}
	
	private ItemStack createBomb(Player player) {
		ItemStack bomb = createBomb();
		HashMap<String, Object> map = bombs.get(bomb);
		bombs.remove(bomb);
		map.put("Owner", player);
		bombs.put(bomb, map);
		return bomb;
	}
	
	private String getName(int time) {
		return ChatColor.RESET + "" + ChatColor.RED + "Bomb! " +  ChatColor.YELLOW + time +  ChatColor.RESET + " seconds";
	}
}