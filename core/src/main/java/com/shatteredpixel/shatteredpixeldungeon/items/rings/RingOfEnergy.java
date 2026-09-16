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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.RogueTalent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.text.DecimalFormat;

public class RingOfEnergy extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_ENERGY;
	}

	public String statsInfo() {
		super.statsInfo();
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					new DecimalFormat("#.##").format(100f * (Math.pow(1.20f, soloBuffedBonus()) - 1f)),
					new DecimalFormat("#.##").format(100f * (Math.pow(1.15f, soloBuffedBonus()) - 1f)));
            if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)) {
                info = info + "\n\n" + Messages.get(this, "combined_stats",
                        new DecimalFormat("#.##").format(100f * (Math.pow(1.20f, combinedBuffedBonus(Dungeon.hero)) - 1f)),
                        new DecimalFormat("#.##").format(100f * (Math.pow(1.15f, combinedBuffedBonus(Dungeon.hero)) - 1f)));
            }
            return info;
		} else {
			return Messages.get(this, "typical_stats",
					new DecimalFormat("#.##").format(100f * (Math.pow(1.20f, TextGuessingLevel()) - 1f)),
					new DecimalFormat("#.##").format(100f * (Math.pow(1.15f, TextGuessingLevel()) - 1f)));
		}
	}
	
	@Override
	protected RingBuff buff( ) {
		return new Energy();
	}
	
	public static float wandChargeMultiplier( Char target ){
        float bonus = (float)Math.pow(1.20, getBuffedBonus(target, Energy.class));
        if (!(target instanceof Hero))
            return bonus;
        Hero hero = (Hero) target;
        if (hero.hasTalent(Talent.FAST_RELOAD) && hero.heroClass != HeroClass.TYPE561)
            bonus *= 1 + (0.1F * hero.pointsInTalent(Talent.FAST_RELOAD)/2F);
        if (hero.hasTalent(Talent.Type56Three_Bomb) && hero.heroClass != HeroClass.TYPE561)
            bonus *= 1 + (0.2F * hero.pointsInTalent(Talent.Type56Three_Bomb)/3F);
        return bonus;
	}

	public static float artifactChargeMultiplier( Char target ){
		float bonus = (float)Math.pow(1.15, getBuffedBonus(target, Energy.class));
        if (!(target instanceof Hero))
            return bonus;
        Hero hero = (Hero) target;
        // 盗贼（UMP9）轻身披风（蜕变）：非盗贼持有时神器充能加速（实现见 RogueTalent）
        bonus *= RogueTalent.lightCloakArtifactChargeMul(hero);
        if ( hero.hasTalent(Talent.Type56Three_Book) && hero.heroClass != HeroClass.TYPE561)
            bonus *= 1f + (0.2f * hero.pointsInTalent(Talent.LIGHT_CLOAK)/3f);
        return bonus;
	}
	
	public class Energy extends RingBuff {
	}
}
