/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.herotalent.GSH18Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class SiriusHeart extends Buff implements ActionIndicator.Action {

    {
        type = buffType.NEUTRAL;
    }

    private static final String SIRIUS_HEART_COOLDOWN = "sirius_heart_cooldown";
    private static final String SIRIUS_HEART_BOOSTED = "sirius_heart_boosted";

    private float cooldown = 0f;
    private boolean boosted = false;

    // 根据天赋等级返回冷却时间：+1=180 / +2=120 / +3=60
    public static float cooldownForLevel(int talentLevel) {
        switch (talentLevel) {
            case 2: return 120f;
            case 3: return 60f;
            default: return 180f;
        }
    }

    // 根据天赋等级返回可选护盾抽离档位：+1={100} / +2={60,100} / +3={40,60,80,100}
    public static int[] extractTiersForLevel(int talentLevel) {
        switch (talentLevel) {
            case 2: return new int[]{60, 100};
            case 3: return new int[]{40, 60, 80, 100};
            default: return new int[]{100};
        }
    }

    // 检查是否可以使用技能
    public boolean canUse() {
        if (!(target instanceof Hero)) return false;
        Hero hero = (Hero) target;
        // 检查是否转职为未来之星
        if (hero.subClass != HeroSubClass.FUTURE_STAR) return false;
        // 检查是否有星之护盾
        StarShield starShield = hero.buff(StarShield.class);
        if (starShield == null || starShield.shielding() <= 0) return false;
        // 检查天赋是否加点
        if (hero.pointsInTalent(Talent.GSH18_SIRIUS_HEART) <= 0) return false;
        // 检查是否在冷却中
        if (cooldown > 0) return false;
        return true;
    }

    // 激活技能：按所选档位抽离部分护盾并快照为附加伤害
    public void activate(int extractPercent) {
        if (!canUse()) return;
        
        Hero hero = (Hero) target;
        int talentLevel = hero.pointsInTalent(Talent.GSH18_SIRIUS_HEART);

        // 校验抽离档位是否对当前天赋等级开放
        boolean tierAllowed = false;
        for (int tier : extractTiersForLevel(talentLevel)) {
            if (tier == extractPercent) {
                tierAllowed = true;
                break;
            }
        }
        if (!tierAllowed) return;

        // 获取当前星之护盾并按档位抽离
        StarShield starShield = hero.buff(StarShield.class);
        if (starShield == null) return;
        int shieldValue = starShield.shielding();
        if (shieldValue <= 0) return;

        int extracted = Math.min(shieldValue, (int) Math.ceil(shieldValue * extractPercent / 100f));
        if (extracted <= 0) return;

        // 根据天赋等级计算倍率（1级20% / 2级40% / 3级100%）
        float multiplier = 0.2f;
        switch (talentLevel) {
            case 2: multiplier = 0.4f; break;
            case 3: multiplier = 1.0f; break;
        }
        int bonusDamage = (int) Math.ceil(extracted * multiplier);
        bonusDamage = Math.max(1, bonusDamage);

        // 施加 tracker buff 并快照伤害
        Talent.SiriusHeartTracker tracker = Buff.affect(hero, Talent.SiriusHeartTracker.class);
        tracker.bonusDamage = bonusDamage;

        // 仅抽离所选护盾，未抽离部分予以保留
        starShield.absorbDamage(extracted);
        
        // 设置冷却时间
        cooldown = 50f;

        // 激活时不再在左下角弹出文字提示：具体数值已在激活窗口中展示
        Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
        
        // 更新UI
        BuffIndicator.refreshHero();
        ActionIndicator.setAction(this);
    }

    @Override
    public boolean act() {
        // 减少冷却时间
        if (cooldown > 0) {
            cooldown -= TICK;
            if (cooldown <= 0) {
                cooldown = 0;
                // 检查是否可以显示技能按钮
                if (canUse()) {
                    ActionIndicator.setAction(this);
                }
                BuffIndicator.refreshHero();
            }
        }
        
        // 检查是否需要显示/隐藏技能按钮
        Hero hero = (Hero) target;
        if (hero != null) {
            if (canUse() && !ActionIndicator.checkAction(this)) {
                ActionIndicator.setAction(this);
            } else if (!canUse() && ActionIndicator.checkAction(this)) {
				ActionIndicator.clearAction(this);
            }
        }
        
        spend(TICK);
        return true;
    }

    @Override
    public void detach() {
        super.detach();
        ActionIndicator.clearAction(this);
    }

    // 获取冷却时间的视觉显示
    public float visualcooldown() {
        return cooldown;
    }

    @Override
    public int icon() {
        return BuffIndicator.NONE; // 让buff不显示在屏幕上
    }

    @Override
    public void tintIcon(Image icon) {
        // 空实现，因为buff不显示
    }

    @Override
    public float iconFadePercent() {
        return 0f; // 不显示
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", 
                dispTurns(visualcooldown()),
                ((Hero)target).pointsInTalent(Talent.GSH18_SIRIUS_HEART) * 20
        );
    }

    // ActionIndicator.Action接口实现
    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public Image actionIcon() {
        Image icon;
        // 使用武器占位符图标，类似于Combo类的实现
        icon = new ItemSprite(new Item(){{image = ItemSpriteSheet.WEAPON_HOLDER; }});
        // 使用正确的tint方法签名（RGB颜色的16进制值）
        icon.tint(0xCC33CC); // 对应0.8f, 0.2f, 0.8f的颜色
        return icon;
    }

    @Override
    public void doAction() {
        GameScene.show(new WndSiriusHeart(this));
    }

    // 处理攻击时的效果：直接使用激活时快照的附加伤害
    public static void onAttack(Hero hero, Char enemy) {
        // 检查是否有SiriusHeartTracker buff
        Talent.SiriusHeartTracker tracker = hero.buff(Talent.SiriusHeartTracker.class);
        if (tracker == null) return;

        int bonusDamage = tracker.bonusDamage;
        
        // 移除buff
        tracker.detach();

        if (bonusDamage <= 0) return;
        
        // 对敌人造成附加伤害（激活时已抽离护盾，此处不再读取）
        enemy.damage(bonusDamage, hero);

        // 天狼星心脏的附加伤害同样计入锁链冲击的溅射判定（实现见 GSH18Talent）
        GSH18Talent.chainShock(hero, enemy, bonusDamage);

        // 显示伤害信息
        GLog.p(Messages.get(SiriusHeart.class, "damage", bonusDamage));
        
        // 设置冷却时间（根据天赋等级：+1=180 / +2=120 / +3=60）
        SiriusHeart siriusHeart = hero.buff(SiriusHeart.class);
        if (siriusHeart != null) {
            siriusHeart.cooldown = cooldownForLevel(hero.pointsInTalent(Talent.GSH18_SIRIUS_HEART));
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SIRIUS_HEART_COOLDOWN, cooldown);
        bundle.put(SIRIUS_HEART_BOOSTED, boosted);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        cooldown = bundle.getFloat(SIRIUS_HEART_COOLDOWN);
        boosted = bundle.getBoolean(SIRIUS_HEART_BOOSTED);
    }

    // 技能窗口
    public static class WndSiriusHeart extends Window {

        private static final int WIDTH = 120;
        private static final int BTN_HEIGHT = 18;
        private static final int SLIDER_HEIGHT = 24;
        private static final float GAP = 2;

        private final SiriusHeart buff;
        private final Hero hero;
        private final int talentLevel;
        private final int[] tiers;
        private int selectedPercent;

        private RenderedTextBlock description;
        private OptionSlider slider;
        private RedButton btnActivate;
        private RedButton btnCancel;

        public WndSiriusHeart(final SiriusHeart buff) {
            super();

            this.buff = buff;
            this.hero = (Hero) buff.target;
            this.talentLevel = hero.pointsInTalent(Talent.GSH18_SIRIUS_HEART);
            this.tiers = extractTiersForLevel(talentLevel);
            this.selectedPercent = tiers[tiers.length - 1]; // 默认100%档位

            // 描述文本（随滑条档位实时更新）
            // 必须通过 PixelScene.renderTextBlock 创建：该分支按 defaultZoom 放大字号后再缩回，
            // 直接 new RenderedTextBlock 会因内部 zoom=0 把字形缩放为0，导致文字完全不显示
            description = PixelScene.renderTextBlock(6);
            description.maxWidth(WIDTH);
            add(description);

            // 护盾抽离比例滑条：滑条位置为档位下标，换算为对应百分比
            slider = new OptionSlider(Messages.get(this, "regulator"),
                    tiers[0] + "%",
                    tiers[tiers.length - 1] + "%",
                    0,
                    tiers.length - 1) {
                @Override
                protected void onChange() {
                    selectedPercent = tiers[getSelectedValue()];
                    relayout();
                }
            };
            slider.setSelectedValue(tiers.length - 1); // 默认定位到100%
            add(slider);

            // 激活按钮
            btnActivate = new RedButton(Messages.get(this, "activate")) {
                @Override
                protected void onClick() {
                    hide();
                    buff.activate(selectedPercent);
                }
            };
            btnActivate.enable(buff.canUse());
            add(btnActivate);

            // 关闭按钮
            btnCancel = new RedButton(Messages.get(this, "cancel")) {
                @Override
                protected void onClick() {
                    hide();
                }
            };
            add(btnCancel);

            relayout();
        }

        // 根据当前档位重新计算描述并重排全部组件
        private void relayout() {
            StarShield shield = hero.buff(StarShield.class);
            int shieldValue = (shield != null) ? shield.shielding() : 0;
            int convertPercent = (talentLevel == 1 ? 20 : (talentLevel == 2 ? 40 : 100));
            int extracted = Math.min(shieldValue, (int) Math.ceil(shieldValue * selectedPercent / 100f));
            int remaining = shieldValue - extracted;
            int expectedDmg = Math.max(1, (int) Math.ceil(extracted * convertPercent / 100f));

            description.text(Messages.get(this, "desc",
                    shieldValue,
                    selectedPercent,
                    extracted,
                    remaining,
                    convertPercent,
                    expectedDmg,
                    (int) cooldownForLevel(talentLevel)
            ));

            float posY = 0;
            description.setRect(0, posY, WIDTH, description.height());
            posY = description.bottom() + GAP;

            slider.setRect(0, posY, WIDTH, SLIDER_HEIGHT);
            posY = slider.bottom() + GAP;

            btnActivate.setRect(0, posY, WIDTH, BTN_HEIGHT);
            btnCancel.setRect(0, btnActivate.bottom() + GAP, WIDTH, BTN_HEIGHT);

            resize(WIDTH, (int) Math.ceil(btnCancel.bottom()));
        }
    }
}
