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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

/**
 * 战士（UMP45）角色天赋池
 *
 * 把原本散落在 Talent / Hero / Weapon / Item / Armor / BrokenSeal / Mob / 药水 等中的
 * 战士天赋效果逻辑按“角色”维度集中到本类，并按生命周期钩子组织。调用方只需在各钩子里
 * 委托本类的对应静态方法，新增战士天赋也只需改本类（外加 Talent 枚举注册）。
 *
 * 刻意保留在 Talent.java 中、不在本类的内容：
 * - 天赋枚举常量（HEARTY_MEAL / IRON_WILL / HOLD_FAST / STRONGMAN …）与天赋表注册；
 * - 会被序列化进存档的 tracker/冷却 buff 内部类（WarriorFoodImmunity、
 *   ImprovisedProjectileCooldown、LethalMomentumTracker、StrikingWaveTracker）——移动会改变
 *   类全名导致旧存档无法反序列化，本类通过 Talent.Xxx 引用。
 */
public final class WarriorTalent {

	private WarriorTalent() {}

	// ===================== 生命周期钩子（供调用方统一委托） =====================

	/** 天赋升级后：武器大师直觉立即鉴定、大力神力量检定 */
	public static void onTalentUpgraded( Hero hero, Talent talent ){
		if (talent == Talent.ARMSMASTERS_INTUITION && hero.pointsInTalent(Talent.ARMSMASTERS_INTUITION) == 2){
			if (hero.belongings.weapon() != null) hero.belongings.weapon().identify();
			if (hero.belongings.armor() != null)  hero.belongings.armor.identify();
			if (hero.belongings.SecondArmor() != null)  hero.belongings.secArmor.identify();
		}
		else if (talent == Talent.STRONGMAN){
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
	}

	/** 进食后：丰盛大餐（残血回血）、铁胃（饥饿伤害免疫） */
	public static void onFoodEaten( Hero hero ){
		if (hero.hasTalent(Talent.HEARTY_MEAL)) {
			//3/5 HP healed, when hero is below 25% health
			if (hero.HP <= hero.HT / 4) {
				hero.HP = Math.min(hero.HP + 1 + 2 * hero.pointsInTalent(Talent.HEARTY_MEAL), hero.HT);
				hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1 + hero.pointsInTalent(Talent.HEARTY_MEAL));
				//2/3 HP healed, when hero is below 50% health
			} else if (hero.HP <= hero.HT / 2) {
				hero.HP = Math.min(hero.HP + 1 + hero.pointsInTalent(Talent.HEARTY_MEAL), hero.HT);
				hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), hero.pointsInTalent(Talent.HEARTY_MEAL));
			}
		}
		if (hero.hasTalent(Talent.IRON_STOMACH)) {
			if (hero.cooldown() > 0) {
				Buff.affect(hero, Talent.WarriorFoodImmunity.class, hero.cooldown());
			}
		}
	}

	/** 鉴定速度乘数：武器大师直觉（近战武器/护甲） */
	public static float itemIDSpeedFactor( Hero hero, Item item, float factor ){
		if (item instanceof MeleeWeapon || item instanceof Armor){
			factor *= 1f + hero.pointsInTalent(Talent.ARMSMASTERS_INTUITION);
		}
		return factor;
	}

	/** 使用药水后：重振决心（把溢出治疗量转为战士护盾/屏障） */
	public static void onPotionUsed( Hero hero, float mul ){
		if (hero.hasTalent(Talent.RESTORED_WILLPOWER)){
			ShieldBuff shield = hero.buff(BrokenSeal.WarriorShield.class);
			int shieldToGive = Math.round( hero.HT/100F*hero.pointsInTalent(Talent.RESTORED_WILLPOWER)*3.5F*mul );
			if (shield != null){
				((BrokenSeal.WarriorShield) shield).supercharge(shieldToGive);
			}
			else {
				shield = Buff.affect(hero, Barrier.class);
				shield.setShield(shieldToGive);
			}
		}
	}

	/** 装备物品后：武器大师直觉+2立即鉴定武器/护甲 */
	public static void onItemEquipped( Hero hero, Item item ){
		if (hero.pointsInTalent(Talent.ARMSMASTERS_INTUITION) == 2 && (item instanceof Weapon || item instanceof Armor))
			item.identify();
	}

	/** 鉴定物品后：试验对象回血 */
	public static void onItemIdentified( Hero hero, Item item ){
		if (hero.hasTalent(Talent.TEST_SUBJECT)){
			//heal for 2/3 HP
			hero.HP = Math.min(hero.HP + 1 + hero.pointsInTalent(Talent.TEST_SUBJECT), hero.HT);
			if (hero.sprite != null) {
				Emitter e = hero.sprite.emitter();
				if (e != null) e.burst(Speck.factory(Speck.HEALING), hero.pointsInTalent(Talent.TEST_SUBJECT));
			}
		}
	}

	// ===================== 大力神（力量药水/灵药额外生效检定） =====================

	/** 大力神：使用力量药水/大力灵药时 1/(20) 概率额外再触发一次 */
	public static boolean rollStrongman( Hero hero ){
		return hero.hasTalentB(Talent.STRONGMAN)
				&& Random.Int(20) < hero.pointsInTalent(Talent.STRONGMAN) + 1;
	}

	// ===================== 铁胃（饥饿伤害免疫） =====================

	/** 当前是否处于铁胃的饥饿伤害免疫窗口 */
	public static boolean hasFoodImmunity( Hero hero ){
		return hero.buff(Talent.WarriorFoodImmunity.class) != null;
	}

	/** 铁胃：把即将受到的饥饿（debuff）伤害按天赋点缩减，+1受到25%、+2免疫 */
	public static int applyFoodImmunityDamage( Hero hero, int dmg ){
		if (hasFoodImmunity(hero)){
			if (hero.pointsInTalent(Talent.IRON_STOMACH) == 1)      dmg = Math.round(dmg * 0.25f);
			else if (hero.pointsInTalent(Talent.IRON_STOMACH) == 2) dmg = Math.round(dmg * 0.00f);
		}
		return dmg;
	}

	// ===================== 致命势能（击杀后下次动作瞬发） =====================

	/** 敌人被英雄击杀后：按概率挂1回合致命势能增益 */
	public static void onMobSlain( Hero hero, Object cause, Char mob ){
		if (cause == hero
				&& hero.hasTalent(Talent.LETHAL_MOMENTUM)
				&& Random.Float() < 0.34f + 0.33f * hero.pointsInTalent(Talent.LETHAL_MOMENTUM)){
			Buff.affect(hero, Talent.LethalMomentumTracker.class, 1f);
		}
	}

	/** 若存在致命势能增益则消耗它（返回true表示本次攻击/投掷不耗回合） */
	public static boolean consumeLethalMomentum( Hero hero ){
		if (hero.buff(Talent.LethalMomentumTracker.class) != null){
			hero.buff(Talent.LethalMomentumTracker.class).detach();
			return true;
		}
		return false;
	}

	// ===================== 简易投掷物（非投掷武器丢中致盲） =====================

	/** 投掷物命中敌人时：简易投掷物天赋致盲（50回合冷却） */
	public static void onThrownItemHit( Hero hero, Item thrown, Char enemy ){
		if (enemy == null) return;
		if (hero.hasTalent(Talent.IMPROVISED_PROJECTILES)
				&& !(thrown instanceof MissileWeapon)
				&& hero.buff(Talent.ImprovisedProjectileCooldown.class) == null){
			if (enemy.alignment != hero.alignment){
				Sample.INSTANCE.play(Assets.Sounds.HIT);
				Buff.affect(enemy, Blindness.class, 1f + hero.pointsInTalent(Talent.IMPROVISED_PROJECTILES));
				Buff.affect(hero, Talent.ImprovisedProjectileCooldown.class, 50f);
			}
		}
	}

	// ===================== 角斗士·强化连击（招架保留） =====================

	/** 强化连击：连击数≥9且天赋+2以上时招架标记不消失 */
	public static boolean parryPersists( Hero hero, int comboCount ){
		return comboCount >= 9 && hero.pointsInTalent(Talent.ENHANCED_COMBO) >= 2;
	}

	// ===================== 坚守（HOLD_FAST）副护甲加成 =====================

	/** 坚守：副护甲贡献的闪避上限 */
	public static float secondArmorEvasionCap( Hero hero, int tier ){
		int talent = hero.pointsInTalent(Talent.HOLD_FAST);
		return (tier + talent) * (1 + talent) / 2F;
	}

	/** 坚守：副护甲贡献的格挡（DR）上限 */
	public static int secondArmorDRCap( Hero hero, int tier ){
		return tier * (1 + hero.pointsInTalent(Talent.HOLD_FAST));
	}

	/**
	 * 装备新护甲时是否走“旧护甲逻辑”（直接替换主护甲）。
	 * 没有坚守天赋、或新护甲阶数不高于现主护甲时沿用旧逻辑。
	 */
	public static boolean useLegacyArmorEquip( Hero hero, int newTier ){
		return hero.belongings.armor().tier() <= newTier || !hero.hasTalent(Talent.HOLD_FAST);
	}

	// ===================== 坚韧意志（IRON_WILL）战士护盾 =====================

	/** 坚韧意志：战士护盾最大层数 += 天赋点 */
	public static int warriorShieldBonus( Hero hero ){
		return hero.pointsInTalent(Talent.IRON_WILL);
	}

	/** 蜕变（非战士）点出坚韧意志时的战士护盾上限；不满足返回-1 */
	public static int metamorphIronWillShield( Hero hero ){
		if (hero.heroClass != HeroClass.WARRIOR && hero.hasTalent(Talent.IRON_WILL)){
			return hero.pointsInTalent(Talent.IRON_WILL);
		}
		return -1;
	}

	// ===================== 符文转移（RUNIC_TRANSFERENCE）刻印 =====================

	/** 从护甲上拆下列痕时，刻印是否随列痕保留（否则刻印被清除） */
	public static boolean sealKeepsGlyphOnDetach( Hero hero, boolean glyphCommonOrUncommon ){
		if (hero.hasTalentA(Talent.RUNIC_TRANSFERENCE) && glyphCommonOrUncommon){
			//+0时检测天赋、携带的刻印稀有度
			return true;
		} else if (hero.pointsInTalent(Talent.RUNIC_TRANSFERENCE) >= 1){
			//+1时直接携带
			return true;
		}
		//没有天赋时直接令袖章刻印清除
		return false;
	}

	/** 能否把当前刻印转移到另一件护甲 */
	public static boolean canTransferGlyph( Hero hero, boolean hasGlyph, boolean glyphCommonOrUncommon ){
		if (hero.hasTalentA(Talent.RUNIC_TRANSFERENCE)){
			if (!hasGlyph){
				return false;
			} else if (hero.pointsInTalent(Talent.RUNIC_TRANSFERENCE) >= 1){
				return true;
			} else {
				return hero.pointsInTalent(Talent.RUNIC_TRANSFERENCE) == 0 && glyphCommonOrUncommon;
			}
		}
		return false;
	}

	/** 符文转移+2：允许直接抽取刻印 */
	public static boolean glyphExtractable( Hero hero ){
		return hero.pointsInTalent(Talent.RUNIC_TRANSFERENCE) == 2;
	}

	/** 非战士角色蜕变出符文转移时，护甲升级丢失刻印的起始强化等级加成 */
	public static int runicLossChanceBonus( Hero hero ){
		if (hero != null && hero.heroClass != HeroClass.WARRIOR && hero.hasTalentA(Talent.RUNIC_TRANSFERENCE)){
			return 1 + hero.pointsInTalent(Talent.RUNIC_TRANSFERENCE);
		}
		return 0;
	}

	// ===================== 附魔触发率（狂战·怒能催化 / 冲击波·冲击波痕） =====================

	/** 战士系附魔额外触发率：怒能催化（基于怒气）+ 冲击波痕T4+4 */
	public static float enchantProcChanceBonus( Hero hero ){
		float bonus = 0f;
		if (hero.hasTalent(Talent.ENRAGED_CATALYST)){
			Berserk rage = hero.buff(Berserk.class);
			if (rage != null) {
				bonus += (rage.rageAmount() * 0.15f) * hero.pointsInTalent(Talent.ENRAGED_CATALYST);
			}
		}
		if (hero.buff(Talent.StrikingWaveTracker.class) != null
				&& hero.pointsInTalent(Talent.STRIKING_WAVE) == 4){
			bonus += 0.2f;
		}
		return bonus;
	}

	// ===================== 狂战（BERSERKING_STAMINA / ENDLESS_RAGE） =====================

	/** 狂战耐力：狂暴恢复时间提前的回合数 */
	public static float berserkingStaminaRecoveryDelay( Hero hero ){
		return hero.pointsInTalent(Talent.BERSERKING_STAMINA) / 3f;
	}

	/** 狂战耐力：触发狂暴时护盾量乘数 */
	public static float berserkingStaminaShieldMultiplier( Hero hero ){
		return 1f + hero.pointsInTalent(Talent.BERSERKING_STAMINA) / 4f;
	}

	/** 无尽怒火：每回合衰减缓冲（天赋点/2） */
	public static int endlessRagePowerLossBuffer( Hero hero ){
		return hero.pointsInTalent(Talent.ENDLESS_RAGE) / 2;
	}

	/** 无尽怒火：怒气上限乘数（1+0.1/级） */
	public static float endlessRageMaxPower( Hero hero ){
		return 1f + 0.1f * hero.pointsInTalent(Talent.ENDLESS_RAGE);
	}

	// ===================== 角斗士连击（CLEAVE / ENHANCED_COMBO / LETHAL_DEFENSE） =====================

	/** 横扫：击杀后连击计时额外保留的回合数 */
	public static float cleaveExtraComboTime( Hero hero ){
		return 15f * hero.pointsInTalent(Talent.CLEAVE);
	}

	/** 强化连击：7连击以上且天赋≥1时击退额外+1并附加眩晕 */
	public static boolean enhancedComboKnockback( Hero hero, int comboCount ){
		return comboCount >= 7 && hero.pointsInTalent(Talent.ENHANCED_COMBO) >= 1;
	}

	/** 致命防御：击杀回盾占战士护盾上限的比例（无天赋为0） */
	public static float lethalDefenseShieldFactor( Hero hero ){
		return hero.hasTalent(Talent.LETHAL_DEFENSE) ? hero.pointsInTalent(Talent.LETHAL_DEFENSE) / 3f : 0f;
	}

	/** 强化连击：天赋≥3时允许在 1+连击数/3 距离内继续连击 */
	public static boolean enhancedComboRangedAttack( Hero hero, int comboCount, int distance ){
		return hero.pointsInTalent(Talent.ENHANCED_COMBO) >= 3
				&& distance <= 1 + comboCount / 3;
	}

	/** 坚守buff描述里的格挡加成展示值（2*天赋点） */
	public static int holdFastDisplayBonus( Hero hero ){
		return 2 * hero.pointsInTalent(Talent.HOLD_FAST);
	}

	// ===================== T4 坚忍（Endure：SHRUG_IT_OFF / SUSTAINED_RETRIBUTION / EVEN_THE_ODDS） =====================

	/** 一笑置之：坚忍期间承伤上限系数（0.67^天赋点） */
	public static double shrugItOffDamageFactor( Hero hero ){
		return Math.pow(0.67f, hero.pointsInTalent(Talent.SHRUG_IT_OFF));
	}

	/** 持续反击：反击伤害乘数 */
	public static float sustainedRetributionDamageMultiplier( Hero hero ){
		return 1f + 0.15f * hero.pointsInTalent(Talent.SUSTAINED_RETRIBUTION);
	}

	/** 势均力敌：每多一个附近敌人的伤害乘数增量（返回整体乘数） */
	public static float evenTheOddsMultiplier( Hero hero, int nearbyEnemies ){
		return 1f + nearbyEnemies * 0.05f * hero.pointsInTalent(Talent.EVEN_THE_ODDS);
	}

	/** 持续反击：反击可保留的命中次数 */
	public static int sustainedRetributionHits( Hero hero ){
		return 1 + hero.pointsInTalent(Talent.SUSTAINED_RETRIBUTION);
	}

	// ===================== T4 英勇跳斩（HeroicLeap：BODY_SLAM / IMPACT_WAVE / DOUBLE_JUMP） =====================

	/** 二段跳：强化期间技能耗能乘数（0.84^天赋点） */
	public static double doubleJumpChargeFactor( Hero hero ){
		return Math.pow(0.84f, hero.pointsInTalent(Talent.DOUBLE_JUMP));
	}

	public static boolean hasBodySlam( Hero hero ){
		return hero.hasTalent(Talent.BODY_SLAM);
	}

	/** 巨力撞击：落点周围敌人受到的伤害（格挡roll*0.25*天赋点） */
	public static int bodySlamDamage( Hero hero ){
		return Math.round(hero.drRoll() * 0.25f * hero.pointsInTalent(Talent.BODY_SLAM));
	}

	/** 冲击波：击退力度（1+天赋点），无天赋返回0 */
	public static int impactWaveStrength( Hero hero ){
		return hero.hasTalent(Talent.IMPACT_WAVE) ? 1 + hero.pointsInTalent(Talent.IMPACT_WAVE) : 0;
	}

	/** 冲击波：1/4概率每点附加易伤 */
	public static boolean rollImpactWaveVulnerable( Hero hero ){
		return Random.Int(4) < hero.pointsInTalent(Talent.IMPACT_WAVE);
	}

	public static boolean hasDoubleJump( Hero hero ){
		return hero.hasTalent(Talent.DOUBLE_JUMP);
	}

	// ===================== T4 冲击波（Shockwave：EXPANDING_WAVE / STRIKING_WAVE / SHOCK_FORCE） =====================

	/** 扩散波：射程/锥角加成（天赋点） */
	public static int expandingWaveBonus( Hero hero ){
		return hero.pointsInTalent(Talent.EXPANDING_WAVE);
	}

	/** 震荡之力：伤害乘数 */
	public static float shockForceDamageMultiplier( Hero hero ){
		return 1f + 0.2f * hero.pointsInTalent(Talent.SHOCK_FORCE);
	}

	/** 冲击波痕满级（+4） */
	public static boolean strikingWaveMaxed( Hero hero ){
		return hero.pointsInTalent(Talent.STRIKING_WAVE) == 4;
	}

	/** 冲击波痕：触发武器proc的检定（30%/点） */
	public static boolean rollStrikingWave( Hero hero ){
		return Random.Int(10) < 3 * hero.pointsInTalent(Talent.STRIKING_WAVE);
	}

	/** 震荡之力：麻痹检定（1/4每点，否则残废） */
	public static boolean rollShockForceParalysis( Hero hero ){
		return Random.Int(4) < hero.pointsInTalent(Talent.SHOCK_FORCE);
	}
}
