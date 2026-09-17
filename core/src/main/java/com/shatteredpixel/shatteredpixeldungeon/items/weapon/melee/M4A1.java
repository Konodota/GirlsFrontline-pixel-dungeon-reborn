package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardCalculator;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.CardSelector;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.FinalCard;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.IntensifySkill;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.SkillItem;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.ThrowingSkill;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class M4A1 extends MeleeWeapon implements ActionIndicator.Action {

	{
		image = ItemSpriteSheet.M4A1;
		defaultAction = AC_CHOOSE;
		tier = 1;
		RCH = 2;
        dmgBaseDiffer = -0.8F;
	}
	@Override
	public int damageRoll(Char owner) {
		float damage = super.damageRoll(owner);
		if (!(owner instanceof Hero))
			return (int) damage;
		return (int) CardCalculator.onM4A1damageRoll((Hero) owner, damage);
	}
	@Override
	public float delayFactor( Char owner ) {
		float delay = super.delayFactor(owner);
		return Math.max(0.3333F, delay);
	}
	private static final String SkillItem_THROWING = "SkillItem_THROWING";
	private static final String SkillItem_INTENSIFY = "SkillItem_INTENSIFY";
	private static final String THROWING_SKILL = "THROWING_SKILL";
	private static final String THROWING_READY = "THROWING_READY";
	private static final String INTENSIFY_READY = "INTENSIFY_READY";
	public boolean throwing_ready;
	public boolean intensify_ready;
	private ThrowingSkill throwing = new ThrowingSkill();
	private IntensifySkill intensify = new IntensifySkill();
	@Override
	public void Tracker( Char owner ){
		super.Tracker(owner);
		throwing.Tracker(owner);
		intensify.Tracker(owner);
	}
	@Override
	public void stopTrack(){
		super.stopTrack();
		throwing.stopTrack();
		intensify.stopTrack();
	}
	@Override
	public void storeInBundle( Bundle bundle ){
		super.storeInBundle(bundle);
		bundle.put(THROWING_READY, throwing_ready);
		bundle.put(INTENSIFY_READY, intensify_ready);
		bundle.put(SkillItem_THROWING, throwing);
		bundle.put(SkillItem_INTENSIFY, intensify);
	}
	@Override
	public void restoreFromBundle( Bundle bundle ){
		super.restoreFromBundle(bundle);
		throwing_ready = bundle.getBoolean(THROWING_READY);
		intensify_ready = bundle.getBoolean(INTENSIFY_READY);
		if (bundle.contains(SkillItem_THROWING))
			throwing = (ThrowingSkill) bundle.get(SkillItem_THROWING);
		if (bundle.contains(SkillItem_INTENSIFY))
			intensify = (IntensifySkill) bundle.get(SkillItem_INTENSIFY);
	}
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add(SkillItem_INTENSIFY);
		actions.add(SkillItem_THROWING);
		if (change() && isEquipped(hero))
			actions.add(THROWING_SKILL);
		if (throwing_ready)
			actions.add(THROWING_READY);
		if (intensify_ready)
			actions.add(INTENSIFY_READY);
		return actions;
	}
	@Override
	public void activate( Char ch ){
		Tracker(ch);
	}
	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {
			//卸下武器时清除动作指针上残留的投技动作
			ActionIndicator.clearAction(this);
			return true;
		}
		return false;
	}
	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals(SkillItem_THROWING)) {
			throwing.Tracker(hero);
			throwing.execute(hero, AC_SKILL);
		}
		else if (action.equals(SkillItem_INTENSIFY)) {
			intensify.Tracker(hero);
			intensify.execute(hero, AC_SKILL);
		}
		else if (action.equals(THROWING_SKILL)) {
			ActionIndicator.setAction(this);
			doAction();
		}
		else if (action.equals(THROWING_READY))
			ThrowingSkill.INSTANCE(ThrowingSelector(true), SnipeSelector(true));
		else if (action.equals(INTENSIFY_READY)) {
			intensify_ready = false;
			IntensifySkill.INSTANCE(hero);
		}
	}
	public boolean lateThrowingReady = false;
	private CellSelector.Listener ThrowingSelector(boolean ready) {
		return new CellSelector.Listener() {
			@Override
			public void onSelect(Integer target) {
				if (target != null) {
					if (!ThrowingSkill.Throwing_INSTANCE(target))
						return;
					if (ready) {
						if (!lateThrowingReady)
							throwing_ready = false;
						lateThrowingReady = false;
						updateQuickslot();
						return;
					}
					lateThrowingReady = false;
					float delay = delayFactor(Dungeon.hero);
					if (delay * 4 < 1F)
						coolDownLeft = 1;
					else
						coolDownLeft = Math.round(delay * 4);
					Dungeon.hero.spendAndNext(delay * 2);
					updateQuickslot();
				}
			}

			@Override
			public String prompt() {
				return ThrowingSkill.prompt;
			}
		};
	}
	private CellSelector.Listener SnipeSelector(boolean ready) {
		return new CellSelector.Listener() {
			@Override
			public void onSelect(Integer target) {
				if (target != null) {
					if (!ThrowingSkill.Snipe_INSTANCE(target))
						return;
					if (ready) {
						if (!lateThrowingReady)
							throwing_ready = false;
						lateThrowingReady = false;
						updateQuickslot();
						return;
					}
					lateThrowingReady = false;
					float delay = delayFactor(Dungeon.hero);
					if (delay * 4 < 1F)
						coolDownLeft = 1;
					else
						coolDownLeft = Math.round(delay * 4);
					Dungeon.hero.spendAndNext(delay * 2);
				}
			}

			@Override
			public String prompt() {
				return ThrowingSkill.prompt;
			}
		};
	}
	public static M4A1 INSTANCE(){
		M4A1 m = Dungeon.hero.belongings.getItem(M4A1.class);
		if (m == null)
			m = new M4A1();
		return m;
	}
	@Override
	public int reach( Char owner ){
		if (owner instanceof Hero && change())
			return 0;
		return super.reach( owner );
	}
	private boolean change(){
		return CardSelector.INSTANCE().hasCard(FinalCard.WA2000.FAL);
	}
	@Override
	public String actionName() {
		return Messages.get(M4A1.class, "ac_throwing_skill");
	}
	@Override
	public Image actionIcon() {
		return new ItemSprite(this);
	}
	@Override
	public void doAction() {
		if (coolDownLeft > 0)
			GLog.w(Messages.get(SkillItem.class, "CoolDown", coolDownLeft));
		else
			ThrowingSkill.INSTANCE(ThrowingSelector(false), SnipeSelector(false));
	}
}
