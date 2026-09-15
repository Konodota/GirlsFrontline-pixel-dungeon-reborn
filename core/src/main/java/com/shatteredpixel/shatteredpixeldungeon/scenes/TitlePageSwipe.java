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

import com.badlogic.gdx.Input;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

/**
 * 标题页翻页监听器，供 TitleScene 与 SecondTitleScene 共用（两个场景文件保持独立）。
 * 统一三种输入入口，方向约定一致（手机主屏式）：
 *   下一页 = 左滑 / 上滑 / 鼠标滚轮向下 / ↓键；
 *   上一页 = 右滑 / 下滑 / 鼠标滚轮向上 / ↑键。
 *
 * 触摸滑动：水平与垂直均可，取位移更大的轴向，移动距离（相机虚拟像素）超过阈值才判定翻页。
 * 滚轮：累计滚动量达到一格才翻页，短时间内的连续小格（含触控板惯性）合并为一次，
 *       超过 {@link #SCROLL_GESTURE_WINDOW} 秒无滚动则清零，避免停留余量被后续误触。
 * 键盘：只响应按下沿，操作系统的按住连发在首次成功翻页后由 flipped 标志吞掉。
 *
 * 事件策略：默认只观察、不拦截，普通点按照常分发给按钮；只有翻页回调返回 true
 * （场景即将切换）时才吞掉本次事件，避免与按钮点击同时触发。
 * 另外，必须在场景内所有按钮创建完成后再 attach()：信号为 LIFO 派发，
 * 后注册的监听器先收到事件，才能在按钮之前决定是否吞掉事件；
 * 之后弹出的窗口对触摸/按键自带拦截（Window 的全屏 blocker 与全焦点按键监听），
 * 滚轮则通过 {@link PixelScene#hasOpenWindow()} 显式让位，弹窗打开时三种输入均不会翻页。
 */
public class TitlePageSwipe {

	public interface PageAction {
		//返回 true 表示已执行翻页（场景将被切换）
		boolean flip();
	}

	//判定为翻页所需的最小滑动距离（相机虚拟像素）
	private static final float SWIPE_THRESHOLD = 30f;

	//滚轮累计达到一格（常规鼠标一格 notch 的 amount 约为 1）才翻页
	private static final float SCROLL_THRESHOLD = 1f;
	//超过该秒数没有新的滚轮事件，则丢弃此前的累计量
	private static final float SCROLL_GESTURE_WINDOW = 0.25f;

	private final PageAction onNext;
	private final PageAction onPrev;

	//首次成功翻页后置 true，忽略同批次后续事件（滚轮多格、按键连发），detach 后随对象回收
	private boolean flipped = false;

	//触摸滑动追踪
	private int trackingId = -1;
	private float startX;
	private float startY;

	//滚轮累计
	private float scrollAccum = 0f;
	private float lastScrollTime = -1f;

	public TitlePageSwipe(PageAction onNext, PageAction onPrev){
		this.onNext = onNext;
		this.onPrev = onPrev;
	}

	public void attach(){
		PointerEvent.addPointerListener(pointerListener);
		KeyEvent.addKeyListener(keyListener);
		ScrollEvent.addScrollListener(scrollListener);
	}

	public void detach(){
		PointerEvent.removePointerListener(pointerListener);
		KeyEvent.removeKeyListener(keyListener);
		ScrollEvent.removeScrollListener(scrollListener);
	}

	private boolean tryFlip(PageAction action){
		if (flipped || action == null){
			return false;
		}
		if (action.flip()){
			flipped = true;
			return true;
		}
		return false;
	}

	private boolean windowOpen(){
		Scene scene = Game.scene();
		return scene instanceof PixelScene && ((PixelScene) scene).hasOpenWindow();
	}

	// *****************
	// *** 触摸滑动 ***
	// *****************

	private final Signal.Listener<PointerEvent> pointerListener = new Signal.Listener<PointerEvent>() {
		@Override
		public boolean onSignal(PointerEvent event) {
			//拖拽中派发的 null 事件与鼠标悬停事件均忽略
			if (event == null || event.type == PointerEvent.Type.HOVER){
				return false;
			}

			if (event.type == PointerEvent.Type.DOWN){
				//只追踪第一根手指，忽略多指
				if (trackingId == -1){
					trackingId = event.id;
					startX = event.current.x;
					startY = event.current.y;
				}
				return false;
			}

			//UP
			if (event.id != trackingId){
				return false;
			}
			trackingId = -1;

			//PointerEvent 坐标为屏幕像素，换算为相机虚拟像素后再比较
			Camera cam = Camera.main;
			PointF start = cam.screenToCamera((int)startX, (int)startY);
			PointF end = cam.screenToCamera((int)event.current.x, (int)event.current.y);
			float dx = end.x - start.x;
			float dy = end.y - start.y;

			if (Math.max(Math.abs(dx), Math.abs(dy)) < SWIPE_THRESHOLD){
				return false;
			}

			PageAction action;
			if (Math.abs(dx) >= Math.abs(dy)){
				action = dx < 0 ? onNext : onPrev;	//左滑下一页，右滑上一页
			} else {
				action = dy < 0 ? onNext : onPrev;	//上滑下一页，下滑上一页
			}

			if (tryFlip(action)){
				//翻页成功：吞掉 UP，防止滑动起点位于按钮上时同时触发按钮点击
				return true;
			}
			return false;
		}
	};

	// *****************
	// *** 键盘 ↑/↓ ***
	// *****************

	private final Signal.Listener<KeyEvent> keyListener = new Signal.Listener<KeyEvent>() {
		@Override
		public boolean onSignal(KeyEvent event) {
			//只响应按下沿；弹窗打开时 Window 的按键监听会先拦截，这里再保险判断一次
			if (!event.pressed || flipped || windowOpen()){
				return false;
			}

			PageAction action;
			if (event.code == Input.Keys.DOWN){
				action = onNext;
			} else if (event.code == Input.Keys.UP){
				action = onPrev;
			} else {
				return false;
			}

			return tryFlip(action);
		}
	};

	// *********************
	// *** 鼠标滚轮滚动 ***
	// *********************

	private final Signal.Listener<ScrollEvent> scrollListener = new Signal.Listener<ScrollEvent>() {
		@Override
		public boolean onSignal(ScrollEvent event) {
			if (event == null || flipped || windowOpen()){
				return false;
			}

			float now = Game.timeTotal;
			if (now - lastScrollTime > SCROLL_GESTURE_WINDOW){
				scrollAccum = 0f;
			}
			lastScrollTime = now;
			scrollAccum += event.amount;

			if (Math.abs(scrollAccum) < SCROLL_THRESHOLD){
				return false;
			}

			//libGDX：amount>0 为向上滚 → 上一页；amount<0 为向下滚 → 下一页
			PageAction action = scrollAccum > 0 ? onPrev : onNext;
			scrollAccum = 0f;

			return tryFlip(action);
		}
	};
}
