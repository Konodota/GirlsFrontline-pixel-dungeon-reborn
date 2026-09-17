package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardCalculator;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardSelector;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.FinalCard;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.RareCard;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class VHS_Hack extends CounterBuff {
    {
        revivePersists = true;
    }
    @Override
    public int icon(){
        return iconNeedDraw();
    }
    @Override
    public void tintIcon( Image icon ){
        tintIconNeedDraw(icon);
    }
    @Override
    public String desc(){
        return Messages.get(this, "desc", count(), hackLeft);
    }
    @Override
    public boolean act(){
        spend(TICK);
        if (hasCard(FinalCard.VHS.AUG)) {
            boolean notice = false;
            for (Mob m : Dungeon.level.mobs){
                if (m.alignment == Char.Alignment.ENEMY
                        && m.enemySeen
                        && target.fieldOfView[m.pos]) {
                    notice = true;
                    break;
                }
            }
            if (notice)
                charge(1F);
        }
        return true;
    }
    private static boolean hasCard( Card card ){
        return CardSelector.INSTANCE().hasCard(card);
    }
    public void fullCharge(){
        countClear();
        if (hacking)
            hackLeft++;
        else
            hacking = true;
    }
    public void charge( float charge ){
        boolean AUG = hasCard(FinalCard.VHS.AUG);
        if (!AUG && isHacking())
            return;

        if (count() < CardCalculator.hack_chargeNeed())
            countUp(charge);
        else {
            if (isHacking()){
                if (AUG)
                    if (hackLeft < maxHack()) {
                        countClear();
                        hackLeft = maxHack();
                    }
            }
            else {
                countClear();
                hackLeft = maxHack() - 1;
                hacking = true;
            }
        }
    }
    private int maxHack(){
        int add = 1;
        if (hasCard(RareCard.VHS.PM_06))
            add += 2;
        return add;
    }
    private int hackLeft;
    private boolean hacking = false;
    public boolean isHacking(){
        return hacking;
    }
    public void completed(){
        hacking = false;
        if(hackLeft > 0){
            hacking = true;
            hackLeft--;
        }
    }
    public boolean againstEnchant(){
        return hacking && !hasCard(FinalCard.VHS.AUG);
    }
    private static final String HACK_LEFT = "HACK_LEFT";
    private static final String HACKING = "Hacking";
    @Override
    public void storeInBundle( Bundle bundle ){
        super.storeInBundle(bundle);
        bundle.put(HACKING, hacking);
        bundle.put(HACK_LEFT, hackLeft);
    }
    @Override
    public void restoreFromBundle( Bundle bundle ){
        super.restoreFromBundle(bundle);
        hacking = bundle.getBoolean(HACKING);
        hackLeft = bundle.getInt(HACK_LEFT);
    }
    public static class VHS_Hack_KillingTracker extends FlavourBuff {}
}
