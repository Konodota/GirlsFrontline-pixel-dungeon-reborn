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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.VHS_Hack;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EquipLevelUp;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.GSH18Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardCalculator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Annoying;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Displacing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Exhausting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Fragile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Friendly;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Polarized;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blooming;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Chilling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Corrupting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Lucky;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Unstable;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vampiric;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStartGame;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

abstract public class Weapon extends KindOfWeapon {

	public float    ACC = 1f;	// Accuracy modifier
	public float	DLY	= 1f;	// Speed modifier
	public int      RCH = 1;    // Reach modifier (only applies to melee hits)
    public int DEF = 0;
    public int DEFUPGRADE = 0;
	public int tier;
	public boolean UpdatedTierToLevel = false;

	public Item clone(Item item){
		Weapon weapon = (Weapon) item;
		super.clone(weapon);
		enchant(weapon.enchantment);
		augment = weapon.augment;
		masteryPotionBonus = weapon.masteryPotionBonus;
		curseInfusionBonus = weapon.curseInfusionBonus;
		return this;
	}
	@Override
	public void update(){
		super.update();
		if (Dungeon.isGameMode(WndStartGame.GameMode.IDENTIFY)){
			if (!UpdatedTierToLevel){
				boolean hasEnchant = enchantment != null;
				boolean curse = cursed;
				UpdatedTierToLevel = true;
				int workingTier = tier - 1;
				if (workingTier > 0) {
					upgrade(hasEnchant);
					workingTier--;
				}
				while (workingTier > 0){
					if (workingTier >= Random.Int(3)+1 )
						upgrade(hasEnchant);
					workingTier -= 3;
				}
				cursed = curse;
				if (this instanceof MissileWeapon && Random.Int(4) == 0)
					enchant();
			}
			tier = 10;
			while (STRReq() > hero.STR())
				tier--;
		}
	}

	protected boolean hasSameEnchant(Weapon item){
		if (enchantment == null && item.enchantment == null)
			return true;
		if (enchantment != null && item.enchantment != null)
			return enchantment.getClass() == item.enchantment.getClass();
		return false;
	}
	public enum Augment {
		SPEED   (0.7f, 0.6667f),
		DAMAGE  (1.5f, 1.6667f),
		NONE	(1.0f, 1.0000f);

		private float damageFactor;
		private float delayFactor;

		Augment(float dmg, float dly){
			damageFactor = dmg;
			delayFactor = dly;
		}

		public int damageFactor(int dmg){
			return Math.round(dmg * damageFactor);
		}

		public float delayFactor(float dly){
			return dly * delayFactor;
		}
	}
	
	public Augment augment = Augment.NONE;
	
	private static final int USES_TO_ID = 20;
	private float usesLeftToID = USES_TO_ID;
	private float availableUsesToID = USES_TO_ID/2f;
	
	public Enchantment enchantment;
	public boolean curseInfusionBonus = false;
	public boolean masteryPotionBonus = false;
	public int enchantmentProc( Char attacker, Char defender, int damage ){
		if (enchantment == null)
			return damage;
		if (attacker.buff(MagicImmune.class) != null)
			return damage;
		VHS_Hack hack = attacker.buff(VHS_Hack.class);
		if (hack != null && hack.againstEnchant())
			return damage;
		return enchantment.proc( this, attacker, defender, damage );
	}
	@Override
	public int proc( Char attacker, Char defender, int damage ) {

		damage = enchantmentProc(attacker, defender, damage);
		
		if (!levelKnown && attacker == Dungeon.hero) {
			float uses = Math.min( availableUsesToID, Talent.itemIDSpeedFactor(Dungeon.hero, this) );
			availableUsesToID -= uses;
			usesLeftToID -= uses;
			if (usesLeftToID <= 0) {
				identify();
				GLog.p( Messages.get(Weapon.class, "identify") );
				Badges.validateItemLevelAquired( this );
			}
		}

		return damage;
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		if (!levelKnown && isEquipped(hero) && availableUsesToID <= USES_TO_ID/2f) {
			//gains enough uses to ID over 0.5 levels
			availableUsesToID = Math.min(USES_TO_ID/2f, availableUsesToID + levelPercent * USES_TO_ID);
		}
	}
	
	private static final String USES_LEFT_TO_ID = "uses_left_to_id";
	private static final String AVAILABLE_USES  = "available_uses";
	private static final String ENCHANTMENT	    = "enchantment";
	private static final String CURSE_INFUSION_BONUS = "curse_infusion_bonus";
	private static final String MASTERY_POTION_BONUS = "mastery_potion_bonus";
	private static final String AUGMENT	        = "augment";
	private static final String TierThisRun		= "TierThisRun";
	private static final String FirstUpdateTier	= "FirstUpdateTier";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USES_LEFT_TO_ID, usesLeftToID );
		bundle.put( AVAILABLE_USES, availableUsesToID );
		bundle.put( ENCHANTMENT, enchantment );
		bundle.put( CURSE_INFUSION_BONUS, curseInfusionBonus );
		bundle.put( MASTERY_POTION_BONUS, masteryPotionBonus );
		bundle.put( AUGMENT, augment );
		bundle.put( FirstUpdateTier, UpdatedTierToLevel);
		bundle.put( TierThisRun, tier);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		usesLeftToID = bundle.getFloat( USES_LEFT_TO_ID );
		availableUsesToID = bundle.getFloat( AVAILABLE_USES );
		enchantment = (Enchantment)bundle.get( ENCHANTMENT );
		curseInfusionBonus = bundle.getBoolean( CURSE_INFUSION_BONUS );
		masteryPotionBonus = bundle.getBoolean( MASTERY_POTION_BONUS );

		augment = bundle.getEnum(AUGMENT, Augment.class);
		UpdatedTierToLevel = bundle.getBoolean( FirstUpdateTier );
		if (bundle.contains( TierThisRun ))
			tier = bundle.getInt( TierThisRun );
	}
	
	@Override
	public void reset() {
		super.reset();
		usesLeftToID = USES_TO_ID;
		availableUsesToID = USES_TO_ID/2f;
	}

	@Override
	public boolean collect(Bag container) {
		if(super.collect(container)){
			if (Dungeon.hero != null && Dungeon.hero.isAlive() && isIdentified() && enchantment != null){
				Catalog.setSeen(enchantment.getClass());
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item identify(boolean byHero) {
		if (enchantment != null && byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
			Catalog.setSeen(enchantment.getClass());
		}
		return super.identify(byHero);
	}
	@Override
	public float accuracyFactor( Char owner ) {
		
		int encumbrance = 0;
		
		if( owner instanceof Hero ){
			encumbrance = STRReq() - ((Hero)owner).STR();
		}

		if (hasEnchant(Wayward.class, owner))
			encumbrance = Math.max(2, encumbrance+2);

		float ACC = this.ACC;

		return encumbrance > 0 ? (float)(ACC / Math.pow( 1.5, encumbrance )) : ACC;
	}
	
	@Override
	public float delayFactor( Char owner ) {
		float delay = delayFuror(owner);
		if (owner instanceof Hero)
			return CardCalculator.cardDelayFactor((Hero) owner, delay, this);
		return delay;
	}
	public float delayFuror( Char owner ){
		float baseDelay = baseDelay(owner) * (1f/speedMultiplier(owner));
		if(baseDelay<2F)
			//攻速小于2回合，返回常态值，无论是基础攻速还是受一次狂怒增益后的攻速
			return baseDelay;

		if((1f/speedMultiplier(owner))>1F)
			//攻速乘数大于1时，即诅咒时，返回常态值
			return baseDelay;

		//受狂怒影响后攻速仍大于2回合，额外吃一倍狂怒戒指收益
		//吃两倍狂怒收益小于2回合时，设置为2回合
		//吃两倍狂怒后仍大于2回合，则返回两倍狂怒收益的攻速
		return Math.max(2, baseDelay* (1f/speedMultiplier(owner)));
	}
	protected float baseDelay( Char owner ){
		float delay = augment.delayFactor(DLY);
		if (owner instanceof Hero) {
			int encumbrance = STRReq() - ((Hero)owner).STR();
			if (encumbrance > 0){
				delay *= Math.pow( 1.2, encumbrance );
			}
			int str = Math.max(0, baseSTRReq() - ((Hero) owner).STR());
			String cause = "";
			if (owner.wholeTime())
				cause = "以完整回合盘初步判断武器等级。";
			else if (encumbrance <= 0)
				cause = "以没有超力惩罚初步判断武器等级。";
			if (owner.wholeTime() || encumbrance <= 0)
				guessLevel(STRNeed(str - Math.max(0, encumbrance)), cause);
		}

		return delay;
	}
	protected float speedMultiplier(Char owner ){
		return RingOfFuror.attackSpeedMultiplier(owner);
	}
	public int reach(Char owner){
		int reach = RCH;
		if (hasEnchant(Projecting.class, owner))
			reach++;
		return reach;
	}
	@Override
	public int reachFactor(Char owner) {
		int reach = reach(owner);
		// GSH18天赋：漫画之心/元气一餐射程加成（实现见 GSH18Talent）
		if (owner instanceof Hero) {
			reach += GSH18Talent.reachBonus((Hero) owner, tier);
		}
		
		return reach;
	}

    public int defenseFactor( Char owner ) {
        return DEF+DEFUPGRADE*buffedLvl();
    }

	public int STRReq(){
		return STRReq(true);
	}

	private int baseSTRReq(){
		int req = STRReq(0);
		if (masteryPotionBonus){
			req -= 2;
		}
		return req;
	}
	public int STRReq(boolean onUse){
		int lvl ;
		if (isIdentified() || onUse)
			lvl = level();
        else {
			lvl = TextGuessingLevel();
		}
		int req = STRReq(lvl);
		if (hero != null && isEquipped(hero))
			req += RingOfKing.updateMultiplier(hero);
		if (masteryPotionBonus){
			req -= 2;
		}
		return req;
	}

	public abstract int STRReq(int lvl);

	protected static int STRReq(int tier, int lvl){
		lvl = Math.max(0, lvl);

		//strength req decreases at +1,+3,+6,+10,etc.
		return (8 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
	}

	@Override
	public int level() {
		int level = super.level();
		if (curseInfusionBonus) level += 1 + level/6;
		return level;
	}
	
	//overrides as other things can equip these
	@Override
	public int buffedLvl( int lvl ) {
        int level = super.buffedLvl(lvl);
        if (BuffLevelPoint != Integer.MIN_VALUE)
            return level;
		if (isEquipped( hero ) || hero.belongings.contains( this )){
            if (hero.buff(EquipLevelUp.class) != null)
                level += Dungeon.hero.hasTalent(Talent.Type56FourTwoTwo)
						? Dungeon.hero.pointsInTalent(Talent.Type56FourTwoTwo)
						: 1;
			//超频瞄准镜不提升投掷武器的虚拟等级
			if (!(this instanceof MissileWeapon)) {
				level += RingOfKing.updateMultiplier(hero);
			}
			// GSH18天赋：伴星同调——未来之星副武器有效等级向主武器看齐（实现见 GSH18Talent）
			level += GSH18Talent.companionStarSyncBonus(hero, this);
		}
        return level;
	}
	
	@Override
	public Item upgrade() {
		return upgrade(false);
	}
	
	public Item upgrade(boolean enchant ) {

		if (enchant){
			if (enchantment == null){
				enchant(Enchantment.random());
			}
		} else {
			if (hasCurseEnchant()){
				if (Random.Int(3) == 0) enchant(null);
			} else if (level() >= 4 && Random.Float(10) < Math.pow(2, level()-4)){
				enchant(null);
			}
		}
		
		cursed = false;
		
		return super.upgrade();
	}

	@Override
	public String name() {
		if (enchantment == null)
			return super.name();
		if (cursedKnown || !enchantment.curse() || Dungeon.isGameMode(WndStartGame.GameMode.IDENTIFY))
			return enchantment.name( super.name() );
		return super.name();
	}
	protected float enchantChance	= 1F;
	protected float cursedChance	= 1F;
	@Override
	public Item random() {
		//+0: 75% (3/4)
		//+1: 20% (4/20)
		//+2: 5%  (1/20)
		int n = 0;
		if (Random.Int(4) == 0) {
			n++;
			if (Random.Int(5) == 0) {
				n++;
			}
		}
		level(n);
		
		//30% chance to be cursed
		//10% chance to be enchanted
		float effectRoll = Random.Float();
		if (effectRoll < 0.3F * cursedChance) {
			enchant(Enchantment.randomCurse());
			cursed = true;
		} else if (effectRoll >= (1F - 0.1F * enchantChance)){
			enchant();
		}

		return this;
	}
	
	public Weapon enchant( Enchantment ench ) {
		if (ench == null || !ench.curse()) curseInfusionBonus = false;
		enchantment = ench;
		updateQuickslot();
		if (ench != null && isIdentified() && Dungeon.hero != null
				&& Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(this)){
			Catalog.setSeen(ench.getClass());
		}
		return this;
	}

	public Weapon enchant() {

		Class<? extends Enchantment> oldEnchantment = enchantment != null ? enchantment.getClass() : null;
		Enchantment ench = Enchantment.random( oldEnchantment );

		return enchant( ench );
	}

	public boolean hasEnchant(Class<?extends Enchantment> type, Char owner) {
		if (enchantment == null)
			return false;
		if (owner.buff(MagicImmune.class) != null)
			return false;
		VHS_Hack hack = owner.buff(VHS_Hack.class);
		if (hack != null && hack.againstEnchant())
			return false;
		return enchantment.getClass() == type;
	}
	
	//these are not used to process specific enchant effects, so magic immune doesn't affect them
	public boolean hasGoodEnchant(){
		return enchantment != null && !enchantment.curse();
	}

	public boolean hasCurseEnchant(){
		return enchantment != null && enchantment.curse();
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return enchantment != null && (cursedKnown || !enchantment.curse()) ? enchantment.glowing() : null;
	}

	public static abstract class Enchantment implements Bundlable {
		
		public static final Class<?>[] common = new Class<?>[]{
				Blazing.class, Chilling.class, Kinetic.class, Shocking.class};
		
		public static final Class<?>[] uncommon = new Class<?>[]{
				Blocking.class, Blooming.class, Elastic.class,
				Lucky.class, Projecting.class, Unstable.class};
		
		public static final Class<?>[] rare = new Class<?>[]{
				Corrupting.class, Grim.class, Vampiric.class};
		
		private static final float[] typeChances = new float[]{
				50, //12.5% each
				40, //6.67% each
				10  //3.33% each
		};
		
		public static final Class<?>[] curses = new Class<?>[]{
				Annoying.class, Displacing.class, Exhausting.class, Fragile.class,
				Sacrificial.class, Wayward.class, Polarized.class, Friendly.class
		};
		
			
		public abstract int proc( Weapon weapon, Char attacker, Char defender, int damage );

		protected float procChanceMultiplier( Char attacker ){
			float multi = 1f;
			if (attacker instanceof Hero && ((Hero) attacker).hasTalent(Talent.ENRAGED_CATALYST)){
				Berserk rage = attacker.buff(Berserk.class);
				if (rage != null) {
					multi += (rage.rageAmount() * 0.15f) * ((Hero) attacker).pointsInTalent(Talent.ENRAGED_CATALYST);
				}
			}
			if (attacker.buff(Talent.SpiritBladesTracker.class) != null
					&& ((Hero)attacker).pointsInTalent(Talent.SPIRIT_BLADES) == 4){
				multi += 0.1f;
			}
			if (attacker.buff(Talent.StrikingWaveTracker.class) != null
					&& ((Hero)attacker).pointsInTalent(Talent.STRIKING_WAVE) == 4){
				multi += 0.2f;
			}
			return multi;
		}

		public String name() {
			if (!curse())
				return name( Messages.get(this, "enchant"));
			else
				return name( Messages.get(Item.class, "curse"));
		}

		public String name( String weaponName ) {
			return Messages.get(this, "name", weaponName);
		}

		public String desc() {
			return Messages.get(this, "desc");
		}

		public boolean curse() {
			return false;
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}
		
		public abstract ItemSprite.Glowing glowing();

		@SafeVarargs
        public static Enchantment random(Class<? extends Enchantment> ... toIgnore ) {
			switch(Random.chances(typeChances)){
				case 0: default:
					return randomCommon( toIgnore );
				case 1:
					return randomUncommon( toIgnore );
				case 2:
					return randomRare( toIgnore );
			}
		}

        @SafeVarargs
        public static Enchantment randomCommon(Class<? extends Enchantment> ... toIgnore ) {
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(common));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}

        @SafeVarargs
        public static Enchantment randomUncommon(Class<? extends Enchantment> ... toIgnore ) {
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(uncommon));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}

		@SafeVarargs
        public static Enchantment randomRare(Class<? extends Enchantment> ... toIgnore ) {
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(rare));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}

		@SafeVarargs
        public static Enchantment randomCurse(Class<? extends Enchantment> ... toIgnore ){
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(curses));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}
		
	}
}
