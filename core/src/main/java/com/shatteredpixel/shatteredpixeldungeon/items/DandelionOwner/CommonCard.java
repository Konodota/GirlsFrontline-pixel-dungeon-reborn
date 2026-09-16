package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.addAll;
import static com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Card.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.AttackDMG_Add;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.AttackDelay_Add;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.utils.Color;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;

public interface CommonCard extends Card {
    // 子枚举覆写保持不变（阵营前缀 + 稀有度前缀 + 本地化卡名）
    @Override
    default int color() {
        return Color.DARK_GREEN;
    }
    @Override
    default String title(){
        return "Common：" + cardName();
    }
    static void getAllCard( CardSelector selector ){
        HashMap<FirstCard, CommonCard[]> map = cardMap();
        for (FirstCard f : map.keySet())
            if (selector.hasCard(f))
                for (CommonCard card : map.get(f)) {
                    if (selector.CommonCards.contains(card)
                            || selector.curCards.contains(card))
                        continue;
                    selector.curCards.add(card);
                }
        for (CommonCard card : UNIVERSAL.values()){
            if (selector.CommonCards.contains(card)
                    || selector.curCards.contains(card))
                continue;
            selector.curCards.add(card);
        }
    }
    static Card random( CardSelector selector, boolean signal ){
        ArrayList<Card> list = new ArrayList<>();
        HashMap<FirstCard, CommonCard[]> map = cardMap();
        for (FirstCard card : map.keySet())
            if (selector.contain(card))
                addAll(list, map.get(card));
        if (!signal)
            addAll(list, UNIVERSAL.values());

        for (CommonCard card : selector.CommonCards)
            list.remove(card);
        for (Card card : selector.curCards)
            list.remove(card);

        return Random.element(list);
    }
    static HashMap<FirstCard, CommonCard[]> cardMap(){
        HashMap<FirstCard, CommonCard[]> map = new HashMap<>();
        map.put(FirstCard.HS2000, HS2000.values());
        map.put(FirstCard.Vector, Vector.values());
        map.put(FirstCard.VHS, VHS.values());
        map.put(FirstCard.WA2000, WA2000.values());
        map.put(FirstCard.General_Liu, General_Liu.values());
        return map;
    }
    @Override
    default Class<? extends Card> getCardClass(){
        return CommonCard.class;
    }
    enum HS2000 implements CommonCard{
        Sten_II;
        @Override
        public String title(){
            return FirstCard.HS2000.cardName() + " " + CommonCard.super.title();
        }
        @Override
        public String extra(){
            if (this == Sten_II)
                return normalChance();
            return null;
        }
        @Override
        public String extra_2(){
            //通道1：司登按当前血量对应的自身倍率独立显示；负面收益时不显示
            if (this == Sten_II && chance() > 0)
                return capTextSingle(CardCalculator.dmgMaxCap(chance()));
            return null;
        }
        @Override
        public float chance( Hero hero) {
            switch (this) {
                case Sten_II:
                    return 0.667F - (float) hero.HP / hero.HT;
            }
            return 0F;
        }
    }
    enum Vector implements CommonCard{
        Type_64, Cx4, KLIN, HONEY_BADGER, MAT_49, PP_90, Beretta_38, Uzi, UKM_2000;
        @Override
        public String title(){
            return FirstCard.Vector.cardName() + " " + CommonCard.super.title();
        }
        @Override
        public float chance( Hero hero ){
            switch (this) {
                case UKM_2000:
                    return 0.05F;
            }
            return 0F;
        }
        @Override
        public void onSelect(){
            if (this == UKM_2000)
                CardPoint.fireChance.pointUp(CardAffect.kiloTimes(
                        (int) Math.floor(CardSelector.INSTANCE().upgradeTime() / 1000F),
                                this));
        }
    }
    enum VHS implements CommonCard{
        Ak5, EM_2, IDW, M82, MP_446, P7, PM1910, SAR_21, SPP_1, Thunder, Spitfire;
        @Override
        public String title(){
            return FirstCard.VHS.cardName() + " " + CommonCard.super.title();
        }
        @Override
        public String extra(){
            if (this == PM1910)
                return normalChance(1);
            return null;
        }
        @Override
        public String extra_2(){
            //通道4：骇入倍率增伤合并上限
            switch (this){
                case EM_2:
                case SAR_21:
                    return capTextMerge(CardCalculator.dmgMaxCap(CardCalculator.VHS_Hack_Factor()));
            }
            return null;
        }
        @Override
        public float chance( Hero hero ){
            switch (this) {
                case PM1910:
                    return 0.4F * (hero.HT - hero.HP);
                case SAR_21:
                    return 0.08F;
            }
            return 0F;
        }
        @Override
        public void onSelect(){
            if (this == SAR_21)
                CardPoint.VHS_Factor.pointUp(CardAffect.kiloTimes(
                        (int) Math.floor(CardSelector.INSTANCE().upgradeTime() / 1000F),
                                this));
        }
    }
    enum WA2000 implements CommonCard{
        SSG3000, SV_98;
        @Override
        public String title(){
            return FirstCard.WA2000.cardName() + " " + CommonCard.super.title();
        }
        @Override
        public String extra_2(){
            //通道5：SV-98需投掷暴击触发增伤buff后才显示
            if (this == SV_98 && hero().buff(AttackDMG_Add.SV_98.class) != null)
                return capTextSingle(Math.round(CardCalculator.M4A1max(3F)));
            return null;
        }
    }
    enum General_Liu implements CommonCard{
        Type_80, Rex_Zero_1, DEFENDER, JERICHO, RIBEYROLLES, MONDRAGON, TaBuKe, MOS;
        @Override
        public String title(){
            return FirstCard.General_Liu.cardName() + " " + CommonCard.super.title();
        }
        @Override
        public String extra_2(){
            //通道5：防卫者需增援退场触发增伤buff后才显示
            if (this == DEFENDER && hero().buff(AttackDMG_Add.DEFENDER.class) != null)
                return capTextSingle(Math.round(CardCalculator.M4A1max(3F)));
            return null;
        }
        @Override
        public String extra_3(){
            //攻速合并组：塔布克需增援退场触发攻速buff后才显示
            if (this == TaBuKe && hero().buff(AttackDelay_Add.TaBuKe.class) != null)
                return delayCapText();
            return null;
        }
    }
    enum UNIVERSAL implements CommonCard{
        Type56_1, _9A91, AEK_999, C96, FAMAS, FX_05, GSh_18, HK512,
        K31, LWMMG, M1014, Mk12, Mk48, PK, PP_19, SPAS_12,
        Super_SASS, USAS_12, V_PM5, Nagant_M1895, Shipka, /*AN94*/;
        @Override
        public String title(){
            return "Universal " + CommonCard.super.title();
        }
        @Override
        public void onSelect(){
            if (this == Super_SASS)
                CardPoint.AttackDamage_Add.pointUp(CardAffect.kiloTimes(
                        (int) Math.floor(CardSelector.INSTANCE().upgradeTime() / 1000F),
                                this));
            else if (this == FX_05)
                CardPoint.AttackDelay_Add.pointUp(CardAffect.kiloTimes(
                        (int) Math.floor(CardSelector.INSTANCE().upgradeTime() / 1000F),
                                this));
        }
        @Override
        public String extra(){
            switch (this){
                case _9A91:
                case Super_SASS:
                    //伤害
                    return damageFactor();
                case FX_05:
                case PK:
                    //攻速
                    return delayFactor();
                case Mk12:
                    //爆伤比例
                    return critFactor();
                case Mk48:
                    //暴击率
                    return crit();
                case AEK_999:
                    return normalChance();
                case M1014:
                    if (CardSelector.INSTANCE().failureCards.contains(this))
                        return failText();
            }

            return null;
        }
        @Override
        public String extra_2(){
            switch (this){
                //通道2：永久增伤聚合组，合并显示当前总上限
                case _9A91:
                case Super_SASS:
                    return capTextMerge(CardCalculator.dmgMaxCap(CardCalculator.everDamageFactor_Add(true)));
                //纳甘伤害奖励前置条件：自身没有任何攻速类永久增益
                case Nagant_M1895:
                    return capTextMerge(CardCalculator.dmgMaxCap(CardCalculator.everDamageFactor_Add(true)));
                //M1014触发失效后才加入聚合组
                case M1014:
                    if (CardSelector.INSTANCE().failureCards.contains(this))
                        return capTextMerge(CardCalculator.dmgMaxCap(CardCalculator.everDamageFactor_Add(true)));
                    return null;
                case AEK_999:
                    return capTextSingle(CardCalculator.dmgMaxCap(chance()));
                case K31:
                    if (hero().buff(IntensifySkill.Intensify.class) != null)
                        return capTextSingle(CardCalculator.dmgMaxCap(1F));
                    return null;
                //通道3：Mk12为永久暴击伤害增益
                case Mk12:
                    return capTextMerge(Math.round(CardCalculator.M4A1max(2 * CardCalculator.critFactor())));
                //通道3：C96仅强化技能生效期间提供暴击伤害增益
                case C96:
                    if (hero().buff(IntensifySkill.Intensify.class) != null)
                        return capTextMerge(Math.round(CardCalculator.M4A1max(2 * CardCalculator.critFactor())));
                    return null;
            }
            return null;
        }
        @Override
        public String extra_3(){
            switch (this){
                //攻速合并组：PK、FX-05为永久攻速增益
                case PK:
                case FX_05:
                    return delayCapText();
                //希普卡仅强化技能生效期间提供攻速增益
                case Shipka:
                    if (hero().buff(IntensifySkill.Intensify.class) != null)
                        return delayCapText();
                    return null;
                //纳甘攻速奖励前置条件：自身没有任何伤害类永久增益
                case Nagant_M1895:
                    return delayCapText();
            }
            return null;
        }
        @Override
        public float chance( Hero hero ){
            switch (this){
                case FX_05:
                case Super_SASS:
                    return 0.04F;
                case _9A91:
                    return 0.3F;
                case AEK_999:
                    return 0.006F * (hero.HT - hero.HP);
                case K31:
                    return 1F;
            }
            return 0F;
        }
    }
}
