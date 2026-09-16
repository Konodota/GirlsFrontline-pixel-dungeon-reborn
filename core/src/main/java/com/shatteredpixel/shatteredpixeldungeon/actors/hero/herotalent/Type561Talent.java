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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TalentSecondSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.RedBook;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ShootGun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 56-1式 角色天赋池
 *
 * 把原本散落在 Talent / Hero / Weapon / Armor / Dungeon / Level 中的 56-1 天赋效果逻辑
 * 按"角色"维度集中到本类，并按生命周期钩子组织。Talent/Hero 等调用方只需在各钩子里
 * 委托本类的对应静态方法，新增 56-1 天赋也只需改本类（外加 Talent 枚举注册）。
 *
 * 刻意保留在 Talent.java 中、不在本类的内容：
 * - 天赋枚举常量（Type56One_* / Type56Two_* / Type56Three_* / Type56Four* / Type56_43*）
 *   与天赋表注册——那是天赋注册表；
 * - 会被序列化进存档的 tracker/冷却 buff 内部类（Type56BookTracker、JIEFANGCI_Tracker、
 *   ZongziDropped）——移动会改变类全名导致旧存档无法反序列化，本类通过 Talent.Xxx 引用。
 */
public final class Type561Talent {

	private Type561Talent() {}

	// ===================== 生命周期钩子（供调用方统一委托） =====================

	/** 天赋升级后：吃货雷达掉落粽子、百战老兵标记诅咒、袖珍读本激活语录书、战地侦察赋予视野 */
	public static void onTalentUpgraded( Hero hero, Talent talent ){
		Item workingItem;
		if (talent == Talent.Type56One_FOOD){
			if ( hero.pointsInTalent(Talent.Type56One_FOOD) == 1) Buff.count(hero, Talent.ZongziDropped.class, 4);
			else                                           Buff.count(hero, Talent.ZongziDropped.class, 2);
		}
		else if (talent == Talent.Type56One_Identify && hero.pointsInTalent(Talent.Type56One_Identify) == 2){
			for (Item item : Dungeon.hero.belongings)
				if (item instanceof MeleeWeapon || item instanceof Armor){
					item.cursedKnown = true;
					Item.updateQuickslot();
				}
		}
		else if (talent == Talent.Type56Three_Book && hero.pointsInTalent(Talent.Type56Three_Book) == 1){
			if ((workingItem = hero.belongings.getItem(RedBook.class)) != null)
				((RedBook) workingItem).activate(Dungeon.hero);
		}
		else if (talent == Talent.Type56Two_Sight){
			Buff.affect( Dungeon.hero, TalentSecondSight.class).Set(Dungeon.levelId, 0);
			if (Dungeon.hero.pointsInTalent(Talent.Type56Two_Sight) == 1){
				Dungeon.level.FirstSight = false;
				Buff.affect(Dungeon.hero, MindVision.class, 3);
			}else if (Dungeon.hero.pointsInTalent(Talent.Type56Two_Sight) == 2) {
				Dungeon.level.SecondSight = false;
				Buff.affect(Dungeon.hero, MindVision.class, 3);
			}
		}
	}

	/** 进食后：饭饱为钢（旧版T2）回血 */
	public static void onFoodEaten( Hero hero ){
		if (Dungeon.hero.hasTalent(Talent.Type56_21V2)){
			if (Dungeon.hero.HP < Dungeon.hero.HT) {
				int add = (int) (Dungeon.hero.HT*(0.02*Dungeon.hero.pointsInTalent(Talent.Type56_21V2)+0.01F));
				Dungeon.hero.HP = Math.min( Dungeon.hero.HP + add, Dungeon.hero.HT );
				Dungeon.hero.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 1 );
			}
		}
	}

	/** 鉴定速度乘数：百战老兵 / 战场老兵（旧版） */
	public static float itemIDSpeedFactor( Hero hero, Item item ){
		float factor = 1f;
		if (item instanceof MeleeWeapon || item instanceof Armor){
			factor *= 1.25f + hero.pointsInTalent(Talent.Type56One_Identify) * 0.75f;
		}
		if (item instanceof MeleeWeapon){
			factor *= 1F + 2 * hero.pointsInTalent(Talent.OLD_SOLDIER);
		}
		return factor;
	}

	/** 装备物品后：战场老兵（旧版）立即鉴定武器 */
	public static void onItemEquipped( Hero hero, Item item ){
		if (hero.pointsInTalent(Talent.OLD_SOLDIER) == 2 && item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)
			item.identify();
	}

	/** 获取物品后：百战老兵标记诅咒 / 战场老兵（旧版）标记诅咒 */
	public static void onItemCollected( Hero hero, Item item ){
		if(hero.pointsInTalent(Talent.Type56One_Identify) == 2 && (item instanceof MeleeWeapon || item instanceof Armor)) {
			item.cursedKnown = true;
			Item.updateQuickslot();
		}
		if (hero.pointsInTalent(Talent.OLD_SOLDIER) == 2 && item instanceof MeleeWeapon) {
			item.cursedKnown = true;
			Item.updateQuickslot();
		}
	}

	/** 攻击命中后：轻装简从（榴弹CD增伤）、胆敢向我还击（恐惧）、解放刺（近战增伤+残废）、侦查部队（伏击增伤） */
	public static int onAttackProc( Hero hero, Char enemy, int dmg ){
		// T1 轻装简从：榴弹在CD或不在背包时伤害增加
		if(hero.hasTalent(Talent.Type56One_Damage)){
			ShootGun Gun = hero.belongings.getItem(ShootGun.class);
			boolean add;
			if (Gun == null && !(hero.belongings.weapon instanceof ShootGun))
				add = true;
			else
				add = ShootGun.cooldown;
			if (add)
				dmg += Random.IntRange(hero.pointsInTalent(Talent.Type56One_Damage)-1 , 2);
		}

		// 旧版T2 胆敢向我还击：概率令敌人恐惧
		if(hero.hasTalent(Talent.HOW_DARE_YOU)){
			float chance  =0.15f;
			float duration=5.01f;
			if(hero.pointsInTalent(Talent.HOW_DARE_YOU)>=2){
				float enemyHealthRate = (float) enemy.HP / (float) enemy.HT;
				chance = 0.1F * enemyHealthRate;
				duration += 10F * enemyHealthRate;
			}
			if(Random.Float() < chance)
				Buff.affect(enemy, Terror.class,duration);
		}

		// 旧版T2 解放刺：近战攻击相邻敌人时增伤+概率残废
		if(hero.hasTalent(Talent.JIEFANGCI) && Dungeon.level.adjacent(hero.pos,enemy.pos)){
			if (hero.belongings.weapon() instanceof ShootGun
					|| hero.buff(Talent.JIEFANGCI_Tracker.class) == null) {
				Buff.prolong(hero, Talent.JIEFANGCI_Tracker.class, 5F);
				dmg += 1 + 4 * hero.pointsInTalent(Talent.JIEFANGCI);
				float chance = 0.05F + 0.1F * hero.pointsInTalent(Talent.JIEFANGCI);
				if (Random.Float() < chance)
					Buff.affect(enemy, Cripple.class, 2F);
			}
		}

		// 旧版T3 侦查部队：伏击伤害增加
		if(hero.hasTalent(Talent.SEARCH_ARMY) && enemy instanceof Mob){
			if(enemy.paralysed>0)
				dmg = (int) (dmg * (1F + (0.4F / 3F) * hero.pointsInTalent(Talent.SEARCH_ARMY)));
			else if(((Mob) enemy).surprisedBy(hero))
				dmg = (int) (dmg * (1F + 0.1F * hero.pointsInTalent(Talent.SEARCH_ARMY)));
		}

		return dmg;
	}

	/** 被攻击后：夜战精英+2（旧版）令攻击者获得视野追踪 */
	public static int onDefenceProc( Hero hero, Char enemy, int dmg ){
		if(hero.pointsInTalent(Talent.NIGHT_EXPERT)>=2)
			Buff.append(enemy, com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight.CharAwareness.class,2f).charID = enemy.id();
		return dmg;
	}

	// ===================== 查询型钩子 =====================

	/** 命中乘数：知识的力量（读书后下次攻击精度提升），不满足返回1 */
	public static float accuracyMultiplier( Hero hero ){
		if (hero.buff(Talent.Type56BookTracker.class) != null) {
			return 1.1F + 0.2F * hero.pointsInTalent(Talent.Type56_14);
		}
		return 1f;
	}

	/** 攻击命中后：消耗"知识的力量"buff */
	public static void onAttackHit( Hero hero ){
		if (hero.buff(Talent.Type56BookTracker.class) != null) {
			hero.buff(Talent.Type56BookTracker.class).detach();
		}
	}

	/** 经验获取乘数：精英部队（旧版T3） */
	public static float expMultiplier( Hero hero ){
		if (hero.hasTalent(Talent.ELITE_ARMY)){
			return 1f + hero.pointsInTalent(Talent.ELITE_ARMY) * 2f / 3f;
		}
		return 1f;
	}

	/** 陷阱发现概率乘数：陷阱达人（旧版T2） */
	public static float trapDetectionMultiplier( Hero hero ){
		if (hero.hasTalent(Talent.TRAP_EXPERT)){
			float mul = 1.5F;
			if(hero.pointsInTalent(Talent.TRAP_EXPERT)>=2)
				mul *= 2F;
			return mul;
		}
		return 1f;
	}

	/** 视野距离加成：夜战精英（旧版T2）在黑暗层视野+1 */
	public static int viewDistanceBonus( Hero hero ){
		if (hero.hasTalent(Talent.NIGHT_EXPERT)
				&& (Dungeon.level.feeling == com.shatteredpixel.shatteredpixeldungeon.levels.Level.Feeling.DARK
				|| Dungeon.isChallenged(com.shatteredpixel.shatteredpixeldungeon.Challenges.DARKNESS)))
			return 1;
		return 0;
	}

	/** 武器有效等级加成：火线补给T4-2电解糖分（EquipLevelUp期间） */
	public static int weaponLevelBonus( Hero hero ){
		if (hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EquipLevelUp.class) != null
				&& hero.hasTalent(Talent.Type56FourTwoTwo)){
			return hero.pointsInTalent(Talent.Type56FourTwoTwo);
		}
		//无天赋时默认+1（EquipLevelUp基础效果）
		if (hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EquipLevelUp.class) != null){
			return 1;
		}
		return 0;
	}

	/** 护甲有效等级加成：火线补给T4-2电解糖分 + 饭饱为钢 + 饱腹护甲（旧版） */
	public static int armorLevelBonus( Hero hero, int baseLevel ){
		int level = baseLevel;
		if (hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EquipLevelUp.class) != null) {
			level += 1 + hero.pointsInTalent(Talent.Type56FourTwoTwo);
		}
		Hunger hunger = hero.buff(Hunger.class);
		if (hunger != null) {
			if (hero.hasTalent(Talent.Type56Two_Armor)) {
				if (hunger.full() >= 500 - 100 * hero.pointsInTalent(Talent.Type56Two_Armor))
					level += 1;
			}
			if (hero.hasTalent(Talent.Type56_22V2)) {
				if (hunger.isFull())
					level += hero.pointsInTalent(Talent.Type56_22V2);
			}
		}
		return level;
	}

	/** 战地侦察（T2）：进入新楼层时的视野逻辑 */
	public static void onLevelEnter(){
		if (Dungeon.hero!=null && Dungeon.level!=null){
			if(Dungeon.hero.hasTalent(Talent.Type56Two_Sight)) {
				TalentSecondSight Sec = Dungeon.hero.buff(TalentSecondSight.class);
				if (Sec==null){
					Buff.affect( Dungeon.hero, TalentSecondSight.class).Set(0, 0);
					Sec = Dungeon.hero.buff(TalentSecondSight.class);
					GLog.p(Messages.get(Type561Talent.class, "resight"));
				}
				if (Dungeon.level.FirstSight){
					Dungeon.level.FirstSight = false;
					Buff.affect(Dungeon.hero, MindVision.class, 3);
					if (Dungeon.hero.pointsInTalent(Talent.Type56Two_Sight) == 2) {
						Sec.Set(Dungeon.levelId, 25);
					}
				}
				if (Dungeon.hero.pointsInTalent(Talent.Type56Two_Sight) == 2
						&& Sec.EndCD(Dungeon.levelId) && Dungeon.level.SecondSight) {
					Dungeon.level.SecondSight = false;
					Buff.affect(Dungeon.hero, MindVision.class, 3);
				}
			}
		}
	}
}
