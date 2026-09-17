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
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.levels.ZeroLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;

//0层前进营地专用：切换可交互物块顶部提示文字的显示/隐藏
public class InteractLabelButton extends Button {

	//触碰区域尺寸（hotArea，保持不变以保证容易点中）
	private static final float HIT_SMALL = 20;
	private static final float HIT_LARGE = 24;
	//视觉尺寸：线性缩小一半（面积约为原来的1/4），居中于触碰区域内
	private static final float VIS_SMALL = HIT_SMALL / 2f;
	private static final float VIS_LARGE = HIT_LARGE / 2f;

	private final float visSize;

	private final NinePatch bg;
	private final RenderedTextBlock glyph;

	//uiSize 取 SPDSettings.interfaceSize()：0=手机小UI，>0=全尺寸/混合UI
	public InteractLabelButton( int uiSize ){
		super();

		boolean largeUI = uiSize > 0;
		//组件宽高即 Button 的 hotArea 命中范围，保持原尺寸
		width = height = largeUI ? HIT_LARGE : HIT_SMALL;
		visSize = largeUI ? VIS_LARGE : VIS_SMALL;

		bg = Chrome.get( Chrome.Type.GREY_BUTTON );
		addToBack( bg );

		glyph = PixelScene.renderTextBlock(
				Messages.get(ZeroLevel.class, "label_toggle_glyph"),
				largeUI ? 8 : 6 );
		add( glyph );

		updateState();
	}

	@Override
	protected void layout() {
		//super.layout() 会把 hotArea 铺满整个组件区域（x,y,width,height），即触碰区域不变
		super.layout();

		//背景与文字只在触碰区域中心绘制视觉尺寸的1/4大小
		float visX = x + (width() - visSize) / 2f;
		float visY = y + (height() - visSize) / 2f;

		bg.x = visX;
		bg.y = visY;
		bg.size( visSize, visSize );
		PixelScene.align( bg );

		glyph.setPos(
				visX + (visSize - glyph.width()) / 2f,
				visY + (visSize - glyph.height()) / 2f );
		PixelScene.align( glyph );
	}

	@Override
	protected void onPointerDown() {
		bg.brightness( 1.2f );
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

	//按当前开关状态刷新外观：开启=金黄文字+正常底色，关闭=灰色文字+暗淡底色
	private void updateState(){
		boolean visible = SPDSettings.zeroLevelLabels();
		bg.resetColor();
		bg.alpha( visible ? 1f : 0.45f );
		glyph.hardlight( visible ? Window.TITLE_COLOR : 0x888888 );
		glyph.alpha( visible ? 1f : 0.5f );
	}
}
