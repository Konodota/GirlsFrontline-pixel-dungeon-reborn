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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.GSH18Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ScrollEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TalentSecondSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.StarShield;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.RedBook;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ShootGun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public enum Talent {

	//Warrior T1
	HEARTY_MEAL(0), ARMSMASTERS_INTUITION(1), TEST_SUBJECT(2), IRON_WILL(HeroClass.WARRIOR, 3),
	//Warrior T2
	IRON_STOMACH(4), RESTORED_WILLPOWER(HeroClass.WARRIOR, 5), RUNIC_TRANSFERENCE(HeroClass.WARRIOR, 6), LETHAL_MOMENTUM(7), IMPROVISED_PROJECTILES(8),
	//Warrior T3
	HOLD_FAST(9, 3), STRONGMAN(10, 3, 655),
	//Berserker T3
	ENDLESS_RAGE(11, 3), BERSERKING_STAMINA(12, 3), ENRAGED_CATALYST(13, 3),
	//Gladiator T3
	CLEAVE(14, 3), LETHAL_DEFENSE(15, 3), ENHANCED_COMBO(16, 3),
	//Heroic Leap T4
	BODY_SLAM(17, 4), IMPACT_WAVE(18, 4), DOUBLE_JUMP(19, 4),
	//Shockwave T4
	EXPANDING_WAVE(20, 4), STRIKING_WAVE(21, 4), SHOCK_FORCE(22, 4),
	//Endure T4
	SUSTAINED_RETRIBUTION(23, 4), SHRUG_IT_OFF(24, 4), EVEN_THE_ODDS(25, 4),

	//Mage T1
	EMPOWERING_MEAL(32), SCHOLARS_INTUITION(33), TESTED_HYPOTHESIS(34), BACKUP_BARRIER(HeroClass.MAGE, 35),
	//Mage T2
	ENERGIZING_MEAL(36), ENERGIZING_UPGRADE(37), WAND_PRESERVATION(HeroClass.MAGE, 38), ARCANE_VISION(39), SHIELD_BATTERY(40),
	//Mage T3
	EMPOWERING_SCROLLS(41, 3), ALLY_WARP(42, 3),
	//Battlemage T3
	EMPOWERED_STRIKE(43, 3), MYSTICAL_CHARGE(44, 3), EXCESS_CHARGE(45, 3),
	//Warlock T3
	SOUL_EATER(46, 3), SOUL_SIPHON(47, 3), NECROMANCERS_MINIONS(48, 3),
	//Elemental Blast T4
	BLAST_RADIUS(49, 4), ELEMENTAL_POWER(50, 4), REACTIVE_BARRIER(51, 4),
	//Wild Magic T4
	WILD_POWER(52, 4), FIRE_EVERYTHING(53, 4), CONSERVED_MAGIC(54, 4),
	//Warp Beacon T4
	TELEFRAG(55, 4), REMOTE_BEACON(56, 4), LONGRANGE_WARP(57, 4),

	//Rogue T1
	CACHED_RATIONS(64, 2, 655), THIEFS_INTUITION(65), SUCKER_PUNCH(66), PROTECTIVE_SHADOWS(67),
	//Rogue T2
	MYSTICAL_MEAL(68), MYSTICAL_UPGRADE(69), WIDE_SEARCH(HeroClass.ROGUE, 70), SILENT_STEPS(71), ROGUES_FORESIGHT(72),
	//Rogue T3
	ENHANCED_RINGS(73, 3), LIGHT_CLOAK(HeroClass.ROGUE, 74, 3),
	//Assassin T3
	ENHANCED_LETHALITY(75, 3), ASSASSINS_REACH(76, 3), BOUNTY_HUNTER(77, 3),
	//Freerunner T3
	EVASIVE_ARMOR(78, 3), PROJECTILE_MOMENTUM(79, 3), SPEEDY_STEALTH(80, 3),
	//Smoke Bomb T4
	HASTY_RETREAT(81, 4), BODY_REPLACEMENT(82, 4), SHADOW_STEP(83, 4),
	//Death Mark T4
	FEAR_THE_REAPER(84, 4), DEATHLY_DURABILITY(85, 4), DOUBLE_MARK(86, 4),
	//Shadow Clone T4
	SHADOW_BLADE(87, 4), CLONED_ARMOR(88, 4), PERFECT_COPY(89, 4),

	//Huntress T1
	NATURES_BOUNTY(96), SURVIVALISTS_INTUITION(97), FOLLOWUP_STRIKE(98), NATURES_AID(99),
	//Huntress T2
	INVIGORATING_MEAL(100), RESTORED_NATURE(101), REJUVENATING_STEPS(102), HEIGHTENED_SENSES(103), DURABLE_PROJECTILES(104),
	//Huntress T3
	POINT_BLANK(105, 3), SEER_SHOT(HeroClass.HUNTRESS, 106, 3),
	//Sniper T3
	FARSIGHT(107, 3), SHARED_ENCHANTMENT(108, 3), SHARED_UPGRADES(109, 3),
	//Warden T3
	DURABLE_TIPS(110, 3), BARKSKIN(111, 3), SHIELDING_DEW(112, 3),
	//Spectral Blades T4
	FAN_OF_BLADES(113, 4), PROJECTING_BLADES(114, 4), SPIRIT_BLADES(115, 4),
	//Natures Power T4
	GROWING_POWER(116, 4), NATURES_WRATH(117, 4), WILD_MOMENTUM(118, 4),
	//Spirit Hawk T4
	EAGLE_EYE(119, 4), GO_FOR_THE_EYES(120, 4), SWIFT_SPIRIT(121, 4),

	//universal T4
	HEROIC_ENERGY(26, 4), //See icon() and title() for special logic for this one
    //Normal
    HIGH_EDUCATION(26, 0xFFFF),
	//Ratmogrify T4
	RATSISTANCE(124, 4), RATLOMACY(125, 4), RATFORCEMENTS(126, 4),
    //type561 T1
    Type56One_FOOD(128, 2 ,655), Type56One_Identify(129), Type56One_Damage(130), Type56_14(HeroClass.TYPE561, 131),
    //type561 T2
    Type56Two_FOOD(132),Type56Two_Armor(133),Type56Two_Grass(134),Type56Two_Sight(135),Type56Two_Damage(136),

    //争议内容
    Type56_14V2(131),
    Type56_21V2(132),Type56_22V2(133),Type56_23V2(134),Type56_23V3(134),Type56_23V4(HeroClass.TYPE561, 134),
    GUN_1V2(142, 3), GUN_1V3(142, 3), GUN_2V2(143, 3),

    //type561 T3
    Type56Three_Bomb(HeroClass.TYPE561, 137, 3), Type56Three_Book(HeroClass.TYPE561, 138, 3),
	//type561 T3-1 EMP
	EMP_One(139, 3), EMP_Two(140, 3), EMP_Three(141, 3),
	//type561 T3-2 GUN
	GUN_1(142, 3), GUN_2(143, 3), GUN_3(144, 3),
    //type561 T4-1
    Type56FourOneOne(137, 4), Type56FourOneTwo(137, 4),Type56FourOneThree(137, 4),
    //type561 T4-2
    Type56FourTwoOne(137, 4), Type56FourTwoTwo(137, 4),Type56FourTwoThree(137, 4),
    //type561 T4-3
    Type56_431(137, 4), Type56_432(137, 4),Type56_433(137, 4),

    //留作旧版本561天赋的接口
    NICE_FOOD(128), OLD_SOLDIER(129), BETTER_FOOD(131),
    BARGAIN_SKILLS(132),TRAP_EXPERT(133),HOW_DARE_YOU(134),JIEFANGCI(135),NIGHT_EXPERT(136),
    SEARCH_ARMY(137, 3),

    //GSH18 T1
	GSH18_MEAL_TREATMENT(160), GSH18_DOCTOR_INTUITION(161), GSH18_CLOSE_COMBAT(162), GSH18_STAR_SHIELD(163),
	//GSH18 T2
	GSH18_ENERGIZING_MEAL(164), GSH18_CHAIN_SHOCK(165), GSH18_LOGISTICS_SUPPORT(166), GSH18_COMIC_HEART(167), GSH18_MEDICAL_COMPATIBILITY(168),
	//GSH18 T3
	GSH18_INTELLIGENCE_AWARENESS(169, 3), GSH18_AGILE_MOVEMENT(170, 3),
	//GSH18未来之星专属天赋 - 天狼星心脏
	GSH18_SIRIUS_HEART(171, 3),
	//GSH18 未来之星 T3
	GSH18_COMPANION_SYNC(172, 3),

    //初始通用
    //t1
    FAST_RELOAD(HeroClass.TYPE561, 130),

    //t2
    //旧磁盘回流(1/2/3次+3)、绝境迫能(最后一充能+1/+2/+3)
    EMPOWERING_SCROLLS_V2(41, 3),DESPERATE_POWER(41, 3),
    //行窃预知变种：加数量(+0.6/+1/+1.5)、不隐藏(+0.6/25%/50%)
    ROGUES_FORESIGHT_V2(72), ROGUES_FORESIGHT_V3(72),

    //t3
    //瞄准镜强化变种-超频校准
    ENHANCED_RINGS_V2(73, 3),
    ELITE_ARMY(138, 3),

    NONE(0, 4);


    public static class ProtectiveShadowsTracker extends Buff {
        float barrierInc = 0.5f;

        @Override
        public boolean act() {
            //barrier every 2/1 turns, to a max of 3/5
            if (((Hero)target).hasTalent(Talent.PROTECTIVE_SHADOWS) && target.invisible > 0){
                Barrier barrier = Buff.affect(target, Barrier.class);
                if (barrier.shielding() < 1 + 2*((Hero)target).pointsInTalent(Talent.PROTECTIVE_SHADOWS)) {
                    barrierInc += 0.5f * ((Hero) target).pointsInTalent(Talent.PROTECTIVE_SHADOWS);
                }
                if (barrierInc >= 1){
                    barrierInc = 0;
                    barrier.incShield(1);
                } else {
                    barrier.incShield(0); //resets barrier decay
                }
            } else {
                detach();
            }
            spend( TICK );
            return true;
        }

        private static final String BARRIER_INC = "barrier_inc";
        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put( BARRIER_INC, barrierInc);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            barrierInc = bundle.getFloat( BARRIER_INC );
        }
    }
	public static class ImprovisedProjectileCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	}
	public static class LethalMomentumTracker extends FlavourBuff{}
	public static class StrikingWaveTracker extends FlavourBuff{}
	public static class WandPreservationCounter extends CounterBuff{{revivePersists = true;}}
	public static class EmpoweredStrikeTracker extends FlavourBuff{}
	public static class BountyHunterTracker extends FlavourBuff{}
	public static class RejuvenatingStepsCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.35f, 0.15f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / (15 - 5*Dungeon.hero.pointsInTalent(REJUVENATING_STEPS))); }
	}
	public static class RejuvenatingStepsFurrow extends CounterBuff{{revivePersists = true;}}
	public static class SeerShotCooldown extends FlavourBuff{
		public int icon() { return target.buff(RevealedArea.class) != null ? BuffIndicator.NONE : BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.7f, 0.4f, 0.7f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	}
	public static class GSH18MealTreatmentTracker extends FlavourBuff{
		public int icon() { return BuffIndicator.HEALING; }
		public void tintIcon(Image icon) { icon.hardlight(0.8f, 0.6f, 0.2f); }
	}
	public static class GSH18EnergizingMealTracker extends Buff{}
    
    //天狼星心脏 buff
    public static class SiriusHeartTracker extends Buff {
        {
            // 设置为不会随时间自然消失，只在攻击后被移除
            revivePersists = true;
            //这个是爆了保修后是否保留buff
        }
        //激活时快照的附加伤害值（由护盾量×倍率计算，攻击时直接取用，不再实时读护盾）
        public int bonusDamage = 0;

        private static final String BONUS_DAMAGE = "bonus_damage";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(BONUS_DAMAGE, bonusDamage);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            bonusDamage = bundle.getInt(BONUS_DAMAGE);
        }

        public int icon() { return BuffIndicator.MOMENTUM; }
        public void tintIcon(Image icon) { icon.hardlight(0.8f, 0.2f, 0.8f); }
        public String toString() { return Messages.get(this, "name"); }
        public String desc() { return Messages.get(this, "desc"); }
    }
    public static class Type56BookTracker extends Buff{
        {
            revivePersists = true;
        }
    }
	public static class StarShieldTracker extends CounterBuff{// GSH18护盾恢复追踪器
		{
			revivePersists = true;
		}
		
		// 回合结束时重置计数器，实现每回合限制
		@Override
		public boolean act() {
			// 每回合开始时重置计数器
			countDown(count());
			spend(TICK); // 等待下一回合
			return true;
		}
	}
	public static class IntelligenceAwarenessCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.0f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	}
	public static class AgileMovement extends FlavourBuff{}
	public static class AgileMovementCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.7f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	}
	public static class SpiritBladesTracker extends FlavourBuff{}
	public static class SuckerPunchTracker extends Buff{}
	public static class FollowupStrikeTracker extends Buff{
        @Override
        public int icon() {
            return iconNeedDraw();
        }
        @Override
        public void tintIcon(Image icon) {
            tintIconNeedDraw(icon);
        }
    }

	final int icon;
	final int maxPoints;
    final int version;
    final HeroClass heroClass;

	// tiers 1/2/3/4 start at levels 2/7/13/21
	public static final int[] tierLevelThresholds = new int[]{0, 2, 7, 13, 21, 31};

	Talent( int icon ){
		this(icon, 2);
	}

    Talent( int icon, int maxPoints ){
        this(icon, maxPoints, 0);
    }
    Talent( int icon, int maxPoints, int lastVersion){
        this(HeroClass.NONE, icon, maxPoints, lastVersion);
    }
    Talent(HeroClass heroClass, int... list){
        this.heroClass = heroClass;
        if (list.length == 1) {
            icon = list[0];
            maxPoints = 2;
            version = 0;
        }
        else if (list.length == 2) {
            icon = list[0];
            maxPoints = list[1];
            version = 0;
        }
        else {
            icon = list[0];
            maxPoints = list[1];
            version = list[2];
        }
    }
	public int icon(){
		if (this == HEROIC_ENERGY){
			if (Ratmogrify.useRatroicEnergy){
				return 127;
			}
			HeroClass cls = Dungeon.hero != null ? Dungeon.hero.heroClass : GamesInProgress.selectedClass;
			switch (cls){
				case WARRIOR: default:
					return 26;
				case MAGE:
					return 58;
				case ROGUE:
					return 90;
				case HUNTRESS:
					return 122;
			}
		} else {
			return icon;
		}
	}

	public int maxPoints(){
		return maxPoints;
	}

	public String title(){
		if (this == HEROIC_ENERGY && Ratmogrify.useRatroicEnergy){
			return Messages.get(this, name() + ".rat_title");
		}
		return Messages.get(this, name() + ".title");
	}

	public String desc(HeroClass heroClass){
        if (heroClass == HeroClass.PUBLIC_1)
            return Messages.get(this, name() + ".meta_desc");
        if (this.heroClass == HeroClass.NONE)
            return Messages.get(this, name() + ".desc");
        if (this.heroClass == heroClass)
            return Messages.get(this, name() + ".desc");
        if (heroClass == HeroClass.NONE)
            return Messages.get(this, name() + ".desc");
        return Messages.get(this, name() + ".meta_desc");
	}

	public static void onTalentUpgraded( Hero hero, Talent talent){
        Item workingItem;
        if (talent == NATURES_BOUNTY){
            if ( hero.pointsInTalent(NATURES_BOUNTY) == 1) Buff.count(hero, NatureBerriesAvailable.class, 4);
            else                                           Buff.count(hero, NatureBerriesAvailable.class, 2);
        }
        else if (talent == CACHED_RATIONS){
            if ( hero.pointsInTalent(CACHED_RATIONS) == 1) Buff.count(hero, CachedRationsDropped.class, 4);
            else                                           Buff.count(hero, CachedRationsDropped.class, 2);
        }
        else if (talent == Type56One_FOOD){
            if ( hero.pointsInTalent(Type56One_FOOD) == 1) Buff.count(hero, ZongziDropped.class, 4);
            else                                           Buff.count(hero, ZongziDropped.class, 2);
        }
		else if (talent == ARMSMASTERS_INTUITION && hero.pointsInTalent(ARMSMASTERS_INTUITION) == 2){
			if (hero.belongings.weapon() != null) hero.belongings.weapon().identify();
            if (hero.belongings.armor() != null)  hero.belongings.armor.identify();
            if (hero.belongings.SecondArmor() != null)  hero.belongings.secArmor.identify();
		}
		else if (talent == THIEFS_INTUITION && hero.pointsInTalent(THIEFS_INTUITION) == 2){
			if (hero.belongings.ring != null) hero.belongings.ring.identify();
			if (hero.belongings.misc instanceof Ring) hero.belongings.misc.identify();
			for (Item item : Dungeon.hero.belongings)
				if (item instanceof Ring)
					((Ring) item).setKnown();
		}
        else if (talent == Type56One_Identify && hero.pointsInTalent(Type56One_Identify) == 2){
            for (Item item : Dungeon.hero.belongings)
                if (item instanceof MeleeWeapon || item instanceof Armor){
                    item.cursedKnown = true;
                    Item.updateQuickslot();
                }
        }
		else if (talent == THIEFS_INTUITION && hero.pointsInTalent(THIEFS_INTUITION) == 1){
			if (hero.belongings.ring != null) hero.belongings.ring.setKnown();
			if (hero.belongings.misc instanceof Ring) ((Ring) hero.belongings.misc).setKnown();
		}
        else if (talent == STRONGMAN){
            int times ;
            if (hero.STR >= Integer.MAX_VALUE / 1.06F){
                times = 1;
                hero.STR = Integer.MAX_VALUE;
            }
            else if (hero.STR >= 100_000){
                times = 1000;
                hero.STR *= Random.Float(1.045F, 1.054F);
            }else {
                times = hero.STR;
            }
            for (int i = 0; i < times; i++) {
                if (hero.STR == Integer.MAX_VALUE || hero.STR == Integer.MIN_VALUE){
                    if (Random.Float() < 1/3F)
                        hero.STR = Integer.MIN_VALUE;
                    break;
                }
                if (Random.Float() < 0.05F) {
                    hero.STR++;
                    times++;
                }
            }
            Badges.validateStrengthAttained();
        }
		else if (talent == LIGHT_CLOAK && hero.pointsInTalent(LIGHT_CLOAK) == 1){
            if ((workingItem = hero.belongings.getItem(CloakOfShadows.class)) != null)
                ((CloakOfShadows) workingItem).activate(Dungeon.hero);
		}
        else if (talent == Type56Three_Book && hero.pointsInTalent(Type56Three_Book) == 1){
            if ((workingItem = hero.belongings.getItem(RedBook.class)) != null)
                ((RedBook) workingItem).activate(Dungeon.hero);
        }
		else if (talent == HEIGHTENED_SENSES || talent == FARSIGHT)
			Dungeon.observe();
        else if (talent == Type56Two_Sight){
            Buff.affect( Dungeon.hero, TalentSecondSight.class).Set(Dungeon.levelId, 0);
            if (Dungeon.hero.pointsInTalent(Talent.Type56Two_Sight) == 1){
                Dungeon.level.FirstSight = false;
                Buff.affect(Dungeon.hero, MindVision.class, 3);
            }else if (Dungeon.hero.pointsInTalent(Talent.Type56Two_Sight) == 2) {
                Dungeon.level.SecondSight = false;
                Buff.affect(Dungeon.hero, MindVision.class, 3);
            }
        }
        else if (talent == PROTECTIVE_SHADOWS && hero.invisible > 0)
            Buff.affect(hero, Talent.ProtectiveShadowsTracker.class);
	}

	public static class CachedRationsDropped extends CounterBuff{{revivePersists = true;}}
    public static class ZongziDropped extends CounterBuff{{revivePersists = true;}}
	public static class NatureBerriesAvailable extends CounterBuff{{revivePersists = true;}}

	public static void onFoodEaten(Hero hero, float foodVal, Item foodSource) {
        if (hero.hasTalent(HEARTY_MEAL)) {
            //3/5 HP healed, when hero is below 25% health
            if (hero.HP <= hero.HT / 4) {
                hero.HP = Math.min(hero.HP + 1 + 2 * hero.pointsInTalent(HEARTY_MEAL), hero.HT);
                hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1 + hero.pointsInTalent(HEARTY_MEAL));
                //2/3 HP healed, when hero is below 50% health
            } else if (hero.HP <= hero.HT / 2) {
                hero.HP = Math.min(hero.HP + 1 + hero.pointsInTalent(HEARTY_MEAL), hero.HT);
                hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), hero.pointsInTalent(HEARTY_MEAL));
            }
        }
        if (hero.hasTalent(IRON_STOMACH)) {
            if (hero.cooldown() > 0) {
                Buff.affect(hero, WarriorFoodImmunity.class, hero.cooldown());
            }
        }
        if (hero.hasTalent(EMPOWERING_MEAL)) {
            //2/3 bonus wand damage for next 3 zaps
            Buff.affect(hero, WandEmpower.class).set(1 + hero.pointsInTalent(EMPOWERING_MEAL), 3);
            ScrollOfRecharging.chargeParticle(hero);
        }
        if (hero.hasTalent(ENERGIZING_MEAL)) {
            //5/8 turns of recharging
            Buff.prolong(hero, Recharging.class, 2 + 3 * (hero.pointsInTalent(ENERGIZING_MEAL)));
            ScrollOfRecharging.chargeParticle(hero);
        }
        if (hero.hasTalent(MYSTICAL_MEAL)) {
            //3/5 turns of recharging
            ArtifactRecharge buff = Buff.affect(hero, ArtifactRecharge.class);
            if (buff.left() < 1 + 2 * (hero.pointsInTalent(MYSTICAL_MEAL))) {
                Buff.affect(hero, ArtifactRecharge.class).set(1 + 2 * (hero.pointsInTalent(MYSTICAL_MEAL))).ignoreHornOfPlenty = foodSource instanceof HornOfPlenty;
            }
            ScrollOfRecharging.chargeParticle(hero);
        }
        if (hero.hasTalent(INVIGORATING_MEAL)) {
            //effectively 1/2 turns of haste
            Buff.prolong(hero, Haste.class, 0.67f + hero.pointsInTalent(INVIGORATING_MEAL));
        }

        // GSH18角色进食相关天赋（疗养一餐/元气一餐，实现见 GSH18Talent）
        GSH18Talent.onFoodEaten(hero);

        if (Dungeon.hero.hasTalent(Talent.Type56_21V2)){
            if (Dungeon.hero.HP < Dungeon.hero.HT) {
                int add = (int) (Dungeon.hero.HT*(0.02*Dungeon.hero.pointsInTalent(Talent.Type56_21V2)+0.01F));
                Dungeon.hero.HP = Math.min( Dungeon.hero.HP + add, Dungeon.hero.HT );
                Dungeon.hero.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 1 );
            }
        }
    }

	public static class WarriorFoodImmunity extends FlavourBuff{
		{ actPriority = HERO_PRIO+1; }
	}

	public static float itemIDSpeedFactor( Hero hero, Item item ){
		// 1.75x/2.5x speed with huntress talent
		float factor = 1f + hero.pointsInTalent(SURVIVALISTS_INTUITION) * 0.75f;
		// 2x/instant for Warrior (see onItemEquipped)
		if (item instanceof MeleeWeapon || item instanceof Armor){
            factor *= 1.25f + hero.pointsInTalent(Type56One_Identify) * 0.75f;
			factor *= 1f + hero.pointsInTalent(ARMSMASTERS_INTUITION);
		}
		// 3x/instant for mage (see Wand.wandUsed())
		if (item instanceof Wand){
			factor *= 1f + 2 * hero.pointsInTalent(SCHOLARS_INTUITION);
		}
		// 2x/instant for rogue (see onItemEqupped), also id's type on equip/on pickup
		if (item instanceof Ring){
			factor *= 1f + hero.pointsInTalent(THIEFS_INTUITION);
		}
        if (item instanceof MeleeWeapon){
            factor *= 1F + 2 * hero.pointsInTalent(OLD_SOLDIER);
        }
		return factor;
	}

    public static void onPotionUsed( Hero hero, float mul ){
        onPotionUsed(hero, mul, hero.pos);
    }
	public static void onPotionUsed( Hero hero, float mul, int pos ){
		if (hero.hasTalent(RESTORED_WILLPOWER)){
			ShieldBuff shield = hero.buff(BrokenSeal.WarriorShield.class);
            int shieldToGive = Math.round( hero.HT/100F*hero.pointsInTalent(RESTORED_WILLPOWER)*3.5F*mul );
			if (shield != null){
                ((BrokenSeal.WarriorShield) shield).supercharge(shieldToGive);
			}
            else {
                shield = Buff.affect(hero, Barrier.class);
                shield.setShield(shieldToGive);
            }
		}
		// GSH18角色药水相关天赋（医护兼容，实现见 GSH18Talent）
		GSH18Talent.onPotionUsed(hero, mul);
		if (hero.hasTalent(RESTORED_NATURE)){
			ArrayList<Integer> grassCells = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				grassCells.add(pos+i);
			}
			Random.shuffle(grassCells);
			for (int cell : grassCells){
				Char ch = Actor.findChar(cell);
				if (ch != null && ch.alignment == Char.Alignment.ENEMY){
					Buff.affect(ch, Roots.class, (1f + hero.pointsInTalent(RESTORED_NATURE))*mul );
				}
				if (Dungeon.level.map[cell] == Terrain.EMPTY ||
						Dungeon.level.map[cell] == Terrain.EMBERS ||
						Dungeon.level.map[cell] == Terrain.EMPTY_DECO){
					Level.set(cell, Terrain.GRASS);
					GameScene.updateMap(cell);
				}
				CellEmitter.get(cell).burst(LeafParticle.LEVEL_SPECIFIC, 4);
			}
			if (hero.pointsInTalent(RESTORED_NATURE) == 1 && mul != 2){
				grassCells.remove(0);
				grassCells.remove(0);
                if (mul != 1.25F)
				    grassCells.remove(0);
			}
			for (int cell : grassCells){
				int t = Dungeon.level.map[cell];
				if ((t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.EMBERS
						|| t == Terrain.GRASS || t == Terrain.FURROWED_GRASS)
						&& Dungeon.level.plants.get(cell) == null){
					Level.set(cell, Terrain.HIGH_GRASS);
					GameScene.updateMap(cell);
				}
			}
			Dungeon.observe();
		}
	}

	public static void onScrollUsed( Hero hero, float mul ){
		if (hero.hasTalent(ENERGIZING_UPGRADE)){
            Buff.prolong(hero, Recharging.class, 4*mul);
		}
		if (hero.hasTalent(MYSTICAL_UPGRADE)){
            Buff.prolong(hero, Invisibility.class, 4*mul);
		}
        float wandMul = Math.min(1, mul) ;
        if (hero.hasTalent(Talent.EMPOWERING_SCROLLS)){
            Buff.affect(hero, ScrollEmpower.class).reset( 1, (int) (hero.pointsInTalent(Talent.EMPOWERING_SCROLLS)*2*wandMul));
            Item.updateQuickslot();
        }else if (hero.hasTalent(Talent.EMPOWERING_SCROLLS_V2)){
            if (wandMul < 1 && hero.pointsInTalent(EMPOWERING_SCROLLS_V2) < 2)
                return;
            Buff.affect(hero, ScrollEmpower.class).reset((int) Math.ceil(hero.pointsInTalent(Talent.EMPOWERING_SCROLLS_V2) * wandMul), 3);
            Item.updateQuickslot();
        }
	}

	public static void onArtifactUsed( Hero hero ){
		if (hero.hasTalent(ENHANCED_RINGS)){
			Buff.prolong(hero, EnhancedRings.class, 3f * hero.pointsInTalent(ENHANCED_RINGS)).set(1);
            hero.updateHT(false);
		}
        else if (hero.hasTalent(ENHANCED_RINGS_V2) && hero.buff(EnhancedRings.CoolDown.class) == null){
            Buff.prolong(hero, EnhancedRings.class, 3).set(hero.pointsInTalent(ENHANCED_RINGS_V2));
            hero.updateHT(false);
        }
	}

	public static void onItemEquipped( Hero hero, Item item ){
		if(hero.pointsInTalent(ARMSMASTERS_INTUITION) == 2 && (item instanceof Weapon || item instanceof Armor))
			item.identify();

        if (hero.pointsInTalent(OLD_SOLDIER) == 2 && item instanceof Weapon)
            item.identify();

		if(hero.hasTalent(THIEFS_INTUITION) && item instanceof Ring){
			if(hero.pointsInTalent(THIEFS_INTUITION) == 2){
				item.identify();
			}else{
				((Ring) item).setKnown();
			}
		}
	}

	public static void onItemCollected( Hero hero, Item item ){
		if(hero.pointsInTalent(THIEFS_INTUITION) == 2 && item instanceof Ring)
            ((Ring) item).setKnown();

        if(hero.pointsInTalent(Type56One_Identify) == 2 && (item instanceof MeleeWeapon || item instanceof Armor)) {
            item.cursedKnown = true;
            Item.updateQuickslot();
        }

        if (hero.pointsInTalent(OLD_SOLDIER) == 2 && item instanceof MeleeWeapon) {
            item.cursedKnown = true;
            Item.updateQuickslot();
        }
	}

	//note that IDing can happen in alchemy scene, so be careful with VFX here
	public static void onItemIdentified( Hero hero, Item item ){
		if (hero.hasTalent(TEST_SUBJECT)){
			//heal for 2/3 HP
			hero.HP = Math.min(hero.HP + 1 + hero.pointsInTalent(TEST_SUBJECT), hero.HT);
			if (hero.sprite != null) {
				Emitter e = hero.sprite.emitter();
				if (e != null) e.burst(Speck.factory(Speck.HEALING), hero.pointsInTalent(TEST_SUBJECT));
			}
		}
		if (hero.hasTalent(TESTED_HYPOTHESIS)){
			//2/3 turns of wand recharging
			Buff.affect(hero, Recharging.class, 1f + hero.pointsInTalent(TESTED_HYPOTHESIS));
			ScrollOfRecharging.chargeParticle(hero);
		}
	}
    public static int onDefenceProc(Hero hero, Char enemy, int dmg){
        if(hero.pointsInTalent(Talent.NIGHT_EXPERT)>=2)
            Buff.append(enemy, TalismanOfForesight.CharAwareness.class,2f).charID = enemy.id();
        return dmg;
    }

	public static int onAttackProc( Hero hero, Char enemy, int dmg ){
		if(hero.hasTalent(SUCKER_PUNCH)
				&& enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)
				&& enemy.buff(SuckerPunchTracker.class) == null){
			dmg += Random.IntRange(hero.pointsInTalent(SUCKER_PUNCH) , 2);
			Buff.affect(enemy, SuckerPunchTracker.class);
		}

        if(hero.hasTalent(Type56One_Damage)){
            ShootGun Gun = hero.belongings.getItem(ShootGun.class);
            boolean add;
            if (Gun == null && !(hero.belongings.weapon instanceof ShootGun))
                //背包不存在榴弹且也没有装备榴弹的情况下
                add = true;
            else
                add = ShootGun.cooldown;

            if (add)
                dmg += Random.IntRange(hero.pointsInTalent(Type56One_Damage)-1 , 2);
        }

		if(hero.hasTalent(FOLLOWUP_STRIKE)) {
			if (hero.belongings.weapon() instanceof MissileWeapon) {
				Buff.affect(enemy, FollowupStrikeTracker.class);
			} else if (enemy.buff(FollowupStrikeTracker.class) != null){
				dmg += 1 + hero.pointsInTalent(FOLLOWUP_STRIKE);
				if (!(enemy instanceof Mob) || !((Mob) enemy).surprisedBy(hero)){
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
				}
				enemy.buff(FollowupStrikeTracker.class).detach();
			}
		}

		// GSH18角色攻击命中相关天赋（锁链冲击溅射/双星守护回盾，实现见 GSH18Talent；
		// 天狼星心脏的附加伤害在 SiriusHeart.onAttack 中同样调用 GSH18Talent.chainShock）
		GSH18Talent.onAttackProc(hero, enemy, dmg);

        if(hero.hasTalent(HOW_DARE_YOU)){
            float chance  =0.15f;
            float duration=5.01f;

            if(hero.pointsInTalent(HOW_DARE_YOU)>=2){
                float enemyHealthRate = (float) enemy.HP / (float) enemy.HT;
                chance = 0.1F * enemyHealthRate;
                duration += 10F * enemyHealthRate;
            }

            if(Random.Float() < chance)
                Buff.affect(enemy, Terror.class,duration);
        }

        if(hero.hasTalent(JIEFANGCI) && Dungeon.level.adjacent(hero.pos,enemy.pos)){
            if (hero.belongings.weapon() instanceof ShootGun
                    || hero.buff(JIEFANGCI_Tracker.class) == null) {
                Buff.prolong(hero, JIEFANGCI_Tracker.class, 5F);
                dmg += 1 + 4 * hero.pointsInTalent(JIEFANGCI);
                float chance = 0.05F + 0.1F * hero.pointsInTalent(JIEFANGCI);
                if (Random.Float() < chance)
                    Buff.affect(enemy, Cripple.class, 2F);
            }
        }

        if(hero.hasTalent(SEARCH_ARMY) && enemy instanceof Mob){
            if(enemy.paralysed>0)
                dmg = (int) (dmg * (1F + (0.4F / 3F) * hero.pointsInTalent(SEARCH_ARMY)));
            else if(((Mob) enemy).surprisedBy(hero))
                dmg = (int) (dmg * (1F + 0.1F * hero.pointsInTalent(SEARCH_ARMY)));
        }

        return dmg;
	}

    public static class JIEFANGCI_Tracker extends FlavourBuff{}

	public static void onShielding(Hero hero){
		// GSH18角色获得护盾相关天赋（情报感知/敏捷移动，实现见 GSH18Talent）
		GSH18Talent.onShielding(hero);
	}

	public static final int MAX_TALENT_TIERS = 4;

	public static void initClassTalents( Hero hero ){
        //重建角色的天赋表，做蜕变处理（如果有），对角色的天赋表填写，不做额外返回值+
		initClassTalents( hero.heroClass, hero.talents, hero.metamorphedTalents, hero.addTalents );
	}

	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents){
        //创建角色职业拥有的原始天赋表，创建完成后，输入参数的talents将包含原始天赋表，目的是原始表，所以不输入蜕变关系、额外天赋
		initClassTalents( cls, talents, new LinkedHashMap<>(), new LinkedHashMap<>());
	}

	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents, LinkedHashMap<Talent, Talent> replacements, LinkedHashMap<Talent, Integer> addTalents ){
        while (talents.size() < MAX_TALENT_TIERS){
            talents.add(new LinkedHashMap<>());
            //令输入的天赋表扩大到至少4个Map以装下四个层的天赋
        }

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 1
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, HEARTY_MEAL, ARMSMASTERS_INTUITION, TEST_SUBJECT, IRON_WILL);
				break;
			case MAGE:
				Collections.addAll(tierTalents, EMPOWERING_MEAL, SCHOLARS_INTUITION, TESTED_HYPOTHESIS, BACKUP_BARRIER);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, CACHED_RATIONS, THIEFS_INTUITION, SUCKER_PUNCH, PROTECTIVE_SHADOWS);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, NATURES_BOUNTY, SURVIVALISTS_INTUITION, FOLLOWUP_STRIKE, NATURES_AID);
				break;
			case TYPE561:
				Collections.addAll(tierTalents, Type56One_FOOD , Type56One_Identify, Type56One_Damage, Type56_14);
				break;
			case GSH18:
				Collections.addAll(tierTalents, GSH18_MEAL_TREATMENT, GSH18_DOCTOR_INTUITION, GSH18_CLOSE_COMBAT, GSH18_STAR_SHIELD);
				break;
			case HK416:
				// 占位天赋：暂用战士的基础天赋
				Collections.addAll(tierTalents, HEARTY_MEAL, ARMSMASTERS_INTUITION, TEST_SUBJECT, IRON_WILL);
				break;
            case Dandelion: break;
            case PUBLIC_1:
                Collections.addAll(tierTalents, NICE_FOOD, OLD_SOLDIER, BETTER_FOOD, FAST_RELOAD);
                break;
        }
        //用一个临时ArrayList记录下t1的原始天赋，Collections.addAll的用法，首个参数是表，后面的是要加入的元素
		for (Talent talent : tierTalents){
            //将上面ArrayList记录的天赋逐个加入到天赋表的Map中
			if (replacements.containsKey(talent)){
                //在加入前先检索该天赋是否有被蜕变，如果有则加入蜕变后的天赋
				talent = replacements.get(talent);
			}
			talents.get(0).put(talent, 0);
            //对天赋表talents的第一个Map【get(0)】即t1天赋表，加入上面得到的天赋【 put(talent 】，天赋加点为0【 ,0) 】
		}
		tierTalents.clear();
        //清空临时ArrayList，以完成对第二个Map即t2天赋表的重建

		//tier 2
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, IRON_STOMACH, RESTORED_WILLPOWER, RUNIC_TRANSFERENCE, LETHAL_MOMENTUM, IMPROVISED_PROJECTILES);
				break;
			case MAGE:
				Collections.addAll(tierTalents, ENERGIZING_MEAL, ENERGIZING_UPGRADE, WAND_PRESERVATION, ARCANE_VISION, SHIELD_BATTERY);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, MYSTICAL_MEAL, MYSTICAL_UPGRADE, WIDE_SEARCH, SILENT_STEPS, ROGUES_FORESIGHT);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, INVIGORATING_MEAL, RESTORED_NATURE, REJUVENATING_STEPS, HEIGHTENED_SENSES, DURABLE_PROJECTILES);
				break;
			case TYPE561:
				Collections.addAll(tierTalents, Type56Two_FOOD, Type56Two_Armor, Type56_23V4, Type56Two_Sight, Type56Two_Damage);
				break;
            case GSH18:
                Collections.addAll(tierTalents, GSH18_ENERGIZING_MEAL, GSH18_CHAIN_SHOCK, GSH18_LOGISTICS_SUPPORT, GSH18_COMIC_HEART, GSH18_MEDICAL_COMPATIBILITY);
                break;
            case HK416:
                // 占位天赋：暂用战士的T2天赋
                Collections.addAll(tierTalents, IRON_STOMACH, RESTORED_WILLPOWER, RUNIC_TRANSFERENCE, LETHAL_MOMENTUM, IMPROVISED_PROJECTILES);
                break;
            case Dandelion: break;
            case PUBLIC_1:
                Collections.addAll(tierTalents, ROGUES_FORESIGHT_V2, ROGUES_FORESIGHT_V3, BARGAIN_SKILLS, TRAP_EXPERT, HOW_DARE_YOU, JIEFANGCI, NIGHT_EXPERT);
                break;
		}
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(1).put(talent, 0);
		}
		tierTalents.clear();

		//tier 3
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, HOLD_FAST, STRONGMAN);
				break;
			case MAGE:
				Collections.addAll(tierTalents, EMPOWERING_SCROLLS, ALLY_WARP);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, ENHANCED_RINGS, LIGHT_CLOAK);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, POINT_BLANK, SEER_SHOT);
				break;
			case TYPE561:
				Collections.addAll(tierTalents, Type56Three_Bomb, Type56Three_Book);
				break;
            case GSH18:
                Collections.addAll(tierTalents,GSH18_INTELLIGENCE_AWARENESS,GSH18_AGILE_MOVEMENT);
                break;
            case HK416:
                // 占位天赋：暂用战士的T3天赋
                Collections.addAll(tierTalents, HOLD_FAST, STRONGMAN);
                break;
            case Dandelion: break;
            case PUBLIC_1:
                Collections.addAll(tierTalents, EMPOWERING_SCROLLS_V2, DESPERATE_POWER, ELITE_ARMY, ENHANCED_RINGS_V2, SEARCH_ARMY);
                break;
		}
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(2).put(talent, 0);
		}
		tierTalents.clear();

        //将新增的天赋加入到对应的天赋层中
        for (Talent talent : addTalents.keySet()){
            talents.get(addTalents.get(talent)).put(talent, 0);
        }
		//tier4
		//TBD
	}

	public static void initSubclassTalents( Hero hero ){
		initSubclassTalents( hero.subClass, hero.talents );
	}

	public static void initSubclassTalents( HeroSubClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (cls == HeroSubClass.NONE) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 3
		switch (cls){
			case BERSERKER: default:
				Collections.addAll(tierTalents, ENDLESS_RAGE, BERSERKING_STAMINA, ENRAGED_CATALYST);
				break;
			case GLADIATOR:
				Collections.addAll(tierTalents, CLEAVE, LETHAL_DEFENSE, ENHANCED_COMBO);
				break;
			case BATTLEMAGE:
				Collections.addAll(tierTalents, EMPOWERED_STRIKE, MYSTICAL_CHARGE, EXCESS_CHARGE);
				break;
			case WARLOCK:
				Collections.addAll(tierTalents, SOUL_EATER, SOUL_SIPHON, NECROMANCERS_MINIONS);
				break;
			case ASSASSIN:
				Collections.addAll(tierTalents, ENHANCED_LETHALITY, ASSASSINS_REACH, BOUNTY_HUNTER);
				break;
			case FREERUNNER:
				Collections.addAll(tierTalents, EVASIVE_ARMOR, PROJECTILE_MOMENTUM, SPEEDY_STEALTH);
				break;
			case SNIPER:
				Collections.addAll(tierTalents, FARSIGHT, SHARED_ENCHANTMENT, SHARED_UPGRADES);
				break;
			case WARDEN:
			Collections.addAll(tierTalents, DURABLE_TIPS, BARKSKIN, SHIELDING_DEW);
			break;
		case SUPER_AI: break; // 超级小爱：天赋暂时为空
			case EMP_BOMB:
				Collections.addAll(tierTalents, EMP_One, EMP_Two, EMP_Three);
				break;
			case GUN_MASTER:
				Collections.addAll(tierTalents, GUN_1V2, GUN_2V2, GUN_3);
				break;
			case FUTURE_STAR:
				Collections.addAll(tierTalents, GSH18_INTELLIGENCE_AWARENESS, GSH18_SIRIUS_HEART, GSH18_COMPANION_SYNC);
				break;
			case MOBILE_MEDICALTABLE:
				Collections.addAll(tierTalents, GSH18_AGILE_MOVEMENT);
				break;
            case EMPTY: break;
		}
		for (Talent talent : tierTalents){
			talents.get(2).put(talent, 0);
		}
		tierTalents.clear();

	}

	public static void initArmorTalents( Hero hero ){
		initArmorTalents( hero.armorAbility, hero.talents);
	}

	public static void initArmorTalents(ArmorAbility abil, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (abil == null) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		for (Talent t : abil.talents()){
			talents.get(3).put(t, 0);
		}
	}

	private static final String TALENT_TIER = "talents_tier_";

	public static void storeTalentsInBundle( Bundle bundle, Hero hero ){
		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = new Bundle();

			for (Talent talent : tier.keySet()){
				if (tier.get(talent) > 0){
					tierBundle.put(talent.name(), tier.get(talent));
				}
				if (tierBundle.contains(talent.name())){
					tier.put(talent, Math.min(tierBundle.getInt(talent.name()), talent.maxPoints()));
				}
			}
			bundle.put(TALENT_TIER+(i+1), tierBundle);
		}

		Bundle replacementsBundle = new Bundle();
		for (Talent t : hero.metamorphedTalents.keySet()){
			replacementsBundle.put(t.name(), hero.metamorphedTalents.get(t));
		}
		bundle.put("replacements", replacementsBundle);
        Bundle addTalentBundle = new Bundle();
        for (Talent t : hero.addTalents.keySet()){
            addTalentBundle.put(t.name(), hero.addTalents.get(t));
        }
        bundle.put("addTalents", addTalentBundle);
	}

    private static final HashSet<String> removed = new HashSet<>();
    static{
        removed.add("enhance_grenade".toUpperCase());
        removed.add("more_accurate".toUpperCase());
        removed.add("simple_reload".toUpperCase());
        removed.add("more_power".toUpperCase());
        removed.add("endure_emp".toUpperCase());
        removed.add("newlife".toUpperCase());
    }

    private static final HashMap<String, String> renamed = new HashMap<>();
    static{
        //nothing atm
    }

	public static void restoreTalentsFromBundle( Bundle bundle, Hero hero, boolean restoreInRanking ){
		//TODO restore replacements
		if (bundle.contains("replacements")){
			Bundle replacements = bundle.getBundle("replacements");
            for (String key : replacements.getKeys()){
                String value = replacements.getString(key);
                while (renamed.containsKey(key)) key = renamed.get(key);
                while (renamed.containsKey(value)) value = renamed.get(value);
                if (!removed.contains(key) && !removed.contains(value)){
                    try {
                        hero.metamorphedTalents.put(Talent.valueOf(key), Talent.valueOf(value));
                    } catch (Exception e) {
                        GirlsFrontlinePixelDungeon.reportException(e);
                    }
                }
            }
		}
        if (bundle.contains("addTalents")){
            Bundle addTalentsBundle = bundle.getBundle("addTalents");
            hero.addTalents = new LinkedHashMap<>();
            for (String key : addTalentsBundle.getKeys()){
                while (renamed.containsKey(key)) key = renamed.get(key);
                if (!removed.contains(key)){
                    try {
                        hero.addTalents.put(Talent.valueOf(key), addTalentsBundle.getInt(key));
                    } catch (Exception e) {
                        GirlsFrontlinePixelDungeon.reportException(e);
                    }
                }
            }
        }

		if (hero.heroClass != null)     initClassTalents(hero);
		if (hero.subClass != null)      initSubclassTalents(hero);
		if (hero.armorAbility != null)  initArmorTalents(hero);

		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = bundle.contains(TALENT_TIER+(i+1)) ? bundle.getBundle(TALENT_TIER+(i+1)) : null;

			if (tierBundle != null){
                for (String talentName : tierBundle.getKeys()){
                    int points = tierBundle.getInt(talentName);
                    if (renamed.containsKey(talentName)) talentName = renamed.get(talentName);
                    if (!removed.contains(talentName)) {
                        try {
                            Talent talent = Talent.valueOf(talentName);
                            if (tier.containsKey(talent)) {
                                int point = Math.min(points, talent.maxPoints());
                                if (restoreInRanking)
                                    tier.put(talent, point);
                                else
                                    TalentUpdate(tier, hero, talent, point);
                            }
                        } catch (Exception e) {
                            GirlsFrontlinePixelDungeon.reportException(e);
                        }
                    }
                }
			}
		}
	}
    private static void TalentUpdate(LinkedHashMap<Talent, Integer> tier, Hero hero, Talent talent, int point){
        //因版本变更导致的天赋更改，对于那些获得了升级收益的天赋，在这里单次逐级升级重新获得收益
        if (Dungeon.version < talent.version){
            tier.put(talent, 0);
            for(int i = 1; i<=point; i++){
                tier.put(talent, i);
                onTalentUpgraded(hero, talent);
            }
            //记得return
            return;
        }
        tier.put(talent, point);
        //对读档时重建的天赋表进行加点记录，不触发加点效果
    }
}
