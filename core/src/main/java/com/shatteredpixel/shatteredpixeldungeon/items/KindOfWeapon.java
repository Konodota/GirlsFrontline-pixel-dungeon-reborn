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

package com.shatteredpixel.shatteredpixeldungeon.items;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EquipmentBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GunSwap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.GSH18Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

abstract public class KindOfWeapon extends EquipableItem {

	protected static final float TIME_TO_EQUIP = 1f;

	protected String hitSound = Assets.Sounds.HIT;
	protected float hitSoundPitch = 1f;

	//武器枪种标签，供角色天赋等系统按武器类别进行判定
	public enum Tag {
		NONE, HG,	//手枪（Handgun）
		RF	        //步枪（Rifle）
	}
	protected Tag tag = Tag.NONE;
	public boolean hasTag( Tag tag ){
		return this.tag == tag;
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		return hero.belongings.weapon() == this || ownerBuff instanceof EquipmentBuff && ownerBuff.target == hero;
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_EQUIP )) {
			GunSwap swap = hero.buff(GunSwap.class);
			if (swap != null) {
				swapWeapon(hero, swap);
				return;
			}
		}
		super.execute( hero, action );
	}
	private void swapWeapon( Hero hero, GunSwap swap ){
		final String action = AC_EQUIP;
		KindOfWeapon other = swap.getEquipment(KindOfWeapon.class);
		KindOfWeapon main = hero.belongings.weapon;
		if (other == null) {
			if (main == null || main.notWorking(hero))
				super.execute(hero, action);
			else if (main.hasTag(Tag.HG)) {
				swap.setEquipment(main);
				hero.belongings.weapon = null;
				super.execute(hero, action);
			}
			else if (hasTag(Tag.HG)) {
				swap.setEquipment((EquipableItem) detachAll(hero.belongings.backpack));
				afterEquip(hero);
			}
			else
				super.execute(hero, action);
		}
		else {
			if (main == null || main.notWorking(hero)) {
				//有一方为HG那不产生限制，走即可常规super即可
				if (!hasTag(Tag.HG) && !other.hasTag(Tag.HG)) {
					//均不是HG，那就将主副手交换再走super
					hero.belongings.weapon = other;
					swap.setEquipment(main);
				}
				super.execute(hero, action);
			}
			//双非空
			else {
				//留空收集
				detachAll(hero.belongings.backpack);
				if (hasTag(Tag.HG)) {
					if (main.hasTag(Tag.HG) && main.doUnequip_copy(hero, true, false)) {
						//优先交换主手HG
						hero.belongings.weapon = this;
						afterEquip(hero);
						return;
					}
					else if (other.doUnequip_copy(hero, true, false)) {
						//主手非HG或者主手取下失败时，尝试副手
						swap.setEquipment(this);
						afterEquip(hero);
						return;
					}
					else if (main.doUnequip_copy(hero, true, false)) {
						//副手也失败时，替换可能的可取下的主手非HG
						hero.belongings.weapon = this;
						afterEquip(hero);
						return;
					}
				}
				else {
					if (other.hasTag(Tag.HG) && main.doUnequip_copy(hero, true, false)) {
						//进非HG的槽，双HG时优先进主手
						hero.belongings.weapon = this;
						afterEquip(hero);
						return;
					}
					else if (main.hasTag(Tag.HG) && other.doUnequip_copy(hero, true, false)) {
						//进副手的分支，顺手将这个非HG挪到主手
						swap.setEquipment(main);
						hero.belongings.weapon = this;
						afterEquip(hero);
						return;
					}
				}
				//装备失败，收回去
				collect(hero.belongings.backpack);
			}
		}
	}
	@Override
	public boolean doEquip( Hero hero ) {

		detachAll( hero.belongings.backpack );

		if (hero.belongings.weapon == null
				|| hero.belongings.weapon.doUnequip( hero, true )) {

			hero.belongings.weapon = this;
			afterEquip(hero);
			return true;

		} else {

			collect( hero.belongings.backpack );
			Tracker(hero);
			return false;
		}
	}
	private void afterEquip( Hero hero ){
		activate( hero );
		Tracker(hero);
		Talent.onItemEquipped(hero, this);
		ActionIndicator.updateIcon();
		updateQuickslot();

		cursedKnown = true;
		if (cursed) {
			equipCursed( hero );
			GLog.n( Messages.get(KindOfWeapon.class, "equip_cursed") );
		}
		hero.spendAndNext( TIME_TO_EQUIP );
	}
    @Override
    public boolean unEquipable(Hero hero){
        // 医生直觉 +2级允许取下被诅咒的武器（实现见 GSH18Talent）
        return  super.unEquipable(hero) || GSH18Talent.canUnequipWeapon(hero);
    }
	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {

			hero.belongings.weapon = null;
			return true;

		} else {

			return false;

		}
	}

	public int min(){
		return min(buffedLvl());
	}

	public int max(){
		return max(buffedLvl());
	}

	abstract public int min(int lvl);
	abstract public int max(int lvl);

	public int damageRoll( Char owner ) {
        int dmg = Random.NormalIntRange( min(), max() );
        if (owner instanceof Hero && hero.enemy instanceof Mob) {
			Mob enemy = (Mob) hero.enemy;
            if (enemy.surprisedBy(hero)) {
                if (hero.hasTalent(Talent.Type56Two_Damage)) {
                    int diff = max() - min();
                    dmg = Random.NormalIntRange(min() + Math.round(0.2f * hero.pointsInTalent(Talent.Type56Two_Damage) * diff), max());
                }
            }
        }
		return dmg;
	}
	public float mulByDelay( Char owner ){
		float delay = delayFactor( owner );
		if (delay > 2F)
			return delay / 2F;
		if (delay < 0.5F)
			return delay * 2F;
		return 1F;
	}
	//供丹德莱傀儡（Puppet）使用：攻速倍率钳制为 0.5~2
	public float puppetMulByDelay( Char owner ){
		float delay = delayFactor( owner );
		if (delay > 2F)
			return 2F;
		if (delay < 0.5F)
			return 0.5F;
		return 1F;
	}
	public float accuracyFactor( Char owner ) {
		return 1f;
	}

	public float delayFactor(Char owner ) {
		return 1f;
	}

	public int STRNeed(int extraSTR){
		return extraSTR * (extraSTR + 1) / 2;
	}
	public int reachFactor( Char owner ){
		return 1;
	}

	public boolean canReach( Char owner, int target){
		if (Dungeon.level.distance( owner.pos, target ) > reachFactor(owner)){
			return false;
		} else {
			boolean[] passable = BArray.not(Dungeon.level.solid, null);
			for (Char ch : Actor.chars()) {
				if (ch != owner) passable[ch.pos] = false;
			}

			PathFinder.buildDistanceMap(target, passable, reachFactor(owner));

			return PathFinder.distance[owner.pos] <= reachFactor(owner);
		}
	}

	public int defenseFactor( Char owner ) {
		return 0;
	}

	public int proc( Char attacker, Char defender, int damage ) {
		return damage;
	}

	public void hitSound( float pitch ){
		Sample.INSTANCE.play(hitSound, 1, pitch * hitSoundPitch);
	}

}
