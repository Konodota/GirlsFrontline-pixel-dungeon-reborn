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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 女猎（隼）角色天赋池
 *
 * 把原本散落在 Talent / Hero / Level / Dungeon / Weapon / MissileWeapon 等中的女猎天赋效果
 * 逻辑按“角色”维度集中到本类，并按生命周期钩子组织。调用方只需在各钩子里委托本类的对应
 * 静态方法，新增女猎天赋也只需改本类（外加 Talent 枚举注册）。
 *
 * 刻意保留在 Talent.java 中、不在本类的内容：
 * - 天赋枚举常量（NATURES_BOUNTY / FOLLOWUP_STRIKE / FARSIGHT …）与天赋表注册；
 * - 会被序列化进存档的 tracker/冷却 buff 内部类（NatureBerriesAvailable、
 *   FollowupStrikeTracker、RejuvenatingStepsCooldown/Furrow、SeerShotCooldown、
 *   SpiritBladesTracker）——移动会改变类全名导致旧存档无法反序列化，本类通过 Talent.Xxx 引用。
 */
public final class HuntressTalent {

	private HuntressTalent() {}

	// ===================== 生命周期钩子（供调用方统一委托） =====================

	/** 天赋升级后：自然馈赠（备浆果断）、强化感官/远视（更新视野） */
	public static void onTalentUpgraded( Hero hero, Talent talent ){
		if (talent == Talent.NATURES_BOUNTY){
			if ( hero.pointsInTalent(Talent.NATURES_BOUNTY) == 1) Buff.count(hero, Talent.NatureBerriesAvailable.class, 4);
			else                                           Buff.count(hero, Talent.NatureBerriesAvailable.class, 2);
		}
		else if (talent == Talent.HEIGHTENED_SENSES || talent == Talent.FARSIGHT){
			Dungeon.observe();
		}
	}

	/** 进食后：振奋一餐（加速） */
	public static void onFoodEaten( Hero hero ){
		if (hero.hasTalent(Talent.INVIGORATING_MEAL)) {
			//effectively 1/2 turns of haste
			Buff.prolong(hero, Haste.class, 0.67f + hero.pointsInTalent(Talent.INVIGORATING_MEAL));
		}
	}

	/** 鉴定速度乘数：生存主义者直觉（+0.75/级） */
	public static float itemIDSpeedFactor( Hero hero, float factor ){
		return factor * (1f + hero.pointsInTalent(Talent.SURVIVALISTS_INTUITION) * 0.75f);
	}

	/** 使用药水后：自然修复（周围生根、长草、高草） */
	public static void onPotionUsed( Hero hero, float mul, int pos ){
		if (!hero.hasTalent(Talent.RESTORED_NATURE)){
			return;
		}
		ArrayList<Integer> grassCells = new ArrayList<>();
		for (int i : PathFinder.NEIGHBOURS8){
			grassCells.add(pos+i);
		}
		Random.shuffle(grassCells);
		for (int cell : grassCells){
			Char ch = Actor.findChar(cell);
			if (ch != null && ch.alignment == Char.Alignment.ENEMY){
				Buff.affect(ch, Roots.class, (1f + hero.pointsInTalent(Talent.RESTORED_NATURE))*mul );
			}
			if (Dungeon.level.map[cell] == Terrain.EMPTY ||
					Dungeon.level.map[cell] == Terrain.EMBERS ||
					Dungeon.level.map[cell] == Terrain.EMPTY_DECO){
				Level.set(cell, Terrain.GRASS);
				GameScene.updateMap(cell);
			}
			CellEmitter.get(cell).burst(LeafParticle.LEVEL_SPECIFIC, 4);
		}
		if (hero.pointsInTalent(Talent.RESTORED_NATURE) == 1 && mul != 2){
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

	/** 攻击命中后：连击（投掷武器标记，标记存在时近战增伤并消耗标记） */
	public static int onAttackProc( Hero hero, Char enemy, int dmg ){
		if (hero.hasTalent(Talent.FOLLOWUP_STRIKE)) {
			if (hero.belongings.weapon() instanceof MissileWeapon) {
				Buff.affect(enemy, Talent.FollowupStrikeTracker.class);
			} else if (enemy.buff(Talent.FollowupStrikeTracker.class) != null){
				dmg += 1 + hero.pointsInTalent(Talent.FOLLOWUP_STRIKE);
				if (!(enemy instanceof Mob) || !((Mob) enemy).surprisedBy(hero)){
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
				}
				enemy.buff(Talent.FollowupStrikeTracker.class).detach();
			}
		}
		return dmg;
	}

	// ===================== 点射（POINT_BLANK）近战投掷命中 =====================

	/** 点射：近战距离使用投掷武器的命中系数（0.5+0.25/级） */
	public static float pointBlankAdjacentAccuracy( Hero hero ){
		return 0.5f + 0.25f * hero.pointsInTalent(Talent.POINT_BLANK);
	}

	// ===================== 自然之力（GROWING_POWER）移速 =====================

	/** 自然之力强化期间移速乘数（2+0.25/级），调用方应先确认 naturesPowerTracker 存在 */
	public static float naturesPowerSpeedMultiplier( Hero hero ){
		return 2f + 0.25f * hero.pointsInTalent(Talent.GROWING_POWER);
	}

	// ===================== 树肤（BARKSKIN）高草减伤 =====================

	/** 站在垄草（FURROWED_GRASS）上行动时获得树肤护盾 */
	public static void applyBarkskinOnFurrowedGrass( Hero hero, int pos ){
		if (hero.hasTalent(Talent.BARKSKIN) && Dungeon.level.map[pos] == Terrain.FURROWED_GRASS){
			Buff.affect(hero, Barkskin.class).set((hero.lvl * hero.pointsInTalent(Talent.BARKSKIN)) / 2, 1);
		}
	}

	// ===================== 幽魂之刃（SPIRIT_BLADES）T4 =====================

	/** 幽魂之刃：增益期间攻击附带灵弓伤害（30%/级，3*天赋点概率） */
	public static int spiritBladesAttack( Hero hero, Char enemy, int damage ){
		if (hero.buff(Talent.SpiritBladesTracker.class) != null
				&& Random.Int(10) < 3 * hero.pointsInTalent(Talent.SPIRIT_BLADES)){
			SpiritBow bow = hero.belongings.getItem(SpiritBow.class);
			if (bow != null) damage = bow.proc(hero, enemy, damage);
			hero.buff(Talent.SpiritBladesTracker.class).detach();
		}
		return damage;
	}

	/** 幽魂之刃+4：附魔触发率+0.1 */
	public static float spiritBladesEnchantBonus( Char attacker ){
		if (attacker instanceof Hero
				&& attacker.buff(Talent.SpiritBladesTracker.class) != null
				&& ((Hero) attacker).pointsInTalent(Talent.SPIRIT_BLADES) == 4){
			return 0.1f;
		}
		return 0f;
	}

	// ===================== 神射手·共享升级（SHARED_UPGRADES） =====================

	/** 共享升级：狙击标记持续时间额外增加武器等级的回合数 */
	public static int sharedUpgradesBonusTurns( Hero hero, int weaponLevel ){
		return hero.hasTalent(Talent.SHARED_UPGRADES) ? weaponLevel : 0;
	}

	// ===================== 恢复步伐（REJUVENATING_STEPS） =====================

	/** 获取经验时：垄沟计数按经验比例衰减，归零后移除 */
	public static void onGainExpFurrow( Hero hero, float percent ){
		if (hero.buff(Talent.RejuvenatingStepsFurrow.class) != null){
			hero.buff(Talent.RejuvenatingStepsFurrow.class).countDown(percent * 200f);
			if (hero.buff(Talent.RejuvenatingStepsFurrow.class).count() <= 0){
				hero.buff(Talent.RejuvenatingStepsFurrow.class).detach();
			}
		}
	}

	/** 踩过草地/灰烬时的恢复步伐地形转化与冷却逻辑 */
	public static void onGrassTrampled( Hero hero, int pos ){
		if (hero == null) return;
		if ( (Dungeon.level.map[pos] == Terrain.GRASS || Dungeon.level.map[pos] == Terrain.EMBERS)
				&& hero.hasTalent(Talent.REJUVENATING_STEPS)
				&& hero.buff(Talent.RejuvenatingStepsCooldown.class) == null){

			if (hero.buff(LockedFloor.class) != null && !hero.buff(LockedFloor.class).regenOn()){
				Level.set(pos, Terrain.FURROWED_GRASS);
			} else if (hero.buff(Talent.RejuvenatingStepsFurrow.class) != null && hero.buff(Talent.RejuvenatingStepsFurrow.class).count() >= 200) {
				Level.set(pos, Terrain.FURROWED_GRASS);
			} else {
				Level.set(pos, Terrain.HIGH_GRASS);
				Buff.count(hero, Talent.RejuvenatingStepsFurrow.class, 3 - hero.pointsInTalent(Talent.REJUVENATING_STEPS));
			}
			GameScene.updateMap(pos);
			Buff.affect(hero, Talent.RejuvenatingStepsCooldown.class, 15f - 5f * hero.pointsInTalent(Talent.REJUVENATING_STEPS));
		}
	}

	// ===================== 视野（FARSIGHT / HEIGHTENED_SENSES / EAGLE_EYE） =====================

	/** 远视：视野距离乘数（1+0.25/级） */
	public static float farsightMultiplier( Hero hero ){
		return 1f + 0.25f * hero.pointsInTalent(Talent.FARSIGHT);
	}

	/** 强化感官的心灵视野范围（1+天赋点），未拥有返回0 */
	public static int heightenedSensesRange( Hero hero ){
		if (hero.hasTalent(Talent.HEIGHTENED_SENSES)){
			return 1 + hero.pointsInTalent(Talent.HEIGHTENED_SENSES);
		}
		return 0;
	}

	/** 鹰眼+3以上时灵鹰额外揭示范围（1+(天赋点-2)），否则返回0 */
	public static int hawkMindRange( Hero hero ){
		if (hero.pointsInTalent(Talent.EAGLE_EYE) >= 3){
			return 1 + (hero.pointsInTalent(Talent.EAGLE_EYE) - 2);
		}
		return 0;
	}

	// ===================== 共享附魔（SHARED_ENCHANTMENT） =====================

	/** 共享附魔：本次投掷/命中是否触发共享灵弓附魔（1/3概率每点） */
	public static boolean rollSharedEnchantment( Hero hero ){
		return Random.Int(3) < hero.pointsInTalent(Talent.SHARED_ENCHANTMENT);
	}

	// ===================== 预知射击（SEER_SHOT） =====================

	/** 非女猎角色蜕变出预知射击时，投掷落空揭示落点 */
	public static boolean appliesSeerShot( Hero hero ){
		return hero.heroClass != HeroClass.HUNTRESS && hero.hasTalent(Talent.SEER_SHOT);
	}

	/** 预知射击：在落点挂揭示区域并进入冷却 */
	public static void applySeerShot( Hero hero, int cell ){
		RevealedArea a = Buff.affect(hero, RevealedArea.class, 5 * hero.pointsInTalent(Talent.SEER_SHOT));
		a.depth = Dungeon.depth;
		a.pos = cell;
		Buff.affect(hero, Talent.SeerShotCooldown.class, 20f);
	}

	// ===================== 耐久弹药（DURABLE_PROJECTILES） =====================

	/** 耐久弹药：投掷武器耐久系数（1.5/1.75） */
	public static float durableProjectilesFactor( Hero hero ){
		if (hero.hasTalent(Talent.DURABLE_PROJECTILES)){
			return 1.25f + 0.25f * hero.pointsInTalent(Talent.DURABLE_PROJECTILES);
		}
		return 1f;
	}

	// ===================== 自然援助（NATURES_AID） / 树肤上限 =====================

	/** 自然援助：英雄视野内植物被触发时获得树肤（2点护甲，持续3/5回合） */
	public static void naturesAidOnPlant( Hero hero, int pos ){
		if (Dungeon.level.heroFOV[pos] && hero.hasTalent(Talent.NATURES_AID)){
			Buff.affect(hero, Barkskin.class).set(2, 1 + 2 * hero.pointsInTalent(Talent.NATURES_AID));
		}
	}

	/** 树肤护盾上限（等级*天赋点/2），调用方再与基础下限取大 */
	public static float barkskinMaxShield( Hero hero ){
		return hero.lvl * (float) hero.pointsInTalent(Talent.BARKSKIN) / 2f;
	}

	// ===================== 自然之力（NATURES_WRATH / WILD_MOMENTUM / GROWING_POWER） =====================

	/** 自然之怒：灵箭命中后1/12概率每点生成有害植物 */
	public static boolean rollNaturesWrath( Hero hero ){
		return Random.Int(12) < hero.pointsInTalent(Talent.NATURES_WRATH);
	}

	/** 野性动能：自然之力期间击杀延长的回合数 */
	public static int wildMomentumTurns( Hero hero ){
		return hero.pointsInTalent(Talent.WILD_MOMENTUM);
	}

	/** 成长之力：灵弓在自然之力期间的攻速加成（(8+天赋点)/24） */
	public static float growingPowerBoltSpeedBonus( Hero hero ){
		return (8 + hero.pointsInTalent(Talent.GROWING_POWER)) / 24f;
	}

	// ===================== 预知射击（SEER_SHOT，女猎原生施放路径） =====================

	/** 女猎本体施放灵弓时，预知射击是否可用（拥有且不在冷却） */
	public static boolean seerShotReady( Hero hero ){
		return hero.hasTalent(Talent.SEER_SHOT)
				&& hero.buff(Talent.SeerShotCooldown.class) == null;
	}

	/** 预知射击buff图标满值（5*天赋点） */
	public static float seerShotMaxDuration( Hero hero ){
		return 5f * hero.pointsInTalent(Talent.SEER_SHOT);
	}

	// ===================== 灵鹰（SpiritHawk：EAGLE_EYE / SWIFT_SPIRIT / GO_FOR_THE_EYES） =====================

	/** 鹰眼：灵鹰视野加成（天赋点，调用方gate到6~8） */
	public static int eagleEyeViewBonus( Hero hero ){
		return hero.pointsInTalent(Talent.EAGLE_EYE);
	}

	/** 疾风之魂：灵鹰速度加成（天赋点/2） */
	public static float swiftSpiritSpeedBonus( Hero hero ){
		return hero.pointsInTalent(Talent.SWIFT_SPIRIT) / 2f;
	}

	/** 疾风之魂：本次受击是否由灵鹰闪避（每回合 1+天赋点 次） */
	public static boolean hawkWantsDodge( Hero hero, int dodgesUsed ){
		return hero.hasTalent(Talent.SWIFT_SPIRIT)
				&& dodgesUsed < 1 + hero.pointsInTalent(Talent.SWIFT_SPIRIT);
	}

	public static int swiftSpiritPoints( Hero hero ){
		return hero.pointsInTalent(Talent.SWIFT_SPIRIT);
	}

	/** 专戳眼睛：灵鹰命中致盲回合数（2*天赋点，0表示无天赋） */
	public static int goForTheEyesBlindTurns( Hero hero ){
		return 2 * hero.pointsInTalent(Talent.GO_FOR_THE_EYES);
	}

	// ===================== T4 幽魂之刃技能（SpectralBlades：FAN_OF_BLADES / PROJECTING_BLADES / SPIRIT_BLADES） =====================

	/** 投射之刃：额外射程（2*天赋点） */
	public static int projectingBladesRange( Hero hero ){
		return 2 * hero.pointsInTalent(Talent.PROJECTING_BLADES);
	}

	public static boolean hasFanOfBlades( Hero hero ){
		return hero.hasTalent(Talent.FAN_OF_BLADES);
	}

	/** 刀锋扇形：锥角（30*天赋点） */
	public static int fanOfBladesConeDegrees( Hero hero ){
		return 30 * hero.pointsInTalent(Talent.FAN_OF_BLADES);
	}

	/** 刀锋扇形：除主目标外最多命中数（天赋点） */
	public static int fanOfBladesMaxExtraTargets( Hero hero ){
		return hero.pointsInTalent(Talent.FAN_OF_BLADES);
	}

	/** 投射之刃：命中精度乘数增量（0.25*天赋点） */
	public static float projectingBladesAccuracyBonus( Hero hero ){
		return 0.25f * hero.pointsInTalent(Talent.PROJECTING_BLADES);
	}

	/** 幽魂之刃：技能施放后挂上0回合追踪buff */
	public static void spiritBladesTrackerOn( Hero hero ){
		if (hero.hasTalent(Talent.SPIRIT_BLADES)){
			Buff.affect(hero, Talent.SpiritBladesTracker.class, 0f);
		}
	}

	// ===================== T1/T3 杂项（NATURES_BOUNTY / INVIGORATING_MEAL / DURABLE_TIPS / SHIELDING_DEW） =====================

	/** 自然馈赠：浆果断档目标层数偏移（2*天赋点） */
	public static int naturesBountyFloorBonus( Hero hero ){
		return 2 * hero.pointsInTalent(Talent.NATURES_BOUNTY);
	}

	/** 振奋一餐：是否可瞬间吃下浆果（无进食耗时） */
	public static boolean invigoratingMealInstant( Hero hero ){
		return hero.hasTalent(Talent.INVIGORATING_MEAL);
	}

	/** 耐用镖尖：投掷耐久消耗除数（1+天赋点） */
	public static int durableTipsDurabilityDivisor( Hero hero ){
		return 1 + hero.pointsInTalent(Talent.DURABLE_TIPS);
	}

	/** 守护露珠：由治疗溢出转化的护盾上限（最大生命值的20%*天赋点，0表示无天赋） */
	public static int shieldingDewMaxShield( Hero hero ){
		return Math.round(hero.HT * 0.2f * hero.pointsInTalent(Talent.SHIELDING_DEW));
	}

	/** 守护露珠：每滴露水中可转化为护盾的生命百分比（20%*天赋点） */
	public static float shieldingDewPercentFactor( Hero hero ){
		return 0.2f * hero.pointsInTalent(Talent.SHIELDING_DEW);
	}

	// ===================== 神射手·共享升级（狙击特殊射击伤害） =====================

	/** 共享升级：狙击特殊射击的额外伤害比例（武器等级*天赋点/10） */
	public static float sharedUpgradesSpecialDamage( Hero hero, int level ){
		return level * (float) hero.pointsInTalent(Talent.SHARED_UPGRADES) / 10f;
	}
}
