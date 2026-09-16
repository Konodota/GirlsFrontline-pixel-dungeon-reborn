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

package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EquipLevelUp;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EquipmentBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.GSH18Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.WarriorTalent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.Type561Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.AntiEntropy;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Bulk;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Displacement;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Metabolism;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Multiplicity;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Overgrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Stench;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Affection;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Camouflage;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Entanglement;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Repulsion;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Stone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Swiftness;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Thorns;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfKing;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStartGame;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class Armor extends EquipableItem {

	protected static final String AC_DETACH       = "DETACH";
	
	public enum Augment {
		EVASION (2 , -1),
		DEFENSE (-2, 1),
		NONE	(0   ,  0);
		
		private final int evasionFactor;
		private final int defenceFactor;
		
		Augment(int eva, int df){
			evasionFactor = eva;
			defenceFactor = df;
		}
		
		public int evasionFactor(int level){
			return (2 + level) * evasionFactor;
		}
		
		public int defenseFactor(int level){
			return (2 + level) * defenceFactor;
		}
	}
	
	public Augment augment = Augment.NONE;
	
	public Glyph glyph;
	public boolean curseInfusionBonus = false;
	public boolean masteryPotionBonus = false;
	
	protected BrokenSeal seal;
	
	public int tier;
    public boolean UpdatedTierToLevel = false;
    public float broken;
    public int duration;
	private static final int USES_TO_ID = 10;
	private float usesLeftToID = USES_TO_ID;
	private float availableUsesToID = USES_TO_ID/2f;
	
	public Armor( int tier ) {
		this.tier = tier;
	}

    public Item clone(Item item){
        Armor armor = (Armor) item;
        broken = armor.broken;
        duration = armor.duration;
        super.clone(armor);
        inscribe(armor.glyph);
        augment = armor.augment;
        masteryPotionBonus = armor.masteryPotionBonus;
        curseInfusionBonus = armor.curseInfusionBonus;
        return this;
    }

    @Override
    public void update(){
        super.update();
        if (Dungeon.isGameMode(WndStartGame.GameMode.IDENTIFY)){
            if (!UpdatedTierToLevel){
                boolean hasGlyph = glyph != null;
                boolean curse = cursed;
                UpdatedTierToLevel = true;
                int workingTier = tier - 1;
                if (workingTier > 0) {
                    upgrade(hasGlyph);
                    workingTier--;
                }
                while (workingTier > 0){
                    if (workingTier >= Random.Int(3)+1 )
                        upgrade(hasGlyph);
                    workingTier -= 3;
                }
                cursed = curse;
            }
            tier = 10;
            while (STRReq() > hero.STR())
                tier--;
        }
    }
	private static final String USES_LEFT_TO_ID = "uses_left_to_id";
	private static final String AVAILABLE_USES  = "available_uses";
	private static final String GLYPH			= "glyph";
	private static final String CURSE_INFUSION_BONUS = "curse_infusion_bonus";
	private static final String MASTERY_POTION_BONUS = "mastery_potion_bonus";
	private static final String SEAL            = "seal";
	private static final String AUGMENT			= "augment";
    private static final String BROKEN          = "broken";
    private static final String DURATION        = "duration";
    private static final String TierThisRun		= "TierThisRun";
    private static final String FirstUpdateTier	= "FirstUpdateTier";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USES_LEFT_TO_ID, usesLeftToID );
		bundle.put( AVAILABLE_USES, availableUsesToID );
		bundle.put( GLYPH, glyph );
		bundle.put( CURSE_INFUSION_BONUS, curseInfusionBonus );
		bundle.put( MASTERY_POTION_BONUS, masteryPotionBonus );
		bundle.put( SEAL, seal);
		bundle.put( AUGMENT, augment);
        bundle.put( BROKEN, broken);
        bundle.put( DURATION, duration);
        bundle.put( FirstUpdateTier, UpdatedTierToLevel);
        bundle.put( TierThisRun, tier);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		usesLeftToID = bundle.getInt( USES_LEFT_TO_ID );
		availableUsesToID = bundle.getInt( AVAILABLE_USES );
		inscribe((Glyph) bundle.get(GLYPH));
		curseInfusionBonus = bundle.getBoolean( CURSE_INFUSION_BONUS );
		masteryPotionBonus = bundle.getBoolean( MASTERY_POTION_BONUS );
		seal = (BrokenSeal)bundle.get(SEAL);
		
		augment = bundle.getEnum(AUGMENT, Augment.class);
        broken = bundle.getFloat(BROKEN);
        duration = bundle.getInt(DURATION);
        UpdatedTierToLevel = bundle.getBoolean( FirstUpdateTier );
        if (bundle.contains( TierThisRun ))
            tier = bundle.getInt( TierThisRun );
	}

	@Override
	public void reset() {
		super.reset();
		usesLeftToID = USES_TO_ID;
		availableUsesToID = USES_TO_ID/2f;
		//armor can be kept in bones between runs, the seal cannot.
		seal = null;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (seal != null) actions.add(AC_DETACH);
		return actions;
	}

    @Override
    public boolean collect(Bag container) {
        if(super.collect(container)){
            if (Dungeon.hero != null && Dungeon.hero.isAlive() && isIdentified() && glyph != null){
                Catalog.setSeen(glyph.getClass());
            }
            return true;
        } else {
            return false;
        }
    }
    @Override
    public Item identify(boolean byHero) {
        if (glyph != null && byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
            Catalog.setSeen(glyph.getClass());
        }
        return super.identify(byHero);
    }
	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_DETACH) && seal != null){
			BrokenSeal.WarriorShield sealBuff = hero.buff(BrokenSeal.WarriorShield.class);
			if (sealBuff != null) sealBuff.setArmor(null);

			BrokenSeal detaching = seal;
			seal = null;

			if (detaching.level() > 0){
				degrade();
			}
			if (detaching.getGlyph() != null){
				boolean glyphCommonOrUncommon = Arrays.asList(Glyph.common).contains(detaching.getGlyph().getClass())
						|| Arrays.asList(Glyph.uncommon).contains(detaching.getGlyph().getClass());
				// 战士（UMP45）符文转移：刻印是否随列痕保留（实现见 WarriorTalent）
				if (WarriorTalent.sealKeepsGlyphOnDetach(hero, glyphCommonOrUncommon)){
					inscribe(null);
				} else {
					//没有天赋时直接令袖章刻印清除
					detaching.setGlyph(null);
				}
			}
			GLog.i( Messages.get(Armor.class, "detach_seal") );
			hero.sprite.operate(hero.pos);
			if (!detaching.collect()){
				Dungeon.level.drop(detaching, hero.pos);
			}
		}
	}

    protected mixArmor mixArmorTracker;
    @Override
    public void Tracker(Char owner){
        super.Tracker(owner);
        if (owner instanceof Hero){
            Hero hero = (Hero) owner;
            singleTracker(hero);
            if (hero.belongings.armor != null)
                hero.belongings.armor.singleTracker(hero);
            if (hero.belongings.secArmor != null)
                hero.belongings.secArmor.singleTracker(hero);
        }
    }
    private void singleTracker(Char owner){
        if (mixArmorTracker == null) {
            mixArmorTracker = new mixArmor();
            mixArmorTracker.attachTo(owner);
        }
    }
    @Override
    public void stopTrack(){
        super.stopTrack();
        if (mixArmorTracker != null) {
            mixArmorTracker.detach();
            mixArmorTracker = null;
        }
    }
	@Override
	public boolean doEquip( Hero hero ) {
        Tracker(hero);
		detach(hero.belongings.backpack);
        //主护甲为空，此时全空或复活未选
		if (hero.belongings.armor() == null) {
            //有且仅有复活未选时，armor()为空而armor不为空，直接脱下即可
            changeFirst(hero);
			onEquip(hero);
			return true;
		}
        //新旧护甲都不适用于调整至副护甲时，按旧逻辑处理（坚守天赋判定见 WarriorTalent）
        if (WarriorTalent.useLegacyArmorEquip(hero, tier())){
            //因为加入了副护甲自动调整至主护甲的机制，所以旧逻辑不方便直接调用doUnequip，把原doUnequip的代码复制一份出来使用
            if (hero.belongings.armor.unEquipable(hero)) {
                changeFirst(hero);
                onEquip(hero);
                return true;
            }
            else {
                GLog.w(Messages.get(EquipableItem.class, "unequip_cursed"));
                collect( hero.belongings.backpack );
                return false;
            }
        }
        //对于pointsInTalent，无加点等效加点0，阶数至少为1
        //所以上述逻辑完全覆盖了旧逻辑，并且执行代码相同，只是为了新内容而更改表述方式，下面的逻辑是为天赋改动新增的
        else {
            //主护甲无红底的情况下，允许与副护甲交互
            if (!hero.belongings.armor.cursed) {
                //副护甲阶数不小于新护甲，那么新护甲只能与副护甲交换
                if (hero.belongings.SecondArmor() != null && hero.belongings.secArmor.tier()>= this.tier()){
                    BrokenSeal.WarriorShield sealBuff = hero.buff(BrokenSeal.WarriorShield.class);
                    if (sealBuff !=null && hero.belongings.secArmor == sealBuff.armor)
                        sealBuff.setArmor(null);
                    hero.belongings.secArmor.doDrop(hero);
                    hero.belongings.secArmor = this;
                    onEquip(hero);
                    curseMoving(hero);
                    return true;
                }
                //新护甲只能与主护甲交换的情况已在旧逻辑部分处理掉了
                //主>天赋 && 新>天赋、主 == 新已处理，那么装备新护甲，新大于主则有顶替主或者主进入副护甲两种情况，主大于新同理，属于复杂情况
                //复杂情况让玩家选择
                //由于要先移除物品以流出空位给原来的装备，而这里是一个监听窗口，不做处理，所以先收集回来
                collect(hero.belongings.backpack);
                armorChoose(hero, this);
                return false;
            }
            else {
                //主护甲红底，新护甲高阶，无副护甲，主护甲可移动至副护甲，则允许移动，否则不允许
                if (tier() > hero.belongings.armor.tier() &&
                        hero.belongings.SecondArmor() == null){
                    if (hero.belongings.secArmor!=null)
                        hero.belongings.secArmor.doDrop(hero);
                    hero.belongings.secArmor = hero.belongings.armor;
                    hero.belongings.armor = this;
                    onEquip(hero);
                    curseMoving(hero);
                    return true;
                }
                GLog.w(Messages.get(Armor.class, "unequip_cursed_second"));
            }
        }

        collect( hero.belongings.backpack );
        return false;
	}

    public int tier() {
        return 0;
    }

    private void onEquip( Hero hero ) {
        cursedKnown = true;
        if (cursed) {
            equipCursed( hero );
            GLog.n( Messages.get(Armor.class, "equip_cursed") );
        }
        if (hero.belongings.armor == this)
            ((HeroSprite)hero.sprite).updateArmor();
        activate(hero);
        Talent.onItemEquipped(hero, this);
        hero.spendAndNext( time2equip() );
        Tracker(hero);
    }
    @Override
    public boolean unEquipable(Hero hero){
        // 医生直觉 +1级允许取下被诅咒的防具（实现见 GSH18Talent）
        return  super.unEquipable(hero) || GSH18Talent.canUnequipArmor(hero);
    }
    private void changeFirst(Hero hero){
        BrokenSeal.WarriorShield sealBuff = hero.buff(BrokenSeal.WarriorShield.class);
        if (sealBuff != null && hero.belongings.armor == sealBuff.armor) {
            sealBuff.setArmor(null);
        }
        if (hero.belongings.armor != null) {
            boolean kept = hero.belongings.armor.keptThoughLostInvent;
            hero.belongings.armor.keptThoughLostInvent = true;
            hero.belongings.armor.collect(hero.belongings.backpack);
            hero.belongings.armor.keptThoughLostInvent = kept;
            hero.spend( time2equip() );
        }
        hero.belongings.armor = this;
        ((HeroSprite)hero.sprite).updateArmor();
    }
    public static void curseMoving( Hero hero ) {
        if (hero.belongings.secArmor != null && hero.belongings.secArmor.cursed){
            //红底转移
            if (!hero.belongings.armor.cursed){
                hero.belongings.armor.cursed = true;
                hero.belongings.secArmor.cursed = false;
                GLog.n( Messages.get(Armor.class, "cursed_moving") );
                //破坏正面附魔
                if (hero.belongings.armor.hasGoodGlyph()) {
                    hero.belongings.armor.glyph = null;
                    GLog.n( Messages.get(Armor.class, "broken") );
                }
            }
        }
    }
    private static void armorChoose (Hero hero, Armor armor){
        String armor1;
        String armor2;
        final String nothing = "---";
        //armor必定不为空，为空已被第一个if截取到了
        //当前结构
        if (hero.belongings.secArmor != null)
            armor1 = Messages.titleCase(hero.belongings.armor.toString());
        //复合结构
        else if (hero.belongings.armor.tier() > armor.tier())
            armor1 =  Messages.titleCase(hero.belongings.armor.toString());
        else
            armor1 = nothing;

        //当前结构
        if (hero.belongings.secArmor != null)
            armor2 = Messages.titleCase(hero.belongings.secArmor.toString());
        //复合结构
        else if (hero.belongings.armor.tier() < armor.tier())
            armor2 =  Messages.titleCase(hero.belongings.armor.toString());
        else
            armor2 = nothing;
        String finalArmor1 = armor1;
        String finalArmor2 = armor2;
        GameScene.show(
                new WndOptions(new ItemSprite(armor),
                        Messages.get(Armor.class, "select_title"),
                        Messages.get(Armor.class, "select_message"),
                        finalArmor1,
                        finalArmor2) {

                    @Override
                    protected void onSelect(int index) {
                        if (index == 0) {
                            armor.detach(hero.belongings.backpack);
                            //选择第一个，那么就是准备占据掉主护甲，在没有副护甲的情况下将主护甲移动到副护甲
                            if (finalArmor1.equals(nothing)) {
                                hero.belongings.secArmor = hero.belongings.armor;
                                hero.belongings.armor = null;
                            }
                            armor.changeFirst(hero);
                            armor.onEquip(hero);
                            curseMoving(hero);
                        } else if (index == 1) {
                            armor.detach(hero.belongings.backpack);
                            if (finalArmor2.equals(nothing)){
                                hero.belongings.secArmor = armor;
                            }
                            else if (finalArmor1.equals(nothing)){
                                boolean kept = hero.belongings.armor.keptThoughLostInvent;
                                hero.belongings.armor.keptThoughLostInvent = true;
                                hero.belongings.armor.collect(hero.belongings.backpack);
                                hero.belongings.armor.keptThoughLostInvent = kept;
                                hero.belongings.armor = armor;
                            }
                            else {
                                if (hero.belongings.secArmor != null) {
                                    hero.belongings.secArmor.doDrop(hero);
                                }
                                if (hero.belongings.armor.tier() < armor.tier()) {
                                    hero.belongings.secArmor = hero.belongings.armor;
                                    hero.belongings.armor = armor;
                                }
                                else {
                                    hero.belongings.secArmor = armor;
                                }
                            }
                            armor.onEquip(hero);
                            curseMoving(hero);
                        }
                    }
                }
        );
    }
	@Override
	public void activate(Char ch) {
		if (seal != null) {
            Buff.affect(ch, BrokenSeal.WarriorShield.class).setArmor(this);
        }
        Tracker(ch);
	}

	public void affixSeal(BrokenSeal seal){
		this.seal = seal;
		if (seal.level() > 0){
			//doesn't trigger upgrading logic such as affecting curses/glyphs
			int newLevel = trueLevel()+1;
			level(newLevel);
			Badges.validateItemLevelAquired(this);
		}
		if (seal.getGlyph() != null){
			inscribe(seal.getGlyph());
		}
		if (isEquipped(Dungeon.hero)){
			Buff.affect(Dungeon.hero, BrokenSeal.WarriorShield.class).setArmor(this);
		}
	}

	public BrokenSeal checkSeal(){
		return seal;
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
        //主护甲红底不允许解除副护甲
        if (hero.belongings.armor()!=null  && hero.belongings.armor.cursed &&
                hero.belongings.secArmor == this ) {
            GLog.w(Messages.get(Armor.class, "unequip_cursed_second"));
            return false;
        }

		if (super.doUnequip( hero, collect, single )) {

            if (hero.belongings.armor == this){
                hero.belongings.armor = hero.belongings.secArmor;
                ((HeroSprite)hero.sprite).updateArmor();
            }
            hero.belongings.secArmor = null;
            BrokenSeal.WarriorShield sealBuff = hero.buff(BrokenSeal.WarriorShield.class);
            if (sealBuff != null && hero.belongings.armor != sealBuff.armor) {
                sealBuff.setArmor(null);
            }

			return true;

		} else {

			return false;

		}
	}
	
	@Override
	public boolean isEquipped( Hero hero ) {
		return hero.belongings.armor() == this || hero.belongings.SecondArmor() == this
                || ownerBuff instanceof EquipmentBuff && ownerBuff.target == hero;
	}

	public final int DRMax(){
		return DRMax(buffedLvl());
	}

	public int DRMax(int lvl){
		if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
			return 1 + tier + lvl + augment.defenseFactor(lvl);
		}

		int max = tier * (2 + lvl) + augment.defenseFactor(lvl);
		if (lvl > max){
			return ((lvl - max)+1)/2;
		} else {
			return max;
		}
	}

	public final int DRMin(){
		return DRMin(buffedLvl());
	}

	public int DRMin(int lvl){
		if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
			return 0;
		}

		int max = DRMax(lvl);
		if (lvl >= max){
			return (lvl - max);
		} else {
			return lvl;
		}
	}

    public float evasionFactor( Char owner, float evasion ,boolean isSecond){
		
		if (hasGlyph(Stone.class, owner) && !((Stone)glyph).testingEvasion()){
			return 0;
		}
		
		if (owner instanceof Hero && !isSecond){
			int aEnc = STRReq() - ((Hero) owner).STR();
			if (aEnc > 0) evasion /= Math.pow(1.5, aEnc);
			
			Momentum momentum = owner.buff(Momentum.class);
			if (momentum != null)
				evasion += momentum.evasionBonus(((Hero) owner).lvl, Math.max(0, -aEnc));
		}
		float add = augment.evasionFactor(buffedLvl());
        if ( isSecond ){
            // 战士（UMP45）坚守：副护甲闪避上限（实现见 WarriorTalent）
            add = Math.min( WarriorTalent.secondArmorEvasionCap(hero, tier()), add);
        }
		return evasion + add;
	}
	
	public float speedFactor( Char owner, float speed ){
		
		if (owner instanceof Hero) {
			int aEnc = STRReq() - ((Hero) owner).STR();
			if (aEnc > 0) {
                speed /= Math.pow(1.2, aEnc);
            }
            int str = Math.max(0, baseSTRReq() - ((Hero) owner).STR());
            String cause = "";
            if (owner.wholeTime())
                cause = "以完整回合盘初步判断护甲等级。";
            else if (aEnc <= 0)
                cause = "以没有超力惩罚初步判断护甲等级。";
            if (owner.wholeTime() || aEnc <=0)
                guessLevel(STRNeed(str - Math.max(0, aEnc)), cause);
		}else {
            if (hasGlyph(Swiftness.class, owner)) {
                boolean enemyNear = false;
                PathFinder.buildDistanceMap(owner.pos, Dungeon.level.passable, 2);
                for (Char ch : Actor.chars()) {
                    if (PathFinder.distance[ch.pos] != Integer.MAX_VALUE && owner.alignment != ch.alignment) {
                        enemyNear = true;
                        break;
                    }
                }
                if (!enemyNear) {
                    speed *= (1.2f + 0.04f * buffedLvl());
                }
            } else if (hasGlyph(Flow.class, owner) && Dungeon.level.water[owner.pos]) {
                speed *= (2f + 0.25f * buffedLvl());
            }

            if (hasGlyph(Bulk.class, owner) &&
                    (Dungeon.level.map[owner.pos] == Terrain.DOOR
                            || Dungeon.level.map[owner.pos] == Terrain.OPEN_DOOR)) {
                speed /= 3f;
            }
        }
		
		return speed;
		
	}

    public float stealthFactor( Char owner, float stealth ){

        if (hasGlyph(Obfuscation.class, owner)){
            stealth += 1 + buffedLvl()/3f;
        }

        return stealth;
    }
    public static float stealthFactor( float stealth ){

        if ( hero.belongings.hasGlyph(Obfuscation.class, hero) ){
            stealth += 1 + hero.belongings.GlyphLevel(Obfuscation.class)/3F;
        }
        return stealth;
    }
	
	@Override
	public int level() {
		int level = super.level();
		if (curseInfusionBonus) level += 1 + level/6;
		return level;
	}
	
	//other things can equip these, for now we assume only the hero can be affected by leveling debuffs
	@Override
	public int buffedLvl(int lvl) {
        int level = super.buffedLvl(lvl);
        if (BuffLevelPoint != Integer.MIN_VALUE)
            return level;
		if (isEquipped( hero ) || hero.belongings.contains(this)) {
            // 56-1式天赋：火线补给/饭饱为钢/饱腹护甲（实现见 Type561Talent）
            level = Type561Talent.armorLevelBonus(hero, level);
            //down at 200, 200+300, 200+300+400, ...
            level -= (int) ((Math.sqrt(200*broken + 22500) - 150)/100);
            level += RingOfKing.updateMultiplier(hero);
		}
		return level;
	}
	
	@Override
	public Item upgrade() {
		return upgrade( false );
	}
	
	public Item upgrade( boolean inscribe ) {

        broken = 0;
		if (inscribe){
			if (glyph == null){
				inscribe( Glyph.random() );
			}
		} else {
			if (hasCurseGlyph()){
				if (Random.Int(3) == 0) inscribe(null);
			} else{

                //the chance from +4/5, and then +6 can be set to 0% with metamorphed runic transference
                // 非战士蜕变符文转移的刻印丢失强化等级加成（实现见 WarriorTalent）
                int lossChanceStart = 4 + WarriorTalent.runicLossChanceBonus(Dungeon.hero);

                if (level() >= lossChanceStart && Random.Float(10) < Math.pow(2, level()-4)) {
                    inscribe(null);
                }
			}
		}
		
		cursed = false;

		if (seal != null && seal.level() == 0)
			seal.upgrade();

		return super.upgrade();
	}
	
	public int proc( Char attacker, Char defender, int damage ) {
		if (overLoad == OverLoad.OVERLOADING)
            overLoadLeft -= 4;
		if (glyph != null && defender.buff(MagicImmune.class) == null) {
			damage = glyph.proc( this, attacker, defender, damage );
		}
		
		if (!levelKnown && defender == Dungeon.hero) {
			float uses = Math.min( availableUsesToID, Talent.itemIDSpeedFactor(Dungeon.hero, this) );
			availableUsesToID -= uses;
			usesLeftToID -= uses;
			if (usesLeftToID <= 0) {
				identify();
				GLog.p( Messages.get(Armor.class, "identify") );
				Badges.validateItemLevelAquired( this );
			}
		}
		
		return damage;
	}
	
	@Override
	public void onHeroGainExp(float levelPercent, Hero hero) {
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		if (!levelKnown && isEquipped(hero) && availableUsesToID <= USES_TO_ID/2f) {
			//gains enough uses to ID over 0.5 levels
			availableUsesToID = Math.min(USES_TO_ID/2f, availableUsesToID + levelPercent * USES_TO_ID);
		}
	}
	
	@Override
	public String name() {
        if (glyph == null)
            return super.name();
        if (cursedKnown || !glyph.curse() || Dungeon.isGameMode(WndStartGame.GameMode.IDENTIFY))
            return glyph.name( super.name() );
		return super.name();
	}
	
	@Override
	public String info() {
		String info = super.info();
		
		if (levelKnown) {
			info += "\n\n" + Messages.get(Armor.class, "curr_absorb", DRMin(), DRMax(), STRReq(true));
			
			if (STRReq(true) > Dungeon.hero.STR()) {
				info += " " + Messages.get(Armor.class, "too_heavy");
			}
		} else {
            int lvl = TextGuessingBuffedLevel();
			info += "\n\n" + Messages.get(Armor.class, "avg_absorb", DRMin(lvl), DRMax(lvl), STRReq(false));

			if (STRReq(false) > Dungeon.hero.STR()) {
				info += " " + Messages.get(Armor.class, "probably_too_heavy");
			}
		}

		switch (augment) {
			case EVASION:
				info += " " + Messages.get(Armor.class, "evasion");
				break;
			case DEFENSE:
				info += " " + Messages.get(Armor.class, "defense");
				break;
			case NONE:
		}
		
		if (glyph != null  && (cursedKnown || !glyph.curse())) {
			info += "\n\n" +  Messages.get(Armor.class, "inscribed", glyph.name());
			info += " " + glyph.desc();
		}
		
		if (cursed && isEquipped( Dungeon.hero )) {
			info += "\n\n" + Messages.get(Armor.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Armor.class, "cursed");
		} else if (seal != null) {
			info += "\n\n" + Messages.get(Armor.class, "seal_attached", seal.maxShield(tier, level()));
		} else if (!isIdentified() && cursedKnown){
			info += "\n\n" + Messages.get(Armor.class, "not_cursed");
		}
        if (broken>0) {
            info += "\n\n" + Messages.get(Armor.class, "broken_times", broken);
        }

        if (overLoad != OverLoad.NONE)
            info += "\n\n" + Messages.get(Item.class, overLoad.name(), overLoadLeft);
		return info;
	}

	@Override
	public Emitter emitter() {
		if (seal == null) return super.emitter();
		Emitter emitter = new Emitter();
		emitter.pos(ItemSpriteSheet.film.width(image)/2f + 2f, ItemSpriteSheet.film.height(image)/3f);
		emitter.fillTarget = false;
		emitter.pour(Speck.factory( Speck.RED_LIGHT ), 0.6f);
		return emitter;
	}

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
		//15% chance to be inscribed
		float effectRoll = Random.Float();
		if (effectRoll < 0.3f) {
			inscribe(Glyph.randomCurse());
			cursed = true;
		} else if (effectRoll >= 0.85f){
			inscribe();
		}

		return this;
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
        else
            lvl = TextGuessingLevel();
        int req = STRReq(lvl);
        if (hero != null && isEquipped(hero))
            req += RingOfKing.updateMultiplier(hero);
        if (masteryPotionBonus){
            req -= 2;
        }
        return req;
    }

	public int STRReq(int lvl){
		return STRReq(tier, lvl);
	}

	protected static int STRReq(int tier, int lvl){
		lvl = Math.max(0, lvl);

		//strength req decreases at +1,+3,+6,+10,etc.
		return (8 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
	}

    public int STRNeed(int extraSTR){
        return extraSTR * (extraSTR + 1) / 2;
    }

	@Override
	public int value() {
		if (seal != null) return 0;

		int price = 20 * tier;
		if (hasGoodGlyph()) {
			price *= 1.5;
		}
		if (cursedKnown && (cursed || hasCurseGlyph())) {
			price /= 2;
		}
		if (levelKnown && level() > 0) {
			price *= (level() + 1);
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}

	public Armor inscribe( Glyph glyph ) {
		if (glyph == null || !glyph.curse()) curseInfusionBonus = false;
		this.glyph = glyph;
		updateQuickslot();
		//the hero needs runic transference to actually transfer, but we still attach the glyph here
		// in case they take that talent in the future
		if (seal != null){
			seal.setGlyph(glyph);
		}
        if (glyph != null && isIdentified() && Dungeon.hero != null
                && Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(this)){
            Catalog.setSeen(glyph.getClass());
        }
		return this;
	}

	public Armor inscribe() {

		Class<? extends Glyph> oldGlyphClass = glyph != null ? glyph.getClass() : null;
		Glyph gl = Glyph.random( oldGlyphClass );

		return inscribe( gl );
	}

	public boolean hasGlyph(Class<?extends Glyph> type, Char owner) {
		return glyph != null && glyph.getClass() == type&& owner.buff(MagicImmune.class) == null;
	}

	//these are not used to process specific glyph effects, so magic immune doesn't affect them
	public boolean hasGoodGlyph(){
		return glyph != null && !glyph.curse();
	}

	public boolean hasCurseGlyph(){
		return glyph != null && glyph.curse();
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return glyph != null && (cursedKnown || !glyph.curse()) ? glyph.glowing() : null;
	}
	
	public static abstract class Glyph implements Bundlable {
		
		public static final Class<?>[] common = new Class<?>[]{
				Obfuscation.class, Swiftness.class, Viscosity.class, Potential.class };
		
		public static final Class<?>[] uncommon = new Class<?>[]{
				Brimstone.class, Stone.class, Entanglement.class,
				Repulsion.class, Camouflage.class, Flow.class };
		
		public static final Class<?>[] rare = new Class<?>[]{
				Affection.class, AntiMagic.class, Thorns.class };
		
		private static final float[] typeChances = new float[]{
				50, //12.5% each
				40, //6.67% each
				10  //3.33% each
		};

		public static final Class<?>[] curses = new Class<?>[]{
				AntiEntropy.class, Corrosion.class, Displacement.class, Metabolism.class,
				Multiplicity.class, Stench.class, Overgrowth.class, Bulk.class
		};
		
		public abstract int proc( Armor armor, Char attacker, Char defender, int damage );
		
		public String name() {
			if (!curse())
				return name( Messages.get(this, "glyph") );
			else
				return name( Messages.get(Item.class, "curse"));
		}
		
		public String name( String armorName ) {
			return Messages.get(this, "name", armorName);
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
        public static Glyph random(Class<? extends Glyph> ... toIgnore ) {
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
        public static Glyph randomCommon(Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(common));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}

		@SafeVarargs
        public static Glyph randomUncommon(Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(uncommon));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}

		@SafeVarargs
        public static Glyph randomRare(Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(rare));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}

		@SafeVarargs
        public static Glyph randomCurse(Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(curses));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}
		
	}
    public class mixArmor extends Buff {

        {
            revivePersists = true;
        }

        private static final float recover  = 1F;
        private static final float unEquip  = 2F;
        private static final float broking   = 1F;
        private static final float inside   = 0.5F;

        @Override
        public boolean attachTo( Char target ) {
            super.attachTo( target );

            if (broken > 0 && duration > 0 && CooldownTracker.updateTime > duration){
                int num = CooldownTracker.updateTime;
                broken-= 2*(num - duration);
                broken = Math.max(0, broken);
                duration = CooldownTracker.updateTime;
            }
            return true;
        }

//        public int icon() {
//            return BuffIndicator.INVISIBLE;
//        }
        @Override
        public String toString() {
            return armor().toString();
        }
        @Override
        public String desc(){
             return armor().broken + "\n" + armor().duration + "\n" + CooldownTracker.updateTime;
        }
        @Override
        public boolean act() {
            spend(TICK);
            duration = CooldownTracker.updateTime;
			
			LockedFloor lock = target.buff(LockedFloor.class);
			if(lock != null && !lock.regenOn())
				return true;
			
            Hero hero = (Hero) target;
            boolean isEquip = isEquipped(hero);
            if (isEquip && (hero.belongings.armor() != null && hero.belongings.SecondArmor() != null)){
                if (hero.belongings.armor == armor())
                    armor().broken += broking;
                else if (hero.belongings.secArmor == armor()) {
                    armor().broken += inside;
                }
            }
            else if (isEquip) {
                armor().broken -= recover;
            }
            else {
                armor().broken -= unEquip;
            }
            if (broken <= 0){
                broken = 0;
                if (hero.belongings.armor() != armor() && hero.belongings.SecondArmor() != armor()) {
                    detach();
                }
            }
            return true;
        }

        public Armor armor(){
            return Armor.this;
        }
    }
}
