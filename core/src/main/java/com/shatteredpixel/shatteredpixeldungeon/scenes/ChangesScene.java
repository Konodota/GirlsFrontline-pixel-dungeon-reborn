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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeInfo;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.girlpd.v0_5_X_Changes;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.girlpd.v0_6_X_Changes;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class ChangesScene extends PixelScene {

	public static int changesSelected = 0;

	//大版本页签名称，顺序与 changesSelected 对应
	private static final String[] VERSION_NAMES = { "0.5.X", "0.6.X" };
	
	@Override
	public void create() {
		super.create();

		Music.INSTANCE.play(Assets.Music.THEME_1,true);

		int w = Camera.main.width;
		int h = Camera.main.height;

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 9 );
		title.hardlight(Window.TITLE_COLOR);
		title.setPos(
				(w - title.width()) / 2f,
				(20 - title.height()) / 2f
		);
		align(title);
		add(title);

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );

		//窗口贴图随状态面板风格切换：第二套(style=1)使用 ZeroshopUI (16,16) 的新窗口框
		//（白边+金色角饰+深色渐变底，3px 九宫格边距固定角饰）；
		//第一套(style=0)与第三套(style=2)沿用破碎地牢原版 TOAST 窗口
		NinePatch panel;
		switch (SPDSettings.statusPaneStyle()) {
			case 1:
				panel = new NinePatch(Assets.Interfaces.ZEROSHOP_UI, 16, 16, 16, 16, 3);
				break;
			default:
				panel = Chrome.get(Chrome.Type.TOAST);
				break;
		}

		int pw = 135 + panel.marginLeft() + panel.marginRight() - 2;
		int ph = h - 36;

		panel.size( pw, ph );
		panel.x = (w - pw) / 2f;
		panel.y = title.bottom() + 5;
		align( panel );
		add( panel );
		
		final ArrayList<ChangeInfo> changeInfos = new ArrayList<>();

		if (Messages.lang() != Languages.ENGLISH){
			ChangeInfo langWarn = new ChangeInfo("", true, Messages.get(this, "lang_warn"));
			langWarn.hardlight(CharSprite.WARNING);
			changeInfos.add(langWarn);
		}
		
		switch (changesSelected){
			case 0: default:
				v0_5_X_Changes.addAllChanges(changeInfos);
				break;
			case 1:
				v0_6_X_Changes.addAllChanges(changeInfos);
				break;
		}

		ScrollPane list = new ScrollPane( new Component() ){

			@Override
			public void onClick(float x, float y) {
				for (ChangeInfo info : changeInfos){
					if (info.onClick( x, y )){
						return;
					}
				}
			}

		};
		add( list );

		Component content = list.content();
		content.clear();

		float posY = 0;
		float nextPosY = 0;
		boolean second = false;
		for (ChangeInfo info : changeInfos){
			if (info.major) {
				posY = nextPosY;
				second = false;
				info.setRect(0, posY, panel.innerWidth(), 0);
				content.add(info);
				posY = nextPosY = info.bottom();
			} else if (info.fullWidth){
				//独占整行的分类：不参与左右两列配对，直接占满面板宽度
				posY = nextPosY;
				second = false;
				info.setRect(0, posY, panel.innerWidth(), 0);
				content.add(info);
				posY = nextPosY = info.bottom();
			} else {
				if (!second){
					second = true;
					info.setRect(0, posY, panel.innerWidth()/2f, 0);
					content.add(info);
					nextPosY = info.bottom();
				} else {
					second = false;
					info.setRect(panel.innerWidth()/2f, posY, panel.innerWidth()/2f, 0);
					content.add(info);
					nextPosY = Math.max(info.bottom(), nextPosY);
					posY = nextPosY;
				}
			}
		}

		content.setSize( panel.innerWidth(), (int)Math.ceil(posY) );

		list.setRect(
				panel.x + panel.marginLeft(),
				panel.y + panel.marginTop() - 1,
				panel.innerWidth() + 2,
				panel.innerHeight() + 2);
		list.scrollTo(0, 0);

		//底部大版本切换页签（参考破碎像素地牢更新日志的版本页签）
		float tabW = pw / VERSION_NAMES.length - 1;
		float tabX = panel.x;
		float tabY = h - 16;
		for (int i = 0; i < VERSION_NAMES.length; i++){
			add( new VersionTab( VERSION_NAMES[i], i, tabX, tabY, tabW, 16 ) );
			tabX += tabW + 1;
		}

		Archs archs = new Archs();
		archs.setSize( Camera.main.width, Camera.main.height );
		addToBack( archs );

		fadeIn();
	}

	//大版本页签按钮，样式仿照 WndTabbed 的 LabeledTab
	private class VersionTab extends Button {

		private final NinePatch bg;
		private final RenderedTextBlock label;
		private final int index;

		VersionTab( String text, int index, float x, float y, float w, float h ){
			super();
			this.index = index;

			label = PixelScene.renderTextBlock( 9 );
			label.text( text );
			label.alpha( index == changesSelected ? 1f : 0.6f );
			add( label );

			bg = Chrome.get( index == changesSelected ?
					Chrome.Type.TAB_SELECTED :
					Chrome.Type.TAB_UNSELECTED );
			addToBack( bg );

			setPos( x, y );
			setSize( w, h );
			PixelScene.align( this );
		}

		@Override
		protected void layout() {
			super.layout();

			bg.x = x;
			bg.y = y;
			bg.size( width, height );

			label.setPos(
					x + (width - label.width()) / 2,
					y + (height - label.height()) / 2 - 3 );
			PixelScene.align( label );
		}

		@Override
		protected void onClick() {
			if (changesSelected == index) return;
			changesSelected = index;
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
			Game.switchScene( ChangesScene.class );
		}
	}

}
