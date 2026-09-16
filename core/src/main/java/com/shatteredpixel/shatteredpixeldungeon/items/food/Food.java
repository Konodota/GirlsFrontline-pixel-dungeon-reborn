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

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.RedBookSpell.ExtraFood;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;
import java.util.HashSet;

public class Food extends Item {

	public static final float TIME_TO_EAT	= 3f;
	
	public static final String AC_EAT	= "EAT";
	
	public float energy = Hunger.HUNGRY;
	
	{
		stackable = true;
		image = ItemSpriteSheet.RATION;

		defaultAction = AC_EAT;

		bones = true;
	}
    public static Food SummonPasty(){
        // 馅饼分割
        if(Dungeon.isXMAS()){
            return new XMasSugar();
        }else {
            return new Pasty();
        }
    }
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_EAT );
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_EAT )) {
			
			detach( hero.belongings.backpack );
			Catalog.countUse(getClass());

			EatText();
			satisfy(hero);

			hero.sprite.operate( hero.pos );
			hero.busy();
			SpellSprite.show( hero, SpellSprite.FOOD );
			Sample.INSTANCE.play( Assets.Sounds.EAT );
			
			hero.spend( eatingTime() );

			Talent.onFoodEaten(hero, energy(), this);
			
			Statistics.foodEaten++;
			Badges.validateFoodEaten();
			
		}
	}
    public void EatText(){
        GLog.i( Messages.get(this, "eat_msg") );
    }

	public static float eatingTimeStatic(){
		for (Talent talent : eatingTimeTalent)
			if (Dungeon.hero.hasTalent(talent))
				return TIME_TO_EAT - 2;

		return TIME_TO_EAT;
	}
	public static final HashSet<Talent> eatingTimeTalent = new HashSet<>();
	static {
		eatingTimeTalent.add(Talent.IRON_STOMACH);
		eatingTimeTalent.add(Talent.ENERGIZING_MEAL);
		eatingTimeTalent.add(Talent.MYSTICAL_MEAL);
		eatingTimeTalent.add(Talent.INVIGORATING_MEAL);
		eatingTimeTalent.add(Talent.Type56Two_FOOD);
		eatingTimeTalent.add(Talent.GSH18_ENERGIZING_MEAL);
		eatingTimeTalent.add(Talent.Type56_21V2);
	}

	protected float eatingTime(){
		return eatingTimeStatic();
	}
    public float energy(){
        float buffEnergy = energy;
		buffEnergy += 30 * Dungeon.hero.pointsInTalent(Talent.Type56Two_FOOD);
		Hunger hunger = Dungeon.hero.buff(Hunger.class);
		if (hunger != null && hunger.isStarving())
			buffEnergy += 50 * Dungeon.hero.pointsInTalent(Talent.NICE_FOOD);
        return buffEnergy;
    }
	
	protected void satisfy( Hero hero ){
		float buffedEnergy=energy();

        if (hero.buff(ExtraFood.FoodExtra.class) != null){
            buffedEnergy*=1.3f;
            hero.buff(ExtraFood.FoodExtra.class).detach();
        }
		//旧版天赋：美食家（BETTER_FOOD）吃咸粽子回血（561旧版角色隐藏功能）
		if (hero.hasTalent(Talent.BETTER_FOOD)&&this instanceof SaltyZongzi){
			int heal=(int)(hero.HT*(0.08f*hero.pointsInTalent(Talent.BETTER_FOOD)-0.06f));
			hero.HP=Math.min(hero.HP+heal,hero.HT);
			hero.sprite.emitter().burst(Speck.factory(Speck.HEALING),hero.pointsInTalent(Talent.BETTER_FOOD));
			buffedEnergy+=30f+10f*hero.pointsInTalent(Talent.BETTER_FOOD);
		}
		if (Dungeon.isChallenged(Challenges.NO_FOOD)){
			buffedEnergy/=3f;
		}

		Buff.affect(hero, Hunger.class).satisfy(buffedEnergy);
	}

    public static void satisfy( Hero hero ,float buffedEnergy){

        if (Dungeon.isChallenged(Challenges.NO_FOOD)){
            buffedEnergy/=3f;
        }

        Buff.affect(hero, Hunger.class).satisfy(buffedEnergy);
    }

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 20*quantity;
	}
}
