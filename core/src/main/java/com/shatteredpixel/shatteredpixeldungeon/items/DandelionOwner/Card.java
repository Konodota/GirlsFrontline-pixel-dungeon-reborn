package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

public interface Card {
    static void getAllCard(CardSelector selector){
        FirstCard.getAllCard(selector);
        CommonCard.getAllCard(selector);
        RareCard.getAllCard(selector);
        FinalCard.getAllCard(selector);
    }
    static void random(CardSelector selector){
        if (selector.curCardNum == 0) {
            for (int i = 0; i < 5; i++)
                addSignalCard(selector);
        }
        else {
            for (int i = 0; i < 3; i++)
                addSignalCard(selector);
            Card card;
            if ((card = signalCard(selector)) != null)
                selector.curCards.add(card);
            else
                addSignalCard(selector);
        }
    }
    static void addSignalCard(CardSelector selector) {
        Card card;
        if ((card = randomCard(selector)) != null)
            selector.curCards.add(card);
    }
    static Card signalCard(CardSelector selector){
        int curCardNum = selector.curCardNum;
        if (curCardNum < 5)
            return CommonCard.random(selector, true);
        if (curCardNum < 7)
            return RareCard.random(selector, true);
        if (curCardNum < 8)
            return FinalCard.random(selector,true);
        return null;
    }
    static Card randomCard(CardSelector selector){
        Card card;
        int curCardNum = selector.curCardNum;
        if (curCardNum < 1 && (card = FirstCard.random(selector)) != null)
            return card;
        if (curCardNum < 5 && (card = CommonCard.random(selector, false)) != null)
            return card;
        if (curCardNum < 7 && (card = RareCard.random(selector, false)) != null)
            return card;
        if (curCardNum < 8 && (card = FinalCard.random(selector, false)) != null)
            return card;
        return null;
    }
    static void addAll(ArrayList<Card> list, Card[] array){
        list.addAll(Arrays.asList(array));
    }
    default Enum<?> getCard(){
        return (Enum<?>) this;
    }
    String title();
    int color();
    default String colorTitle() {
        return GLog.DIY_COLOR + color() + GLog.DIY_COLOR + "_" + title() + "_";
    }
    default String cardName(){
        return EnumString(this, ".name");
    }
    default String info(){
        String desc = desc();
        String extra = extra();
        if (extra != null)
            desc += "\n\n" + extra;
        extra = extra_2();
        if (extra != null)
            desc += "\n" + extra;
        extra = extra_3();
        if (extra != null)
            desc += "\n" + extra;
        return desc;
    }
    default void onSelect(){ }
    default String desc(){
        return EnumString(this, ".desc");
    }
    default String extra(){
        return null;
    }
    //增伤上限追加行：非"M4A1"武器触发时，最多提供的伤害点数
    default String extra_2(){
        return null;
    }
    //攻速加成追加行：非"M4A1"武器触发时，当前实际生效的合并攻速
    default String extra_3(){
        return null;
    }
    //独立生效（通道1、5）：本卡单独享有一个上限
    default String capTextSingle( int cap ){
        return Messages.get(Card.class, "extra_cap_damage_single", cap);
    }
    //合并生效（通道2、3、4）：同类增益相加后共用一个上限
    default String capTextMerge( int cap ){
        return Messages.get(Card.class, "extra_cap_damage_merge", cap);
    }
    //攻速合并行：手持非M4A1武器时返回当前实际生效的合并攻速百分比；手持M4A1/空手时返回null
    default String delayCapText(){
        int percent = CardCalculator.delayBonusShown(hero());
        if (percent < 0)
            return null;
        return Messages.get(Card.class, "extra_cap_delay", percent);
    }
    static Hero hero(){
        return Dungeon.hero;
    }
    String extraKey = ".extra";
    default String failText(){ return Messages.get(Card.class, "fail"); }
    Class<? extends Card> getCardClass();
    default float chance() {
        return chance(Dungeon.hero);
    }
    default float chance( Hero hero ){
        return 0;
    }
    static String EnumString(Card card, String key, Object... args){
        return Messages.get(card.getCardClass(), card.getCard().name() + key, args);
    }
    enum CardPoint{
        R93_HitPoint,
        lock,
        fireChance, VHS_Factor, critChance,
        AttackDamage_Add, AttackDelay_Add;
        private float point;
        private static final String CardPointBundle = "Card_Point_Bd";
        public void pointUp(){
            point++;
        }
        public void pointUp( float p ){
            point += p;
        }
        public void pointClear(){
            point = 0;
        }
        public float point(){
            return point;
        }
        public static void reset(){
            for (CardPoint c : values())
                c.pointClear();
        }
        public static void store( Bundle bundle ){
            Bundle b = new Bundle();
            for (CardPoint c : CardPoint.values())
                b.put(c.name(), c.point);
            bundle.put(CardPointBundle, b);
        }
        public static void restore( Bundle bundle ){
            Bundle b = bundle.contains(CardPointBundle) ? bundle.getBundle(CardPointBundle) : new Bundle();
            for (CardPoint c : CardPoint.values())
                if (b.contains(c.name()))
                    c.point = b.getFloat(c.name());
        }
    }
    default String damageFactor(){
        return EnumString(this, extraKey, Math.round(CardCalculator.everDamageFactor_Add(true) * 100));
    }
    default String delayFactor(){
        return EnumString(this, extraKey, Math.round(CardCalculator.everDelayFactor_Add(true) * 100));
    }
    default String critFactor(){
        return EnumString(this, extraKey, Math.round(CardCalculator.critFactor() * 100));
    }
    default String crit(){
        return EnumString(this, extraKey, Math.round(CardCalculator.crit() * 100));
    }
    default String normalChance( float mul ){
        return EnumString(this, extraKey, Math.round(chance() * mul));
    }
    default String normalChance(){
        return normalChance(100);
    }
    static int shield( Hero hero ){
        int shield = 0;
        for (ShieldBuff shieldBuff : hero.buffs(ShieldBuff.class))
            shield += shieldBuff.shielding();
        return shield;
    }
}
