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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.levels.ZeroLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.audio.Sample;

//0层前进营地专用：切换可交互物块顶部提示文字的显示/隐藏
public class InteractLabelButton extends Button {

	public static final float SIZE = 18;

	private ColorBlock bg;
	private RenderedTextBlock glyph;

	public InteractLabelButton(){
		super();

		width = height = SIZE;

		bg = new ColorBlock( SIZE, SIZE, 0xFF000000 );
		add( bg );

		glyph = PixelScene.renderTextBlock( Messages.get(ZeroLevel.class, "label_toggle_glyph"), 9 );
		add( glyph );

		updateState();
	}

	@Override
	protected void layout() {
		super.layout();

		bg.x = x;
		bg.y = y;
		PixelScene.align( bg );

		glyph.setPos(
				x + (width() - glyph.width()) / 2f,
				y + (height() - glyph.height()) / 2f );
	}

	@Override
	protected void onPointerDown() {
		bg.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK );
	}

	@Override
	protected void onPointerUp() {
		updateState();
	}

	@Override
	protected void onClick() {
		boolean visible = !SPDSettings.zeroLevelLabels();
		SPDSettings.zeroLevelLabels( visible );
		updateState();
		GLog.i( Messages.get(ZeroLevel.class, visible ? "label_toggle_on" : "label_toggle_off") );
	}

	@Override
	protected String hoverText() {
		return Messages.get(ZeroLevel.class, "label_toggle");
	}

	//按当前开关状态刷新外观：开启=金黄文字+半透明黑底，关闭=灰色暗淡
	private void updateState(){
		boolean visible = SPDSettings.zeroLevelLabels();
		bg.resetColor();
		bg.alpha( visible ? 0.55f : 0.3f );
		glyph.hardlight( visible ? Window.TITLE_COLOR : 0x888888 );
		glyph.alpha( visible ? 1f : 0.5f );
	}
}
