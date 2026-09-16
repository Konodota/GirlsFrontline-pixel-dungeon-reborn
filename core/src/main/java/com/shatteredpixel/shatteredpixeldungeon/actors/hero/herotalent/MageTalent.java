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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.watabou.utils.Random;

/**
 * 法师（G11）角色天赋池
 *
 * 把原本散落在 Talent / Hero / Char / Mob / Wand 等中的法师天赋效果逻辑按“角色”维度
 * 集中到本类，并按生命周期钩子组织。调用方只需在各钩子里委托本类的对应静态方法，
 * 新增法师天赋也只需改本类（外加 Talent 枚举注册）。
 *
 * 刻意保留在 Talent.java 中、不在本类的内容：
 * - 天赋枚举常量（EMPOWERING_MEAL / SCHOLARS_INTUITION / ALLY_WARP …）与天赋表注册；
 * - 会被序列化进存档的 tracker/计数 buff 内部类（WandPreservationCounter、
 *   EmpoweredStrikeTracker）——移动会改变类全名导致旧存档无法反序列化，本类通过 Talent.Xxx 引用。
 */
public final class MageTalent {

	private MageTalent() {}

	// ===================== 生命周期钩子（供调用方统一委托） =====================

	/** 进食后：充能一餐（法杖增伤）、回能一餐（充能回合） */
	public static void onFoodEaten( Hero hero ){
		if (hero.hasTalent(Talent.EMPOWERING_MEAL)) {
			//2/3 bonus wand damage for next 3 zaps
			Buff.affect(hero, WandEmpower.class).set(1 + hero.pointsInTalent(Talent.EMPOWERING_MEAL), 3);
			ScrollOfRecharging.chargeParticle(hero);
		}
		if (hero.hasTalent(Talent.ENERGIZING_MEAL)) {
			//5/8 turns of recharging
			Buff.prolong(hero, Recharging.class, 2 + 3 * (hero.pointsInTalent(Talent.ENERGIZING_MEAL)));
			ScrollOfRecharging.chargeParticle(hero);
		}
	}

	/** 鉴定速度乘数：学者直觉（法杖） */
	public static float itemIDSpeedFactor( Hero hero, Item item, float factor ){
		if (item instanceof Wand){
			factor *= 1f + 2 * hero.pointsInTalent(Talent.SCHOLARS_INTUITION);
		}
		return factor;
	}

	/** 使用卷轴后：充能升级（按倍率回复充能） */
	public static void onScrollUsed( Hero hero, float mul ){
		if (hero.hasTalent(Talent.ENERGIZING_UPGRADE)){
			Buff.prolong(hero, Recharging.class, 4 * mul);
		}
	}

	/** 鉴定物品后：验证假说（回复充能） */
	public static void onItemIdentified( Hero hero, Item item ){
		if (hero.hasTalent(Talent.TESTED_HYPOTHESIS)){
			//2/3 turns of wand recharging
			Buff.affect(hero, Recharging.class, 1f + hero.pointsInTalent(Talent.TESTED_HYPOTHESIS));
			ScrollOfRecharging.chargeParticle(hero);
		}
	}

	/** 升级后：法杖保留+2时消耗一层计数（防止法杖因升级而免费回充） */
	public static void onHeroLevelUp( Hero hero ){
		if (hero.pointsInTalent(Talent.WAND_PRESERVATION) == 2){
			Talent.WandPreservationCounter counter = Buff.affect(hero, Talent.WandPreservationCounter.class);
			if (counter.count() > 0)
				counter.countDown(1);
		}
	}

	// ===================== 法杖相关 =====================

	/** 充能升级+2时法杖充能下限可到-2，否则为0 */
	public static int wandMinCharges( Hero hero ){
		if (hero.pointsInTalent(Talent.ENERGIZING_UPGRADE) == 2)
			return -2;
		return 0;
	}

	/** 充能升级未满+2时，多耗充能的法杖需要鉴定提示 */
	public static boolean wandOverchargeIdentify( Hero hero ){
		return hero.pointsInTalent(Talent.ENERGIZING_UPGRADE) != 2;
	}

	/** 学者直觉+2：法杖使用一次后立即鉴定 */
	public static boolean instantIdentifyWand( Hero hero ){
		return hero.pointsInTalent(Talent.SCHOLARS_INTUITION) == 2;
	}

	/** 战法·充能打击：增益期间附魔/触发概率乘数，无增益返回1 */
	public static float empoweredStrikeMultiplier( Char attacker ){
		if (attacker instanceof Hero
				&& attacker.buff(Talent.EmpoweredStrikeTracker.class) != null){
			return 1.0F + (float) ((Hero) attacker).pointsInTalent(Talent.EMPOWERED_STRIKE) / 2.0F;
		}
		return 1.0F;
	}

	/** 秘法视野：法杖命中目标后赋予对该目标的心灵感知 */
	public static void onWandZapped( Hero hero, Char target ){
		if (hero.hasTalent(Talent.ARCANE_VISION)) {
			int dur = 5 + 5 * hero.pointsInTalent(Talent.ARCANE_VISION);
			Buff.append(hero, TalismanOfForesight.CharAwareness.class, dur).charID = target.id();
		}
	}

	// ===================== 盟军传送（ALLY_WARP） =====================

	/** 盟军传送：允许与英雄换位的额外交互距离（2*天赋点） */
	public static int allyWarpInteractDistance( Hero hero ){
		return 2 * hero.pointsInTalent(Talent.ALLY_WARP);
	}

	/** 是否拥有盟军传送（与友军交互时立即换位） */
	public static boolean hasAllyWarp( Hero hero ){
		return hero.hasTalent(Talent.ALLY_WARP);
	}

	// ===================== 术士·噬魂（SOUL_EATER / SOUL_SIPHON / NECROMANCERS_MINIONS） =====================

	/** 噬魂：非英雄造成的物理伤害转治疗的衰减系数（0.4*天赋点/3） */
	public static float soulSiphonFactor( Hero hero ){
		return 0.4f * hero.pointsInTalent(Talent.SOUL_SIPHON) / 3f;
	}

	/** 噬魂：治疗量同步为饥饿值的系数（天赋点/3） */
	public static float soulEaterHungerFactor( Hero hero ){
		return hero.pointsInTalent(Talent.SOUL_EATER) / 3f;
	}

	/** 噬魂：敌人回合触发“进食效果”的检定（10点roll小于天赋点） */
	public static boolean rollSoulEater( Hero hero ){
		return Random.Int(10) < hero.pointsInTalent(Talent.SOUL_EATER);
	}

	/** 死灵仆从：被魂标记的敌人死亡时召唤堕落幽魂的检定 */
	public static boolean rollNecromancerWraith( Hero hero ){
		return Random.Float() < (0.4f * hero.pointsInTalent(Talent.NECROMANCERS_MINIONS) / 3f);
	}

	// ===================== 战法·法杖（EMPOWERED_STRIKE / EXCESS_CHARGE / MYSTICAL_CHARGE / WAND_PRESERVATION） =====================

	/** 充能打击：老魔杖发射后首次攻击的法杖伤害乘数（无增益返回原值） */
	public static int empoweredStrikeStaffDamage( Char attacker, int damage ){
		if (attacker instanceof Hero && attacker.buff(Talent.EmpoweredStrikeTracker.class) != null){
			return Math.round(damage * (1f + ((Hero) attacker).pointsInTalent(Talent.EMPOWERED_STRIKE) / 6f));
		}
		return damage;
	}

	/** 盈能屏障：法杖充满时命中概率获得护盾 */
	public static boolean rollExcessCharge( Hero hero, boolean fullyCharged ){
		return fullyCharged && Random.Int(5) < hero.pointsInTalent(Talent.EXCESS_CHARGE);
	}

	/** 充能秘术：战法命中时为所有神器充能（0.5*天赋点） */
	public static void mysticalChargeArtifacts( Hero hero ){
		if (!hero.hasTalent(Talent.MYSTICAL_CHARGE)) return;
		for (Buff b : hero.buffs()){
			if (b instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.ArtifactBuff) {
				if (!((com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.ArtifactBuff) b).isCursed()) {
					((com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.ArtifactBuff) b).charge(hero, 0.5f * hero.pointsInTalent(Talent.MYSTICAL_CHARGE));
				}
			}
		}
	}

	/**
	 * 法杖保留：消耗一次保留计数（若有），返回true表示本次灌注/炼金应保留法杖等级。
	 */
	public static boolean spendWandPreservation( Hero hero ){
		if (hero != null && hero.hasTalent(Talent.WAND_PRESERVATION)){
			Talent.WandPreservationCounter counter = Buff.affect(hero, Talent.WandPreservationCounter.class);
			if (counter.count() < hero.pointsInTalent(Talent.WAND_PRESERVATION)){
				counter.countUp(1);
				return true;
			}
		}
		return false;
	}

	/** 法杖保留剩余次数（天赋点-已用计数） */
	public static int wandPreservesLeft( Hero hero ){
		int left = hero.pointsInTalent(Talent.WAND_PRESERVATION);
		if (hero.buff(Talent.WandPreservationCounter.class) != null){
			left -= (int) hero.buff(Talent.WandPreservationCounter.class).count();
		}
		return left;
	}

	public static boolean hasWandPreservation( Hero hero ){
		return hero != null && hero.hasTalent(Talent.WAND_PRESERVATION);
	}

	/**
	 * 奥术树脂炼金预览/产出的额外数量：
	 * 非法师按天赋点追加；法师有剩余保留次数时追加2；其余为0。
	 */
	public static int arcaneResinExtraQuantity( Hero hero ){
		if (!hasWandPreservation(hero)){
			return 0;
		}
		if (hero.heroClass != HeroClass.MAGE){
			return hero.pointsInTalent(Talent.WAND_PRESERVATION);
		} else if (wandPreservesLeft(hero) > 0){
			return 2;
		}
		return 0;
	}

	/** 野性魔力T4：强化期间法杖等级的额外提升 */
	public static int wildMagicBonusLevel( Hero hero, int level ){
		if (hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic.WildMagicTracker.class) != null){
			int bonus = 2 + hero.pointsInTalent(Talent.WILD_POWER);
			if (Random.Int(2) == 0) bonus++;
			bonus /= 2; // +1/+1.5/+2/+2.5/+3 at 0/1/2/3/4 talent points

			int maxBonusLevel = 2 + hero.pointsInTalent(Talent.WILD_POWER);
			if (level < maxBonusLevel) {
				level = Math.min(level + bonus, maxBonusLevel);
			}
		}
		return level;
	}

	// ===================== T4 信标跃迁（WarpBeacon：TELEFRAG / REMOTE_BEACON / LONGRANGE_WARP） =====================

	public static boolean hasRemoteBeacon( Hero hero ){
		return hero.hasTalent(Talent.REMOTE_BEACON);
	}

	public static boolean hasLongrangeWarp( Hero hero ){
		return hero.hasTalent(Talent.LONGRANGE_WARP);
	}

	/** 远程跃迁：跨层传送的额外耗能乘数（1.833-0.333*天赋点） */
	public static float longrangeWarpChargeMultiplier( Hero hero ){
		return 1.833f - 0.333f * hero.pointsInTalent(Talent.LONGRANGE_WARP);
	}

	public static boolean hasTelefrag( Hero hero ){
		return hero.hasTalent(Talent.TELEFRAG);
	}

	/** 跃迁打击：对英雄自身造成的伤害（5*天赋点） */
	public static int telefragSelfDamage( Hero hero ){
		return 5 * hero.pointsInTalent(Talent.TELEFRAG);
	}

	/** 跃迁打击：对落点敌人造成的伤害 */
	public static int telefragDamage( Hero hero ){
		int pts = hero.pointsInTalent(Talent.TELEFRAG);
		return Random.NormalIntRange(10 * pts, 15 * pts);
	}

	/** 遥控信标：可放置信标的最远距离（4*天赋点） */
	public static int remoteBeaconRange( Hero hero ){
		return 4 * hero.pointsInTalent(Talent.REMOTE_BEACON);
	}

	// ===================== T4 野性施法（WildMagic：CONSERVED_MAGIC / FIRE_EVERYTHING） =====================

	/** 节约魔法：每发法杖的耗能乘数（0.67^天赋点） */
	public static float conservedMagicChargeUse( Hero hero ){
		return (float) Math.pow(0.67f, hero.pointsInTalent(Talent.CONSERVED_MAGIC));
	}

	/** 无限齐射：最多同时发射的法杖数（4+天赋点） */
	public static int fireEverythingMaxWands( Hero hero ){
		return 4 + hero.pointsInTalent(Talent.FIRE_EVERYTHING);
	}

	/** 节约魔法：第三次发射检定通过 */
	public static boolean conservedMagicAllowsThirdShot( Hero hero ){
		return Random.Int(4) <= hero.pointsInTalent(Talent.CONSERVED_MAGIC);
	}

	// ===================== T4 元素爆破（ElementalBlast：BLAST_RADIUS / ELEMENTAL_POWER / REACTIVE_BARRIER） =====================

	/** 爆炸半径：AOE大小（4+天赋点） */
	public static int blastRadiusSize( Hero hero ){
		return 4 + hero.pointsInTalent(Talent.BLAST_RADIUS);
	}

	/** 元素之力：效果乘数（1+0.2*天赋点） */
	public static float elementalPowerMultiplier( Hero hero ){
		return 1f + 0.2f * hero.pointsInTalent(Talent.ELEMENTAL_POWER);
	}

	/** 反应屏障：爆破后按命中敌人数获得护盾量，无天赋返回0 */
	public static int reactiveBarrierShield( Hero hero, int charsHit ){
		if (hero.hasTalent(Talent.REACTIVE_BARRIER)){
			return charsHit * 2 * hero.pointsInTalent(Talent.REACTIVE_BARRIER);
		}
		return 0;
	}

	// ===================== 法杖施法（Wand：EMPOWERED_STRIKE / BACKUP_BARRIER / SHIELD_BATTERY） =====================

	/** 蓄能打击：装入老魔杖的法杖施法后挂蓄能追踪，供下次近战增伤 */
	public static void onWandZappedEmpoweredStrike( Hero hero, Wand wand, boolean heroCharging ){
		if (hero.hasTalent(Talent.EMPOWERED_STRIKE)
				&& heroCharging
				&& !hero.belongings.contains(wand)){
			Buff.prolong(hero, Talent.EmpoweredStrikeTracker.class, 10f);
		}
	}

	/** 备用法障：空充能施法时是否获得护盾（法师需法杖装于老魔杖中；蜕变持有时需该法杖等级不低于其他法杖） */
	public static boolean backupBarrierTriggers( Hero hero, Wand wand, int curCharges, boolean heroCharging ){
		if (!hero.hasTalent(Talent.BACKUP_BARRIER)
				|| curCharges > 0
				|| !heroCharging){
			return false;
		}
		if (hero.heroClass == HeroClass.MAGE){
			return !hero.belongings.contains(wand);
		} else {
			for (Wand i : hero.belongings.getAllItems(Wand.class)){
				if (i.level() > wand.level()){
					return false;
				}
			}
			return true;
		}
	}

	/** 备用法障：护盾量（1+2*天赋点） */
	public static int backupBarrierShield( Hero hero ){
		return 1 + 2 * hero.pointsInTalent(Talent.BACKUP_BARRIER);
	}

	public static boolean hasShieldBattery( Hero hero ){
		return hero.hasTalent(Talent.SHIELD_BATTERY);
	}

	/** 护盾电池：2点时对自身施放的护盾量乘数为1.5，否则1 */
	public static float shieldBatteryMultiplier( Hero hero ){
		return hero.pointsInTalent(Talent.SHIELD_BATTERY) == 2 ? 1.5f : 1f;
	}
}
