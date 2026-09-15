/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Bones;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AdrenalineSurge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AnkhInvulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BasicBuffs;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.CardSelectorBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.HS2000_Shield;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Drowsy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Foresight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GunSwap;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ItemBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SiriusHeart;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SuperAiFlight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.StarShield;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.GSH18Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TalentSecondSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elphelt;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DEL;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.Constants;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CheckedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Battery;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardAffect;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardSelector;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.FinalCard;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.IntensifySkill;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Bulk;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Swiftness;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.LloydsBeacon;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.RedBook;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.fairyitems.Commander;
import com.shatteredpixel.shatteredpixeldungeon.items.journal.Guidebook;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDivineInspiration;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DMR.AK47;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ShootGun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RabbitBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.WeakFloorRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.levels.triggers.Trigger;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AlchemyScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.SurfaceScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ElpheltSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class Hero extends Char {

	{
		actPriority = HERO_PRIO;
		
		alignment = Alignment.ALLY;
	}
	
	public static final int MAX_LEVEL = 40;

	public static final int STARTING_STR = 10;
	
	private static final float TIME_TO_REST		    = 1f;
	private static final float TIME_TO_SEARCH	    = 2f;
	private static final float HUNGER_FOR_SEARCH	= 6f;
	
	public HeroClass heroClass = HeroClass.ROGUE;
	public HeroSubClass subClass = HeroSubClass.NONE;
	public ArmorAbility armorAbility = null;
	public ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
	public LinkedHashMap<Talent, Talent> metamorphedTalents = new LinkedHashMap<>();
    public LinkedHashMap<Talent, Integer> addTalents = new LinkedHashMap<>();
	
	public int attackSkill = 10;
	public int defenseSkill = 5;

	public boolean ready = false;
	private boolean damageInterrupt = true;
	public HeroAction curAction = null;
	public HeroAction lastAction = null;
	
	public boolean resting = false;
	
	public Belongings belongings;
	
	public int STR;
	
	public float awareness;
	
	public int lvl = 1;
	public int exp = 0;
	
	public int HTBoost = 0;
	
	private ArrayList<Mob> visibleEnemies;
	public ArrayList<Mob> getVisibleEnemies(){
		if (visibleEnemies != null)
			return new ArrayList<>(visibleEnemies);
		return new ArrayList<>();
	}

	//This list is maintained so that some logic checks can be skipped
	// for enemies we know we aren't seeing normally, resultign in better performance
	public ArrayList<Mob> mindVisionEnemies = new ArrayList<>();

	public Hero(HeroClass heroClass){
		this();
		this.heroClass = heroClass;
	}
	public Hero() {
		super();

		HP = HT = 20;
		STR = STARTING_STR;
		
		belongings = new Belongings( this );
		
		visibleEnemies = new ArrayList<>();
	}
	@Override
	public String info(){
		return heroClass.desc();
	}
	public void updateHT( boolean boostHP ){
		int curHT = HT;
		int baseHT;
		
		// GSH18角色：初始血量为默认的80%，10级后升级获得的生命值为默认的50%
		if (heroClass == HeroClass.GSH18) {
			if (lvl <= 10) {
				baseHT = 20 + 5*(lvl-1);
				HT = Math.round(baseHT * 0.8f);
			} else {
				baseHT = 20 + 5*9 + 5*(lvl-10)/2; // 10级及以下每级5点，11级及以上每级2.5点（向下取整为2点）
				HT = Math.round(baseHT * 0.8f);
			}
		} else {
			HT = 20 + 5*(lvl-1);
		}
		
		HT += HTBoost;
		float multiplier = RingOfMight.HTMultiplier(this);
		HT = Math.round(multiplier * HT);
		
		if (buff(ElixirOfMight.HTBoost.class) != null){
			HT += buff(ElixirOfMight.HTBoost.class).boost();
		}
		
		if (boostHP){
			HP += Math.max(HT - curHT, 0);
		}
		HP = Math.min(HP, HT);
	}

	public int STR() {
		int strBonus = 0;

		strBonus += RingOfMight.strengthBonus( this );
		
		AdrenalineSurge buff = buff(AdrenalineSurge.class);
		if (buff != null){
			strBonus += buff.boost();
		}
		
		/*561角色：当饥饿度低于饥饿状态时，力量+1；当饥饿度处于饥饿状态且力量大于等于13时，力量-1，现在已经不需要了
		Hunger hunger = buff(Hunger.class);
		if (hunger != null && heroClass == HeroClass.TYPE561 ){
            if( !hunger.isHungry()){
                strBonus +=1;
            }else if (hunger.isStarving() && STR >= 13) {
                strBonus -= 1;
            }
		}
		*/

		return STR + strBonus;
	}

	private static final String CLASS       = "class";
	private static final String SUBCLASS    = "subClass";
	private static final String ABILITY     = "armorAbility";

	private static final String ATTACK		= "attackSkill";
	private static final String DEFENSE		= "defenseSkill";
	private static final String STRENGTH	= "STR";
	private static final String LEVEL		= "lvl";
	private static final String EXPERIENCE	= "exp";
	private static final String HTBOOST     = "htboost";
    private static final String HUNGER      = "hunger";
	
	@Override
	public void storeInBundle( Bundle bundle ) {

		super.storeInBundle( bundle );

		bundle.put( CLASS, heroClass );
		bundle.put( SUBCLASS, subClass );
		bundle.put( ABILITY, armorAbility );
		Talent.storeTalentsInBundle( bundle, this );
		
		bundle.put( ATTACK, attackSkill );
		bundle.put( DEFENSE, defenseSkill );
		
		bundle.put( STRENGTH, STR );
		
		bundle.put( LEVEL, lvl );
		bundle.put( EXPERIENCE, exp );
		
		// 保存饥饿值
		Hunger hunger = buff(Hunger.class);
		if (hunger != null) {
			bundle.put(HUNGER, (int)hunger.full());
		}
		
		bundle.put( HTBOOST, HTBoost );

		belongings.storeInBundle( bundle );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {

		lvl = bundle.getInt( LEVEL );
		exp = bundle.getInt( EXPERIENCE );

		HTBoost = bundle.getInt(HTBOOST);

		super.restoreFromBundle( bundle );

		heroClass = bundle.getEnum( CLASS, HeroClass.class, HeroClass.rename );
		subClass = bundle.getEnum( SUBCLASS, HeroSubClass.class, HeroSubClass.rename );
		armorAbility = (ArmorAbility)bundle.get( ABILITY );
		Talent.restoreTalentsFromBundle( bundle, this, Rankings.restoreInRanking );
		
		attackSkill = bundle.getInt( ATTACK );
		defenseSkill = bundle.getInt( DEFENSE );
		
		STR = bundle.getInt( STRENGTH );
		belongings.restoreFromBundle( bundle );

		restoreUpdateByVersion(bundle);
	}
	private void restoreUpdateByVersion( Bundle bundle ) {
		if (Dungeon.version < 671) {
			// 如果是未来之星职业，自动添加天狼星心脏buff
			if (subClass == HeroSubClass.FUTURE_STAR) {
				Buff.affect(this, SiriusHeart.class);
			}
			// 未来之星读档时补挂副手换枪指示器
			if (subClass == HeroSubClass.FUTURE_STAR) {
				Buff.affect(this, GunSwap.class);
			}
			// 超级小爱读档时补挂飞行切换指示器
			if (subClass == HeroSubClass.SUPER_AI) {
				Buff.affect(this, SuperAiFlight.class);
			}
			CardAffect.kiloTimesVersionUpdate();
			CardSelector selector = null;
			for (Item item : belongings)
				if (item instanceof CardSelector)
					selector = (CardSelector) item;
			if (selector != null)
				Buff.affect(this, CardSelectorBuff.class).item(selector.detachAll(belongings.backpack));
		}
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.level = bundle.getInt( LEVEL );
		info.str = bundle.getInt( STRENGTH );
		info.exp = bundle.getInt( EXPERIENCE );
		info.hp = bundle.getInt( Char.TAG_HP );
		info.ht = bundle.getInt( Char.TAG_HT );
		info.shld = bundle.getInt( Char.TAG_SHLD );
        info.hunger = bundle.getInt(HUNGER);
		info.heroClass = bundle.getEnum( CLASS, HeroClass.class, HeroClass.rename );
		info.subClass = bundle.getEnum( SUBCLASS, HeroSubClass.class );
		Belongings.preview( info, bundle );
	}

	public boolean hasTalent( Talent talent ){
		return pointsInTalent(talent) > 0;
	}

    public int pointsInTalent( Talent talent ){
        for (LinkedHashMap<Talent, Integer> tier : talents){
            for (Talent f : tier.keySet()){
                if (f == talent) return tier.get(f);
            }
        }
        return 0;
    }

    public boolean hasTalentA( Talent talent ){
        if (TierOfTalent.Tier(talent) >= 3 && Dungeon.hero.subClass == HeroSubClass.NONE)
            return false;

        if (TierOfTalent.Tier(talent) >= 4 && Dungeon.hero.armorAbility == null)
            return false;

        int need = Talent.tierLevelThresholds[TierOfTalent.Tier(talent)] - 1;
        return Dungeon.hero.lvl >= need && pointsInTalentA(talent) >= 0;
    }
    public boolean hasTalentB( Talent talent){
        return pointsInTalentA(talent) >= 0;
    }

    public int pointsInTalentA( Talent talent ){
        for (LinkedHashMap<Talent, Integer> tier : talents){
            for (Talent f : tier.keySet()){
                if (f == talent) return tier.get(f);
            }
        }
        return -1;
    }
	public void upgradeTalent( Talent talent ){
		for (LinkedHashMap<Talent, Integer> tier : talents){
			for (Talent f : tier.keySet()){
				if (f == talent) tier.put(talent, tier.get(talent)+1);
			}
		}
		Talent.onTalentUpgraded(this, talent);
	}

	public int talentPointsSpent(int tier){
		int total = 0;
		for (int i : talents.get(tier-1).values()){
			total += i;
		}
		return total;
	}

	public int talentPointsAvailable(int tier){
		if (lvl < (Talent.tierLevelThresholds[tier] - 1)
			|| (tier == 4 && armorAbility == null)) {
			return 0;
		} else if (lvl >= Talent.tierLevelThresholds[tier+1]){
			return Talent.tierLevelThresholds[tier+1] - Talent.tierLevelThresholds[tier] - talentPointsSpent(tier) + bonusTalentPoints(tier);
		} else {
			return 1 + lvl - Talent.tierLevelThresholds[tier] - talentPointsSpent(tier) + bonusTalentPoints(tier);
		}
	}

	public int bonusTalentPoints(int tier){
        int point;
		if (lvl < (Talent.tierLevelThresholds[tier]-1)
				|| (tier == 4 && armorAbility == null))
			point = 0;
		else if (buff(PotionOfDivineInspiration.DivineInspirationTracker.class) != null)
            point = buff(PotionOfDivineInspiration.DivineInspirationTracker.class).getBoosted(tier);
		else
            point = 0;
		if (tier == 4)
			return point;

        int lvl = Dungeon.hero.pointsInTalent(Talent.HIGH_EDUCATION);
		point += lvl / 3
				+ (lvl%3 >= tier ? 1 : 0);
        return point;
	}
	
	public String className() {
        if (subClass != null && subClass != HeroSubClass.NONE && subClass != HeroSubClass.EMPTY)
            return subClass.title();
        if (heroClass != null && heroClass != HeroClass.NONE)
		    return heroClass.title();
        return "404 NOT FOUND";
	}

	@Override
	public String name(){
		return className();
	}

	@Override
	public void hitSound(float pitch) {
		if ( belongings.weapon() != null ){
			belongings.weapon().hitSound(pitch);
		} else if (RingOfForce.getBuffedBonus(this, RingOfForce.Force.class) > 0) {
			//pitch deepens by 2.5% (additive) per point of strength, down to 75%
			super.hitSound( pitch * GameMath.gate( 0.75f, 1.25f - 0.025f*STR(), 1f) );
		} else {
			super.hitSound(pitch * 1.1f);
		}
	}

	@Override
	public boolean blockSound(float pitch) {
		if ( belongings.weapon() != null && belongings.weapon().defenseFactor(this) >= 4 ){
			Sample.INSTANCE.play( Assets.Sounds.HIT_PARRY, 1, pitch);
			return true;
		}
		return super.blockSound(pitch);
	}

	public void live() {
		for (Buff b : buffs()){
			if (!b.revivePersists) b.detach();
		}
		for (Item i : belongings){
			if (!i.keptThoughLostInvent) i.stopTrack();
		}
		Buff.affect( this, Regeneration.class );
		Buff.affect( this, Hunger.class ).satisfy(Hunger.minLevel);
        Buff.affect( this, TalentSecondSight.class).Set(0, 0);
        Buff.affect( this, Item.CooldownTracker.class );
	}
	
	public int tier() {
		if (belongings.armor() == null)
			return 0;
		else
			return belongings.armor().tier();
	}
	
	public boolean shoot( Char enemy, MissileWeapon wep ) {

		this.enemy = enemy;

		//temporarily set the hero's weapon to the missile weapon being used
		//TODO improve this!
		belongings.thrownWeapon = wep;
		boolean hit = attack( enemy );
		Invisibility.dispel();
		belongings.thrownWeapon = null;
		
		if (hit && subClass == HeroSubClass.GLADIATOR){
			Buff.affect( this, Combo.class ).hit( enemy );
		}

		return hit;
	}
	
	@Override
	public int attackSkill( Char target ) {
		KindOfWeapon wep = belongings.weapon();

		float accuracy = 1;
		accuracy *= RingOfAccuracy.accuracyMultiplier( this );

        BasicBuffs.Accuracy acc = Dungeon.hero.buff(BasicBuffs.Accuracy.class);
        if (acc != null)
            accuracy *= acc.percent();

		// GSH18天赋：短线作战命中加成 / 元气一餐必中（实现见 GSH18Talent）
		accuracy *= GSH18Talent.accuracyMultiplier(this, target);
		if (GSH18Talent.guaranteedHit(this)) {
			// 下次攻击必定命中，设置一个非常高的accuracy值，变成测试枪了（）
			return 1_000_000;
		}
        // 56天赋：1-4
        if (buff(Talent.Type56BookTracker.class) != null) {
            accuracy *= 1.1F + 0.2F * pointsInTalent(Talent.Type56_14);
        }
        // 56天赋：1-4V2
        if (buff(ShootGun.ShootTracker.class) != null) {
            accuracy *= 1.1F + 0.2F * pointsInTalent(Talent.Type56_14V2);
        }
		
		if (wep instanceof MissileWeapon){
			if (Dungeon.level.adjacent( pos, target.pos )) {
				accuracy *= (0.5f + 0.25f*pointsInTalent(Talent.POINT_BLANK));
			} else {
				accuracy *= 1.5f;
			}
		}
		
		if (wep != null) {
			return (int)(attackSkill * accuracy * wep.accuracyFactor( this ));
		} else {
			return (int)(attackSkill * accuracy);
		}
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		if (buff(Combo.ParryTracker.class) != null){
			if (canAttack(enemy)){
				Buff.affect(this, Combo.RiposteTracker.class).enemy = enemy;
			}
			return INFINITE_EVASION;
		}

		// GSH18天赋：敏捷移动闪避判定（实现见 GSH18Talent）
		if (GSH18Talent.tryEvade(this)){
			return INFINITE_EVASION;
		}
		
		float evasion = defenseSkill;

        BasicBuffs.Evasion eva = Dungeon.hero.buff(BasicBuffs.Evasion.class);
        if (eva != null)
            evasion *= eva.percent();

		evasion *= RingOfEvasion.evasionMultiplier( this );
		
		if (paralysed > 0) {
			evasion /= 2;
		}


		if (belongings.armor() != null)
            // Use the only or first
			evasion = belongings.armor().evasionFactor(this, evasion, false);

        if (belongings.SecondArmor() != null)
            evasion = belongings.SecondArmor().evasionFactor(this, evasion, true);

//        if (belongings.hasGlyph(Stone.class, this) && !((Stone)glyph).testingEvasion())
//            evasion = 0;
		//仅生效副护甲的闪避，似乎可接受？

		return Math.round(evasion);
	}

	@Override
	public String defenseVerb() {
		Combo.ParryTracker parry = buff(Combo.ParryTracker.class);
		if (parry == null){
			return super.defenseVerb();
		} else {
			parry.parried = true;
			if (buff(Combo.class).getComboCount() < 9 || pointsInTalent(Talent.ENHANCED_COMBO) < 2){
				parry.detach();
			}
			return Messages.get(Monk.class, "parried");
		}
	}

	@Override
	public int drRoll() {
		int dr = 0;

        if (belongings.armor() != null) {
            // Use the only or first
            int armDr = Random.NormalIntRange( belongings.armor().DRMin(), belongings.armor().DRMax());
            if (STR() < belongings.armor().STRReq()){
                armDr -= 2*(belongings.armor().STRReq() - STR());
            }
            if (armDr > 0) dr += armDr;
        }
        if (belongings.SecondArmor() != null) {
            int armDr = Random.NormalIntRange( belongings.SecondArmor().DRMin(), belongings.SecondArmor().DRMax());
            if (STR() < belongings.SecondArmor().STRReq()){
                armDr -= 2*(belongings.SecondArmor().STRReq() - STR());
            }
            armDr = Math.min( armDr, belongings.SecondArmor().tier * (1 + pointsInTalent( Talent.HOLD_FAST )));
            if (armDr > 0) dr += armDr;
        }
		if (belongings.weapon() != null)  {
			int wepDr = Random.NormalIntRange( 0 , belongings.weapon().defenseFactor( this ) );
			if (STR() < ((Weapon)belongings.weapon()).STRReq()){
				wepDr -= 2*(((Weapon)belongings.weapon()).STRReq() - STR());
			}
			if (wepDr > 0) dr += wepDr;
		}

		Barkskin bark = buff(Barkskin.class);
		if (bark!=null) dr+= Random.NormalIntRange(0,bark.level());
		
		return dr;
	}
	
	@Override
	public int damageRoll() {
		KindOfWeapon wep = belongings.weapon();
		int dmg;

		if (wep != null) {
			dmg = wep.damageRoll( this );
			if (!(wep instanceof MissileWeapon)) dmg += RingOfForce.armedDamageBonus(this);
		} else {
			dmg = RingOfForce.damageRoll(this);
		}
        if (Dungeon.hero.subClass == HeroSubClass.GUN_MASTER) {
            Hunger hunger = Dungeon.hero.buff(Hunger.class);
            if (hunger != null) {
                if (!hunger.isStarving()) {
                    dmg = Math.round(dmg * dmgMul());
                }
            }
        }
		if (dmg < 0) dmg = 0;
		
		return dmg;
	}
	private float dmgMul(){
        float mul = 1 ;
        Hunger hunger = Dungeon.hero.buff(Hunger.class);
        if (hunger==null)
            return 1;
		float full = hunger.full();
		mul += 0.00045F * full;
        if (Dungeon.hero.hasTalent(Talent.GUN_1V2)) {
            mul += 0.00015f * Dungeon.hero.pointsInTalent(Talent.GUN_1V2) * full;
            mul = Math.min(1.35f, mul);
        }
        return mul;
    }
	@Override
	public float speed() {

		float speed = super.speed();

		speed *= RingOfHaste.speedMultiplier(this);

		Ring.guessSignalRing(this, RingOfHaste.class, true);

		if (belongings.armor() != null) {
            speed = belongings.armor().speedFactor(this, speed);
        }
        if (belongings.SecondArmor() != null) {
            speed = belongings.SecondArmor().speedFactor(this, speed);
        }

		if (belongings.hasGlyph(Swiftness.class, this)) {
			boolean enemyNear = false;
			PathFinder.buildDistanceMap(pos, Dungeon.level.passable, 2);
			for (Char ch : Actor.chars()) {
				if (PathFinder.distance[ch.pos] != Integer.MAX_VALUE && alignment != ch.alignment) {
					enemyNear = true;
					break;
				}
			}
			if (!enemyNear) {
				speed *= (1.2f + 0.04f * belongings.GlyphLevel(Swiftness.class));
				if (wholeTime())
					belongings.guessArmorByGlyph(Swiftness.class);
			}
		}
		else if (belongings.hasGlyph(Flow.class, this) && Dungeon.level.water[pos]) {
			speed *= (2f + 0.25f * belongings.GlyphLevel(Flow.class));
			if (wholeTime())
				belongings.guessArmorByGlyph(Flow.class);
		}

		if (belongings.hasGlyph(Bulk.class, this) &&
				(Dungeon.level.map[pos] == Terrain.DOOR
						|| Dungeon.level.map[pos] == Terrain.OPEN_DOOR)) {
			speed /= 3f;
		}

		Momentum momentum = buff(Momentum.class);
		if (momentum != null){
			((HeroSprite)sprite).sprint( momentum.freerunning() ? 1.5f : 1f );
			speed *= momentum.speedMultiplier();
		} else {
			((HeroSprite)sprite).sprint( 1f );
		}

		NaturesPower.naturesPowerTracker natStrength = buff(NaturesPower.naturesPowerTracker.class);
		if (natStrength != null){
			speed *= (2f + 0.25f*pointsInTalent(Talent.GROWING_POWER));
		}
		
		// GSH18天赋：后勤支援——拥有星之护盾时增加移动速度（实现见 GSH18Talent）
		speed *= GSH18Talent.speedMultiplier(this);
		
		return speed;
		
	}

	public boolean canSurpriseAttack(){
		if (belongings.weapon() == null || !(belongings.weapon() instanceof Weapon))    return true;
		if (STR() < ((Weapon)belongings.weapon()).STRReq())                             return false;
		if (belongings.weapon() instanceof AK47 )   return false;
		//ak47无法偷袭了
		return true;
	}
	//未来之星专属：副手武器（HG手枪）存放在GunSwap换枪buff中
	private KindOfWeapon secondaryWeapon(){
		GunSwap swap = buff( GunSwap.class );
		if (swap == null) return null;
		return swap.getEquipment( KindOfWeapon.class );
	}

	//未来之星专属：敌人在副手HG武器射程内、但不在主手武器射程内时，应使用副手武器快速攻击
	private boolean shouldUseSecondary( Char enemy ){
		if (subClass != HeroSubClass.FUTURE_STAR) return false;
		KindOfWeapon second = secondaryWeapon();
		if (second == null || !second.hasTag( KindOfWeapon.Tag.HG )) return false;
		KindOfWeapon main = belongings.weapon();
		boolean mainCanReach = main != null && main.canReach( this, enemy.pos );
		boolean secondCanReach = second.canReach( this, enemy.pos );
		return secondCanReach && !mainCanReach;
	}

	public boolean canAttack(Char enemy){
		if (enemy == null || pos == enemy.pos || !Actor.chars().contains(enemy)) {
			return false;
		}

		KindOfWeapon wep = Dungeon.hero.belongings.weapon();
		//can always attack adjacent enemies
		if (Dungeon.level.adjacent(pos, enemy.pos)) {
			if (!(wep instanceof Weapon) || ((Weapon) wep).reach(this) > 0)
				//KindOfWeapon目前只有一个子类即Weapon，但还是补一个instanceOf
				//此处是为了让M4A1选择了FAL时禁用近战
				return true;
		}

		if (wep != null && wep.canReach(this, enemy.pos)){
			return true;
		}

		//未来之星：主手够不到时，若副手HG武器够得到，也允许攻击（快速攻击）
		return shouldUseSecondary( enemy );
	}
	
	public float attackDelay() {
		if (buff(Talent.LethalMomentumTracker.class) != null){
			buff(Talent.LethalMomentumTracker.class).detach();
			return 0;
		}

        float delay;
		KindOfWeapon weapon = belongings.weapon();
		if (weapon != null)
			delay = weapon.delayFactor( this );

		else
			//Normally putting furor speed on unarmed attacks would be unnecessary
			//But there's going to be that one guy who gets a furor+force ring combo
			//This is for that one guy, you shall get your fists of fury!
			delay = 1f/RingOfFuror.attackSpeedMultiplier(this);

		Ring.guessSignalRing(this, RingOfFuror.class, true);

        if ( buff(Adrenaline.class) != null) delay /= 1.5f;
        return delay;
	}

	@Override
	public void spend( float time ) {
		justMoved = false;
		TimekeepersHourglass.timeFreeze freeze = buff(TimekeepersHourglass.timeFreeze.class);
		if (freeze != null) {
			freeze.processTime(time);
			return;
		}
		
		Swiftthistle.TimeBubble bubble = buff(Swiftthistle.TimeBubble.class);
		if (bubble != null){
			bubble.processTime(time);
			return;
		}
		
		super.spend(time);
	}

    public void spendAndNext( float time ) {
        spendAndNext(time, false);
    }
    public void spendAndNext( float time, boolean strict ) {
        busy();
        if (strict)
            spendStrict( time );
        else
            spend( time );
        next();
    }

    @Override
    public CharSprite sprite() {
        int tier = 0;
        if (belongings != null && belongings.armor() != null)
            tier = belongings.armor().tier();
        return new HeroSprite(heroClass, tier);
    }

    @Override
	public boolean act() {
        Dungeon.GetSight();
		//calls to dungeon.observe will also update hero's local FOV.
		fieldOfView = Dungeon.level.heroFOV;

		if (buff(Endure.EndureTracker.class) != null){
			buff(Endure.EndureTracker.class).endEnduring();
		}
		
		if (!ready) {
			//do a full observe (including fog update) if not resting.
			if (!resting || buff(MindVision.class) != null || buff(Awareness.class) != null) {
				Dungeon.observe();
			} else {
				//otherwise just directly re-calculate FOV
				Dungeon.level.updateFieldOfView(this, fieldOfView);
			}
		}
		
		checkVisibleMobs();
		BuffIndicator.refreshHero();
		BuffIndicator.refreshBoss();

		if (paralysed > 0) {
			
			curAction = null;
			
			spendAndNext( TICK );
			return false;
		}
		
		boolean actResult;
		if (curAction == null) {
			
			if (resting) {
				spend( TIME_TO_REST );
				next();
			} else {
				ready();
			}
			
			actResult = false;
			
		} else {
			
			resting = false;
			
			ready = false;
			
			if (curAction instanceof HeroAction.Move) {
				actResult = actMove( (HeroAction.Move)curAction );
			} else if (curAction instanceof HeroAction.Interact) {
				actResult = actInteract( (HeroAction.Interact)curAction );
			} else if (curAction instanceof HeroAction.Buy) {
				actResult = actBuy( (HeroAction.Buy)curAction );
			}else if (curAction instanceof HeroAction.PickUp) {
				actResult = actPickUp( (HeroAction.PickUp)curAction );
			} else if (curAction instanceof HeroAction.OpenChest) {
				actResult = actOpenChest( (HeroAction.OpenChest)curAction );
			} else if (curAction instanceof HeroAction.Unlock) {
				actResult = actUnlock((HeroAction.Unlock) curAction);
			} else if (curAction instanceof HeroAction.InteractTrigger) {
				actResult = actTrigger( (HeroAction.InteractTrigger)curAction );
			} else if (curAction instanceof HeroAction.Descend) {
				actResult = actDescend( (HeroAction.Descend)curAction );
			} else if (curAction instanceof HeroAction.Ascend) {
				actResult = actAscend( (HeroAction.Ascend)curAction );
			} else if (curAction instanceof HeroAction.Attack) {
				actResult = actAttack( (HeroAction.Attack)curAction );
			} else if (curAction instanceof HeroAction.Alchemy) {
				actResult = actAlchemy( (HeroAction.Alchemy)curAction );
			} else {
				actResult = false;
			}
		}
		
		if(hasTalent(Talent.BARKSKIN) && Dungeon.level.map[pos] == Terrain.FURROWED_GRASS){
			Buff.affect(this, Barkskin.class).set( (lvl*pointsInTalent(Talent.BARKSKIN))/2, 1 );
		}
		
		return actResult;
	}
	
	public void busy() {
		ready = false;
	}
	
	private void ready() {
		if (sprite.looping()) sprite.idle();
		curAction = null;
		damageInterrupt = true;
		ready = true;

		AttackIndicator.updateState();
		
		GameScene.ready();
	}
	
	public void interrupt() {
		if (isAlive() && curAction != null &&
			((curAction instanceof HeroAction.Move && curAction.dst != pos) ||
			(curAction instanceof HeroAction.Ascend || curAction instanceof HeroAction.Descend))) {
			lastAction = curAction;
		}
		curAction = null;
		GameScene.resetKeyHold();
	}
	
	public void resume() {
		curAction = lastAction;
		lastAction = null;
		damageInterrupt = false;
		next();
	}
	
	private boolean actMove( HeroAction.Move action ) {

		if (getCloser( action.dst )) {
			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actInteract( HeroAction.Interact action ) {
		
		Char ch = action.ch;

		if (ch.canInteract(this)) {
			
			ready();
			sprite.turnTo( pos, ch.pos );
			return ch.interact(this);
			
		} else {
			
			if (fieldOfView[ch.pos] && getCloser( ch.pos )) {

				return true;

			} else {
				ready();
				return false;
			}
			
		}
	}
	
	private boolean actBuy( HeroAction.Buy action ) {
		int dst = action.dst;
		if (pos == dst) {

			ready();
			
			Heap heap = Dungeon.level.heaps.get( dst );
			if (heap != null && heap.type == Type.FOR_SALE && heap.size() == 1) {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndTradeItem( heap ) );
					}
				});
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actAlchemy( HeroAction.Alchemy action ) {
		int dst = action.dst;
		if (Dungeon.level.distance(dst, pos) <= 1) {

			ready();
			
			AlchemistsToolkit.kitEnergy kit = buff(AlchemistsToolkit.kitEnergy.class);
			if (kit != null && kit.isCursed()){
				GLog.w( Messages.get(AlchemistsToolkit.class, "cursed"));
				return false;
			}

			AlchemyScene.clearToolkit();
			GirlsFrontlinePixelDungeon.switchScene(AlchemyScene.class);
			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actPickUp( HeroAction.PickUp action ) {
		int dst = action.dst;
		if (pos == dst) {
			
			Heap heap = Dungeon.level.heaps.get( pos );
			if (heap != null) {
				Item item = heap.peek();
				if (item.doPickUp( this )) {
					heap.pickUp();

					if (item instanceof Dewdrop
							|| item instanceof TimekeepersHourglass.sandBag
							|| item instanceof DriedRose.Petal
							|| item instanceof Key) {
						//Do Nothing
					} else {

						//TODO make all unique items important? or just POS / SOU?
						boolean important = item.unique && item.isIdentified() &&
								(item instanceof Scroll || item instanceof Potion);
						if (important) {
							GLog.p( Messages.get(this, "you_now_have", item.name()) );
						} else {
							GLog.i( Messages.get(this, "you_now_have", item.name()) );
						}
					}
					
					curAction = null;
				} else {

					if (item instanceof Dewdrop
							|| item instanceof TimekeepersHourglass.sandBag
							|| item instanceof DriedRose.Petal
							|| item instanceof Key) {
						//Do Nothing
					} else {
						GLog.newLine();
						GLog.n(Messages.get(this, "you_cant_have", item.name()));
					}

					heap.sprite.drop();
					ready();
				}
			} else {
				ready();
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actOpenChest( HeroAction.OpenChest action ) {
		int dst = action.dst;
		if (Dungeon.level.adjacent( pos, dst ) || pos == dst) {
			
			Heap heap = Dungeon.level.heaps.get( dst );
			if (heap != null && (heap.type != Type.HEAP && heap.type != Type.FOR_SALE)) {
				
				if ((heap.type == Type.LOCKED_CHEST && Notes.keyCount(new GoldenKey(Dungeon.depth)) < 1)
					|| (heap.type == Type.CRYSTAL_CHEST && Notes.keyCount(new CrystalKey(Dungeon.depth)) < 1)){

						GLog.w( Messages.get(this, "locked_chest") );
						ready();
						return false;

				}
				
				switch (heap.type) {
				case TOMB:
					Sample.INSTANCE.play( Assets.Sounds.TOMB );
					Camera.main.shake( 1, 0.5f );
					break;
				case SKELETON:
				case REMAINS:
					break;
				default:
					Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
				}
				
				sprite.operate( dst );
				
			} else {
				ready();
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actUnlock( HeroAction.Unlock action ) {
		int doorCell = action.dst;
		if (Dungeon.level.adjacent( pos, doorCell )) {
			
			boolean hasKey = false;
			int door = Dungeon.level.map[doorCell];
			
			if (door == Terrain.LOCKED_DOOR
					&& Notes.keyCount(new IronKey(Dungeon.depth)) > 0) {
				
				hasKey = true;
				
			} else if (door == Terrain.CRYSTAL_DOOR
					&& Notes.keyCount(new CrystalKey(Dungeon.depth)) > 0) {

				hasKey = true;

			} else if (door == Terrain.LOCKED_EXIT
					&& Notes.keyCount(new SkeletonKey(Dungeon.depth)) > 0) {

				hasKey = true;
				
			}
			
			if (hasKey) {
				
				sprite.operate( doorCell );
				
				Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
				
			} else {
				GLog.w( Messages.get(this, "locked_door") );
				ready();
			}

			return false;

		} else if (getCloser( doorCell )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actTrigger(HeroAction.InteractTrigger action){
		Trigger trigger = action.trigger;
		if (trigger.canInteract(this)){
			ready();
			sprite.turnTo(pos,trigger.pos);
			return trigger.interact(this);
		}else if(getCloser(trigger.pos)){
			return true;
		}else{
			ready();
			return false;
		}
	}
	
	private boolean actDescend( HeroAction.Descend action ) {
		int stairs = action.dst;

		if (rooted) {
			Camera.main.shake(1, 1f);
			ready();
			return false;
		//there can be multiple exit tiles, so descend on any of them
		//TODO this is slightly brittle, it assumes there are no disjointed sets of exit tiles
		} else if ((Dungeon.level.map[pos] == Terrain.EXIT || Dungeon.level.map[pos] == Terrain.UNLOCKED_EXIT)) {
            if (Dungeon.level instanceof RabbitBossLevel){
                Game.runOnRenderThread(new Callback() {
                    @Override
                    public void call() {
                        GameScene.show(
                                new WndOptions(new ElpheltSprite(),
                                        Messages.titleCase(Messages.get(Elphelt.class, "name", new Object[0])),
                                        Messages.get(Elphelt.class, "pick_warn"),
                                        Messages.get(Elphelt.class, "yes"),
                                        Messages.get(Elphelt.class, "no")) {
                                    @Override
                                    protected void onSelect(int index) {
                                        if (index == 0){
                                            curAction = null;

                                            TimekeepersHourglass.timeFreeze timeFreeze = buff(TimekeepersHourglass.timeFreeze.class);
                                            if (timeFreeze != null) timeFreeze.disarmPressedTraps();
                                            Swiftthistle.TimeBubble timeBubble = buff(Swiftthistle.TimeBubble.class);
                                            if (timeBubble != null) timeBubble.disarmPressedTraps();
                                            InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
                                            Game.switchScene( InterlevelScene.class );
                                        }
                                    }
                                }
                                );
                    }
                });
                ready();
            } else {
                curAction = null;

                TimekeepersHourglass.timeFreeze timeFreeze = buff(TimekeepersHourglass.timeFreeze.class);
                if (timeFreeze != null) timeFreeze.disarmPressedTraps();
                Swiftthistle.TimeBubble timeBubble = buff(Swiftthistle.TimeBubble.class);
                if (timeBubble != null) timeBubble.disarmPressedTraps();

                InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
                Game.switchScene(InterlevelScene.class);
            }
            return false;

		} else if (getCloser( stairs )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actAscend( HeroAction.Ascend action ) {
		int stairs = action.dst;


		if (rooted){
			Camera.main.shake( 1, 1f );
			ready();
			return false;
		//there can be multiple entrance tiles, so descend on any of them
		//TODO this is slightly brittle, it assumes there are no disjointed sets of entrance tiles
		} else if (Dungeon.level.map[pos] == Terrain.ENTRANCE) {
			
			if (Dungeon.depth == 1) {
				
				if (belongings.getItem( Amulet.class ) == null) {
					Game.runOnRenderThread(new Callback() {
						@Override
						public void call() {
							GameScene.show( new WndMessage( Messages.get(Hero.this, "leave") ) );
						}
					});
					ready();
				} else {
					Badges.silentValidateHappyEnd();
					//返程成功（持护符回到地面）：在通关奖励之外额外获得电池。
					//基础3电池；种子局额外仅1电池；每开启一个挑战+1，开启10个挑战再+10；测试模式不发放
					if (!Dungeon.isChallenged(Challenges.TEST_MODE))
						SPDSettings.batteryAdd( Battery.returnReward() );
					Dungeon.win( Amulet.class );
					Dungeon.deleteGame( GamesInProgress.curSlot, true );
					Game.switchScene( SurfaceScene.class );
				}
				
			}
			else if (Dungeon.level instanceof RabbitBossLevel){
                Game.runOnRenderThread(new Callback() {
                    @Override
                    public void call() {
                        GameScene.show(
                                new WndOptions(new ElpheltSprite(),
                                        Messages.titleCase(Messages.get(Elphelt.class, "name")),
                                        Messages.get(Elphelt.class, "pick_warn"),
                                        Messages.get(Elphelt.class, "yes"),
                                        Messages.get(Elphelt.class, "no")) {
                                    @Override
                                    protected void onSelect(int index) {
                                        if (index == 0) {

                                            curAction = null;

                                            TimekeepersHourglass.timeFreeze timeFreeze = buff(TimekeepersHourglass.timeFreeze.class);
                                            if (timeFreeze != null) timeFreeze.disarmPressedTraps();
                                            Swiftthistle.TimeBubble timeBubble = buff(Swiftthistle.TimeBubble.class);
                                            if (timeBubble != null) timeBubble.disarmPressedTraps();

                                            InterlevelScene.mode = InterlevelScene.Mode.ASCEND;
                                            Game.switchScene(InterlevelScene.class);
                                        }
                                    }
                                }
                        );
                    }
                });
                ready();
            }
			else {
				
				curAction = null;

				TimekeepersHourglass.timeFreeze timeFreeze = buff(TimekeepersHourglass.timeFreeze.class);
				if (timeFreeze != null) timeFreeze.disarmPressedTraps();
				Swiftthistle.TimeBubble timeBubble = buff(Swiftthistle.TimeBubble.class);
				if (timeBubble != null) timeBubble.disarmPressedTraps();

				InterlevelScene.mode = InterlevelScene.Mode.ASCEND;
				Game.switchScene( InterlevelScene.class );
			}

			return false;

		} else if (getCloser( stairs )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actAttack( HeroAction.Attack action ) {

        DEL.RemovingCurse removing = buff(DEL.RemovingCurse.class);
        if (removing != null)
            removing.detach();
		enemy = action.target;

		if (enemy.isAlive() && canAttack( enemy ) && !isCharmedBy( enemy )) {
			
			sprite.attack( enemy.pos );

			return false;

		} else {

			if (fieldOfView[enemy.pos] && getCloser( enemy.pos )) {

				return true;

			} else {
				ready();
				return false;
			}

		}
	}
	public void rest( boolean fullRest ) {
		spendAndNext( TIME_TO_REST );
		if (!fullRest) {
			if (sprite != null) {
				sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "wait"));
			}
		}
		resting = fullRest;
	}
	
	@Override
	public int attackProc( final Char enemy, int damage ) {
		final int baseDMG = damage;
		damage = super.attackProc( enemy, damage );
        if (Dungeon.hero.buff(LloydsBeacon.beaconRecharge.class)!=null
                && Dungeon.hero.buff(LloydsBeacon.beaconRecharge.class).isCursed()){
            //装备诅咒鸽子时攻击视为拥有转移
            LloydsBeacon.proc(enemy);
        }
		KindOfWeapon wep = belongings.weapon();

		if (wep != null) damage = wep.proc( this, enemy, damage );

		if (buff(Talent.SpiritBladesTracker.class) != null
				&& Random.Int(10) < 3*pointsInTalent(Talent.SPIRIT_BLADES)){
			SpiritBow bow = belongings.getItem(SpiritBow.class);
			if (bow != null) damage = bow.proc( this, enemy, damage );
			buff(Talent.SpiritBladesTracker.class).detach();
		}

		damage = Talent.onAttackProc( this, enemy, damage );
		
		switch (subClass) {
		case SNIPER:
			if (wep instanceof MissileWeapon && !(wep instanceof SpiritBow.SpiritArrow) && enemy != this) {
				Actor.add(new Actor() {
					
					{
						actPriority = VFX_PRIO;
					}
					
					@Override
					protected boolean act() {
						if (enemy.isAlive()) {
							int bonusTurns = hasTalent(Talent.SHARED_UPGRADES) ? wep.buffedLvl() : 0;
							Buff.prolong(Hero.this, SnipersMark.class, SnipersMark.DURATION + bonusTurns).set(enemy.id(), bonusTurns);
						}
						Actor.remove(this);
						return true;
					}
				});
			}
			break;
		default:
		}
        BasicBuffs.Increase increase = Dungeon.hero.buff(BasicBuffs.Increase.class);
        if (increase != null)
            damage *= increase.percent();

		damage = CardAffect.cardAttackProc(this, enemy, damage, baseDMG, wep);
		return damage;
	}
	
	@Override
	public int defenseProc( Char enemy, int damage ) {
        BasicBuffs.Reduce reduce = Dungeon.hero.buff(BasicBuffs.Reduce.class);
		if (reduce != null)
            damage *= reduce.percent();

		if (damage > 0 && subClass == HeroSubClass.BERSERKER)
			Buff.affect(this, Berserk.class).damage(damage);

        if (Dungeon.hero.buff(LloydsBeacon.beaconRecharge.class)!=null
                && Dungeon.hero.buff(LloydsBeacon.beaconRecharge.class).isCursed())
            LloydsBeacon.proc(this);

        if (belongings.armor() != null)
            damage = belongings.ArmorProc( enemy, this, damage );

		damage = Talent.onDefenceProc(this, enemy, damage);
		
		Earthroot.Armor armor = buff( Earthroot.Armor.class );
		if (armor != null)
			damage = armor.absorb( damage );

		WandOfLivingEarth.RockArmor rockArmor = buff(WandOfLivingEarth.RockArmor.class);
		if (rockArmor != null)
			damage = rockArmor.absorb(damage);
		
		return damage;
	}
	
	@Override
	public void damage( int dmg, Object src ) {
		if (buff(TimekeepersHourglass.timeStasis.class) != null)
			return;

		if (!(src instanceof Hunger || src instanceof Viscosity.DeferedDamage) && damageInterrupt) {
			interrupt();
			resting = false;
		}

		if (buff(Drowsy.class) != null){
			Buff.detach(this, Drowsy.class);
			GLog.w( Messages.get(this, "pain_resist") );
		}

		Endure.EndureTracker endure = buff(Endure.EndureTracker.class);
		if (!(src instanceof Char)){
			//reduce damage here if it isn't coming from a character (if it is we already reduced it)
			if (endure != null){
				dmg = endure.adjustDamageTaken(dmg);
			}
			//the same also applies to challenge scroll damage reduction
			if (buff(ScrollOfChallenge.ChallengeArena.class) != null){
				dmg *= 0.67f;
			}
		}

		CapeOfThorns.Thorns thorns = buff( CapeOfThorns.Thorns.class );
		if (thorns != null) {
			dmg = thorns.proc(dmg, (src instanceof Char ? (Char)src : null),  this);
		}

		dmg = (int)Math.ceil(dmg * RingOfTenacity.damageMultiplier( this ));

		//TODO improve this when I have proper damage source logic
		if ( belongings.hasGlyph(AntiMagic.class, this)
				&& AntiMagic.RESISTS.contains(src.getClass())){
			dmg -= AntiMagic.drRoll( belongings.GlyphLevel(AntiMagic.class) );
		}

		if (buff(Talent.WarriorFoodImmunity.class) != null){
			if (pointsInTalent(Talent.IRON_STOMACH) == 1)       dmg = Math.round(dmg*0.25f);
			else if (pointsInTalent(Talent.IRON_STOMACH) == 2)  dmg = Math.round(dmg*0.00f);
		}

		int preHP = HP + shielding();
		super.damage( dmg, src );
		CardAffect.afterDamage(this);
		int postHP = HP + shielding();
		int effectiveDamage = preHP - postHP;

		if (effectiveDamage <= 0) return;

		//flash red when hit for serious damage.
		float percentDMG = effectiveDamage / (float)preHP; //percent of current HP that was taken
		float percentHP = 1 - ((HT - postHP) / (float)HT); //percent health after damage was taken
		// The flash intensity increases primarily based on damage taken and secondarily on missing HP.
		float flashIntensity = 0.25f * (percentDMG * percentDMG) / percentHP;
		//if the intensity is very low don't flash at all
		if (flashIntensity >= 0.05f){
			flashIntensity = Math.min(1/3f, flashIntensity); //cap intensity at 1/3
			GameScene.flash( (int)(0xFF*flashIntensity) << 16 );
			if (isAlive()) {
				if (flashIntensity >= 1/6f) {
					Sample.INSTANCE.play(Assets.Sounds.HEALTH_CRITICAL, 1/3f + flashIntensity * 2f);
				} else {
					Sample.INSTANCE.play(Assets.Sounds.HEALTH_WARN, 1/3f + flashIntensity * 4f);
				}
			}
		}
	}
	public void checkVisibleMobs() {
		ArrayList<Mob> visible = new ArrayList<>();

		boolean newMob = false;

		Mob target = null;
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (fieldOfView[ m.pos ] && m.landmark() != null){
				Notes.add(m.landmark());
			}

			if (fieldOfView[ m.pos ] && m.alignment == Alignment.ENEMY) {
				visible.add(m);
				if (!visibleEnemies.contains( m )) {
					newMob = true;
				}

				if (!mindVisionEnemies.contains(m) && QuickSlotButton.autoAim(m) != -1){
					if (target == null){
						target = m;
					} else if (distance(target) > distance(m)) {
						target = m;
					}
					if (m instanceof Snake && Dungeon.level.distance(m.pos, pos) <= 4
							&& !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_EXAMINING)){
						GLog.p(Messages.get(Guidebook.class, "hint"));
						GameScene.flashForDocument(Document.GUIDE_EXAMINING);
						//we set to read here to prevent this message popping up a bunch
						Document.ADVENTURERS_GUIDE.readPage(Document.GUIDE_EXAMINING);
					}
				}
			}
		}

		Char lastTarget = QuickSlotButton.lastTarget;
		if (target != null && (lastTarget == null ||
							!lastTarget.isAlive() ||
							lastTarget.alignment == Alignment.ALLY ||
							!fieldOfView[lastTarget.pos])){
			QuickSlotButton.target(target);
		}
		
		if (newMob) {
			interrupt();
			if (resting){
				Dungeon.observe();
				resting = false;
			}
		}

		visibleEnemies = visible;
		//we also scan for blob landmarks here
		for (Blob b : Dungeon.level.blobs.values().toArray(new Blob[0])){
			if (b.volume > 0 && b.landmark() != null && !Notes.contains(b.landmark())){
				int cell;
				boolean found = false;
				//if a single cell within the blob is visible, we add the landmark
				for (int i=b.area.top; i < b.area.bottom; i++) {
					for (int j = b.area.left; j < b.area.right; j++) {
						cell = j + i* Dungeon.level.width();
						if (fieldOfView[cell] && b.cur[cell] > 0) {
							Notes.add( b.landmark() );
							found = true;
							break;
						}
					}
					if (found) break;
				}

				//Clear blobs that only exist for landmarks.
				// Might want to make this a properly if it's used more
				if (found && b instanceof WeakFloorRoom.WellID){
					b.fullyClear();
				}
			}
		}
	}
	
	public int visibleEnemies() {
		return visibleEnemies.size();
	}
	
	public Mob visibleEnemy( int index ) {
		return visibleEnemies.get(index % visibleEnemies.size());
	}
	
	private boolean walkingToVisibleTrapInFog = false;
	
	//FIXME this is a fairly crude way to track this, really it would be nice to have a short
	//history of hero actions
	public boolean justMoved = false;
	
	private boolean getCloser( final int target ) {

		if (target == pos)
			return false;

		if (rooted) {
			Camera.main.shake( 1, 1f );
			return false;
		}
		
		int step = -1;
		
		if (Dungeon.level.adjacent( pos, target )) {

			path = null;

			if (Actor.findChar( target ) == null) {
				if (Dungeon.level.pit[target] && !flying && !Dungeon.level.solid[target]) {
					if (!Chasm.jumpConfirmed){
						Chasm.heroJump(this);
						interrupt();
					} else {
						Chasm.heroFall(target);
					}
					return false;
				}
				if (Dungeon.level.passable[target] || Dungeon.level.avoid[target]) {
					step = target;
				}
				if (walkingToVisibleTrapInFog
						&& Dungeon.level.traps.get(target) != null
						&& Dungeon.level.traps.get(target).visible){
					return false;
				}
			}
			
		} else {

			boolean newPath = false;
			if (path == null || path.isEmpty() || !Dungeon.level.adjacent(pos, path.getFirst()))
				newPath = true;
			else if (path.getLast() != target)
				newPath = true;
			else {
				if (!Dungeon.level.passable[path.get(0)] || Actor.findChar(path.get(0)) != null) {
					newPath = true;
				}
			}

			if (newPath) {

				int len = Dungeon.level.length();
				boolean[] p = Dungeon.level.passable;
				boolean[] v = Dungeon.level.visited;
				boolean[] m = Dungeon.level.mapped;
				boolean[] passable = new boolean[len];
				for (int i = 0; i < len; i++) {
					passable[i] = p[i] && (v[i] || m[i]);
				}

				PathFinder.Path newpath = Dungeon.findPath(this, target, passable, fieldOfView, true);
				if (newpath != null && path != null && newpath.size() > 2*path.size()){
					path = null;
				} else {
					path = newpath;
				}
			}

			if (path == null) return false;
			step = path.removeFirst();

		}

		if (step != -1) {

			if (subClass == HeroSubClass.FREERUNNER){
				Buff.affect(this, Momentum.class).gainStack();
			}

			float speed = speed();
			
			sprite.move(pos, step);
			move(step);

			spend( 1 / speed );
			justMoved = true;
			CardAffect.onMove(this);
			
			search(false);

			return true;

		} else {

			return false;
			
		}

	}
	
	public boolean handle( int cell ) {
		if (cell == -1) {
			return false;
		}

		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
			Dungeon.level.updateFieldOfView( this, fieldOfView );
		}
		
		Char ch = Actor.findChar( cell );
		Heap heap = Dungeon.level.heaps.get( cell );
		
		if (Dungeon.level.map[cell] == Terrain.ALCHEMY && cell != pos) {
			curAction = new HeroAction.Alchemy( cell );
		} else if (fieldOfView[cell] && ch instanceof Mob) {
			if (ch.alignment != Alignment.ENEMY && ch.buff(Amok.class) == null) {
				curAction = new HeroAction.Interact( ch );
			} else {
				curAction = new HeroAction.Attack( ch );
			}
		} else if (heap != null
				//moving to an item doesn't auto-pickup when enemies are near...
				&& (visibleEnemies.size() == 0 || cell == pos ||
				//...but only for standard heaps, chests and similar open as normal.
				(heap.type != Type.HEAP && heap.type != Type.FOR_SALE))) {

			switch (heap.type) {
			case HEAP:
				curAction = new HeroAction.PickUp( cell );
				break;
			case FOR_SALE:
				curAction = heap.size() == 1 && heap.peek().value() > 0 ?
					new HeroAction.Buy( cell ) :
					new HeroAction.PickUp( cell );
				break;
			default:
				curAction = new HeroAction.OpenChest( cell );
			}
		} else if (Dungeon.level.map[cell] == Terrain.LOCKED_DOOR || Dungeon.level.map[cell] == Terrain.CRYSTAL_DOOR || Dungeon.level.map[cell] == Terrain.LOCKED_EXIT) {			
			curAction = new HeroAction.Unlock( cell );
		} else if (Dungeon.level.triggers.get(cell)!=null && Dungeon.level.triggers.get(cell).canBeTouched()){
			curAction = new HeroAction.InteractTrigger(Dungeon.level.triggers.get(cell));
		} else if ((cell == Dungeon.level.exit || Dungeon.level.map[cell] == Terrain.EXIT || Dungeon.level.map[cell] == Terrain.UNLOCKED_EXIT)
		&& Dungeon.depth < Constants.MAX_DEPTH) {
			curAction = new HeroAction.Descend( cell );
		} else if (cell == Dungeon.level.entrance || Dungeon.level.map[cell] == Terrain.ENTRANCE) {
			curAction = new HeroAction.Ascend( cell );
		} else  {
			if (!Dungeon.level.visited[cell] && !Dungeon.level.mapped[cell]
			&& Dungeon.level.traps.get(cell) != null && Dungeon.level.traps.get(cell).visible) {
				walkingToVisibleTrapInFog = true;
			} else {
				walkingToVisibleTrapInFog = false;
			}
			
			curAction = new HeroAction.Move( cell );
			lastAction = null;
		}

		return true;
	}
	
	public void earnExp( int exp, Class source ) {
        Commander.Command command = buff(Commander.Command.class);
        if (command != null && command.count()>0)
            exp *= 2;
		this.exp += (int) (exp * (1+pointsInTalent(Talent.ELITE_ARMY)*2/3F));

		float percent = exp/(float)maxExp();

		EtherealChains.chainsRecharge chains = buff(EtherealChains.chainsRecharge.class);
		if (chains != null) chains.gainExp(percent);

        RedBook.BookRecharge redbook = buff(RedBook.BookRecharge.class);
        if (redbook != null) redbook.gainExp(percent);

		HornOfPlenty.hornRecharge horn = buff(HornOfPlenty.hornRecharge.class);
		if (horn != null) horn.gainCharge(percent);

		AlchemistsToolkit.kitEnergy kit = buff(AlchemistsToolkit.kitEnergy.class);
		if (kit != null) kit.gainCharge(percent);

		MasterThievesArmband.Thievery armband = buff(MasterThievesArmband.Thievery.class);
		if (armband != null) armband.gainCharge(percent);

		Berserk berserk = buff(Berserk.class);
		if (berserk != null) berserk.recover(percent);

		if (source != PotionOfExperience.class) {
			for (Item i : belongings) {
				i.onHeroGainExp(percent, this);
			}
			if (buff(Talent.RejuvenatingStepsFurrow.class) != null){
				buff(Talent.RejuvenatingStepsFurrow.class).countDown(percent*200f);
				if (buff(Talent.RejuvenatingStepsFurrow.class).count() <= 0){
					buff(Talent.RejuvenatingStepsFurrow.class).detach();
				}
			}
		}

		boolean levelUp = false;
		while (this.exp >= maxExp()) {
			this.exp -= maxExp();
            if (command != null && command.count()>0)
                command.countDown(1);
            if (pointsInTalent(Talent.WAND_PRESERVATION) == 2){
                Talent.WandPreservationCounter counter = Buff.affect(this, Talent.WandPreservationCounter.class);
                if (counter.count()>0)
                    counter.countDown(1);
            }
			if (lvl < MAX_LEVEL) {
				lvl++;
				levelUp = true;

				if (buff(ElixirOfMight.HTBoost.class) != null){
					buff(ElixirOfMight.HTBoost.class).onLevelUp();
				}

				updateHT( true );
				attackSkill++;
				defenseSkill++;

			} else {
				Buff.prolong(this, Bless.class, Bless.DURATION);
				this.exp = 0;

				GLog.newLine();
				GLog.p( Messages.get(this, "level_cap"));
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
			}
		}

		if (levelUp) {

			if (sprite != null) {
				GLog.newLine();
				GLog.p( Messages.get(this, "new_level") );
				sprite.showStatus( CharSprite.POSITIVE, Messages.get(Hero.class, "level_up") );
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
				if (lvl < Talent.tierLevelThresholds[Talent.MAX_TALENT_TIERS+1]){
					GLog.newLine();
					GLog.p( Messages.get(this, "new_talent") );
					StatusPane.talentBlink = 10f;
					WndHero.lastIdx = 1;
				}
			}

			Item.updateQuickslot();

			Badges.validateLevelReached();
		}
	}

	public int maxExp() {
		return maxExp( lvl );
	}

	public static int maxExp( int lvl ){
		return 5 + lvl * 5;
	}

	public boolean isStarving() {
		return Buff.affect(this, Hunger.class).isStarving();
	}

	@Override
	public void add( Buff buff ) {

		if (buff(TimekeepersHourglass.timeStasis.class) != null)
			return;

		super.add( buff );

		if (sprite != null && buffs().contains(buff)) {
			String msg = buff.heroMessage();
			if (msg != null){
				GLog.w(msg);
			}

			if (buff instanceof Paralysis || buff instanceof Vertigo) {
				interrupt();
			}

		}

		BuffIndicator.refreshHero();
	}

	@Override
	public void remove( Buff buff ) {
		super.remove( buff );

		BuffIndicator.refreshHero();
	}

	@Override
	public float stealth() {
		float stealth = super.stealth();

		if (belongings.hasGlyph(Obfuscation.class, this)){
			stealth = Armor.stealthFactor(stealth);
		}

		return stealth;
	}

	@Override
	public void die( Object cause ) {

		curAction = null;
		if (CardSelector.INSTANCE().hasCard(FinalCard.HS2000.Webley)){
			GameScene.flash(0x80FFFF40);
			Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
			GLog.w(Card.EnumString(FinalCard.HS2000.Webley, "fail"));
			HP = HT / 4;
			PotionOfHealing.cure(this);
			Buff.affect(this, HS2000_Shield.class).incShield(HT);
			Buff.affect(this, IntensifySkill.Intensify.class, 20F);
			CardSelector.INSTANCE().destroyCard(FinalCard.HS2000.Webley);
		}

		Ankh ankh = null;

		//look for ankhs in player inventory, prioritize ones which are blessed.
		for (Ankh i : belongings.getAllItems(Ankh.class)){
			if (ankh == null || i.isBlessed()) {
				ankh = i;
			}
		}

		if (ankh != null) {
			interrupt();
			resting = false;
			Catalog.countUse(Ankh.class);

			if (ankh.isBlessed()) {
				this.HP = HT / 4;

				PotionOfHealing.cure(this);
				Buff.prolong(this, AnkhInvulnerability.class, AnkhInvulnerability.DURATION);

				SpellSprite.show(this, SpellSprite.ANKH);
				GameScene.flash(0x80FFFF40);
				Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
				GLog.w(Messages.get(this, "revive"));
				Statistics.ankhsUsed++;

				ankh.detach(belongings.backpack);

				for (Char ch : Actor.chars()) {
					if (ch instanceof DriedRose.GhostHero) {
						((DriedRose.GhostHero) ch).sayAnhk();
						return;
					}
				}
			} else {

				//this is hacky, basically we want to declare that a wndResurrect exists before
				//it actually gets created. This is important so that the game knows to not
				//delete the run or submit it to rankings, because a WndResurrect is about to exist
				//this is needed because the actual creation of the window is delayed here
				WndResurrect.instance = new Object();
				Ankh finalAnkh = ankh;
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndResurrect(finalAnkh) );
					}
				});

			}
			return;
		}

		Actor.fixTime();
		super.die( cause );
		reallyDie( cause );
	}
	@Override
	protected synchronized void onRemove() {
		for (Buff buff : buffs.toArray(new Buff[0]))
			if (!(buff instanceof ItemBuff))
				buff.detach();
	}
	public static void reallyDie( Object cause ) {

		int length = Dungeon.level.length();
		int[] map = Dungeon.level.map;
		boolean[] visited = Dungeon.level.visited;
		boolean[] discoverable = Dungeon.level.discoverable;

		for (int i=0; i < length; i++) {

			int terr = map[i];

			if (discoverable[i]) {

				visited[i] = true;
				if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {
					Dungeon.level.discover( i );
				}
			}
		}

		Bones.leave();

		Dungeon.observe();
		GameScene.updateFog();

		Dungeon.hero.belongings.identify();

		int pos = Dungeon.hero.pos;

		ArrayList<Integer> passable = new ArrayList<>();
		for (Integer ofs : PathFinder.NEIGHBOURS8) {
			int cell = pos + ofs;
			if ((Dungeon.level.passable[cell] || Dungeon.level.avoid[cell]) && Dungeon.level.heaps.get( cell ) == null) {
				passable.add( cell );
			}
		}
		Collections.shuffle( passable );

		ArrayList<Item> items = new ArrayList<>(Dungeon.hero.belongings.backpack.items);
		for (Integer cell : passable) {
			if (items.isEmpty()) {
				break;
			}

			Item item = Random.element( items );
			Dungeon.level.drop( item, cell ).sprite.drop( pos );
			items.remove( item );
		}

		for (Char c : Actor.chars()){
			if (c instanceof DriedRose.GhostHero){
				((DriedRose.GhostHero) c).sayHeroKilled();
			}
		}

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.gameOver();
				Sample.INSTANCE.play(Assets.Sounds.DEATH);
			}
		});

		if (cause instanceof Hero.Doom) {
			((Hero.Doom)cause).onDeath();
		}

		Dungeon.deleteGame( GamesInProgress.curSlot, true );
	}

	//effectively cache this buff to prevent having to call buff(...) a bunch.
	//This is relevant because we call isAlive during drawing, which has both performance
	//and thread coordination implications if that method calls buff(...) frequently
	private Berserk berserk;

	@Override
	public boolean isAlive() {

		if (HP <= 0){
			if (berserk == null) berserk = buff(Berserk.class);
			return berserk != null && berserk.berserking();
		} else {
			berserk = null;
			return super.isAlive();
		}
	}

	@Override
	public void move(int step, boolean travelling) {
		boolean wasHighGrass = Dungeon.level.map[step] == Terrain.HIGH_GRASS;

		super.move( step, travelling);

		if (!flying && travelling) {
			if (Dungeon.level.water[pos]) {
				Sample.INSTANCE.play( Assets.Sounds.WATER, 1, Random.Float( 0.8f, 1.25f ) );
			} else if (Dungeon.level.map[pos] == Terrain.EMPTY_SP) {
				Sample.INSTANCE.play( Assets.Sounds.STURDY, 1, Random.Float( 0.96f, 1.05f ) );
			} else if (Dungeon.level.map[pos] == Terrain.GRASS
					|| Dungeon.level.map[pos] == Terrain.EMBERS
					|| Dungeon.level.map[pos] == Terrain.FURROWED_GRASS){
				if (step == pos && wasHighGrass) {
					Sample.INSTANCE.play(Assets.Sounds.TRAMPLE, 1, Random.Float( 0.96f, 1.05f ) );
				} else {
					Sample.INSTANCE.play( Assets.Sounds.GRASS, 1, Random.Float( 0.96f, 1.05f ) );
				}
			} else {
				Sample.INSTANCE.play( Assets.Sounds.STEP, 1, Random.Float( 0.96f, 1.05f ) );
			}
		}
	}
	@Override
	public void onAttackComplete() {

		AttackIndicator.target(enemy);

		//未来之星：敌人在副手HG射程内但不在主手射程内时，临时改用副手武器，
		//使伤害、命中、攻速、攻击距离、附魔触发全部按副手武器计算
		boolean useSecondary = shouldUseSecondary( enemy );
		KindOfWeapon savedMain = belongings.weapon;
		if (useSecondary) {
			//粉色文本，与星之护盾（ShieldHalo）同色
			sprite.showStatus( 0xFF99CC, Messages.get(this, "quick_attack") );
			belongings.weapon = secondaryWeapon();
			//伴星同调：快速攻击期间主武器不在任何装备槽中，暂存以供副武器等级同调判定
			GSH18Talent.beginQuickAttack(this, savedMain);
		}

		boolean hit;
		try {
			hit = attack( enemy );
		} finally {
			GSH18Talent.endQuickAttack();
		}

		Invisibility.dispel();
		spend( attackDelay() );

		if (hit && subClass == HeroSubClass.GLADIATOR){
			Buff.affect( this, Combo.class ).hit( enemy );
		}

		// GSH18天赋：元气一餐 - 攻击命中后消耗增益（实现见 GSH18Talent）
		if (hit) {
			GSH18Talent.onAttackHit(this);
		}

		// GSH18天赋：天狼星心脏 - 攻击时附加伤害
		if (hit&&(buff(Talent.SiriusHeartTracker.class) != null)) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SiriusHeart.onAttack(this, enemy);
		}
        // 56天赋 - 攻击后移除buff
        if (hit&&(buff(Talent.Type56BookTracker.class) != null)) {
            buff(Talent.Type56BookTracker.class).detach();
        }

		//恢复主手武器
		if (useSecondary) {
			belongings.weapon = savedMain;
		}

		curAction = null;

		super.onAttackComplete();
	}

	@Override
	public void onMotionComplete() {
		GameScene.checkKeyHold();
	}

	@Override
	public void onOperateComplete() {

		if (curAction instanceof HeroAction.Unlock) {

			int doorCell = ((HeroAction.Unlock)curAction).dst;
			int door = Dungeon.level.map[doorCell];

			if (Dungeon.level.distance(pos, doorCell) <= 1) {
				boolean hasKey = true;
				if (door == Terrain.LOCKED_DOOR) {
					hasKey = Notes.remove(new IronKey(Dungeon.depth));
					if (hasKey) Level.set(doorCell, Terrain.DOOR);
				} else if (door == Terrain.CRYSTAL_DOOR) {
					hasKey = Notes.remove(new CrystalKey(Dungeon.depth));
					if (hasKey) {
						Notes.remove(Notes.Landmark.DISTANT_WELL, Dungeon.depth-1);
						Level.set(doorCell, Terrain.EMPTY);
						Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
						CellEmitter.get( doorCell ).start( Speck.factory( Speck.DISCOVER ), 0.025f, 20 );
					}
				} else {
					hasKey = Notes.remove(new SkeletonKey(Dungeon.depth));
					if (hasKey) Level.set(doorCell, Terrain.UNLOCKED_EXIT);
				}

				if (hasKey) {
					GameScene.updateKeyDisplay();
					GameScene.updateMap(doorCell);
					spend(Key.TIME_TO_UNLOCK);
				}
			}

		} else if (curAction instanceof HeroAction.OpenChest) {

			Heap heap = Dungeon.level.heaps.get( ((HeroAction.OpenChest)curAction).dst );

			if (Dungeon.level.distance(pos, heap.pos) <= 1){
				boolean hasKey = true;
				if (heap.type == Type.SKELETON || heap.type == Type.REMAINS) {
					Sample.INSTANCE.play( Assets.Sounds.BONES );
				} else if (heap.type == Type.LOCKED_CHEST){
					hasKey = Notes.remove(new GoldenKey(Dungeon.depth));
				} else if (heap.type == Type.CRYSTAL_CHEST){
					hasKey = Notes.remove(new CrystalKey(Dungeon.depth));
				}

				if (hasKey) {
					GameScene.updateKeyDisplay();
					heap.open(this);
					spend(Key.TIME_TO_UNLOCK);
				}
			}

		}
		curAction = null;

		super.onOperateComplete();
	}

	@Override
	public boolean isImmune(Class effect) {
		if (effect == Burning.class
				&& belongings.hasGlyph(Brimstone.class, this)){
			return true;
		}
		return super.isImmune(effect);
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		return buff(AnkhInvulnerability.class) != null;
	}

	public boolean search( boolean intentional ) {

		if (!isAlive()) return false;

		boolean smthFound = false;

		boolean circular = pointsInTalent(Talent.WIDE_SEARCH) == 1;
		int distance = heroClass == HeroClass.ROGUE ? 2 : 1;
		if (hasTalent(Talent.WIDE_SEARCH)) distance++;

		boolean foresight = buff(Foresight.class) != null;
		boolean foresightScan = foresight && !Dungeon.level.mapped[pos];

		if (foresightScan){
			Dungeon.level.mapped[pos] = true;
		}

		if (foresight) {
			distance = Foresight.DISTANCE;
			circular = true;
		}

		Point c = Dungeon.level.cellToPoint(pos);

		TalismanOfForesight.Foresight talisman = buff( TalismanOfForesight.Foresight.class );
		boolean cursed = talisman != null && talisman.isCursed();

		int[] rounding = ShadowCaster.rounding[distance];

		int left, right;
		int curr;
		for (int y = Math.max(0, c.y - distance); y <= Math.min(Dungeon.level.height()-1, c.y + distance); y++) {
			if (!circular){
				left = c.x - distance;
			} else if (rounding[Math.abs(c.y - y)] < Math.abs(c.y - y)) {
				left = c.x - rounding[Math.abs(c.y - y)];
			} else {
				left = distance;
				while (rounding[left] < rounding[Math.abs(c.y - y)]){
					left--;
				}
				left = c.x - left;
			}
			right = Math.min(Dungeon.level.width()-1, c.x + c.x - left);
			left = Math.max(0, left);
			for (curr = left + y * Dungeon.level.width(); curr <= right + y * Dungeon.level.width(); curr++){

				if ((foresight || fieldOfView[curr]) && curr != pos) {

					if ((foresight && (!Dungeon.level.mapped[curr] || foresightScan))){
						GameScene.effectOverFog(new CheckedCell(curr, foresightScan ? pos : curr));
					} else if (intentional) {
						GameScene.effectOverFog(new CheckedCell(curr, pos));
					}

					if (foresight){
						Dungeon.level.mapped[curr] = true;
					}

					if (Dungeon.level.secret[curr]){

						Trap trap = Dungeon.level.traps.get( curr );
						float chance;

						//searches aided by foresight always succeed, even if trap isn't searchable
						if (foresight){
							chance = 1f;

						//otherwise if the trap isn't searchable, searching always fails
						} else if (trap != null && !trap.canBeSearched){
							chance = 0f;

						//intentional searches always succeed against regular traps and doors
						} else if (intentional){
							chance = 1f;

						//unintentional searches always fail with a cursed talisman
						} else if (cursed) {
							chance = 0f;

						//unintentional trap detection scales from 40% at floor 0 to 30% at floor 30
						} else if (Dungeon.level.map[curr] == Terrain.SECRET_TRAP) {
							chance = 0.3F + 0.1F * (1F - Statistics.deepestFloor / 30F);
							if(hasTalent(Talent.TRAP_EXPERT)){
								chance *= 1.5F;
								if(pointsInTalent(Talent.TRAP_EXPERT)>=2)
									chance *= 2F;
							}
						//unintentional door detection scales from 20% at floor 0 to 0% at floor 25
						} else {
							chance=     0.2f*(1f-Dungeon.depth/25f);
						}

						if (Random.Float() < chance) {

							int oldValue = Dungeon.level.map[curr];

							GameScene.discoverTile( curr, oldValue );

							Dungeon.level.discover( curr );

							ScrollOfMagicMapping.discover( curr );

							if (fieldOfView[curr]) smthFound = true;

							if (talisman != null){
								if (oldValue == Terrain.SECRET_TRAP){
									talisman.charge(2);
								} else if (oldValue == Terrain.SECRET_DOOR){
									talisman.charge(10);
								}
							}
						}
					}
				}
			}
		}

		if (intentional) {
			sprite.showStatus( CharSprite.DEFAULT, Messages.get(this, "search") );
			sprite.operate( pos );
			if (!Dungeon.level.locked) {
				if (cursed) {
					GLog.n(Messages.get(this, "search_distracted"));
					Buff.affect(this, Hunger.class).affectHunger(TIME_TO_SEARCH - (2 * HUNGER_FOR_SEARCH));
				} else {
					Buff.affect(this, Hunger.class).affectHunger(TIME_TO_SEARCH - HUNGER_FOR_SEARCH);
				}
			}
			spendAndNext(TIME_TO_SEARCH);

		}

		if (smthFound) {
			GLog.w( Messages.get(this, "noticed_smth") );
			Sample.INSTANCE.play( Assets.Sounds.SECRET );
			interrupt();
		}

		if (foresight){
			GameScene.updateFog(pos, Foresight.DISTANCE+1);
		}

		return smthFound;
	}

	public void resurrect() {
		HP = HT;
		live();

		MagicalHolster holster = belongings.getItem(MagicalHolster.class);

		Buff.affect(this, LostInventory.class);
		Buff.affect(this, Invisibility.class, 3f);
		//lost inventory is dropped in interlevelscene

		//activate items that persist after lost inventory
		//FIXME this is very messy, maybe it would be better to just have one buff that
		// handled all items that recharge over time?
		for (Item i : belongings){
			if (i instanceof EquipableItem && i.isEquipped(this)){
				((EquipableItem) i).activate(this);
			} else if (i instanceof CloakOfShadows && i.keptThoughLostInvent && hasTalent(Talent.LIGHT_CLOAK)){
				((CloakOfShadows) i).activate(this);
			} else if (i instanceof RedBook && i.keptThoughLostInvent && hasTalent(Talent.Type56Three_Book)){
                ((RedBook) i).activate(this);
            } else if (i instanceof Wand && i.keptThoughLostInvent){
				if (holster != null && holster.contains(i)){
					((Wand) i).charge(this, MagicalHolster.HOLSTER_SCALE_FACTOR);
				} else {
					((Wand) i).charge(this);
				}
			}
		}

		updateHT(false);
	}

	@Override
	public void next() {
		if (isAlive())
			super.next();
	}

	public static interface Doom {
		public void onDeath();
	}
}
