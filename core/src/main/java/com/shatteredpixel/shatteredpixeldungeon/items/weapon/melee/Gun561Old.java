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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Empulse;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * 旧版56-1式突击步枪（重制前的机制），作为同贴图的独立武器保留。
 * 通过561角色能力介绍页的隐藏开关在新开局时启用，不影响新版Gun561的任何能力。
 * 旧版GUN_MASTER转职后可用56式升级配件改造为旧版56-2式（Gun562Old）。
 */
public class Gun561Old extends ShootGun {
	{
		hasCharge=false;
		image = ItemSpriteSheet.GUN561;
		RCH = 2;
		cooldownTurns=200;
		dmgBaseMul = 4;
	}

	@Override
	public float minUpgrade(int lvl) {
		return hasCharge ? 0 :super.minUpgrade(lvl);
	}
	@Override
	public float maxUpgrade(int lvl) {
		return hasCharge ? 0 :super.maxUpgrade(lvl);
	}
	@Override
	public float maxBaseDmg() {
		return hasCharge ? 2 :super.maxBaseDmg();
	}

	@Override
	protected int BombDamage(int lvl){
		//旧版榴弹伤害：2+等级 ~ 16+2*等级
		return Random.NormalIntRange(2+lvl, 16+2*lvl);
	}

	@Override
	public void onShootComplete(int cell, int lvl){
		//旧版射击结算：无护盾/无FAST_RELOAD减CD/无ShootTracker/无EMPCharge
		BombDestory(cell);
		BombAttack(cell, lvl);
		if(!Dungeon.hero.isAlive()){
			Dungeon.fail(getClass());
		}
		hasCharge=false;
		int down = 0;
		switch (Dungeon.hero.pointsInTalent(Talent.Type56Three_Bomb)){
			case 1: down=15;break;
			case 2: down=35;break;
			case 3: down=50;break;
		}
		cooldownLeft=cooldownTurns-down;
		updateQuickslot();
		curUser.spendAndNext(1f);
	}

	@Override
	protected int BombAttack(int cell, int lvl){
		//旧版爆炸结算：仅九格、无内外圈减伤、无GUN天赋加成、无自伤给盾；
		//EMP_Three按次数回充（上限5回合）
		resetEMP();
		int attack = 0;
		for(int m : PathFinder.NEIGHBOURS9) {
			int d = cell + m;
			if (d >= 0 && d < Dungeon.level.length()) {

				Char target = Actor.findChar(d);

				if (target != null) {
					if(Dungeon.hero.hasTalent(Talent.EMP_Three))
						attack+=Dungeon.hero.pointsInTalent(Talent.EMP_Three);
					int damage = BombDamage(lvl);
					target.damage((int)(damage*rate), this);

					if(target.isAlive()&&EMPduration>0){
						Buff.prolong(target, Empulse.class, EMPduration);
						CellEmitter.get(d).burst(EnergyParticle.FACTORY, 10);
					}
				}

			}
		}
		if (attack>0){
			attack = Math.min(attack, 5);
			Buff.affect(Dungeon.hero, Recharging.class, attack);
			Buff.affect(Dungeon.hero, ArtifactRecharge.class).prolong( attack ).ignoreHornOfPlenty = false;
			ScrollOfRecharging.chargeParticle(Dungeon.hero);
		}
		return 0;
	}

	@Override
	protected void resetEMP(){
		//旧版EMP时长：3回合起，EMP_One每点+1回合
		if (Dungeon.hero.subClass== HeroSubClass.EMP_BOMB){
			EMPduration = 3;
			if(Dungeon.hero.hasTalent(Talent.EMP_One)){
				EMPduration+=Dungeon.hero.pointsInTalent(Talent.EMP_One);
			}
		}
	}
}
