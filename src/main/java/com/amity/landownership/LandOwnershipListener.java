package com.amity.landownership;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class LandOwnershipListener implements Listener {
	
	private final LandOwnership plugin;
	private Logger log;
	private HashMap<String, Land> chunks;
	private HashMap<String, LandGroup> groups;
	private HashMap<String, String> currentChunkOwner = new HashMap<String, String>();

	
	public LandOwnershipListener(LandOwnership pl) {
		plugin = pl;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		chunks = plugin.getChunks();
		groups = plugin.getGroups();
		
		log = plugin.getLogger();
		
		log.info("LandOwnershipListener enabled.");
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		String id = ChunkID.get(event.getBlock());
		if (!this.hasPerm(player, id)) {
			player.sendMessage(ChatColor.RED + "You can't build here.");
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractEvent(PlayerInteractEvent event) {
		switch (event.getAction()) {
		case RIGHT_CLICK_BLOCK:
		case PHYSICAL:
			Player player = event.getPlayer();
			String id = ChunkID.get(event.getClickedBlock());

			if (!this.hasPerm(player, id) && !this.getToggle(Toggle.Public, id)) {
				player.sendMessage(ChatColor.RED + "You can't use that.");
				event.setCancelled(true);
			}

			break;
			default:
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		String id = ChunkID.get(event.getBlock());
		
		if (!this.hasPerm(player, id)) {
			player.sendMessage(ChatColor.RED + "You can't break here.");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		onPlayerUseBucket(event);
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		onPlayerUseBucket(event);
	}
	
	private void onPlayerUseBucket(PlayerBucketEvent event) {
		Player player = event.getPlayer();
		String id = ChunkID.get(event.getBlockClicked());
		
		if (chunks.containsKey(id) && !chunks.get(id).isMember(player)) {
			player.sendMessage(ChatColor.RED + "You can't do that here!");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onExplosions(EntityExplodeEvent event) {
		Iterator<Block> iter = event.blockList().iterator();
		while (iter.hasNext()) {
			Block block = iter.next();
			String id = ChunkID.get(block);

			switch (event.getEntityType()) {
			case CREEPER:
				if (!this.getToggle(Toggle.CreeperExplosions,id))
					iter.remove();
				break;
			case GHAST:
				if (!this.getToggle(Toggle.GhastExplosions,id))
					iter.remove();
				break;
			case MINECART_TNT:
			case PRIMED_TNT:
				if (!this.getToggle(Toggle.TntExplosions,id))
					iter.remove();
				break;
			case WITHER:
				if (!this.getToggle(Toggle.WitherExplosions,id))
					iter.remove();
				break;
			default:
			}

		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		
		if (event.getEntity() instanceof Animals)
			return;
		
		String id = ChunkID.get(event.getLocation());
		if (!this.getToggle(Toggle.MobSpawning,id))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		String id = ChunkID.get(event.getBlock());
		
		if (!this.getToggle(Toggle.EndermanPickup,id)) {
			if (event.getEntityType() == EntityType.ENDERMAN)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent  event) {
		/*
		if (event.getEntity() instanceof Player) {

			Entity damager = event.getDamager();

			if (event.getCause() == DamageCause.PROJECTILE) {
				damager = ((Projectile)damager).getShooter();
			}
			
			Player defender = (Player) event.getEntity();
			Player attacker = (Player) damager.getLocation();
			
			String id1 = ChunkID.get(defender);
			String id2 = ChunkID.get(attacker);
			
			if (!this.getToggle(Toggle.Pvp,id1) || !this.getToggle(Toggle.Pvp,id2)) {
				String message = ChatColor.RED + "PVP is disabled in this plot.";
				defender.sendMessage(message);
				attacker.sendMessage(message);
				event.setCancelled(true);
			}
		}
		*/
		
		if (event.isCancelled()) return;
		if (event.getEntity() instanceof Player) {
		
			Player defender = (Player) event.getEntity();
			
			if (event instanceof EntityDamageByEntityEvent) {
				
				Player attacker = null;
				EntityDamageByEntityEvent dmgEvent = (EntityDamageByEntityEvent) event;
				
				if (dmgEvent.getDamager() instanceof Projectile) {
					Projectile projectile = (Projectile) dmgEvent.getDamager();
					if (projectile.getShooter() instanceof Player) {
						attacker = (Player) projectile.getShooter();
					} else return;
					
				} else if (dmgEvent.getDamager() instanceof Player) {
					attacker = (Player) dmgEvent.getDamager();
				} else return;
				
				String id1 = ChunkID.get(defender);
				String id2 = ChunkID.get(attacker);
				
				if (!this.getToggle(Toggle.Pvp,id1) || !this.getToggle(Toggle.Pvp,id2)) {
					String message = ChatColor.RED + "PVP is disabled in this plot.";
					defender.sendMessage(message);
					attacker.sendMessage(message);
					event.setCancelled(true);
				}				
			}
		} else if (event.getEntity() instanceof Animals) {
			
			Animals animal = (Animals) event.getEntity();
			String id = ChunkID.get(animal);
			
				if (event instanceof EntityDamageByEntityEvent) {
				if (!chunks.containsKey(id))
					return;
				
				Player attacker = null;
				Land chunk = chunks.get(id);
				
				EntityDamageByEntityEvent dmgEvent = (EntityDamageByEntityEvent) event;				
				
				if (dmgEvent.getDamager() instanceof Projectile) {
					Projectile projectile = (Projectile) dmgEvent.getDamager();
					if (projectile.getShooter() instanceof Player) {
						attacker = (Player) projectile.getShooter();
					} else return;
					
				} else if (dmgEvent.getDamager() instanceof Player) {
					attacker = (Player) dmgEvent.getDamager();
				} else return;			
				
				if (!chunk.isMember(attacker)) {
					attacker.sendMessage(ChatColor.RED + "You are not allowed to attack animals here.");
					event.setCancelled(true);
				}
			}
		}
	}
	
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		if (event.getBlock().getType() == Material.STATIONARY_WATER || event.getBlock().getType() == Material.STATIONARY_LAVA) {
			Toggle toggle = event.getBlock().getType() == Material.STATIONARY_WATER ? Toggle.WaterFlow : Toggle.LavaFlow;
			String id1 = ChunkID.get(event.getBlock());
			String id2 = ChunkID.get(event.getToBlock());
			
			if (!this.getToggle(toggle,id1) || !this.getToggle(toggle,id2))
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event) {
		if (event.getSource().getType() == Material.FIRE) {
			String id = ChunkID.get(event.getBlock());
			
			if (chunks.containsKey(id) && !chunks.get(id).getToggle(Toggle.FireSpread))
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.getFrom().getChunk() != event.getTo().getChunk()) {
			notifyPlayer(event.getPlayer(), event.getTo());
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getFrom().getChunk() != event.getTo().getChunk()) {
			notifyPlayer(event.getPlayer(), event.getTo());
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		notifyPlayer(event.getPlayer());
	}
	
	public void notifyPlayer(Player player, Location loc) {
		String id = ChunkID.get(loc);
		String ownerName = "Wild";
		if (!currentChunkOwner.containsKey(player.getName()))
			currentChunkOwner.put(player.getName(), ownerName);
		
		if (chunks.containsKey(id)) 
			ownerName = chunks.get(id).getOwner();
			
		
		if (!ownerName.equals(currentChunkOwner.get(player.getName()))) {
			currentChunkOwner.put(player.getName(), ownerName);
			if (ownerName.equals("Wild")) {
				player.sendMessage("You have crossed into the wild!");
			}
			else if (chunks.get(id).isServerLand()) {
				player.sendMessage("You have crossed into server land!");
			}
			else {
				player.sendMessage(String.format("You have crossed into land owned by %s!", ownerName));
			}
		}
	}
	
	public void notifyPlayer(Player player) {
		notifyPlayer(player, player.getLocation());
	}
	
	private boolean hasPerm(Player player, String chunkID) {
		
		if (!chunks.containsKey(chunkID)) 
			return true;
		
		Land chunk = chunks.get(chunkID);
		
		// test plot
		if (chunk.isMember(player)) 
			return true;
		
		// loop through groups
		for(LandGroup group : groups.values()) {
			if (group.getLands().contains(chunkID) &&  group.isMember(player))
				return true;
		}
		
		return false;
		
	}
		
	private boolean getToggle(Toggle toggle, String chunkID) {
		
		if (!chunks.containsKey(chunkID)) 
			return true;
		
		Land chunk = chunks.get(chunkID);
		
		// test plot
		if (chunk.getToggle(toggle)) 
			return true;
		
		// loop through groups
		for(LandGroup group : groups.values()) {
			if (group.getLands().contains(chunkID) && group.getToggle(toggle))
				return true;
		}
		
		return false;
		
	}
	
	
	
}