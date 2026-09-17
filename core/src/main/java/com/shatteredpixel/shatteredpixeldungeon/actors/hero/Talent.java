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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.GSH18Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.HuntressTalent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.MageTalent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.RogueTalent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.Type561Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.WarriorTalent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ScrollEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

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
	//旧版56-1式角色 GUN_MASTER 转职天赋（隐藏功能，仅旧版模式使用）
	NEWLIFE(142, 3), MORE_ACCURATE(143, 3), ENHANCE_GRENADE(144, 3),
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
    
    //蓄能星击 buff（天狼星心脏天赋激活后挂在角色身上的子buff，下次攻击时结算快照伤害）
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

        public int icon() { return BuffIndicator.CHARGED_STAR_STRIKE; }
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
        // 56-1式角色天赋升级逻辑（实现见 Type561Talent）
        Type561Talent.onTalentUpgraded(hero, talent);
        // 战士（UMP45）天赋升级逻辑（武器大师直觉/大力神，实现见 WarriorTalent）
        WarriorTalent.onTalentUpgraded(hero, talent);
        // 盗贼（UMP9）天赋升级逻辑（储备口粮/盗贼直觉/轻身披风/暗影护身，实现见 RogueTalent）
        RogueTalent.onTalentUpgraded(hero, talent);
        // 女猎（隼）天赋升级逻辑（自然馈赠/强化感官/远视，实现见 HuntressTalent）
        HuntressTalent.onTalentUpgraded(hero, talent);
	}

	public static class CachedRationsDropped extends CounterBuff{{revivePersists = true;}}
    public static class ZongziDropped extends CounterBuff{{revivePersists = true;}}
	public static class NatureBerriesAvailable extends CounterBuff{{revivePersists = true;}}

	public static void onFoodEaten(Hero hero, float foodVal, Item foodSource) {
        // 战士（UMP45）进食天赋（丰盛大餐/铁胃，实现见 WarriorTalent）
        WarriorTalent.onFoodEaten(hero);
        // 法师（G11）进食天赋（充能一餐/回能一餐，实现见 MageTalent）
        MageTalent.onFoodEaten(hero);
        // 盗贼（UMP9）进食天赋（神秘一餐，实现见 RogueTalent）
        RogueTalent.onFoodEaten(hero, foodSource);
        // 女猎（隼）进食天赋（振奋一餐，实现见 HuntressTalent）
        HuntressTalent.onFoodEaten(hero);

        // GSH18角色进食相关天赋（疗养一餐/元气一餐，实现见 GSH18Talent）
        GSH18Talent.onFoodEaten(hero);

        // 56-1式角色进食相关天赋（饭饱为钢旧版回血，实现见 Type561Talent）
        Type561Talent.onFoodEaten(hero);
    }

	public static class WarriorFoodImmunity extends FlavourBuff{
		{ actPriority = HERO_PRIO+1; }
	}

	public static float itemIDSpeedFactor( Hero hero, Item item ){
		float factor = 1f;
		// 女猎（隼）生存主义者直觉（实现见 HuntressTalent）
		factor = HuntressTalent.itemIDSpeedFactor(hero, factor);
		// 战士（UMP45）武器大师直觉（实现见 WarriorTalent）
		factor = WarriorTalent.itemIDSpeedFactor(hero, item, factor);
		// 法师（G11）学者直觉（实现见 MageTalent）
		factor = MageTalent.itemIDSpeedFactor(hero, item, factor);
		// 盗贼（UMP9）盗贼直觉（实现见 RogueTalent）
		factor = RogueTalent.itemIDSpeedFactor(hero, item, factor);
		// 56-1式角色鉴定速度（百战老兵/战场老兵旧版，实现见 Type561Talent）
		factor *= Type561Talent.itemIDSpeedFactor(hero, item);
		return factor;
	}

    public static void onPotionUsed( Hero hero, float mul ){
        onPotionUsed(hero, mul, hero.pos);
    }
	public static void onPotionUsed( Hero hero, float mul, int pos ){
		// 战士（UMP45）药水天赋（重振决心，实现见 WarriorTalent）
		WarriorTalent.onPotionUsed(hero, mul);
		// GSH18角色药水相关天赋（医护兼容，实现见 GSH18Talent）
		GSH18Talent.onPotionUsed(hero, mul);
		// 女猎（隼）药水天赋（自然修复，实现见 HuntressTalent）
		HuntressTalent.onPotionUsed(hero, mul, pos);
	}

	public static void onScrollUsed( Hero hero, float mul ){
		// 法师（G11）卷轴天赋（充能升级，实现见 MageTalent）
		MageTalent.onScrollUsed(hero, mul);
		// 盗贼（UMP9）卷轴天赋（神秘升级，实现见 RogueTalent）
		RogueTalent.onScrollUsed(hero, mul);
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
		// 盗贼（UMP9）神器天赋（强化戒指，实现见 RogueTalent）；未处理时再判断蜕变V2
		if (RogueTalent.onArtifactUsed(hero)){
			// ENHANCED_RINGS 已在 RogueTalent 内处理
		}
        else if (hero.hasTalent(ENHANCED_RINGS_V2) && hero.buff(EnhancedRings.CoolDown.class) == null){
            Buff.prolong(hero, EnhancedRings.class, 3).set(hero.pointsInTalent(ENHANCED_RINGS_V2));
            hero.updateHT(false);
        }
	}

	public static void onItemEquipped( Hero hero, Item item ){
		// 战士（UMP45）装备后鉴定（武器大师直觉，实现见 WarriorTalent）
		WarriorTalent.onItemEquipped(hero, item);

		// 56-1式角色装备后鉴定（战场老兵旧版，实现见 Type561Talent）
		Type561Talent.onItemEquipped(hero, item);

		// 盗贼（UMP9）装备戒指（盗贼直觉，实现见 RogueTalent）
		RogueTalent.onItemEquipped(hero, item);
	}

	public static void onItemCollected( Hero hero, Item item ){
		// 盗贼（UMP9）获取戒指标记已知（盗贼直觉，实现见 RogueTalent）
		RogueTalent.onItemCollected(hero, item);

		// 56-1式角色获取物品时标记诅咒（百战老兵/战场老兵旧版，实现见 Type561Talent）
		Type561Talent.onItemCollected(hero, item);
	}

	//note that IDing can happen in alchemy scene, so be careful with VFX here
	public static void onItemIdentified( Hero hero, Item item ){
		// 战士（UMP45）鉴定回血（试验对象，实现见 WarriorTalent）
		WarriorTalent.onItemIdentified(hero, item);
		// 法师（G11）鉴定回充（验证假说，实现见 MageTalent）
		MageTalent.onItemIdentified(hero, item);
	}
    public static int onDefenceProc(Hero hero, Char enemy, int dmg){
        // 56-1式角色被攻击天赋（夜战精英旧版，实现见 Type561Talent）
        return Type561Talent.onDefenceProc(hero, enemy, dmg);
    }

	public static int onAttackProc( Hero hero, Char enemy, int dmg ){
		// 盗贼（UMP9）攻击命中天赋（偷袭，实现见 RogueTalent）
		dmg = RogueTalent.onAttackProc(hero, enemy, dmg);

		// 女猎（隼）攻击命中天赋（连击，实现见 HuntressTalent）
		dmg = HuntressTalent.onAttackProc(hero, enemy, dmg);

		// GSH18角色攻击命中相关天赋（锁链冲击溅射/双星守护回盾，实现见 GSH18Talent；
		// 天狼星心脏的附加伤害在 SiriusHeart.onAttack 中同样调用 GSH18Talent.chainShock）
		GSH18Talent.onAttackProc(hero, enemy, dmg);

		// 56-1式角色攻击命中天赋（轻装简从/胆敢向我还击/解放刺/侦查部队，实现见 Type561Talent）
		dmg = Type561Talent.onAttackProc(hero, enemy, dmg);

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
				if (SPDSettings.type561OldMode()){
					//旧版56-1式角色天赋表（隐藏功能）
					Collections.addAll(tierTalents, NICE_FOOD, OLD_SOLDIER, FAST_RELOAD, BETTER_FOOD);
				} else {
					Collections.addAll(tierTalents, Type56One_FOOD , Type56One_Identify, Type56One_Damage, Type56_14);
				}
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
				if (SPDSettings.type561OldMode()){
					//旧版56-1式角色天赋表（隐藏功能）
					Collections.addAll(tierTalents, BARGAIN_SKILLS, TRAP_EXPERT, HOW_DARE_YOU, JIEFANGCI, NIGHT_EXPERT);
				} else {
					Collections.addAll(tierTalents, Type56Two_FOOD, Type56Two_Armor, Type56_23V4, Type56Two_Sight, Type56Two_Damage);
				}
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
				if (SPDSettings.type561OldMode()){
					//旧版56-1式角色天赋表（隐藏功能）
					Collections.addAll(tierTalents, SEARCH_ARMY, ELITE_ARMY);
				} else {
					Collections.addAll(tierTalents, Type56Three_Bomb, Type56Three_Book);
				}
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
				if (SPDSettings.type561OldMode()){
					//旧版56-1式角色转职天赋（隐藏功能）
					Collections.addAll(tierTalents, NEWLIFE, MORE_ACCURATE, ENHANCE_GRENADE);
				} else {
					Collections.addAll(tierTalents, GUN_1V2, GUN_2V2, GUN_3);
				}
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
