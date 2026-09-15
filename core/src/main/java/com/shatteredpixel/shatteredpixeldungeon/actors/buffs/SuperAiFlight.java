package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

//超级小爱专属被动：可随时启停的无限时长飞行，启用时移动速度+20%且视野穿透高草
public class SuperAiFlight extends Buff implements ActionIndicator.Action {

	{
		type = buffType.NEUTRAL;
	}

	private boolean active = false;

	private static final String ACTIVE = "active";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(ACTIVE, active);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		active = bundle.getBoolean(ACTIVE);
	}

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)) {
			if (active) {
				target.flying = true;
			}
			return true;
		}
		return false;
	}

	@Override
	public void detach() {
		if (active && target != null) {
			target.flying = false;
			if (GirlsFrontlinePixelDungeon.scene() instanceof GameScene && Dungeon.level != null) {
				Dungeon.level.occupyCell(target);
			}
		}
		active = false;
		super.detach();
	}

	@Override
	public void fx(boolean on) {
		if (on) {
			ActionIndicator.setAction(this);
			if (active && target.sprite != null) {
				target.sprite.add(CharSprite.State.LEVITATING);
			}
		} else {
			ActionIndicator.clearAction(this);
			if (target != null && target.sprite != null) {
				target.sprite.remove(CharSprite.State.LEVITATING);
			}
		}
	}

	public boolean isActive() {
		return active;
	}

	//切换飞行状态（免费、即时、不消耗回合）
	public void toggle() {
		active = !active;
		if (target != null) {
			if (active) {
				target.flying = true;
				if (target.sprite != null) {
					target.sprite.add(CharSprite.State.LEVITATING);
				}
			} else {
				target.flying = false;
				if (target.sprite != null) {
					target.sprite.remove(CharSprite.State.LEVITATING);
				}
				//降落时触发脚下地形（陷阱、水等）
				if (GirlsFrontlinePixelDungeon.scene() instanceof GameScene && Dungeon.level != null) {
					Dungeon.level.occupyCell(target);
				}
			}
		}
		ActionIndicator.updateIcon();
		Sample.INSTANCE.play(Assets.Sounds.MASTERY);
	}

	//ActionIndicator.Action 接口实现
	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public Image actionIcon() {
		return new HeroIcon(HeroIcon.HEROIC_LEAP);
	}

	@Override
	public void doAction() {
		toggle();
	}

	@Override
	public int bgColor() {
		return 0xFF99CC;
	}
}
