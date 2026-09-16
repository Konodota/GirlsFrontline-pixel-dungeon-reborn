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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

/**
 * 旧版56-2式自动步枪（重制前的机制），作为同贴图的独立武器保留。
 * 旧版561角色转职GUN_MASTER（现代重生者）后，用56式升级配件将旧版56-1改造而来，
 * 不影响新版Gun562的任何能力。
 * 旧版特点：自动装填、射程3格、榴弹为九格固定高额伤害（最大生命值的2/3），
 * ENHANCE_GRENADE（改进型榴弹）天赋仅对本武器生效。
 */
public class Gun562Old extends ShootGun {
	{
		image = ItemSpriteSheet.GUN562;
		needReload = false;
		tier = 3;
		RCH = 3;
		cooldownTurns = 200;
	}

	@Override
	public void onShootComplete(int cell, int lvl) {
		//播放音效
		Sample.INSTANCE.play(Assets.Sounds.BLAST);

		//爆炸特效
		if (Dungeon.level.heroFOV[cell]) {
			CellEmitter.get(cell).burst(BlastParticle.FACTORY, 30);
		}

		for (int i : PathFinder.NEIGHBOURS9) {
			int targetCell = cell + i;
			if (targetCell >= 0 && targetCell < Dungeon.level.length()) {
				if (Dungeon.level.heroFOV[targetCell]) {
					//烟雾特效
					CellEmitter.get(targetCell).burst(SmokeParticle.FACTORY, 4);
				}
				//烧毁地形
				if (Dungeon.level.flammable[targetCell]) {
					Dungeon.level.destroy(targetCell);
					GameScene.updateMap(targetCell);
				}
				//烧毁物品
				Heap heap = Dungeon.level.heaps.get(targetCell);
				if (heap != null) {
					heap.explode();
				}

				//伤害：旧版固定为自身最大生命值的2/3
				Char target = Actor.findChar(targetCell);
				if (null != target) {
					int damage = curUser.HT * 2 / 3;
					target.damage(damage, this);
					//旧版GUN_MASTER转职天赋：改进型榴弹（+1流血，+2机动模块故障）
					if (Dungeon.hero.hasTalent(Talent.ENHANCE_GRENADE)) {
						Buff.affect(target, Bleeding.class).set(Math.round(damage * 0.4f));
						if (Dungeon.hero.pointsInTalent(Talent.ENHANCE_GRENADE) >= 2) {
							Buff.prolong(target, Cripple.class, 3f);
						}
					}
				}
			}
		}

		if (!Dungeon.hero.isAlive()) {
			Dungeon.fail(getClass());
		}

		//旧版GUN_MASTER转职天赋：改进型榴弹3点时装填冷却170回合
		cooldownTurns = (Dungeon.hero.pointsInTalent(Talent.ENHANCE_GRENADE) >= 3 ? 170 : 200);

		hasCharge = false;
		cooldownLeft = cooldownTurns;
		updateQuickslot();
		curUser.spendAndNext(1f);
	}
}
