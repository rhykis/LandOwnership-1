package com.amity.landownership;

public enum Toggle {
 	
	MobSpawning,
	CreeperExplosions,
	GhastExplosions,
	WitherExplosions,
	EndermanPickup,
	TntExplosions,
	Pvp,
	FireSpread,
	LavaFlow,
	WaterFlow,
	Public,
	PublicChests,
	PublicCrafting,
	PublicTriggers,
	AllowPotions;
	
	public static Toggle getByName(String str) {
		for (Toggle toggle : Toggle.values()) {
			if (str.equalsIgnoreCase(toggle.name()))
				return toggle;
		}
		
		return Toggle.valueOf(str);
	}
	
	public String toString() {
		return this.name();
	}
}