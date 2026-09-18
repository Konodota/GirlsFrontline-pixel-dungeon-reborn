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

package com.shatteredpixel.shatteredpixeldungeon.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidAudio;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.AsynchronousAndroidAudio;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.android.safeinput.SafeGLSurfaceView20;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.CrashHandler;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.services.news.NewsImpl;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.update.UpdateImpl;
import com.shatteredpixel.shatteredpixeldungeon.update.Updates;
import com.watabou.noosa.Game;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.FileUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class AndroidGame extends AndroidApplication {
	
	public static AndroidApplication instance;
	
	private static AndroidPlatformSupport support;

    private static class CrashEventListener implements CustomActivityOnCrash.EventListener {
        @Override
        public void onLaunchErrorActivity() {
            // 在跳转到错误页面之前保存错误报告
            CrashHandler.getInstance().init();
        }

        @Override
        public void onRestartAppFromErrorActivity() {
            // 从错误页面重启应用时的处理
        }

        @Override
        public void onCloseAppFromErrorActivity() {
            // 从错误页面关闭应用时的处理
        }
    }
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // 初始化CustomActivityOnCrash
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                .minTimeBetweenCrashesMs(2000)
                .errorActivity(ErrorActivity.class)
                .eventListener(new CrashEventListener())
                .apply();

        // 保存CustomActivityOnCrash的处理器
        final Thread.UncaughtExceptionHandler caocHandler = Thread.getDefaultUncaughtExceptionHandler();

        // 设置新的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                // 先用CrashHandler保存报告
                CrashHandler crashHandler = CrashHandler.getInstance();
                crashHandler.init();

                // 保存崩溃报告但不退出
                try {
                    crashHandler.saveCrashReport(crashHandler.generateCrashReport(thread, throwable));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 然后调用CustomActivityOnCrash的处理器
                if (caocHandler != null) {
                    caocHandler.uncaughtException(thread, throwable);
                }
            }
        });

		//there are some things we only need to set up on first launch
		if (instance == null) {

			instance = this;

			try {
				Game.version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (PackageManager.NameNotFoundException e) {
				Game.version = "???";
			}
			try {
				Game.versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			} catch (PackageManager.NameNotFoundException e) {
				Game.versionCode = 0;
			}

			if (NewsImpl.supportsNews() && !DeviceCompat.isDebug()){
				Updates.service = UpdateImpl.getUpdateService();
				News.service = NewsImpl.getNewsService();
			}

			FileUtils.setDefaultFileProperties(Files.FileType.Local, "");

			// grab preferences directly using our instance first
			// so that we don't need to rely on Gdx.app, which isn't initialized yet.
			// Note that we use a different prefs name on android for legacy purposes,
			// this is the default prefs filename given to an android app (.xml is automatically added to it)
			SPDSettings.set(instance.getPreferences("GirlsFrontlinePixelDungeon"));

		} else {
			instance = this;
		}
		
		//set desired orientation (if it exists) before initializing the app.
		if (SPDSettings.landscape()) {
			instance.setRequestedOrientation( SPDSettings.landscape() ?
					ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE :
					ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT );
		}
		
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.depth = 0;
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			//use rgb565 on ICS devices for better performance
			config.r = 5;
			config.g = 6;
			config.b = 5;
		}
		
		config.useCompass = false;
		config.useAccelerometer = false;
		
		if (support == null) support = new AndroidPlatformSupport();
		else                 support.reloadGenerators();
		
		support.updateSystemUI();

		Button.longClick = ViewConfiguration.getLongPressTimeout()/1000f;
		
		initialize(new GirlsFrontlinePixelDungeon(support), config);

		// ===== Hook: 替换 GLSurfaceView20 为 SafeGLSurfaceView20 =====
		// 去掉 inputType 里的密码 TAG，避免 Android 安全键盘弹起
		// 注：安全键盘检测仅在 libGDX 1.13+ 版本才会触发，当前项目使用 1.10.0，暂不需要
		// hookSafeInputConnection();
	}

//	/**
//	 * 用 SafeGLSurfaceView20 替换 libGDX 默认的 GLSurfaceView20，
//	 * 让输入法 inputType 不再包含密码/敏感标记 TAG，从而避免安全键盘弹起。
//	 *
//	 * 原理：AndroidApplication.init() 硬编码 new AndroidGraphics()，
//	 * AndroidGraphics.createGLSurfaceView() 创建 GLSurfaceView20，
//	 * 其 onCreateInputConnection 会根据 onscreenKeyboardType 设置 inputType。
//	 * Default 类型返回 TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD (33)，
//	 * 其中 TYPE_TEXT_VARIATION_VISIBLE_PASSWORD 会触发部分 Android 11+ 设备的
//	 * "Private Space Keyboard"（安全键盘）。
//	 *
//	 * 本方法用反射把 AndroidGraphics.view 这个 final 字段替换为
//	 * 我们的 SafeGLSurfaceView20（强制 inputType = TYPE_CLASS_TEXT 纯文本）。
//	 */
//	private void hookSafeInputConnection() {
//		try {
//			// 1. 获取旧 view
//			AndroidGraphics graphicsRef = this.graphics; // protected 直接访问
//			Field viewField = AndroidGraphics.class.getDeclaredField("view");
//			viewField.setAccessible(true);
//			// 绕过 final 修饰符检查（Android ART 特有的技巧）
//			setFieldFullyAccessible(viewField);
//			GLSurfaceView20 oldView = (GLSurfaceView20) viewField.get(graphicsRef);
//			if (oldView == null) return;
//
//			// 2. 创建 SafeGLSurfaceView20 新实例（复制旧 view 的关键配置）
//			ResolutionStrategy rs = (ResolutionStrategy) getField(oldView.getClass(), "resolutionStrategy", oldView);
//			int glVer = ((AndroidApplicationConfiguration) getField(AndroidGraphics.class, "config", graphicsRef)).useGL30 ? 3 : 2;
//			SafeGLSurfaceView20 newView = new SafeGLSurfaceView20(getApplicationContext(), rs, glVer);
//
//			// 3. 复制配置：EGLConfigChooser、onscreenKeyboardType、EGL 配置
//			try { newView.setEGLConfigChooser(getEglConfigChooser(graphicsRef)); } catch (Exception ignored) {}
//			newView.onscreenKeyboardType = oldView.onscreenKeyboardType;
//			newView.setRenderer(graphicsRef); // renderer 是 AndroidGraphics.this
//
//			// 4. 复制旧 view 上的事件 listeners
//			copyListeners(oldView, newView);
//
//			// 5. 从旧 view 的父容器中移除旧 view，添加新 view
//			android.view.ViewParent parent = oldView.getParent();
//			if (parent instanceof FrameLayout) {
//				FrameLayout container = (FrameLayout) parent;
//				int index = container.indexOfChild(oldView);
//				FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) oldView.getLayoutParams();
//				container.removeView(oldView);
//				newView.setLayoutParams(lp);
//				if (index >= 0) container.addView(newView, index);
//				else container.addView(newView);
//			}
//
//			// 6. 用反射替换 AndroidGraphics.view final 字段
//			viewField.set(graphicsRef, newView);
//
//		} catch (Throwable t) {
//			// 反射失败不应导致 app 崩溃，降级为只 log
//			t.printStackTrace();
//		}
//	}
//
//	/** 反射获取字段值（处理 package-private / protected） */
//	private static Object getField(Class<?> cls, String name, Object obj) throws Exception {
//		Field f = cls.getDeclaredField(name);
//		f.setAccessible(true);
//		setFieldFullyAccessible(f);
//		return f.get(obj);
//	}
//
//	/**
//	 * 在 Android ART 上绕过 final 字段的 setAccessible 限制
//	 * （Android 的 Field 没有像标准 JVM 那样严格检查 final 的 setAccessible）
//	 */
//	private static void setFieldFullyAccessible(Field f) {
//		try {
//			// Android ART 上这步通常不需要，但为了兼容性还是尝试
//			Field modifiersField = Field.class.getDeclaredField("modifiers");
//			modifiersField.setAccessible(true);
//			int modifiers = modifiersField.getInt(f);
//			modifiers &= ~java.lang.reflect.Modifier.FINAL;
//			modifiersField.setInt(f, modifiers);
//		} catch (Throwable ignored) {
//			// ART 上 modifers 可能不存在或不允许修改，忽略
//		}
//	}
//
//	/** 反射获取 AndroidGraphics.getEglConfigChooser() —— protected */
//	private static android.opengl.GLSurfaceView.EGLConfigChooser getEglConfigChooser(AndroidGraphics g) throws Exception {
//		try {
//			Method m = AndroidGraphics.class.getDeclaredMethod("getEglConfigChooser");
//			m.setAccessible(true);
//			return (android.opengl.GLSurfaceView.EGLConfigChooser) m.invoke(g);
//		} catch (Throwable t) {
//			return null;
//		}
//	}
//
//	/** 复制旧 view 上的各类 listeners 到新 view（通过反射） */
//	private static void copyListeners(GLSurfaceView20 from, SafeGLSurfaceView20 to) {
//		try {
//			// setOnTouchListener
//			try {
//				Method getOnTouchListener = View.class.getDeclaredMethod("getOnTouchListener");
//				getOnTouchListener.setAccessible(true);
//				Object listener = getOnTouchListener.invoke(from);
//				if (listener instanceof android.view.View.OnTouchListener) {
//					to.setOnTouchListener((android.view.View.OnTouchListener) listener);
//				}
//			} catch (NoSuchMethodException ignored) {}
//
//			// setOnKeyListener
//			try {
//				Method getOnKeyListener = View.class.getDeclaredMethod("getOnKeyListener");
//				getOnKeyListener.setAccessible(true);
//				Object listener = getOnKeyListener.invoke(from);
//				if (listener instanceof android.view.View.OnKeyListener) {
//					to.setOnKeyListener((android.view.View.OnKeyListener) listener);
//				}
//			} catch (NoSuchMethodException ignored) {}
//
//			// setOnGenericMotionListener
//			try {
//				Method getOnGenericMotionListener = View.class.getDeclaredMethod("getOnGenericMotionListener");
//				getOnGenericMotionListener.setAccessible(true);
//				Object listener = getOnGenericMotionListener.invoke(from);
//				if (listener instanceof android.view.View.OnGenericMotionListener) {
//					to.setOnGenericMotionListener((android.view.View.OnGenericMotionListener) listener);
//				}
//			} catch (NoSuchMethodException ignored) {}
//		} catch (Throwable ignored) {}
//	}

	@Override
	public AndroidAudio createAudio(Context context, AndroidApplicationConfiguration config) {
		return new AsynchronousAndroidAudio(context, config);
	}

	@Override
	protected void onResume() {
		//prevents weird rare cases where the app is running twice
		if (instance != this){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				finishAndRemoveTask();
			} else {
				finish();
			}
		}
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		//do nothing, game should catch all back presses
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		support.updateSystemUI();
	}
	
	@Override
	public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
		super.onMultiWindowModeChanged(isInMultiWindowMode);
		support.updateSystemUI();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		AndroidPlatformSupport.handleActivityResult(requestCode, resultCode, data);
	}
}