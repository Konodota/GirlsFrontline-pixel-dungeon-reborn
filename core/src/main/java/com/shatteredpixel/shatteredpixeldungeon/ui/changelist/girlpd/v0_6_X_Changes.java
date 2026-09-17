package com.shatteredpixel.shatteredpixeldungeon.ui.changelist.girlpd;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeInfo;

import java.util.ArrayList;

//v0.6.X 版本更新日志，结构仿照 v0_5_X_Changes，为 0.6 版本做的骨架
public class v0_6_X_Changes {

    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_0_6_0_Changes(changeInfos);
    }

    public static void add_0_6_0_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.6.0", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        //新增
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY),
                "v0.6.x 更新日志",
                "_-_ 0.6.x 版本的更新内容正在整理中，敬请期待。\n"+
                "_-_ 本页面将随版本推进持续更新，新条目会陆续补充到下方各分类中。"));

        //调整
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight( CharSprite.POSITIVE );
        changeInfos.add(changes);
        //在这里追加 changes.addButton(...)
        changes.addButton(new ChangeButton(Icons.get(HeroClass.HK416),
                "v0.6.x HK416将在0.6版本添加",
                "_-_ 0.6.x 版本的调整内容正在整理中，敬请期待。\n"));



        //增强
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "buffs"), false, null);
        changes.hardlight( CharSprite.POSITIVE );
        changeInfos.add(changes);
        //在这里追加 changes.addButton(...)

        //削弱
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "nerfs"), false, null);
        changes.hardlight( CharSprite.NEGATIVE );
        changeInfos.add(changes);
        //在这里追加 changes.addButton(...)
    }
}
