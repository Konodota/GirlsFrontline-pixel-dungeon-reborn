package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.AttackDMG_Add;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.AttackDelay_Add;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.AttackSpeed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.Buff_Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.HS2000_Shield;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.MoveSpeed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.S_M82A1;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.VHS_Hack;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.Vulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.Weakly;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DandelionOwner.Puppet;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.HackParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.M4A1;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CardAffect {
    private static float partialShield = 0;
    //这个不需要存档，即便靠跨存档进行操作或者关闭重启，也就多一点或者少一点护盾。
    private static int attackMask = 0;
    public static int cardAttackProc( Hero hero, Char enemy, int damage, int baseDMG, KindOfWeapon wep ) {
        float dmg = damage;
        if (hasCard(FirstCard.VHS))
            dmg = CardCalculator.VHS_Hack_Proc(hero, enemy, dmg, wep);
        dmg += CardCalculator.cardAttackProc_NormalAdd(hero, wep);
        if (!(wep instanceof M4A1) || true) {
            //此二者已在M4A1damageRoll执行。
            //作为限制的M4A1上限不再执行此二者，所以将对M4A1的放在这里执行了
            dmg = CardCalculator.cardAttackProc_Add(hero, dmg, false);
            dmg = CardCalculator.cardAttackProc_Mul(hero, dmg, false);
        }
        int d = tryCrit(dmg, wep instanceof M4A1);
        if (hasCard(RareCard.General_Liu.CZ75))
            d /= 2;
//        AN94_Tracker an94 = enemy.buff(AN94_Tracker.class);
//        if (hasCard(CommonCard.UNIVERSAL.AN94) && (an94 == null || an94.count() <= 0) && hero.belongings.thrownWeapon == null)
//            d /= 2;
        if (hasCard(RareCard.UNIVERSAL.Type_97_SHOTGUN)
                && hero.belongings.thrownWeapon == null)
            //投武不触发此效果。
            d /= 2;
        affectAfterAttack(hero, enemy, baseDMG, wep);
        return d;
    }
    private static void affectAfterAttack( Hero hero, Char enemy, int baseDMG, KindOfWeapon wep ){
        if (hasCard(FirstCard.HS2000)){
            float s = CardCalculator.shieldPerHit();
            //空手攻击（wep 为 null）时按默认攻速倍率 1F 处理，避免空指针
            if (wep != null && !(wep instanceof M4A1))
                s *= wep.mulByDelay(hero);
            partialShield += s;
            int shield = (int) Math.floor(partialShield);
            partialShield -= shield;
            Buff.affect(hero, HS2000_Shield.class).incShield(shield);
        }
        if (hasCard(FirstCard.VHS) && hero.buff(VHS_Hack.class).isHacking()) {
            hero.buff(VHS_Hack.class).completed();
            VHS_Hack_Affect(enemy);
        }
        if (hasCard(RareCard.WA2000.R93))
            Card.CardPoint.R93_HitPoint.pointUp();
        if (hasCard(CommonCard.UNIVERSAL.SPAS_12) && Random.Float() < 0.06F)
            throwChar(enemy, hero.pos, 2, false, false);
        if (hasCard(CommonCard.UNIVERSAL.USAS_12))
            affect(enemy, MoveSpeed.SM_USAS_12.class).upgrade().setActiveTime(5F);

//        if (hasCard(CommonCard.UNIVERSAL.AN94) && enemy.buff(AN94_Tracker.class) == null) {
//            Buff.affect(enemy, AN94_Tracker.class);
//            addDoubleAttack(hero, enemy, Integer.MIN_VALUE, 0);
//        }
        if (hasCard(RareCard.UNIVERSAL.Type_97_SHOTGUN)) {
            ArrayList<Mob> mobs = new ArrayList<>();
            for (Mob m : hero.getVisibleEnemies()) {
                if (m.alignment == Char.Alignment.ALLY || m instanceof NPC)
                    continue;
                if (hero.canAttack(m))
                    mobs.add(m);
            }
            if (mobs.isEmpty())
                if (enemy instanceof Mob)
                    mobs.add((Mob) enemy);
            if (!mobs.isEmpty())
                addDoubleAttack(hero, Random.element(mobs), baseDMG, 1);
        }
        if (hasCard(FinalCard.UNIVERSAL.Kar98k))
            addDoubleAttack(hero, enemy, baseDMG, 2);
        if (hasCard(RareCard.UNIVERSAL.FP_6) && Random.Float() < 0.15F)
            throwChar(enemy, hero.pos, 2, false, false);
    }
//    public static class AN94_Tracker extends CounterBuff {
//        @Override
//        public boolean act() {
//            countUp(1);
//            return super.act();
//        }
//    }
    private static void addDoubleAttack(Hero hero, Char enemy, int damage, int code ){
        if (attackMask >> code != 0)
            return;
        if (hero.belongings.thrownWeapon != null)
            return;

        int mask = attackMask | (int) Math.pow(2, code);
        Actor.addDelayed(new Actor() {
            {
                actPriority = HERO_PRIO - code;
            }
            @Override
            protected boolean act() {
                attackMask = mask;
                try {
                    hero.attack(enemy, damage, 1F, 0F, Char.INFINITE_ACCURACY);
                } finally {
                    //虽然我感觉无需try-finally，但是AI推荐。
                    attackMask = 0;
                    Actor.remove(this);
                }
                return true;
            }
        }, -1);
    }
    public static void fireAllAffect(Char ch){
        if (hasCard(CommonCard.Vector.Type_64))
            affect(ch, MoveSpeed.SM_Type_64.class).upgrade().setActiveTime(5F);
        if (hasCard(CommonCard.Vector.Cx4))
            affect(ch, Vulnerability.V_Cx4.class).upgrade().setActiveTime(5F);
        if (hasCard(CommonCard.Vector.MAT_49))
            affect(ch, Vulnerability.V_MAT_49.class);

        if (hasCard(RareCard.Vector.Type_79))
            affect(ch, MoveSpeed.SM_Type_79.class);
        if (hasCard(RareCard.Vector.AK_74U)){
            if(ch.buff(MoveSpeed.SM_AK_74U.class) == null)
                affect(ch, MoveSpeed.SM_AK_74U.class).setActiveTime(5F);
            if(ch.buff(Vulnerability.V_AK_74U.class) == null)
                affect(ch, Vulnerability.V_AK_74U.class).setActiveTime(5F);
        }
        if (hasCard(RareCard.Vector.HP_35))
            affect(ch, Vulnerability.V_HP_35.class);

        if (hasCard(FinalCard.Vector.KSVK))
            affect(ch, Vulnerability.V_KSVK.class).upgrade().setActiveTime(5F);
    }
    public static void VHS_Hack_Affect( Char ch ){
        if (hasCard(CommonCard.VHS.M82))
            affect(ch, Weakly.W_M82.class).upgrade().setActiveTime(8F);
        if (hasCard(CommonCard.VHS.MP_446))
            affect(ch, AttackSpeed.SA_MP_446.class).upgrade().setActiveTime(8F);
        if (hasCard(CommonCard.VHS.P7))
            affect(ch, Weakly.W_P7.class).setActiveTime(15F);
        if (hasCard(CommonCard.VHS.SPP_1))
            affect(ch, MoveSpeed.SM_SPP_1.class).setActiveTime(15F);
        if (hasCard(CommonCard.VHS.Spitfire))
            affect(ch, AttackSpeed.SA_Spitfire.class).setActiveTime(15F);

        if (hasCard(RareCard.VHS.M82A1) && ch.buff(S_M82A1.class) == null)
            affect(ch, S_M82A1.class).setActiveTime(8F);
        for (int i : PathFinder.NEIGHBOURS9)
            CellEmitter.get(i + ch.pos).burst(HackParticle.FACTORY, 5);
    }
    private static void addThrowing(){
        GLog.p(Messages.get(CardAffect.class, "throwing_charged"));
        if (M4A1.INSTANCE().throwing_ready)
            M4A1.INSTANCE().lateThrowingReady = true;
        else
            M4A1.INSTANCE().throwing_ready = true;
    }
    private static void addIntensify(){
        GLog.p(Messages.get(CardAffect.class, "intensify_charged"));
        M4A1.INSTANCE().intensify_ready = true;
    }
    public static void onThrowing(){
        if (hasCard(CommonCard.UNIVERSAL.PP_19) && Random.Float() < 0.3F)
            addThrowing();
    }
    public static void onIntensify(){
        if (hasCard(CommonCard.UNIVERSAL.HK512) && Random.Float() < 0.3F)
            addIntensify();
        if (hasCard(RareCard.WA2000.K11))
            addThrowing();
    }
    public static void onMove( Hero hero ){
        if (hasCard(CommonCard.UNIVERSAL.V_PM5)
                && hero.buff(V_PM5_Tracker.class) == null){
            Buff.affect(hero, V_PM5_Tracker.class, 80F);
            Buff.affect(hero, HS2000_Shield.class).incShield(5);
        }
        if (hasCard(FinalCard.UNIVERSAL.Kar98k) && Random.Int(10) == 0)
            Buff.prolong( hero, Cripple.class, 5F);
    }
    public static void afterDamage(Hero hero ){
        if (hasCard(CommonCard.UNIVERSAL.M1014)
                && hero.HP < hero.HT * 0.05F)
            CardSelector.INSTANCE().destroyCard(CommonCard.UNIVERSAL.M1014);
    }
    public static class V_PM5_Tracker extends FlavourBuff{ }
    public static int tryCrit( float baseDmg, boolean M4A1 ){
        return crit()
                ? CardCalculator.critDamage(baseDmg, M4A1)
                : Math.round(baseDmg);
    }
    public static boolean crit(){
        return Random.Float() < CardCalculator.crit();
    }
    public static void getCore( Hero hero, Dummy_Core core ){
        if (!core.doPickUp(hero))
            Dungeon.level.drop(core, hero.pos);
    }
    public static float coreHtMul( Dummy_Core core ){
        float mul = core.htMul;
        if (hasCard(CommonCard.General_Liu.JERICHO))
            mul -= 0.05F;
        if (hasCard(RareCard.General_Liu.Contender))
            mul *= 0.05F;
        if (core instanceof Cores.C93)
            mul *= 0.75F;
        if (core instanceof Cores.VP1915)
            mul *= 2F;
        return mul;
    }
    public static float coreAttackSpeedMul( Dummy_Core core ){
        float mul = core.attackSpeedMul;
        if (hasCard(CommonCard.General_Liu.Type_80))
            mul += 0.25F;
        if (hasCard(CommonCard.General_Liu.JERICHO))
            mul += 0.2F;
        if (hasCard(RareCard.General_Liu.Contender))
            mul *= 0.3F;
        return mul;
    }
    public static float coreDamageMul( Dummy_Core core ){
        float mul = core.damageMul;
        if (hasCard(CommonCard.General_Liu.JERICHO))
            mul += 0.2F;
        if (hasCard(CommonCard.General_Liu.RIBEYROLLES))
            mul += 0.25F;
        if (hasCard(RareCard.General_Liu.CZ75))
            mul *= 0.5F;
        if (hasCard(RareCard.General_Liu.Contender))
            mul *= 0.3F;
        if (core instanceof Cores.Savage_99)
            mul *= 2F;
        return mul;
    }
    public static void summonPuppet( Puppet puppet ){
        puppet.htMul = coreHtMul(puppet.core);
        puppet.attackSpeedMul = coreAttackSpeedMul(puppet.core);
        puppet.damageMul = coreDamageMul(puppet.core);
        puppet.update();
        puppet.HP = puppet.HT;
    }
    public static <T extends Puppet> void puppetDie( T puppet ){
        if (hasCard(CommonCard.General_Liu.Rex_Zero_1))
            puppet.core.htMul += 0.005F;
        if (hasCard(CommonCard.General_Liu.DEFENDER))
            Buff.affect(Dungeon.hero, AttackDMG_Add.DEFENDER.class, 5F);
        if (hasCard(CommonCard.General_Liu.MONDRAGON)){
            puppet.core.attackSpeedMul += 0.03F;
            puppet.core.damageMul += 0.03F;
        }
        if (hasCard(CommonCard.General_Liu.TaBuKe))
            Buff.affect(Dungeon.hero, AttackDelay_Add.TaBuKe.class, 5F);

        if (hasCard(RareCard.General_Liu.M26_ASW)){
            addThrowing();
            addIntensify();
        }
        if (hasCard(RareCard.General_Liu.X95)){
            for (Mob m : Dungeon.level.mobs)
                if (m instanceof Puppet && m != puppet)
                    if (m.fieldOfView[puppet.pos]){
                        Buff.affect(m, AttackDMG_Add.X95.class, 5F);
                        Buff.affect(m, AttackDelay_Add.X95.class, 5F);
                    }
        }
    }
    public static void halfKilo( boolean lock ){
        if (hasCard(RareCard.General_Liu.QBU_88)){
            for (Mob m : Dungeon.level.mobs)
                if (m instanceof Puppet){
                    Buff.affect(m, AttackDMG_Add.QBU_88.class).upgrade().notDetach();
                    if (m.HP < m.HT)
                        Buff.affect(m, HS2000_Shield.class).incShield(m.HT - m.HP);
                }
        }
        if (!lock)
            Card.CardPoint.lock.pointClear();
    }
    public static void kiloTimes( boolean lock ){
        if (lock) {
            if (Random.Int((int) GameMath.gate(0, Card.CardPoint.lock.point(), 10)) != 0)
                return;
            Card.CardPoint.lock.pointUp();
        }
        else
            Card.CardPoint.lock.pointClear();
        Card card;
        if (hasCard((card = CommonCard.Vector.UKM_2000)))
            Card.CardPoint.fireChance.pointUp(card.chance());
        if (hasCard((card = CommonCard.VHS.SAR_21)))
            Card.CardPoint.VHS_Factor.pointUp(card.chance());
        if (hasCard((card = RareCard.WA2000.MOSIN_NAGANT)))
            Card.CardPoint.critChance.pointUp(card.chance());
        if (hasCard((card = CommonCard.UNIVERSAL.FX_05)))
            Card.CardPoint.AttackDelay_Add.pointUp(card.chance());
        if (hasCard((card = CommonCard.UNIVERSAL.Super_SASS)))
            Card.CardPoint.AttackDamage_Add.pointUp(card.chance());
    }
    public static float kiloTimes( int times, Card card ) {
        float baseChance = card.chance();
        float values = 0;
        int t = 0;
        for (int i = 0; i < times; i++) {
            if (Random.Int((int) GameMath.gate(0, t, 10)) == 0)
                values += baseChance;
            t++;
        }
        return values;
    }
    public static void kiloTimesVersionUpdate() {
        Card card;
        if (hasCard((card = CommonCard.Vector.UKM_2000)))
            card.onSelect();
        if (hasCard((card = CommonCard.VHS.SAR_21)))
            card.onSelect();
        if (hasCard((card = RareCard.WA2000.MOSIN_NAGANT)))
            card.onSelect();
        if (hasCard((card = CommonCard.UNIVERSAL.FX_05)))
            card.onSelect();
        if (hasCard((card = CommonCard.UNIVERSAL.Super_SASS)))
            card.onSelect();
    }
    private static boolean hasCard( Card card ){
        return CardSelector.INSTANCE().hasCard(card);
    }
    public static<T extends Buff> T affect( Char target, Class<T> buffClass ) {
        Buff.affect(target, Buff_Statistics.class);
        return Buff.affect(target, buffClass);
    }
    public static void throwChar(final Char ch, final int centerPos, int power,
                                 boolean closeDoors, boolean collideDmg){
        if (ch.properties().contains(Char.Property.BOSS))
            power /= 2;
        int pos = ch.pos;
        int oppositeAdjacent = pos + (pos - centerPos);

        final Ballistica trajectory = new Ballistica(pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);

        int dist = Math.min(trajectory.dist, power);

        boolean collided = dist == trajectory.dist;

        if (dist == 0
                || ch.rooted
                || ch.properties().contains(Char.Property.IMMOVABLE)) return;

        //large characters cannot be moved into non-open space
        if (Char.hasProp(ch, Char.Property.LARGE)) {
            for (int i = 1; i <= dist; i++) {
                if (!Dungeon.level.openSpace[trajectory.path.get(i)]){
                    dist = i-1;
                    collided = true;
                    break;
                }
            }
        }

        if (Actor.findChar(trajectory.path.get(dist)) != null){
            dist--;
            collided = true;
        }

        if (dist < 0) return;

        final int newPos = trajectory.path.get(dist);

        if (newPos == ch.pos) return;

        final int finalDist = dist;
        final boolean finalCollided = collided && collideDmg;

        int oldPos = ch.pos;
        ch.pos = newPos;
        if (finalCollided && ch.isAlive()) {
            Actor.add(new Actor() {
                @Override
                protected boolean act() {
                    ch.damage(Random.NormalIntRange(finalDist, 2*finalDist), M4A1.INSTANCE());
                    Actor.remove(this);
                    return true;
                }
            });
            Paralysis.prolong(ch, Paralysis.class, 1 + finalDist/2f);
        }
        if (closeDoors && Dungeon.level.map[oldPos] == Terrain.OPEN_DOOR){
            Door.leave(oldPos);
        }
        ch.moveSprite(oldPos, ch.pos);
    }
}
