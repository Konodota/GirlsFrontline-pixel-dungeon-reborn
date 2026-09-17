package com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner.Core_Calling;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DandelionOwner.Puppet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public abstract class Dummy_Core extends Item {
    {
        image = ItemSprite.itemSpriteNeedDraw;
        unique = true;
        defaultAction = AC_CHOOSE;
        stackable = true;
    }
    private static final String AC_FIX = "fix";
    private static final String AC_FIX_ALL = "fixAll";
    private static final String AC_CALL = "call";
    protected int fixTimeNeed = 0;
    public float htMul = 0.1F;
    public float attackSpeedMul = 0.5F;
    public float damageMul = 0.5F;
    @SuppressWarnings("unchecked")
    public <T extends Dummy_Core> T set( float ht, float speed, float dmg ) {
        htMul = ht;
        attackSpeedMul = speed;
        damageMul = dmg;
        return (T) this;
    }
    @SuppressWarnings("unchecked")
    public <T extends Dummy_Core> T broken() {
        fixTimeNeed = 5;
        if (CardSelector.INSTANCE().hasCard(CommonCard.General_Liu.MOS))
            fixTimeNeed -= Random.NormalIntRange(1, 2);
        if (CardSelector.INSTANCE().hasCard(RareCard.General_Liu.SAIGA))
            fixTimeNeed -= Random.NormalIntRange(2, 4);
        return (T) this;
    }
    public Dummy_Core quantity( int value ) {
        quantity = value;
        return this;
    }
    @Override
    public boolean isSimilar( Item item ) {
        if (!super.isSimilar(item))
            return false;
        //已判定getClass()
        Dummy_Core core = (Dummy_Core) item;
        return core.fixTimeNeed == fixTimeNeed
                && core.htMul == htMul
                && core.damageMul == damageMul
                && core.attackSpeedMul == attackSpeedMul;
    }
    @Override
    public ArrayList<String> actions( Hero hero ){
        ArrayList<String> actions = super.actions(hero);
        if (fixTimeNeed <= 0)
            actions.add(AC_CALL);
        else {
            actions.add(AC_FIX);
            if (fixTimeNeed > 1)
                actions.add(AC_FIX_ALL);
        }
        return actions;
    }
    @Override
    public void execute( Hero hero, String action ){
        super.execute(hero, action);
        if (action.equals(AC_FIX)){
            Dummy_Core core = (Dummy_Core) detach(hero.belongings.backpack);
            core.fixTimeNeed--;
            if (core.collect())
                Dungeon.quickslot.replaceSlot(this, core);
            else
                Dungeon.level.drop(core, hero.pos);
            hero.spendAndNext(1F);
        }
        else if (action.equals(AC_FIX_ALL)) {
            Dummy_Core core = (Dummy_Core) detach(hero.belongings.backpack);
            core.fixTimeNeed = 0;
            hero.spend(fixTimeNeed);
            Buff.affect(hero, Core_Calling.class).addCore(core, hero.cooldown());
        }
        else if (action.equals(AC_CALL))
            Buff.affect(hero, Core_Calling.class).addCore((Dummy_Core) detach(hero.belongings.backpack));
    }
    @Override
    public String desc() {
        return Messages.get(this, "desc",
                (int) (CardAffect.coreHtMul(this) * 100),
                (int) (CardAffect.coreDamageMul(this) * 100),
                (int) (CardAffect.coreAttackSpeedMul(this) * 100)) + "\n" + fixTimeNeed;
    }
    public void summon( int pos ){
        Puppet puppet = puppet();
        setSize(puppet);
        GameScene.add(puppet, 0);
        ScrollOfTeleportation.appear(puppet, pos);
    }
    protected abstract void setSize( Puppet puppet );
    protected Class<? extends Puppet> puppetClass;
    @SuppressWarnings("unchecked")
    protected <T extends Puppet> T puppet() {
        return (T) Reflection.newInstance(puppetClass).set(this);
    }
    private static final String FIX_TIME_NEED = "FIX_TIME_NEED";
    private static final String MulAtHT         = "Mul_T";
    private static final String MulAtAtkSpeed   = "Mul_AS";
    private static final String MulAtDamage     = "Mul_DMG";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(FIX_TIME_NEED, fixTimeNeed);
        bundle.put(MulAtHT, htMul );
        bundle.put(MulAtAtkSpeed, attackSpeedMul);
        bundle.put(MulAtDamage, damageMul);
    }
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        fixTimeNeed = bundle.getInt(FIX_TIME_NEED);
        htMul           = bundle.getFloat(MulAtHT);
        attackSpeedMul  = bundle.getFloat(MulAtAtkSpeed);
        damageMul       = bundle.getFloat(MulAtDamage);
    }
    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }
}
