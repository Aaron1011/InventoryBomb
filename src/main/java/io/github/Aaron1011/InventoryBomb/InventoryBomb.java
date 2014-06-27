package io.github.Aaron1011.InventoryBomb;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public final class InventoryBomb extends JavaPlugin implements Listener {
	
	ConcurrentHashMap<ItemStack, ConcurrentHashMap<String, Object>> bombs;
	ConcurrentHashMap<Item, ConcurrentHashMap<String, Object>> droppedBombs;
	ItemStack bombItem = new ItemStack(Material.BLAZE_ROD);
	int delay;
	//private float power;

	@Override
	public void onDisable() {
		this.bombs = new ConcurrentHashMap<ItemStack, ConcurrentHashMap<String, Object>>();
		this.droppedBombs = new ConcurrentHashMap<Item, ConcurrentHashMap<String, Object>>();
	}

	@Override
	public void onEnable() {
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		
		this.saveDefaultConfig();

		delay = getConfig().getInt("bomb.delay");
		//power = (float) getConfig().getDouble("bomb.power");
		
		this.bombs = new ConcurrentHashMap<ItemStack, ConcurrentHashMap<String, Object>>();
		this.droppedBombs = new ConcurrentHashMap<Item, ConcurrentHashMap<String, Object>>();

		this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
		ItemMeta meta = bombItem.getItemMeta();
		meta.setDisplayName(getName(delay));

		this.bombItem.setItemMeta(meta);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				for (Map.Entry<ItemStack, ConcurrentHashMap<String, Object>> entry : bombs.entrySet()) {				
					ConcurrentHashMap<String, Object> data = entry.getValue();
					int time = ((int) data.get("Timer")) - 1;

					ItemStack item = entry.getKey();
					
					Player player = (Player) data.get("Owner");

					data.put("Timer", time);
					if (time <= 0) {
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


					player.playSound(player.getLocation(), Sound.CLICK, 1, 5);


					ItemStack inInventory = player.getInventory().getItem(player.getInventory().first(item));
					if (inInventory == null) {
						getLogger().warning("A bomb is missing from the inventory it should be in");
						continue;
					}
					item = inInventory;
					
					updateBomb(item, time, data);


				}
				for (Map.Entry<Item, ConcurrentHashMap<String, Object>> entry : droppedBombs.entrySet()) {
					ConcurrentHashMap<String, Object> data = entry.getValue();
					int time = ((int) data.get("Timer")) - 1;

					Item bomb = entry.getKey();

					data.put("Timer", time);
					if (time <= 0) {
						List<Map<?, ?>> actions = getConfig().getMapList("bomb.actions.dropped");
						for (Map<?,?> action: actions) {
							String type = (String) action.get("action");
							switch (type) {
								case "explode": {
									bomb.getWorld().createExplosion(bomb.getLocation(), Float.valueOf(action.get("power").toString()));
									break;
								}
								case "command": {
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ((String) action.get("command")).replaceAll("<bomber>", ((Player)data.get("Bomber")).getDisplayName()));
									break;
								}
								default: {
									getLogger().warning("Unknown action type: " + type);
								}
							}
						}
						
						bomb.remove();

						if (!droppedBombs.containsKey(bomb)) {
							getLogger().severe("Somehow, we've lost track of a bomb. This should NEVER happen - something is messed up");
							continue;
						}
						droppedBombs.remove(bomb);

					}

					ItemStack stack = bomb.getItemStack();
					ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName(getName(time));
					stack.setItemMeta(meta);
					bomb.setItemStack(stack);
				}

			}

		}, 0L, 20L);
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("giveBomb")) {
			if (!sender.hasPermission("inventorybomb.givebomb")) {
				sender.sendMessage("You don't have permission to use /givebomb!");
				return true;
			}
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					player.getInventory().addItem(createBomb(player, player, delay));
					return true;
				}
				else {
					sender.sendMessage("This command can only be run as a player");
				}
			}
			else if (args.length == 1) {
				int time = Integer.valueOf(args[0]);
				if (sender instanceof Player) {
					Player player = (Player) sender;
					player.getInventory().addItem(createBomb(player, player, time));
					return true;
				}
				else {
					sender.sendMessage("This command can only be run as a player");
				}
				
			}
			else if (args.length == 2) {
				int time = Integer.valueOf(args[0]);
				@SuppressWarnings("deprecation")
				Player player = Bukkit.getPlayer(args[1]);
				player.getInventory().addItem(createBomb(player, player, time));
				return true;
			}	
		}
		return false;
	}

	public ItemStack createBomb(Player bomber, int time) {
		ItemStack item = new ItemStack(bombItem);
		ItemMeta meta = item.getItemMeta();
		ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
		map.put("Timer", time);
		map.put("Bomber", bomber);
		meta.setDisplayName(getName(time));
		item.setItemMeta(meta);
		bombs.put(item, map);
		return item;
	}
	
	public ItemStack createBomb(Player bomber, Player player, int time) {
		ItemStack bomb = createBomb(bomber, time);
		ConcurrentHashMap<String, Object> map = bombs.get(bomb);
		bombs.remove(bomb);
		map.put("Owner", player);
		bombs.put(bomb, map);
		return bomb;
	}
	
	private void updateBomb(ItemStack item, int time, ConcurrentHashMap<String, Object> data) {
		ItemMeta meta = item.getItemMeta();

		bombs.remove(item);
		meta.setDisplayName(getName(time));

		item.setItemMeta(meta);
		bombs.put(item, data);
	}
	
	
	private String getName(int time) {
		return ChatColor.RESET + "" + ChatColor.RED + "Bomb! " +  ChatColor.YELLOW + time +  ChatColor.RESET + " seconds";
	}
}