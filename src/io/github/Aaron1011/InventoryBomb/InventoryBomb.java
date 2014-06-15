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
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.permissions.BroadcastPermissions;

import de.ntcomputer.minecraft.controllablemobs.*;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;

public final class InventoryBomb extends JavaPlugin implements Listener {
	
	HashMap<ItemStack, HashMap<String, Object>> bombs;
	HashMap<Item, HashMap<String, Object>> droppedBombs;
	ItemStack bombItem = new ItemStack(Material.BLAZE_ROD);
	private ItemStack globalBomb;
	//HashMap<ItemStack, HashMap<String, Object>> addBombs;
	//HashMap<ItemStack, HashMap<String, Object>> removeBombs;

	@Override
	public void onDisable() {
		this.bombs = new HashMap<ItemStack, HashMap<String, Object>>();
		//this.addBombs = new HashMap<ItemStack, HashMap<String, Object>>();
		//this.removeBombs = new HashMap<ItemStack, HashMap<String, Object>>();
		this.droppedBombs = new HashMap<Item, HashMap<String, Object>>();
	}

	@Override
	public void onEnable() {
		this.bombs = new HashMap<ItemStack, HashMap<String, Object>>();
		//this.addBombs = new HashMap<ItemStack, HashMap<String, Object>>();
		//this.removeBombs = new HashMap<ItemStack, HashMap<String, Object>>();
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
					Bukkit.broadcastMessage("Time: " + time);
					
					
					

					data.put("Timer", time);
					if (time <= 0) {
						Bukkit.broadcastMessage("Boom!");

						//if (data.containsKey("Owner")) {
						Player player = (Player) data.get("Owner");
						player.playSound(player.getLocation(), Sound.EXPLODE, 1, 5);
						player.sendMessage("Your bomb blew up!");
						player.getInventory().remove(item);
						player.damage(0.0);
						//}
						/*else if (data.containsKey("Item")) {
							Item bomb = (Item) data.get("Item");
							bomb.getWorld().createExplosion(bomb.getLocation(), 1);
							bomb.remove();
						}*/
						if (bombs.containsKey(item)) {
							Bukkit.broadcastMessage("Yes! - bomb!");
						}
						else {
							Bukkit.broadcastMessage("No! - bomb!");
							Bukkit.broadcastMessage(bombs.keySet().toString());
						}
						bombs.remove(item);
						continue;
					}

					
					//if (data.containsKey("Owner")) {
					Player player = (Player) data.get("Owner");
					player.playSound(player.getLocation(), Sound.CLICK, 1, 5);
					//}
					
					/*if (data.containsKey("Item")) {
						Item drop = ((Item) data.get("Item"));
						data.remove(drop);
						ItemStack stack = drop.getItemStack();
						ItemMeta meta = stack.getItemMeta();
						meta.setDisplayName(getName(time));
						stack.setItemMeta(meta);
						drop.setItemStack(stack);
						drop.setFireTicks(20);
						data.put("Item", drop);
						
					}*/
					
					ItemStack inInventory = player.getInventory().getItem(player.getInventory().first(item));
					if (inInventory != null) {
						Bukkit.broadcastMessage("Found it!");
						item = inInventory;
					}
					else {
						Bukkit.broadcastMessage("Aww!");
					}
					
					ItemMeta meta = item.getItemMeta();
					
					bombs.remove(item);
					meta.setDisplayName(getName(time));

					item.setItemMeta(meta);
					bombs.put(item, data);
					
					if (item != null) {
						Bukkit.broadcastMessage("Global!");
						item.setItemMeta(meta);
					}
					

				
				/*bombs.putAll(addBombs);
				for (ItemStack key : removeBombs.keySet()) {
					bombs.remove(key);
				}*/
				/*for (ItemStack key : removeBombs.keySet()) {
					bombs.remove(key);
				}
				bombs.putAll(addBombs);
				
				addBombs.clear();
				removeBombs.clear();*/
					
			}
			for (Map.Entry<Item, HashMap<String, Object>> entry : droppedBombs.entrySet()) {
				HashMap<String, Object> data = entry.getValue();
				int time = ((int) data.get("Timer")) - 1;
				
				Item bomb = entry.getKey();
				Bukkit.broadcastMessage("Time: " + time);
				
				
				

				data.put("Timer", time);
				if (time <= 0) {
					Bukkit.broadcastMessage("Boom!");

					/*//if (data.containsKey("Owner")) {
					Player player = (Player) data.get("Owner");
					player.playSound(player.getLocation(), Sound.EXPLODE, 1, 5);
					player.sendMessage("Your bomb blew up!");
					player.getInventory().remove(item);
					player.damage(0.0);
					//}*/
					/*else if (data.containsKey("Item")) {
						Item bomb = (Item) data.get("Item");
						bomb.getWorld().createExplosion(bomb.getLocation(), 1);
						bomb.remove();
					}*/
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
				
					//Item drop = ((Item) data.get("Item"));
				ItemStack stack = bomb.getItemStack();
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(getName(time));
				stack.setItemMeta(meta);
				bomb.setItemStack(stack);
				bomb.setFireTicks(20);
				//data.put("Item", drop);
				
			}

			}
			
		}, 0L, 20L);
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			final Player player = (Player) event.getEntity();
			//event.getDrops().add(item);
			ItemStack item = createBomb();
			
			event.getDrops().add(item);
			
			Bukkit.broadcastMessage("Hello!");
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					for (Entity e: player.getNearbyEntities(4,4,4)) {
						if (e.getType() == EntityType.DROPPED_ITEM) {
							Bukkit.broadcastMessage("Dropped item!");
							Item i = (Item) e;
							if (bombs.containsKey(i.getItemStack())) {
								HashMap<String, Object> data = bombs.get(i.getItemStack());
								bombs.remove(i.getItemStack());
								droppedBombs.put(i, data);
								i.setPickupDelay(0);
								Bukkit.broadcastMessage("Adding!");
								//bombs.get(i.getItemStack()).put("Item", i);
							}
						}
					}
				}
			});
		}
	}
	
	/*@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getItem().getType() == Material.BLAZE_ROD) {
			Bukkit.broadcastMessage("Hi");
			event.getItem().addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
			ItemStack drop = new ItemStack(event.getItem());
			ItemMeta meta = drop.getItemMeta();
			meta.setDisplayName("Hello world!");
			drop.setItemMeta(meta);
			Item item = event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), drop);
			item.setFireTicks(40);
		}
	}*/
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		ItemStack item = event.getItem().getItemStack();
		Bukkit.broadcastMessage("Current stack: " + item.toString());
		Bukkit.broadcastMessage("All bombs: " + bombs.keySet());
		
		//item.setAmount(20);
		Bukkit.broadcastMessage("20 items!");
		//item.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
		
		event.setCancelled(true);
		event.getPlayer().getInventory().addItem(item);
		event.getItem().remove();
		
		
		
		if (droppedBombs.containsKey(event.getItem())) {
			HashMap<String, Object> data = droppedBombs.get(event.getItem());
			data.put("Owner", event.getPlayer());
			droppedBombs.remove(event.getItem());
			bombs.put(item, data);
			globalBomb = item;
			Bukkit.broadcastMessage("Updated owner: " + bombs.get(item).containsKey("Owner"));
		}
		else {
			Bukkit.broadcastMessage("Nope!");
		}
		
		
		
		/*if (bombs.containsKey(item)) {
			//bombs.get(item).put("Owner", event.getPlayer());
			HashMap<String, Object> data = bombs.get(item);
			removeBombs.put(item, null);
			data.put("Owner", event.getPlayer());
			bombs.put(item, data);
			Bukkit.broadcastMessage("Updated owner: " + bombs.get(item).containsKey("Owner"));
		}*/
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Item item = event.getItemDrop();
		if (bombs.containsKey(item.getItemStack())) {
			//item.setPickupDelay(2);
			Bukkit.broadcastMessage("Dropped!");
			HashMap<String, Object> data = bombs.get(item.getItemStack());
			bombs.remove(item.getItemStack());
			droppedBombs.put(item, data);
		}

	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("giveBomb")) {
			if (args.length == 1) {
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
		Bukkit.broadcastMessage("Time called: " + time);
		return ChatColor.RESET + "" + ChatColor.RED + "Bomb! " +  ChatColor.YELLOW + time +  ChatColor.RESET + " seconds";
	}
}