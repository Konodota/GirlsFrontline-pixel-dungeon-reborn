package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.AttackDMG_Add;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.AttackDelay_Add;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.VHS_Hack;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.Vulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.Weakly;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.M4A1;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CardCalculator {
    public static int vulner( Char ch, int dmg ){
        float chance = 1F;
        for (Vulnerability v : ch.buffs(Vulnerability.class))
            if (v.working())
                chance += v.modifier();
        return (int) Math.ceil(dmg * chance);
    }
    public static int weakly( Char ch, int dmg ){
        float chance = 1F;
        for (Weakly w : ch.buffs(Weakly.class))
            if (w.working())
                chance -= w.modifier();
        return (int) Math.max(0, Math.ceil(dmg * chance));
    }
    public static float cardAttackProc_NormalAdd( Hero hero, KindOfWeapon wep ){
        //空手攻击（未装备武器，或延迟攻击结算期间武器被切换）时按默认攻速倍率 1F 处理
        float mulByDelay = wep == null ? 1F : wep.mulByDelay(hero);
        int m4Add = 0;
        int add = 0;
        if (hasCard(RareCard.HS2000.DESERT_EAGLE)) {
            int dmg = shieldAttack(Dungeon.hero, 1);
            m4Add += dmg;
            add += GameMath.gate(0.25F, mulByDelay / 3F, 0.5F) * dmg;
        }
        if (hasCard(FinalCard.HS2000.CAWS)) {
            int dmg = shieldAttack(Dungeon.hero, 3);
            m4Add += dmg;
            add += GameMath.gate(0.25F, mulByDelay / 3F, 0.5F) * dmg;
        }
        if (wep instanceof M4A1)
            return m4Add;
        else
            return add;
    }
    public static float cardAttackProc_Add( Hero hero, float damage, boolean isM4A1 ){
        float add = 0;
        for (AttackDMG_Add a : hero.buffs(AttackDMG_Add.class))
            if (isM4A1)
                add += a.increase(damage);
            else
                add += a.otherGet(damage);
        //加性叠加
        return damage + add;
    }
    public static float cardAttackProc_Mul( Hero hero, float damage, boolean isM4A1 ){
        float add = 0;
        if (hasCard(CommonCard.HS2000.Sten_II))
            add += dmgIncrease(damage, CommonCard.HS2000.Sten_II.chance(), isM4A1);
        if (hasCard(CommonCard.UNIVERSAL.AEK_999))
            add += dmgIncrease(damage, CommonCard.UNIVERSAL.AEK_999.chance(), isM4A1);
        if (hasCard(CommonCard.UNIVERSAL.K31) && hero.buff(IntensifySkill.Intensify.class) != null)
            add += dmgIncrease(damage, CommonCard.UNIVERSAL.K31.chance(), isM4A1);
        if (hasCard(RareCard.HS2000.Type_64_Auto) && Card.shield(hero) > hero.HP)
            add += dmgIncrease(damage, RareCard.HS2000.Type_64_Auto.chance(), isM4A1);
        if (hasCard(RareCard.HS2000.AA_12))
            add += dmgIncrease(damage, RareCard.HS2000.AA_12.chance(), isM4A1);

        add += dmgIncrease(damage, everDamageFactor_Add(true), isM4A1);

        return Math.round(damage + add);
    }
    private static float dmgIncrease( float damage, float chance, boolean isM4A1 ){
        if (isM4A1)
            return damage * chance;
        else
            return Math.min(damage * chance, dmgMaxCap(chance));
    }
    //百分比增伤在非M4A1上的上限（M4A1max 伤害点数），供文案显示复用
    public static int dmgMaxCap( float chance ){
        return Math.round(M4A1max(Math.max(2 * chance, 1)));
    }
    public static float everDamageFactor_Add( boolean checkNagant ){
        float mul = 0;
        if (hasFailCard(CommonCard.UNIVERSAL.M1014))
            mul += 1F;
        if (hasCard(CommonCard.UNIVERSAL._9A91))
            mul += 0.3F;
        mul += Card.CardPoint.AttackDamage_Add.point();
        if (checkNagant){
            if (hasCard(CommonCard.UNIVERSAL.Nagant_M1895)
                    && everDelayFactor_Add(false) <= 0)
                mul += 0.25F;
        }
        return mul;
    }
    public static float everDelayFactor_Add( boolean checkNagant ){
        float mul = 0;
        if (hasCard(CommonCard.UNIVERSAL.PK))
            mul += 0.3F;
        mul += Card.CardPoint.AttackDelay_Add.point();
        if (checkNagant){
            if (hasCard(CommonCard.UNIVERSAL.Nagant_M1895)
                    && everDamageFactor_Add(false) <= 0)
                mul += 0.25F;
        }
        return mul;
    }
    public static float cardDelayFactor( Hero hero, float delay, KindOfWeapon wep ){
        boolean isM4A1 = wep instanceof M4A1;
        //MG5在M4A1上直接锁定最终间隔，不参与倍率聚合
        if (isM4A1 && hasCard(FinalCard.UNIVERSAL.MG5))
            return 0.3333F;
        float multiplier = delayMultiplier(hero, isM4A1);
        if (!isM4A1)
            multiplier *= AttackDelay_Add.otherChance(delay);

        delay /= multiplier + 1;
        return delay;
    }
    //攻速倍率聚合：永久组 + 强化期间希普卡 + 在场攻速buff，CZ75减半、非M4A1上MG5翻3倍
    public static float delayMultiplier( Hero hero, boolean isM4A1 ){
        float multiplier = 0;
        multiplier += everDelayFactor_Add(true);
        if (hero.buff(IntensifySkill.Intensify.class) != null
                && hasCard(CommonCard.UNIVERSAL.Shipka))
            multiplier += 1F;
        for (AttackDelay_Add a : hero.buffs(AttackDelay_Add.class))
            multiplier += a.percent();
        if (hasCard(RareCard.General_Liu.CZ75))
            multiplier /= 2;
        if (!isM4A1 && hasCard(FinalCard.UNIVERSAL.MG5))
            multiplier *= 3;
        return multiplier;
    }
    //非M4A1武器上当前实际生效的合并攻速百分比；手持M4A1/空手（或非Weapon）时返回-1（不显示）
    public static int delayBonusShown( Hero hero ){
        KindOfWeapon wep = hero.belongings.weapon;
        if (!(wep instanceof Weapon) || wep instanceof M4A1)
            return -1;
        float baseDelay = ((Weapon) wep).delayFuror(hero);
        float effective = delayMultiplier(hero, false)
                * AttackDelay_Add.otherChance(baseDelay);
        return Math.round(effective * 100);
    }
    public static float onM4A1damageRoll( Hero hero, float damage ){
        //外部因素产生的固定伤害就不加给M4A1了。
//        damage = cardAttackProc_Add(hero, damage, true);
//        damage = cardAttackProc_Mul(hero, damage, true);
        //作为限制用的M4A1攻击上限就不计算卡牌增伤了，以免没有产生限制作用。
        return damage;
    }
    public static float M4A1damageRoll( float minimax, float maxMul, float lastMul ){
        M4A1 m = M4A1.INSTANCE();
        float min = m.min();
        float max = m.max() * maxMul;
        float dmg = Random.NormalFloat(min + (max - min) * minimax, max * maxMul);
        dmg = m.augment.damageFactor(Math.round(dmg * lastMul));
        return onM4A1damageRoll(Dungeon.hero, dmg);
    }
    public static float M4A1damageRoll( float mul ){
        return M4A1damageRoll(0F, 1F, mul);
    }
    public static float M4A1max( float mul ){
        M4A1 m = M4A1.INSTANCE();
        float dmg = m.augment.damageFactor(m.max()) * mul;
        return onM4A1damageRoll(Dungeon.hero, dmg);
        //暂时还是不吃强化符石好了
//        return onM4A1damageRoll(Dungeon.hero, M4A1.INSTANCE().max() * mul);
    }
    public static int shieldPerHit(){
        int s = 1;
        if (hasCard(CommonCard.HS2000.Sten_II) && Random.Int(3) != 0)
            s += 1;
        if (hasCard(RareCard.HS2000.KSG))
            s += 2;
        if (hasCard(FinalCard.HS2000.S_A_T_8))
            s += 4;
        return s;
    }
    public static int shieldAttack( Hero hero, float f ){
        int shield = 0;
        for (ShieldBuff shieldBuff : hero.buffs(ShieldBuff.class))
            shield += shieldBuff.shielding();
        shield *= f;
        if (shield == 0)
            return 0;
        return (int) Math.floor(Math.sqrt(2 * (shield - 1)) + 1);
    }
    public static float fireDamageChance( boolean auras ){
        if (auras){
            float chance = 0.3F;
            if (hasCard(CommonCard.Vector.KLIN))
                chance += 0.2F;
            if (hasCard(CommonCard.Vector.HONEY_BADGER))
                chance += 0.2F;
            if (hasCard(RareCard.Vector.K2))
                chance += 0.5F;
            chance += Card.CardPoint.fireChance.point();
            return chance;
        }
        else
            return 0.5F;
    }
    public static int fireDamage( boolean auras ){
        return Math.round(M4A1damageRoll( fireDamageChance(auras) ));
    }
    public static int hack_chargeNeed(){
        int need = 5;
        if (hasCard(CommonCard.VHS.IDW))
            need--;
        if (hasCard(RareCard.VHS.P90))
            need -= 2;
        return need;
    }
    public static float VHS_Hack_Proc(Hero hero, Char enemy, float damage, KindOfWeapon wep ){
        VHS_Hack hack = Buff.affect(hero, VHS_Hack.class);
        boolean isM4A1 = wep instanceof M4A1;
        //空手攻击（未装备武器，或延迟攻击结算期间武器被切换）时按默认攻速 1F 处理，避免空指针
        float delay = wep == null ? 1F : wep.delayFactor(hero);
        if (!hack.isHacking()){
            if (isM4A1)
                hack.charge(1F);
            else
                hack.charge(GameMath.gate(0.5F, delay, 2F));
            return damage;
        }

        float add = dmgIncrease(damage, VHS_Hack_Factor(), wep instanceof M4A1);
        if (hasCard(CommonCard.VHS.Ak5))
            add += 5;
        if (hasCard(CommonCard.VHS.PM1910))
            add += Math.min(CommonCard.VHS.PM1910.chance(), CardCalculator.M4A1max(isM4A1 ? 2 : (wep == null ? 1F : wep.mulByDelay(hero))));
        if (hasCard(CommonCard.VHS.Thunder))
            add += Math.min(enemy.HT * 0.02F, 15);
        if (hasCard(RareCard.VHS.TAC_50))
            add += Math.min(enemy.HT * 0.05F, 30);

        int dmg = CardAffect.tryCrit(damage + add, isM4A1);
        if (hasCard(RareCard.VHS.MDR)) {
            dmg /= 5;
            if (mask >> 0 == 0) {
                for (Mob m : hero.getVisibleEnemies()) {
                    if (m.alignment == Char.Alignment.ALLY || m instanceof NPC)
                        continue;
                    if (m == enemy)
                        continue;
                    int finalMask = mask | (int) Math.pow(2, 0);
                    Actor.add(new Actor() {
                        {
                            actPriority = VFX_PRIO;
                        }
                        @Override
                        protected boolean act() {
                            mask = finalMask;
                            m.damage(Math.round(VHS_Hack_Proc(hero, m, damage, wep)), VHS_Hack.class);
                            mask = 0;
                            Actor.remove(this);
                            return true;
                        }
                    });
                    CardAffect.VHS_Hack_Affect(m);
                }
            }
        }
        if (hasCard(RareCard.VHS.Zas_M21)) {
            float mul = 1F;
            ArrayList<Char> mobs = new ArrayList<>();
            for (int i : PathFinder.NEIGHBOURS25) {
                Char m = Actor.findChar(i + enemy.pos);
                if (m == null || m.alignment == Char.Alignment.ALLY || m instanceof NPC)
                    continue;
                if (m == enemy)
                    continue;
                if (mul > 0.5F)
                    mul -= 0.1F;
                mobs.add(m);
            }
            for (Char ch : mobs) {
                int finalDmg = dmg;
                float finalMul = mul;
                Actor.add(new Actor() {
                    @Override
                    protected boolean act() {
                        ch.damage(Math.round(finalDmg * finalMul), VHS_Hack.class);
                        Actor.remove(this);
                        return true;
                    }
                });
            }
        }
        if (hasCard(RareCard.VHS.RFB)){
            //MDR与RFB搭配可以快速无限积攒骇入次数，但是MDR的将伤害降低80%还是比较致命的，所以允许这种操作
            if (enemy.buff(VHS_Hack.VHS_Hack_KillingTracker.class) == null)
                Actor.add(new Actor() {
                    {
                        actPriority = VFX_PRIO;
                    }
                    @Override
                    protected boolean act() {
                        if (enemy.isAlive() && enemy.buff(VHS_Hack.VHS_Hack_KillingTracker.class) == null) {
                            hack.fullCharge();
                            Buff.affect(enemy, VHS_Hack.VHS_Hack_KillingTracker.class, 20F);
                        }
                        Actor.remove(this);
                        return true;
                    }
                });
        }
        if (hasCard(FinalCard.VHS.PA_15))
            if (enemy.HP < 0.15F * enemy.HT)
                Actor.add(new Actor() {
                    @Override
                    protected boolean act() {
                        enemy.damage(enemy.HT / 4, VHS_Hack.class);
                        Actor.remove(this);
                        return true;
                    }
                });

        return dmg;
    }
    private static int mask = 0;
    public static float VHS_Hack_Factor() {
        float factor = 0.5F;
        if (hasCard(CommonCard.VHS.EM_2))
            factor += 0.5F;
        factor += Card.CardPoint.VHS_Factor.point();
        return factor;
    }
    public static float crit(){
        float rate = 0F;
        if (Dungeon.hero.buff(IntensifySkill.Intensify.class) != null)
            rate += 0.3F;
        if (hasCard(FirstCard.WA2000))
            rate += 0.3F;
        if (hasCard(CommonCard.UNIVERSAL.Mk48))
            rate += 0.2F;
        if (hasCard(CommonCard.WA2000.SSG3000)
                && Dungeon.hero.buff(IntensifySkill.Intensify.class) != null)
            rate += 0.6F;
        if (hasCard(RareCard.WA2000.PKP))
            rate += 0.5F;
        rate += Card.CardPoint.critChance.point();
        return rate;
    }
    public static int critDamage( float baseDmg, boolean isM4A1 ){
        float critFactor = critFactor();
        if (hasCard(RareCard.WA2000.R93)) {
            if (Card.CardPoint.R93_HitPoint.point() >= 5) {
                Card.CardPoint.R93_HitPoint.pointClear();
                critFactor *= 2F;
            }
        }
        if (isM4A1)
            return Math.round(baseDmg + baseDmg * critFactor);

        return Math.round(baseDmg + Math.min(baseDmg * critFactor, M4A1max(critFactor * 2)));
    }
    public static float critFactor(){
        float chance = 0.5F;
        if (hasCard(CommonCard.UNIVERSAL.Mk12))
            chance += 0.4F;
        if (hasCard(RareCard.WA2000.Px4))
            chance += 1F;
        if (Dungeon.hero.buff(IntensifySkill.Intensify.class) != null && hasCard(CommonCard.UNIVERSAL.C96))
            chance += 1.5F;
        return chance;
    }
    private static boolean hasCard(Card card){
        return CardSelector.INSTANCE().hasCard(card);
    }
    private static boolean hasFailCard(Card card){
        return CardSelector.INSTANCE().failureCards.contains(card);
    }
}
