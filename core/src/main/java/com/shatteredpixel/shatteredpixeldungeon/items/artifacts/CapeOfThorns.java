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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CapeOfThorns extends Artifact {

	public static final String AC_ACTIVATE = "ACTIVATE";

	{
		image = ItemSpriteSheet.ARTIFACT_CAPE;

		levelCap = 10;

		charge = 0;
		chargeCap = 100;
		cooldown = 0;

		defaultAction = AC_ACTIVATE;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_ACTIVATE );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_ACTIVATE )) {

			if (!isEquipped( hero )) {
				GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			} else if (cooldown > 0) {
				GLog.w( Messages.get(this, "already_active") );
			} else if (cursed) {
				GLog.w( Messages.get(this, "cursed") );
			} else if (charge < chargeCap) {
				GLog.w( Messages.get(this, "no_charge") );
			} else {
				charge = 0;
				cooldown = 10 + level();
				GLog.p( Messages.get(this, "radiating") );
				updateQuickslot();
			}
		}
        lockchB();
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new Thorns();
	}

	@Override
	public void charge(Hero target, float amount) {
		if (cooldown == 0) {
			charge += Math.round(4*amount);
			if (charge > chargeCap) charge = chargeCap;
			updateQuickslot();
		}
        lockchB();
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		if (isEquipped( Dungeon.hero )) {
			desc += "\n\n";
			if (cooldown == 0)
				desc += Messages.get(this, "desc_inactive");
			else
				desc += Messages.get(this, "desc_active");

			if (cursed) {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			}
		}

		return desc;
	}

	public class Thorns extends ArtifactBuff{

		@Override
		public boolean act(){
            lockcha();

			//自然充能：每回合恢复 0.1% 充能
			if (!isCursed() && cooldown == 0 && charge < chargeCap) {
				partialCharge += 0.1f;
				while (partialCharge >= 1) {
					partialCharge--;
					charge++;
					if (charge >= chargeCap) {
						charge = chargeCap;
						partialCharge = 0;
					}
				}
				updateQuickslot();
			}

			if (cooldown > 0) {
				cooldown--;
				if (cooldown == 0) {
					GLog.w( Messages.get(this, "inert") );
				}
				updateQuickslot();
			}
			spend(TICK);
			return true;
		}

		public int proc(int damage, Char attacker, Char defender){
			if (!isCursed() && cooldown == 0){
				partialCharge += damage * (0.5F + level() * 0.05F);
				while (partialCharge >= 1)
					partialCharge--;
				if (charge > chargeCap) charge = chargeCap;
			}

			if (cooldown != 0){
				int deflected = Random.NormalIntRange(0, damage);
				damage -= deflected;

				if (attacker != null && Dungeon.level.adjacent(attacker.pos, defender.pos)) {
					attacker.damage(deflected, this);
				}

				exp+= deflected;

				while (exp >= (level()+1)*5 && level() < levelCap){
					exp -= (level()+1)*5;
					upgrade();
					Catalog.countUse(CapeOfThorns.class);
					GLog.p( Messages.get(this, "levelup") );
				}

			}

			//诅咒效果：50%所受伤害转化为临时最大生命值削减
			if (isCursed() && defender instanceof Hero) {
				float dmg = damage * 0.5F;
				damage = (int) Math.ceil(dmg);
				applyThornCurse( (Hero) defender, (int) Math.floor(dmg) );
			}

			updateQuickslot();
			return damage;
		}

		private void applyThornCurse( Hero hero, int damage ) {
			int time = hero.buff(ThornCurse.class) == null ? 10 : 5;
			Buff.affect(hero, ThornCurse.class, time).extend(damage);
			hero.updateHT( false );
			BuffIndicator.refreshHero();
		}

		@Override
		public String toString() {
				return Messages.get(this, "name");
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(cooldown));
		}

		@Override
		public int icon() {
			if (cooldown == 0)
				return BuffIndicator.NONE;
			else
				return BuffIndicator.THORNS;
		}

		@Override
		public void detach(){
			cooldown = 0;
			charge = 0;
			super.detach();
		}

	}

	/**
	 * 荆棘诅咒：临时削减最大生命值。
	 * 持续 10 回合，后续受到伤害会额外延长 5 回合并叠加削减量。
	 */
	public static class ThornCurse extends FlavourBuff implements Hero.Doom {
		{
			type = buffType.NEGATIVE;
			announced = true;
		}
		public int reduction = 0;
		@Override
		public void detach() {
			super.detach();
			if (target instanceof Hero) {
				((Hero) target).updateHT( false );
				BuffIndicator.refreshHero();
			}
		}

		public int reduction() {
			return reduction;
		}

		public void extend( int amount ) {
			reduction += amount;
		}

		@Override
		public int icon() {
			return BuffIndicator.SACRIFICE;
		}

		@Override
		public void tintIcon( Image icon ) {
			icon.hardlight( 0.6f, 0.1f, 0.3f );
		}

		@Override
		public String desc() {
			return Messages.get( this, "desc", reduction, (int) visualcooldown() );
		}
		private static final String REDUCTION = "reduction";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( REDUCTION, reduction );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			reduction = bundle.getInt( REDUCTION );
		}

		@Override
		public void onDeath() {
			Dungeon.fail( getClass() );
			GLog.n( Messages.get(this, "onDeath") );
		}
	}


}
