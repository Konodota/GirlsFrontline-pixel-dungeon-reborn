package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.MoveSpeed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.HS2000_Shield;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;

import java.util.ArrayList;

public class IntensifySkill extends SkillItem {
    @Override
    public void onSkill( Hero hero ){
        INSTANCE(hero);
        hero.spendAndNext(Actor.TICK);
        if (!isBlast())
            coolDownLeft = 50;
        else
            coolDownLeft = 30;
        updateQuickslot();
    }
    private static void intensify_INSTANCE( Hero hero ){
        int shield = (int) (Math.ceil(Dungeon.curDepth() / 5F) * 3);
        if (hasCard(CommonCard.UNIVERSAL.LWMMG))
            shield += 8;
        Buff.affect(hero, HS2000_Shield.class).incShield(shield);
        getIntensify(hero, 10F);
    }
    private static void blast_INSTANCE( Hero hero ){
        ArrayList<Mob> mobs = hero.getVisibleEnemies();
        for (Mob mob : mobs)
            CardAffect.affect(mob, MoveSpeed.SM_LTLX7000.class).addActiveTime(5F);

        int size = -1;
        while (mobs.size() != size){
            size = mobs.size();
            for (Mob mob : mobs.toArray(new Mob[0])){
                if (mob.alignment == Char.Alignment.ALLY || mob instanceof NPC)
                    continue;

                int oldPos = mob.pos;

                CardAffect.throwChar(mob, hero.pos, 2, false, false);

                if (oldPos != mob.pos)
                    mobs.remove(mob);

            }
        }
        getIntensify(hero, 7F);
    }
    public static void INSTANCE( Hero hero ){
        if (!isBlast())
            intensify_INSTANCE(hero);
        else
            blast_INSTANCE(hero);
        CardAffect.onIntensify();
        updateQuickslot();
    }
    private static void getIntensify( Hero hero, float baseTime ){
        if (hasCard(CommonCard.UNIVERSAL.GSh_18))
            baseTime += 3F;
        if (hasCard(RareCard.UNIVERSAL.DP_12))
            baseTime += 8F;
        Buff.affect(hero, Intensify.class, baseTime);
    }
    public static boolean isBlast(){
        return hasCard(FinalCard.UNIVERSAL.LTLX7000);
    }
    private static boolean hasCard( Card card ){
        return CardSelector.INSTANCE().hasCard(card);
    }
    public static class Intensify extends FlavourBuff { }
}
