/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the the terms of the GNU General Public License as published by
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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.DataTransfer;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GirlsFrontlinePixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

//玩家数据转移窗口：以 RedButton 形式提供导出/导入入口。
//注意：该窗口暂时不接入任何场景，待需要时再添加入口。
public class WndDataTransfer extends Window {

	private static final int WIDTH = 130;
	private static final int MARGIN = 2;
	private static final int BTN_HEIGHT = 18;
	private static final int BTN_SMALL_HEIGHT = 15;

	private int pos;

	public WndDataTransfer() {
		super();

		IconTitle title = new IconTitle(Icons.get(Icons.DATA), Messages.get(this, "title"));
		title.setRect(MARGIN, 0, WIDTH - 2 * MARGIN, 0);
		add(title);
		pos = (int) title.bottom() + MARGIN;

		RenderedTextBlock info = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
		info.maxWidth(WIDTH - 2 * MARGIN);
		info.setPos(MARGIN, pos);
		add(info);
		pos = (int) info.bottom() + 2 * MARGIN;

		if (Dungeon.hero != null) {
			//冒险途中禁止转移，防止写入过程中存档状态混乱
			RenderedTextBlock warn = PixelScene.renderTextBlock(Messages.get(this, "in_game"), 6);
			warn.maxWidth(WIDTH - 2 * MARGIN);
			warn.setPos(MARGIN, pos);
			add(warn);
			pos = (int) warn.bottom() + 2 * MARGIN;
			resize(WIDTH, pos);
			return;
		}

		RedButton btnExport = new RedButton(Messages.get(this, "export")) {
			@Override
			protected void onClick() {
				doExport();
			}
		};
		btnExport.setRect(MARGIN, pos, WIDTH - 2 * MARGIN, BTN_HEIGHT);
		add(btnExport);
		pos = (int) btnExport.bottom() + 2 * MARGIN;

		ArrayList<FileHandle> archives = DataTransfer.availableTransfers();
		if (archives.isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(Messages.get(this, "no_file"), 6);
			empty.maxWidth(WIDTH - 2 * MARGIN);
			empty.setPos(MARGIN, pos);
			add(empty);
			pos = (int) empty.bottom() + MARGIN;
		} else {
			RenderedTextBlock listTitle = PixelScene.renderTextBlock(Messages.get(this, "list_title"), 6);
			listTitle.maxWidth(WIDTH - 2 * MARGIN);
			listTitle.setPos(MARGIN, pos);
			add(listTitle);
			pos = (int) listTitle.bottom() + MARGIN;

			for (final FileHandle archive : archives) {
				RedButton btnImport = new RedButton(archiveLabel(archive), 7) {
					@Override
					protected void onClick() {
						showImportConfirm(archive);
					}
				};
				btnImport.setRect(MARGIN, pos, WIDTH - 2 * MARGIN, BTN_SMALL_HEIGHT);
				add(btnImport);
				pos = (int) btnImport.bottom() + MARGIN;
			}
		}

		resize(WIDTH, pos);
	}

	private void doExport() {
		try {
			FileHandle zip = DataTransfer.exportData();
			hide();
			Game.platform.shareFile(zip);
		} catch (Exception e) {
			GirlsFrontlinePixelDungeon.reportException(e);
			showWindow(new WndMessage(Messages.get(this, "export_fail", e.toString())));
		}
	}

	private void doImport(FileHandle archive) {
		try {
			DataTransfer.importData(archive);
			showImportDone();
		} catch (Exception e) {
			GirlsFrontlinePixelDungeon.reportException(e);
			showWindow(new WndMessage(Messages.get(this, "import_fail", e.toString())));
		}
	}

	private void showImportConfirm(final FileHandle archive) {
		showWindow(new Window() {
			{
				IconTitle title = new IconTitle(Icons.get(Icons.WARNING), Messages.get(WndDataTransfer.this, "confirm_title"));
				title.setRect(MARGIN, 0, WIDTH - 2 * MARGIN, 0);
				add(title);
				int p = (int) title.bottom() + MARGIN;

				RenderedTextBlock text = PixelScene.renderTextBlock(
						Messages.get(WndDataTransfer.this, "confirm_text", archiveLabel(archive)), 6);
				text.maxWidth(WIDTH - 2 * MARGIN);
				text.setPos(MARGIN, p);
				add(text);
				p = (int) text.bottom() + 2 * MARGIN;

				RedButton btnConfirm = new RedButton(Messages.get(WndDataTransfer.this, "confirm")) {
					@Override
					protected void onClick() {
						hide();
						doImport(archive);
					}
				};
				btnConfirm.setRect(MARGIN, p, (WIDTH - 3 * MARGIN) / 2f, BTN_HEIGHT);
				add(btnConfirm);

				RedButton btnCancel = new RedButton(Messages.get(WndDataTransfer.this, "cancel")) {
					@Override
					protected void onClick() {
						hide();
					}
				};
				btnCancel.setRect(btnConfirm.right() + MARGIN, p, (WIDTH - 3 * MARGIN) / 2f, BTN_HEIGHT);
				add(btnCancel);

				resize(WIDTH, (int) btnCancel.bottom() + MARGIN);
			}
		});
	}

	private void showImportDone() {
		showWindow(new Window() {
			{
				IconTitle title = new IconTitle(Icons.get(Icons.INFO), Messages.get(WndDataTransfer.this, "import_done_title"));
				title.setRect(MARGIN, 0, WIDTH - 2 * MARGIN, 0);
				add(title);
				int p = (int) title.bottom() + MARGIN;

				RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(WndDataTransfer.this, "import_done"), 6);
				text.maxWidth(WIDTH - 2 * MARGIN);
				text.setPos(MARGIN, p);
				add(text);
				p = (int) text.bottom() + 2 * MARGIN;

				RedButton btnOK = new RedButton(Messages.get(WndDataTransfer.this, "back_to_title")) {
					@Override
					protected void onClick() {
						hide();
						GirlsFrontlinePixelDungeon.switchNoFade(TitleScene.class);
					}
				};
				btnOK.setRect(MARGIN, p, WIDTH - 2 * MARGIN, BTN_HEIGHT);
				add(btnOK);

				resize(WIDTH, (int) btnOK.bottom() + MARGIN);
			}
		});
	}

	//转移包文件名转展示名：GFPD_Data_20260918_153000.zip -> 2026-09-18 15:30:00
	public static String archiveLabel(FileHandle archive) {
		String name = archive.name();
		if (name.startsWith("GFPD_Data_") && name.toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
			name = name.substring("GFPD_Data_".length(), name.length() - ".zip".length()).replace('_', ' ');
			try {
				Date date = new SimpleDateFormat("yyyyMMdd HHmmss").parse(name);
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
			} catch (Exception ignored) {}
			return name;
		}
		return archive.name();
	}

	//统一通过 GameScene.show 展示窗口，确保窗口压栈与遮罩行为一致
	private static void showWindow(Window wnd) {
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(wnd);
	}
}
