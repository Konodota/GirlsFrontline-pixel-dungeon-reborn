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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.items.ChristmasTicket;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.XMasGift;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Choco;
//import com.shatteredpixel.shatteredpixeldungeon.items.SALTYMOONCAKE;
import com.shatteredpixel.shatteredpixeldungeon.items.food.XMasSugar;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FncSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStartGame;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

import java.io.IOException;

// 在导入部分添加CounterBuff类的导入
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.utils.Holidays;

public class RatKing extends NPC {

    {        
        spriteClass = FncSprite.class;
        
        state =Dungeon.depth==0?PASSIVE: SLEEPING;
    }

    @Override
    public Notes.Landmark landmark() {
        return Dungeon.depth == 5 ? Notes.Landmark.RAT_KING : null;
    }
    // 添加统一的礼物计数器，用于跟踪鼠王是否已经给予过玩家物品
    public static class GiftTracker extends CounterBuff {
        { revivePersists = true; }
    }
    public static class ChocoTracker extends CounterBuff {
        { revivePersists = true; }
    }
    
    // 添加提示计数器，用于跟踪已显示的提示次数
    public static class HintTracker extends CounterBuff {
        { revivePersists = true; }
    }

    //对FNC最后使用的物品的记录
    public static class LastTracker extends CounterBuff {
        { revivePersists = true; }
    }

    @Override
    public int defenseSkill( Char enemy ) {
        return INFINITE_EVASION;
    }
    
    @Override
    public float speed() {
        return 2f;
    }
    
    @Override
    protected Char chooseEnemy() {
        return null;
    }
    
    @Override
    public void damage( int dmg, Object src ) {
    }
    
    @Override
    public void add( Buff buff ) {
    }
    
    @Override
    public boolean reset() {
        return true;
    }

    //***This functionality is for when rat king may be summoned by a distortion trap

    @Override
    protected void onAdd() {
        super.onAdd();
        if (Dungeon.depth == 0){
            //0层营地FNC：使用独立占位文本
            yell(Messages.get(this, "zero_spawn"));
        }
        else if (Dungeon.depth != 5){
            yell(Messages.get(this, "confused"));
        }
    }

    protected  boolean exit =false;

    @Override
    protected boolean act() {
        if(Dungeon.depth==0){
            state = PASSIVE;
        }else
        if(Dungeon.depth!=5){
            state = WANDERING;
        }
        return super.act();
    }

    //***
    @Override
    public boolean interact(Char c) {
        sprite.turnTo( pos, c.pos );

        // 检查是否已经给予过玩家巧克力
        ChocoTracker trackerA = hero.buff(ChocoTracker.class);
        boolean hasGivenChoco = (trackerA != null) ;

        // 检查是否已经给予过玩家拐杖糖
        GiftTracker trackerB = hero.buff(GiftTracker.class);
        boolean hasGivenSugar = (trackerB != null) ;

        boolean isMidAutumn = Holidays.holiday == Holidays.Holiday.midAutumnFestival;
        // 检查玩家是否持有月饼
        boolean hasMooncake = hasPlayerMooncake();

        // 已经给予过物品，根据提示次数显示不同的消息
        HintTracker hintTracker = hero.buff(HintTracker.class);
        float hintCount = (hintTracker != null) ? hintTracker.count() : 0;

        if (c != hero){
            return super.interact(c);
        }

        KingsCrown crown = hero.belongings.getItem(KingsCrown.class);
        XMasGift gift = hero.belongings.getItem(XMasGift.class);
        if (state == SLEEPING) {
            notice();
            yell( Messages.get(this, "not_sleeping") );
            state = WANDERING;
        }
        else if (Dungeon.depth == 0) {
            //0层营地FNC：不直接开启新存档，仅指引玩家前往右侧消毒通道
            if (hintCount < 3) {
                yell(Messages.get(this, "zero_" + hintCount));
                Buff.count(hero, HintTracker.class, 1);
            }
            else {
                yellNormal(Messages.get(this, "zero_guide"));
            }
            //圣诞节彩蛋功能：未永久解锁时需持圣诞入场券找FNC解锁；解锁后弹出开关窗口
            showXmasEntry();
        }
        else if (crown != null){
            if (hero.belongings.armor() == null) {
                yell(Messages.get(RatKing.class, "crown_clothes"));
            } else {
                Badges.validateRatmogrify();
                Game.runOnRenderThread(new Callback() {
                    @Override
                    public void call() {
                        GameScene.show(new WndOptions(
                                sprite(),
                                Messages.titleCase(name()),
                                Messages.get(RatKing.class, "crown_desc"),
                                Messages.get(RatKing.class, "crown_yes"),
                                Messages.get(RatKing.class, "crown_info"),
                                Messages.get(RatKing.class, "crown_no")
                        ) {
                            @Override
                            protected void onSelect(int index) {
                                if (index == 0) {
                                    SetLast(1);
                                    crown.upgradeArmor(hero, hero.belongings.armor(), new Ratmogrify());
                                    ((FncSprite) sprite).resetAnims();
                                    yellGood(Messages.get(RatKing.class, "crown_thankyou"));
                                } else if (index == 1) {
                                    GameScene.show(new WndInfoArmorAbility(hero.heroClass, new Ratmogrify()));
                                } else {
                                    yell(Messages.get(RatKing.class, "crown_fine"));
                                }
                            }
                        });
                    }
                });
            }
        }
        else if (gift!=null&&!hasGivenSugar){
            SetLast(2);
            gift.GiftCost();
            if(!hasGivenChoco){
                yellGood(Messages.get(this, "both") );
                hero.spendAndNext( -1 );
                GetChock();
            }
            else {
                yellGood(Messages.get(this, "onlysugar") );
            }
            GetSugar();
            ((FncSprite) sprite).resetAnims();
            Badges.validateXMASGift();
            if(hero.buff(HintTracker.class)!=null)
                hero.buff(HintTracker.class).detach();
            //重置点击次数以获取特色文案
        }
        else if (!hasGivenChoco) {
            // FNC尚未给予Choco
            GetChock();
            if (isMidAutumn && !hasMooncake) {
                // 中秋节期间，且玩家没有月饼，显示中秋节对话
                yellGood(Messages.get(this, "mid_autumn_greeting"));
            }
            if(hero.buff(HintTracker.class)!=null)
                hero.buff(HintTracker.class).detach();
            //重置点击次数以获取特色文案
        }
        else if (hintCount < 3) {
            if(hasGivenSugar){
                // FNC获得礼物后的诚挚祝福
                yellGood(Messages.get(this,"wish_"+hintCount));
//                if (hintCount == 0) {
//                    yell(Messages.get(this, "no_more"));
//                } else if (hintCount == 1) {
//                    yell(Messages.get(this, "no_more_2"));
//                } else {
//                    yell(Messages.get(this, "no_more_3"));
//                }
            }else {
                // FNC护食哈气了
                String argue = Messages.get(this,"argue_"+hintCount);
                if (hintCount == 0) {
                    yellGood(argue);
                } else if (hintCount == 1) {
                    yellNormal(argue);
                } else {
                    yell(argue);
                }
            }
                // 增加提示计数器
                Buff.count(hero, HintTracker.class, 1);
        }
        else if (hero.armorAbility instanceof Ratmogrify) {
            yellGood( Messages.get(this, "crown_after") );
        }
        else if (hasGivenSugar) {
            // 给予了礼物后，循环文案是祝福而非护食哈气
            yellGood(Messages.get(this, "wish_cyc"));
        }
        else {
            // 没有交易鼠王护甲，并且没有给予礼物，还在获取巧克力后点击多次，触发护食哈气
            yell(Messages.get(this, "what_is_it"));
        }
        return true;
    }

    private void SetLast(int i) {
        Buff.count(hero,LastTracker.class,i);
    }

    //0层营地FNC：圣诞节彩蛋开关窗口
    private void showXmasEggWindow() {
        Game.runOnRenderThread(new Callback() {
            @Override
            public void call() {
                final boolean enabled = SPDSettings.xmasEgg();
                GameScene.show(new WndOptions(
                        sprite(),
                        Messages.titleCase(name()),
                        Messages.get(RatKing.this, "xmas_egg_prompt"),
                        Messages.get(RatKing.this, enabled ? "xmas_egg_disable" : "xmas_egg_enable"),
                        Messages.get(RatKing.this, "xmas_egg_cancel")
                ) {
                    @Override
                    protected void onSelect(int index) {
                        if (index == 0) {
                            toggleXmasEgg();
                        }
                    }
                });
            }
        });
    }

    //切换全局圣诞节彩蛋开关，并立即同步当前0层存档与FNC外观
    private void toggleXmasEgg() {
        boolean enabled = !SPDSettings.xmasEgg();
        SPDSettings.xmasEgg(enabled);

        long xmasBit = (long) Math.pow(2, WndStartGame.GameMode.CHRISTMAS.code());
        if (enabled) {
            Dungeon.GameMode |= xmasBit;
        } else {
            Dungeon.GameMode &= ~xmasBit;
        }
        Game.GameMode = Dungeon.GameMode;

        //立即刷新FNC的节日皮肤
        if (sprite instanceof FncSprite) {
            ((FncSprite) sprite).resetAnims();
        }

        if (enabled) {
            yellGood(Messages.get(this, "xmas_egg_enabled"));
        } else {
            yellNormal(Messages.get(this, "xmas_egg_disabled"));
        }

        //立即持久化当前0层存档的GameMode
        try {
            Dungeon.saveAll();
        } catch (IOException e) {
            GirlsFrontlinePixelDungeon.reportException(e);
        }
    }

    //圣诞节彩蛋入口：未永久解锁时检查英雄是否持有圣诞入场券，持票则询问是否交付
    private void showXmasEntry() {
        if (SPDSettings.xmasUnlocked()) {
            showXmasEggWindow();
            return;
        }
        if (hero.belongings.getItem(ChristmasTicket.class) != null) {
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    GameScene.show(new WndOptions(
                            sprite(),
                            Messages.titleCase(name()),
                            Messages.get(RatKing.this, "xmas_ticket_prompt"),
                            Messages.get(RatKing.this, "xmas_ticket_yes"),
                            Messages.get(RatKing.this, "xmas_ticket_no")
                    ) {
                        @Override
                        protected void onSelect(int index) {
                            if (index == 0) {
                                unlockXmasWithTicket();
                            }
                        }
                    });
                }
            });
        }
        else {
            //尚未持票：提示入场券的获取途径
            yellNormal(Messages.get(this, "xmas_ticket_needed"));
        }
    }

    //消耗圣诞入场券，永久解锁圣诞节彩蛋功能，随后直接弹出开关窗口供玩家开启
    private void unlockXmasWithTicket() {
        if (SPDSettings.xmasUnlocked()) return;
        ChristmasTicket ticket = hero.belongings.getItem(ChristmasTicket.class);
        if (ticket == null) return;

        ticket.detach(hero.belongings.backpack);
        SPDSettings.xmasUnlocked(true);

        //立即刷新FNC的节日皮肤
        if (sprite instanceof FncSprite) {
            ((FncSprite) sprite).resetAnims();
        }

        yellGood(Messages.get(this, "xmas_ticket_used"));

        Game.runOnRenderThread(new Callback() {
            @Override
            public void call() {
                showXmasEggWindow();
            }
        });
    }

    private void GetChock(){
        Choco t1 = new Choco(); // Choco在中秋节期间会自动显示为月饼
        t1.identify();
        if (t1.doPickUp(hero)) {
            Messages.get(hero, "you_now_have", t1.name());
        } else {
            Dungeon.level.drop(t1, hero.pos).sprite.drop();
        }
        // 标记为已给予物品
        Buff.count(hero, ChocoTracker.class, 1);
    }
    private void GetSugar(){
        XMasSugar t1 = new XMasSugar(); // 拐杖糖
        t1.identify();
        if (t1.doPickUp(hero)) {
            Messages.get(hero, "you_now_have", t1.name());
        } else {
            Dungeon.level.drop(t1, hero.pos).sprite.drop();
        }
        // 标记为已给予物品
        Buff.count(hero, GiftTracker.class, 1);
    }
    
    // 检查玩家是否持有巧克力
    private boolean hasPlayerMooncake() {
        return hero.belongings.getItem(Choco.class) != null;
    }
    
    @Override
    public String description() {
        boolean XMAS;
        if (sprite == null)
            XMAS = Dungeon.isXMAS();
        else
            XMAS = ((FncSprite)sprite).XMAS;
        return XMAS ?
                Messages.get(this, "desc_festive")
                : super.description();
    }
}
