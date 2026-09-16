package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.EnumString;
import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.VHS_Hack;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.Vector_Fire_Aura;
import com.shatteredpixel.shatteredpixeldungeon.utils.Color;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public enum FirstCard implements Card {
    HS2000, Vector, VHS, WA2000, General_Liu;
    private static boolean hasCard( Card card ){
        return CardSelector.INSTANCE().hasCard(card);
    }
    public static void getAllCard( CardSelector selector ){
        for (FirstCard f : FirstCard.values()) {
            if (selector.FirstCards.contains(f)
                    || selector.curCards.contains(f))
                continue;
            selector.curCards.add(f);
        }
    }
    @Override
    public int color() {
        return Color.WHITE;
    }
    @Override
    public String title(){
        return "Initial：" + cardName();
    }
    @Override
    public void onSelect(){
        if (this == Vector)
            Buff.affect(hero(), Vector_Fire_Aura.class).resetAction();
        if (this == VHS)
            Buff.affect(hero(), VHS_Hack.class);
        if (this == General_Liu)
            CardAffect.getCore(hero(),
                    new Cores.NormalCore());
    }
    @Override
    public String extra(){
        if (!hasCard(this))
            return null;
        switch (this) {
            case HS2000:
                return EnumString(this, extraKey, CardCalculator.shieldPerHit());
            case Vector:
                return EnumString(this, extraKey, Math.round(CardCalculator.fireDamageChance(true) * 100));
            case VHS:
                return EnumString(this, extraKey,
                        CardCalculator.hack_chargeNeed(),
                        Math.round(CardCalculator.VHS_Hack_Factor() * 100));
            case WA2000:
                return EnumString(this, extraKey,
                        Math.round(CardCalculator.crit() * 100),
                        Math.round(CardCalculator.critFactor() * 100));
        }
        return null;
    }
    @Override
    public String extra_2(){
        if (!hasCard(this))
            return null;
        switch (this) {
            //通道4：骇入倍率增伤合并上限
            case VHS:
                return capTextMerge(CardCalculator.dmgMaxCap(CardCalculator.VHS_Hack_Factor()));
            //通道3：暴击额外伤害合并上限
            case WA2000:
                return capTextMerge(Math.round(CardCalculator.M4A1max(2 * CardCalculator.critFactor())));
        }
        return null;
    }
    @Override
    public Class<? extends Card> getCardClass(){
        return FirstCard.class;
    }
    public static Card random( CardSelector selector ){
        ArrayList<Card> list = new ArrayList<>(Arrays.asList(values()));
        for (Card card : values())
            if (selector.contain(card))
                list.remove(card);
        return Random.element(list);
    }
}
