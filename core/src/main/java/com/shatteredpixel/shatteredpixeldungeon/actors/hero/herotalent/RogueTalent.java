/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2018 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.watabou.utils.Random;

/**
 * 盗贼（UMP9）角色天赋池
 *
 * 把原本散落在 Talent / Hero / Char / Mob / MissileWeapon 等中的盗贼天赋效果逻辑按“角色”
 * 维度集中到本类，并按生命周期钩子组织。调用方只需在各钩子里委托本类的对应静态方法，
 * 新增盗贼天赋也只需改本类（外加 Talent 枚举注册）。
 *
 * 刻意保留在 Talent.java 中、不在本类的内容：
 * - 天赋枚举常量（CACHED_RATIONS / THIEFS_INTUITION / SUCKER_PUNCH …）与天赋表注册；
 * - 会被序列化进存档的 tracker/计数 buff 内部类（CachedRationsDropped、
 *   ProtectiveShadowsTracker、SuckerPunchTracker、BountyHunterTracker）——移动会改变类全名
 *   导致旧存档无法反序列化，本类通过 Talent.Xxx 引用。
 */
public final class RogueTalent {

	private RogueTalent() {}

	// ===================== 生命周期钩子（供调用方统一委托） =====================

	/** 天赋升级后：储备口粮、盗贼直觉（戒指）、轻身披风激活、暗影护身 */
	public static void onTalentUpgraded( Hero hero, Talent talent ){
		if (talent == Talent.CACHED_RATIONS){
			if ( hero.pointsInTalent(Talent.CACHED_RATIONS) == 1) Buff.count(hero, Talent.CachedRationsDropped.class, 4);
			else                                           Buff.count(hero, Talent.CachedRationsDropped.class, 2);
		}
		else if (talent == Talent.THIEFS_INTUITION && hero.pointsInTalent(Talent.THIEFS_INTUITION) == 2){
			if (hero.belongings.ring != null) hero.belongings.ring.identify();
			if (hero.belongings.misc instanceof Ring) hero.belongings.misc.identify();
			for (Item item : hero.belongings)
				if (item instanceof Ring)
					((Ring) item).setKnown();
		}
		else if (talent == Talent.THIEFS_INTUITION && hero.pointsInTalent(Talent.THIEFS_INTUITION) == 1){
			if (hero.belongings.ring != null) hero.belongings.ring.setKnown();
			if (hero.belongings.misc instanceof Ring) ((Ring) hero.belongings.misc).setKnown();
		}
		else if (talent == Talent.LIGHT_CLOAK && hero.pointsInTalent(Talent.LIGHT_CLOAK) == 1){
			Item cloak = hero.belongings.getItem(CloakOfShadows.class);
			if (cloak != null)
				((CloakOfShadows) cloak).activate(hero);
		}
		else if (talent == Talent.PROTECTIVE_SHADOWS && hero.invisible > 0)
			Buff.affect(hero, Talent.ProtectiveShadowsTracker.class);
	}

	/** 进食后：神秘一餐（神器充能） */
	public static void onFoodEaten( Hero hero, Item foodSource ){
		if (hero.hasTalent(Talent.MYSTICAL_MEAL)) {
			//3/5 turns of recharging
			ArtifactRecharge buff = Buff.affect(hero, ArtifactRecharge.class);
			if (buff.left() < 1 + 2 * (hero.pointsInTalent(Talent.MYSTICAL_MEAL))) {
				Buff.affect(hero, ArtifactRecharge.class).set(1 + 2 * (hero.pointsInTalent(Talent.MYSTICAL_MEAL))).ignoreHornOfPlenty = foodSource instanceof HornOfPlenty;
			}
			ScrollOfRecharging.chargeParticle(hero);
		}
	}

	/** 使用卷轴后：神秘升级（隐身回合） */
	public static void onScrollUsed( Hero hero, float mul ){
		if (hero.hasTalent(Talent.MYSTICAL_UPGRADE)){
			Buff.prolong(hero, Invisibility.class, 4 * mul);
		}
	}

	/** 使用神器后：强化戒指（+1/+2）；返回是否已处理（供蜕变互斥判断） */
	public static boolean onArtifactUsed( Hero hero ){
		if (hero.hasTalent(Talent.ENHANCED_RINGS)){
			Buff.prolong(hero, EnhancedRings.class, 3f * hero.pointsInTalent(Talent.ENHANCED_RINGS)).set(1);
			hero.updateHT(false);
			return true;
		}
		return false;
	}

	/** 鉴定速度乘数：盗贼直觉（戒指） */
	public static float itemIDSpeedFactor( Hero hero, Item item, float factor ){
		if (item instanceof Ring){
			factor *= 1f + hero.pointsInTalent(Talent.THIEFS_INTUITION);
		}
		return factor;
	}

	/** 装备物品后：盗贼直觉（戒指+1已知、+2鉴定） */
	public static void onItemEquipped( Hero hero, Item item ){
		if (hero.hasTalent(Talent.THIEFS_INTUITION) && item instanceof Ring){
			if (hero.pointsInTalent(Talent.THIEFS_INTUITION) == 2){
				item.identify();
			} else {
				((Ring) item).setKnown();
			}
		}
	}

	/** 获取物品后：盗贼直觉+2标记戒指已知 */
	public static void onItemCollected( Hero hero, Item item ){
		if (hero.pointsInTalent(Talent.THIEFS_INTUITION) == 2 && item instanceof Ring)
			((Ring) item).setKnown();
	}

	/** 攻击命中后：偷袭（目标未察觉时额外伤害，每目标一次） */
	public static int onAttackProc( Hero hero, Char enemy, int dmg ){
		if (hero.hasTalent(Talent.SUCKER_PUNCH)
				&& enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)
				&& enemy.buff(Talent.SuckerPunchTracker.class) == null){
			dmg += Random.IntRange(hero.pointsInTalent(Talent.SUCKER_PUNCH), 2);
			Buff.affect(enemy, Talent.SuckerPunchTracker.class);
		}
		return dmg;
	}

	// ===================== 广泛搜索（WIDE_SEARCH） =====================

	/** 广泛搜索+1时为环形搜索 */
	public static boolean wideSearchCircular( Hero hero ){
		return hero.pointsInTalent(Talent.WIDE_SEARCH) == 1;
	}

	/** 拥有广泛搜索时搜索距离+1 */
	public static int wideSearchDistanceBonus( Hero hero ){
		return hero.hasTalent(Talent.WIDE_SEARCH) ? 1 : 0;
	}

	// ===================== 轻身披风（LIGHT_CLOAK）遗失后仍充能 =====================

	/** 遗失背包里的暗影披风是否仍应激活（拥有轻身披风） */
	public static boolean activatesLostCloak( Hero hero ){
		return hero.hasTalent(Talent.LIGHT_CLOAK);
	}

	// ===================== 赏金猎人（BOUNTY_HUNTER） =====================

	/** 预谋（潜行）伤害掷骰时：拥有赏金猎人则挂0回合标记，供击杀掉金 */
	public static void onPreparationDamageRoll( Hero hero ){
		Buff.affect(hero, Talent.BountyHunterTracker.class, 0.0f);
	}

	/** 敌人死亡时：赏金猎人按预谋等级概率掉落金币 */
	public static void rollBountyGold( Hero hero, int pos ){
		if (hero.buff(Talent.BountyHunterTracker.class) != null) {
			Preparation prep = hero.buff(Preparation.class);
			if (prep != null && Random.Float() < 0.25f * prep.attackLevel()) {
				Dungeon.level.drop(new Gold(15 * hero.pointsInTalent(Talent.BOUNTY_HUNTER)), pos).sprite.drop();
			}
		}
	}

	// ===================== 无声步伐（SILENT_STEPS） =====================

	/** 与英雄距离足够远时，敌人无法察觉潜行中的英雄 */
	public static boolean silentStepsConceals( Hero hero, int distance ){
		return hero.hasTalent(Talent.SILENT_STEPS)
				&& distance >= 4 - hero.pointsInTalent(Talent.SILENT_STEPS);
	}

	// ===================== 疾行者·弹射势能（PROJECTILE_MOMENTUM） =====================

	/** 弹射势能：疾跑中投掷武器命中系数 */
	public static float projectileAccuracyFactor( Hero hero, float accFactor ){
		if (hero.buff(Momentum.class) != null && hero.buff(Momentum.class).freerunning()){
			accFactor *= 1f + 0.25f * hero.pointsInTalent(Talent.PROJECTILE_MOMENTUM);
		}
		return accFactor;
	}

	/** 弹射势能：疾跑中投掷武器伤害系数 */
	public static int projectileDamage( Hero hero, int damage ){
		if (hero.buff(Momentum.class) != null && hero.buff(Momentum.class).freerunning()){
			damage = Math.round(damage * (1f + 0.15f * hero.pointsInTalent(Talent.PROJECTILE_MOMENTUM)));
		}
		return damage;
	}

	// ===================== 轻身披风 / 暗影护身 / 强化戒指 =====================

	/** 轻身披风（蜕变到非盗贼时）：神器充能速度乘数 */
	public static float lightCloakArtifactChargeMul( Hero hero ){
		if (hero.heroClass == HeroClass.ROGUE) return 1f;
		return 1f + (0.2f * hero.pointsInTalent(Talent.LIGHT_CLOAK) / 3f);
	}

	/** 轻身披风：披风未装备时被动充能保留比例（0.75*天赋点/3） */
	public static float lightCloakUnequippedChargeFactor( Hero hero ){
		return 0.75f * hero.pointsInTalent(Talent.LIGHT_CLOAK) / 3f;
	}

	/** 进入隐身时：拥有暗影护身则挂上可叠加屏障的追踪buff */
	public static void protectiveShadowsOnInvisible( Hero hero ){
		if (hero.hasTalent(Talent.PROTECTIVE_SHADOWS)){
			Buff.affect(hero, Talent.ProtectiveShadowsTracker.class);
		}
	}

	/** 神秘升级+2：隐身术卷轴不会驱散隐身 */
	public static boolean mysticalUpgradeBlocksScrollDispel( Hero hero ){
		return hero.pointsInTalent(Talent.MYSTICAL_UPGRADE) >= 2;
	}

	/** 强化戒指buff的冷却图标满值（3*天赋点，无天赋为3） */
	public static float enhancedRingsMaxCooldown( Hero hero ){
		if (hero.hasTalent(Talent.ENHANCED_RINGS)){
			return 3f * hero.pointsInTalent(Talent.ENHANCED_RINGS);
		}
		return 3f;
	}

	// ===================== T4 疾跑（Momentum：SPEEDY_STEALTH / EVASIVE_ARMOR） =====================

	/** 疾风潜行：隐身且天赋≥1时获得疾跑层数 */
	public static boolean speedyStealthGainsStacks( Hero hero ){
		return hero.invisible > 0 && hero.pointsInTalent(Talent.SPEEDY_STEALTH) >= 1;
	}

	/** 疾风潜行：天赋≥2时疾跑回合不因隐身而衰减 */
	public static boolean speedyStealthKeepsFreerun( Hero hero ){
		return hero.pointsInTalent(Talent.SPEEDY_STEALTH) >= 2;
	}

	/** 疾风潜行+3：单纯隐身也视为疾跑（2倍速） */
	public static boolean speedyStealthInvisibleDoubleSpeed( Hero hero ){
		return hero.invisible > 0 && hero.pointsInTalent(Talent.SPEEDY_STEALTH) == 3;
	}

	/** 闪避护甲：疾跑期间额外护甲转化的闪避 */
	public static int evasiveArmorEvasionBonus( Hero hero, int excessArmorStr ){
		return excessArmorStr * hero.pointsInTalent(Talent.EVASIVE_ARMOR);
	}

	// ===================== T4 烟雾弹（SmokeBomb：HASTY_RETREAT / BODY_REPLACEMENT / SHADOW_STEP） =====================

	/** 暗影步伐：隐身时烟雾弹耗能乘数（0.795^天赋点），不满足返回1 */
	public static double shadowStepChargeFactor( Hero hero ){
		if (!hero.hasTalent(Talent.SHADOW_STEP) || hero.invisible <= 0) return 1.0;
		return Math.pow(0.795f, hero.pointsInTalent(Talent.SHADOW_STEP));
	}

	/** 当前是否处于“暗影步伐”状态（隐身且拥有天赋） */
	public static boolean shadowStepping( Hero hero ){
		return hero.invisible > 0 && hero.hasTalent(Talent.SHADOW_STEP);
	}

	public static boolean hasBodyReplacement( Hero hero ){
		return hero.hasTalent(Talent.BODY_REPLACEMENT);
	}

	/** 仓促撤退：加速/隐身持续时间（0.67+天赋点） */
	public static float hastyRetreatDuration( Hero hero ){
		return 0.67f + hero.pointsInTalent(Talent.HASTY_RETREAT);
	}

	/** 替身木桩：天赋点（同时决定其生命20*点、格挡点~3点） */
	public static int bodyReplacementPoints( Hero hero ){
		return hero.pointsInTalent(Talent.BODY_REPLACEMENT);
	}

	// ===================== T4 暗影分身（ShadowClone：PERFECT_COPY / SHADOW_BLADE / CLONED_ARMOR） =====================

	/** 完美复制：分身生命加成（10%/点） */
	public static int perfectCopyHpBonus( Hero hero, int baseBonus ){
		return Math.round(0.1f * hero.pointsInTalent(Talent.PERFECT_COPY) * baseBonus);
	}

	/** 暗影之刃：分身额外伤害（7.5%/点的英雄伤害） */
	public static int shadowBladeBonusDamage( Hero hero, int heroDamage ){
		return Math.round(0.075f * hero.pointsInTalent(Talent.SHADOW_BLADE) * heroDamage);
	}

	/** 暗影之刃：分身攻击借用英雄武器附魔的检定 */
	public static boolean rollShadowBladeProc( Hero hero ){
		return Random.Int(4) < hero.pointsInTalent(Talent.SHADOW_BLADE)
				&& hero.belongings.weapon() != null;
	}

	/** 克隆护甲：分身额外格挡（15%/点的英雄格挡） */
	public static int clonedArmorBonusDR( Hero hero, int heroRoll ){
		return Math.round(0.15f * hero.pointsInTalent(Talent.CLONED_ARMOR) * heroRoll);
	}

	/** 克隆护甲：分身受击借用英雄护甲附魔的检定 */
	public static boolean rollClonedArmorProc( Hero hero ){
		return Random.Int(4) < hero.pointsInTalent(Talent.CLONED_ARMOR)
				&& hero.belongings.armor() != null;
	}

	/** 完美复制：可与分身换位的最远距离（天赋点） */
	public static int perfectCopyInteractRange( Hero hero ){
		return hero.pointsInTalent(Talent.PERFECT_COPY);
	}

	public static boolean hasPerfectCopy( Hero hero ){
		return hero.hasTalent(Talent.PERFECT_COPY);
	}

	// ===================== T4 死亡标记（DeathMark：DOUBLE_MARK / FEAR_THE_REAPER / DEATHLY_DURABILITY） =====================

	/** 双重标记：强化期间耗能乘数（0.707^天赋点） */
	public static double doubleMarkChargeFactor( Hero hero ){
		return Math.pow(0.707f, hero.pointsInTalent(Talent.DOUBLE_MARK));
	}

	public static boolean hasDoubleMark( Hero hero ){
		return hero.hasTalent(Talent.DOUBLE_MARK);
	}

	/** 死神恐惧的天赋点（0表示未拥有） */
	public static int fearTheReaperPoints( Hero hero ){
		return hero.pointsInTalent(Talent.FEAR_THE_REAPER);
	}

	/** 死亡耐久：处决敌人后获得的护盾量 */
	public static int deathlyDurabilityShield( Hero hero, int initialHP ){
		return Math.round(initialHP * (0.125f * hero.pointsInTalent(Talent.DEATHLY_DURABILITY)));
	}

	// ===================== 刺客准备（Preparation：ENHANCED_LETHALITY / ASSASSINS_REACH） =====================

	/** 强化致命：当前准备等级对应的处决阈值表列索引（天赋点） */
	public static int enhancedLethalityPoints( Hero hero ){
		return hero.pointsInTalent(Talent.ENHANCED_LETHALITY);
	}

	/** 刺客延伸：闪现距离表列索引（天赋点） */
	public static int assassinsReachPoints( Hero hero ){
		return hero.pointsInTalent(Talent.ASSASSINS_REACH);
	}

	// ===================== 盗贼直觉（ROGUES_FORESIGHT） =====================

	public static boolean hasRoguesForesight( Hero hero ){
		return hero.hasTalent(Talent.ROGUES_FORESIGHT);
	}

	/** 盗贼直觉：进入楼层时揭示密室提示的概率检定（50%/100%，调用方负责用楼层种子压栈随机数） */
	public static boolean rollRoguesForesightHint( Hero hero ){
		return Random.Int(2) + 1 <= hero.pointsInTalent(Talent.ROGUES_FORESIGHT);
	}
}
