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
	HashMap<Item, ItemStack> droppedBombs;
	ItemStack bombItem = new ItemStack(Material.BLAZE_ROD);

	@Override
	public void onDisable() {
		this.bombs = new HashMap<ItemStack, HashMap<String, Object>>();
		this.droppedBombs = new HashMap<Item, ItemStack>();
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
							player.playSound(player.getLocation(), Sound.EXPLODE, 1, 5);
							player.sendMessage("Your bomb blew up!");
							player.getInventory().remove(item);
							player.damage(0.0);
						}
						else if (data.containsKey("Item")) {
							Item bomb = (Item) data.get("Item");
							bomb.getWorld().createExplosion(bomb.getLocation(), 1);
							bomb.remove();
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
					
					if (data.containsKey("Owner")) {
						Player player = (Player) data.get("Owner");
						player.playSound(player.getLocation(), Sound.CLICK, 1, 5);
						player.getInventory().addItem(new ItemStack(Material.LEAVES));
					}
					
					if (data.containsKey("Item")) {
						/*Item drop = ((Item) data.get("Item"));
						data.remove(drop);
						ItemStack stack = drop.getItemStack();
						ItemMeta meta = stack.getItemMeta();
						meta.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Bomb!" +  ChatColor.YELLOW + time +  ChatColor.RESET + " seconds");
						stack.setItemMeta(meta);
						drop.setItemStack(stack);
						drop.setFireTicks(20);
						data.put("Item", drop);*/
						
					}
					

					/*ItemMeta meta = item.getItemMeta();
					
					bombs.remove(item);
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
								Bukkit.broadcastMessage("Adding!");
								bombs.get(i.getItemStack()).put("Item", i);
							}
						}
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getItem().getType() == Material.BLAZE_ROD) {
			Bukkit.broadcastMessage("Hi");
			event.getItem().addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
			ItemStack drop = new ItemStack(event.getItem());
			drop.setAmount(1);
			Item item = event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), drop);
			item.setFireTicks(40);
		}
	}
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		ItemStack item = event.getItem().getItemStack();
		Bukkit.broadcastMessage("Current stack: " + item.toString());
		Bukkit.broadcastMessage("All bombs: " + bombs.keySet());
		
		if (bombs.containsKey(item)) {
			bombs.get(item).put("Owner", event.getPlayer());
			Bukkit.broadcastMessage("Updated owner: " + bombs.get(item).containsKey("Owner"));
		}
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Item item = event.getItemDrop();
		if (bombs.containsKey(item.getItemStack())) {
			Bukkit.broadcastMessage("Dropped!");
			bombs.get(item.getItemStack()).put("Item", item);
			bombs.get(item.getItemStack()).remove("Owner");
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
		map.put("Timer", 5);
		meta.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Bomb! " +  ChatColor.YELLOW + "5" +  ChatColor.RESET + " seconds");
		item.setItemMeta(meta);
		bombs.put(item, map);
		return item;
	}
	
	private ItemStack createBomb(Player player) {
		ItemStack bomb = createBomb();
		HashMap<String, Object> map = bombs.get(bomb);
		map.put("Owner", player);
		bombs.put(bomb, map);
		return bomb;
	}
}