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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.StarShield;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

// GSH-18 专属神器：便携医疗包（未完成）
public class MedicalKit extends Artifact {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.MEDICAL_KET;

		levelCap = 10;
		exp = 0;

		// 充能以百分比计：满充能 100，使用一次消耗 100%
		charge = 100;
		partialCharge = 0;
		chargeCap = 100;

		defaultAction = AC_USE;
		unique = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		// GSH-18 专属；需装备、满充能、未诅咒才可使用
		if (hero.heroClass == HeroClass.GSH18
				&& isEquipped( hero )
				&& charge >= chargeCap
				&& !cursed){
			actions.add( AC_USE );
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_USE )){
			if (hero.heroClass != HeroClass.GSH18){
				GLog.w( Messages.get( this, "not_owner" ) );
			} else if (!isEquipped( hero )){
				GLog.i( Messages.get( Artifact.class, "need_to_equip" ) );
			} else if (charge < chargeCap){
				GLog.i( Messages.get( this, "no_charge" ) );
			} else if (cursed){
				GLog.w( Messages.get( this, "cursed" ) );
			} else {
				// 消耗 100% 充能
				charge = 0;
				partialCharge = 0;

				// 获得 10 点星之护盾
				Buff.affect( hero, StarShield.class ).incShield( 10 );
				if (hero.sprite != null){
					hero.sprite.centerEmitter().burst( MagicMissile.WardParticle.FACTORY, 4 );
				}

				hero.sprite.operate( hero.pos );
				hero.busy();
				Sample.INSTANCE.play( Assets.Sounds.DRINK );

				hero.spend( 1f );
				Talent.onArtifactUsed( hero );

				// 随使用次数成长（最高 +10，成长效果暂未实装）
				growByUse();

				updateQuickslot();
				GLog.i( Messages.get( this, "used" ) );
			}
		}

		lockchB();
	}

	private void growByUse(){
		if (level() >= levelCap) return;
		// 每次使用获得 100 经验，升级所需 = 100 + 当前等级 * 100
		exp += 100;
		int need = 100 + level() * 100;
		while (exp >= need && level() < levelCap){
			exp -= need;
			upgrade();
			GLog.p( Messages.get( this, "levelup" ) );
			need = 100 + level() * 100;
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new KitRecharge();
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc" );
	}

	public class KitRecharge extends ArtifactBuff {

		@Override
		public boolean act() {
			lockcha();
			LockedFloor lock = target.buff( LockedFloor.class );
			if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())){
				// 每回合自然充能 0.5%
				float chargeGain = 0.5f;
				chargeGain *= RingOfEnergy.artifactChargeMultiplier( target );
				partialCharge += chargeGain;

				while (partialCharge >= 1){
					partialCharge -= 1;
					charge++;
					if (charge == chargeCap){
						partialCharge = 0;
					}
				}
			} else {
				partialCharge = 0;
			}

			updateQuickslot();
			spend( TICK );
			return true;
		}
	}
}
