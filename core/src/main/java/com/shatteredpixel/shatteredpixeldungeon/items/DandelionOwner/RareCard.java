package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.EnumString;
import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.hero;
import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.addAll;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.AttackDMG_Add;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.utils.Color;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;

public interface RareCard extends Card {
    @Override
    default int color() {
        return Color.LIGHT_BLUE;
    }
    @Override
    default String title(){
        return "Rare：" + cardName();
    }
    static void getAllCard( CardSelector selector ){
        HashMap<FirstCard, RareCard[]> map = cardMap();
        for (FirstCard f : map.keySet())
            if (selector.hasCard(f))
                for (RareCard card : map.get(f)) {
                    if (selector.RareCards.contains(card)
                            || selector.curCards.contains(card))
                        continue;
                    selector.curCards.add(card);
                }
        for (RareCard card : UNIVERSAL.values()){
            if (selector.RareCards.contains(card)
                    || selector.curCards.contains(card))
                continue;
            selector.curCards.add(card);
        }
    }
    static Card random( CardSelector selector, boolean signal ){
        ArrayList<Card> list = new ArrayList<>();
        HashMap<FirstCard, RareCard[]> map = cardMap();
        for (FirstCard card : map.keySet())
            if (selector.contain(card))
                addAll(list, map.get(card));
        if (!signal)
            addAll(list, UNIVERSAL.values());

        for (RareCard card : selector.RareCards)
            list.remove(card);
        for (Card card : selector.curCards)
            list.remove(card);

        return Random.element(list);
    }
    static HashMap<FirstCard, RareCard[]> cardMap(){
        HashMap<FirstCard, RareCard[]> map = new HashMap<>();
        map.put(FirstCard.HS2000, HS2000.values());
        map.put(FirstCard.Vector, Vector.values());
        map.put(FirstCard.VHS, VHS.values());
        map.put(FirstCard.WA2000, WA2000.values());
        map.put(FirstCard.General_Liu, General_Liu.values());
        return map;
    }
    @Override
    default Class<? extends Card> getCardClass(){
        return RareCard.class;
    }
    enum HS2000 implements RareCard{
        Type_64_Auto, AA_12, KSG, DESERT_EAGLE;
        @Override
        public String title(){
            return FirstCard.HS2000.cardName() + " " + RareCard.super.title();
        }
        @Override
        public String extra(){
            switch (this){
                case Type_64_Auto:
                    if (Card.shield(hero()) <= hero().HP)
                        break;
                case AA_12:
                    return normalChance();
                case DESERT_EAGLE:
                    return EnumString(this, extraKey, CardCalculator.shieldAttack(hero(), 1F));
            }
            return null;
        }
        @Override
        public String extra_2(){
            switch (this){
                //通道1：64式自仅在护盾大于血量时生效，按当前倍率独立显示
                case Type_64_Auto:
                    if (Card.shield(hero()) <= hero().HP)
                        break;
                //通道1：AA-12常驻，按当前掉血倍率独立显示
                case AA_12:
                    return capTextSingle(CardCalculator.dmgMaxCap(chance(hero())));
            }
            return null;
        }
        @Override
        public float chance( Hero hero ){
            switch (this){
                case Type_64_Auto:
                    return 0.015F * (Card.shield(hero) - hero.HP);
                case AA_12:
                    return 0.5F + 0.01F * (hero.HT - hero.HP);
            }
            return 0;
        }
    }
    enum Vector implements RareCard{
        Type_79, AK_74U, HP_35, K2, PP_19;
        @Override
        public String title(){
            return FirstCard.Vector.cardName() + " " + RareCard.super.title();
        }
    }
    enum VHS implements RareCard{
        M82A1, MDR, P90, PM_06, RFB, TAC_50, Zas_M21;
        @Override
        public String title(){
            return FirstCard.VHS.cardName() + " " + RareCard.super.title();
        }
        @Override
        public String extra(){
            switch (this){
                case P90:
                    return EnumString(this, extraKey,
                            CardCalculator.hack_chargeNeed());
            }
            return null;
        }
    }
    enum WA2000 implements RareCard{
        K11, NTW_20, PKP, Px4, R93, MOSIN_NAGANT;
        @Override
        public String title(){
            return FirstCard.WA2000.cardName() + " " + RareCard.super.title();
        }
        @Override
        public String extra(){
            switch (this){
                case PKP:
                case MOSIN_NAGANT:
                    return crit();
                case Px4:
                    return critFactor();
                case R93:
                    return EnumString(this, extraKey,
                            Math.min(5, (int) CardPoint.R93_HitPoint.point()));
            }
            return null;
        }
        @Override
        public String extra_2(){
            //通道3：暴击额外伤害聚合组，合并显示当前总上限
            if (this == Px4)
                return capTextMerge(Math.round(CardCalculator.M4A1max(2 * CardCalculator.critFactor())));
            return null;
        }
        @Override
        public void onSelect(){
            if (this == MOSIN_NAGANT)
                CardPoint.critChance.pointUp(CardAffect.kiloTimes(
                        (int) Math.floor(CardSelector.INSTANCE().upgradeTime() / 1000F),
                                this));
        }
        @Override
        public float chance( Hero hero ){
            switch (this) {
                case MOSIN_NAGANT:
                    return 0.02F;
            }
            return 0;
        }
    }
    enum General_Liu implements RareCard{
        C_93, CZ75, M26_ASW, QBU_88, X95, Contender, COLT_SAA, STECHKIN, SAIGA;
        @Override
        public void onSelect(){
            if (this == C_93)
                CardAffect.getCore(hero(), new Cores.C93());
            else if (this == CZ75 || this == Contender)
                CardAffect.getCore(hero(), new Cores.NormalCore().quantity(4));
            else if (this == COLT_SAA || this == STECHKIN)
                CardAffect.getCore(hero(), new Cores.NormalCore().quantity(2));
        }
        @Override
        public String extra_2(){
            //通道5：C-93需施展【军团】技能挂上增伤buff后才显示
            if (this == C_93 && hero().buff(AttackDMG_Add.C93.class) != null)
                return capTextSingle(Math.round(CardCalculator.M4A1max(1F)));
            return null;
        }
        @Override
        public String extra_3(){
            //攻速合并组：CZ75使总攻速倍率减半，合并值实时反映
            if (this == CZ75)
                return delayCapText();
            return null;
        }
        @Override
        public String title(){
            return FirstCard.General_Liu.cardName() + " " + RareCard.super.title();
        }
    }
    enum UNIVERSAL implements RareCard{
        Type_97_SHOTGUN, FP_6, M1887, DP_12;
        @Override
        public String title(){
            return "Universal " + RareCard.super.title();
        }
    }
}
