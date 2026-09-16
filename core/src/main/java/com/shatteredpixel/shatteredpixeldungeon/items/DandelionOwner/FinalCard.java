package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.EnumString;
import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.addAll;
import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.hero;

import com.shatteredpixel.shatteredpixeldungeon.utils.Color;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;

public interface FinalCard extends Card {
    @Override
    default int color() {
        return Color.LIGHT_GOLD;
    }
    @Override
    default String title(){
        return "Final：" + cardName();
    }
    static void getAllCard( CardSelector selector ){
        HashMap<FirstCard, FinalCard[]> map = cardMap();
        for (FirstCard f : map.keySet())
            if (selector.hasCard(f))
                for (FinalCard card : map.get(f)) {
                    if (selector.FinalCards.contains(card)
                            || selector.curCards.contains(card))
                        continue;
                    selector.curCards.add(card);
                }
        for (FinalCard card : UNIVERSAL.values()){
            if (selector.FinalCards.contains(card)
                    || selector.curCards.contains(card))
                continue;
            selector.curCards.add(card);
        }
    }
    static Card random( CardSelector selector, boolean signal ){
        ArrayList<Card> list = new ArrayList<>();
        HashMap<FirstCard, FinalCard[]> map = cardMap();
        for (FirstCard card : map.keySet())
            if (selector.contain(card))
                addAll(list, map.get(card));
        if (!signal)
            addAll(list, UNIVERSAL.values());

        for (FinalCard card : selector.FinalCards)
            list.remove(card);
        for (Card card : selector.curCards)
            list.remove(card);

        return Random.element(list);
    }
    static HashMap<FirstCard, FinalCard[]> cardMap(){
        HashMap<FirstCard, FinalCard[]> map = new HashMap<>();
        map.put(FirstCard.HS2000, HS2000.values());
        map.put(FirstCard.Vector, Vector.values());
        map.put(FirstCard.VHS, VHS.values());
        map.put(FirstCard.WA2000, WA2000.values());
        map.put(FirstCard.General_Liu, General_Liu.values());
        return map;
    }
    @Override
    default Class<? extends Card> getCardClass(){
        return FinalCard.class;
    }
    enum HS2000 implements FinalCard{
        CAWS, S_A_T_8, Webley;
        @Override
        public String title(){
            return FirstCard.HS2000.cardName() + " " + FinalCard.super.title();
        }
        @Override
        public String extra(){
            switch (this){
                case CAWS:
                    return EnumString(this, extraKey, CardCalculator.shieldAttack(hero(), 3F));
                case Webley:
                    if (CardSelector.INSTANCE().failureCards.contains(this))
                        return EnumString(this, ".fail");
            }
            return null;
        }
    }
    enum Vector implements FinalCard{
        G36, KSVK;
        @Override
        public String title(){
            return FirstCard.Vector.cardName() + " " + FinalCard.super.title();
        }
    }
    enum VHS implements FinalCard{
        AUG, PA_15;
        @Override
        public String title(){
            return FirstCard.VHS.cardName() + " " + FinalCard.super.title();
        }
    }
    enum WA2000 implements FinalCard{
        FAL, Python;
        @Override
        public void onSelect(){
            if (this == Python){
                float damage = CardCalculator.everDamageFactor_Add(true);
                float delay = CardCalculator.everDelayFactor_Add(true);
                CardPoint.AttackDamage_Add.pointUp(damage);
                CardPoint.AttackDelay_Add.pointUp(delay);
            }
        }
        @Override
        public String title(){
            return FirstCard.WA2000.cardName() + " " + FinalCard.super.title();
        }
        @Override
        public String extra(){
            if (this == Python) {
                if (CardSelector.INSTANCE().hasCard(this))
                    return EnumString(this, extraKey,
                            Math.round(CardCalculator.everDamageFactor_Add(true) * 100),
                            Math.round(CardCalculator.everDelayFactor_Add(true) * 100));
            }
            return null;
        }
    }
    enum General_Liu implements FinalCard{
        Savage_99, VP1915, VSK_94, Kolibri_Pistole;
        @Override
        public void onSelect(){
            if (this == Savage_99)
                CardAffect.getCore(hero(), new Cores.Savage_99());
            else if (this == VP1915)
                CardAffect.getCore(hero(), new Cores.VP1915());
            else if (this == VSK_94)
                CardAffect.getCore(hero(), new Cores.NormalCore().quantity(3));
            else if (this == Kolibri_Pistole)
                CardAffect.getCore(hero(), new Cores.Kolibri_Pistole());
        }
        @Override
        public String title(){
            return FirstCard.General_Liu.cardName() + " " + FinalCard.super.title();
        }
    }
    enum UNIVERSAL implements FinalCard{
        Kar98k, LTLX7000, MG5;
        @Override
        public String title(){
            return "Universal " + FinalCard.super.title();
        }
        @Override
        public String extra_3(){
            //攻速合并组：MG5在非M4A1武器上使总攻速倍率翻3倍，合并值实时反映
            if (this == MG5)
                return delayCapText();
            return null;
        }
    }
}
