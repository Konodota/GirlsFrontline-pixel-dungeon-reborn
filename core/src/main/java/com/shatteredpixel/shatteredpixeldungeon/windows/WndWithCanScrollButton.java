package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.canScrollButton;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndWithCanScrollButton extends Window {
    public WndWithCanScrollButton(ArrayList<? extends canScrollButton> buttons){
        this(buttons, 1);
    }
    public WndWithCanScrollButton(Image image, String title, String messages, ArrayList<? extends canScrollButton> buttons){
        this(image, title, messages, buttons, 1, new ArrayList<>(), 1);
    }
    public WndWithCanScrollButton(ArrayList<? extends canScrollButton> buttons, int buttonsPerRow){
        this(buttons, buttonsPerRow, new ArrayList<>(), 1);
    }
    public WndWithCanScrollButton(ArrayList<? extends canScrollButton> buttons, ArrayList<Button> buttonsB){
        this(buttons, 1, buttonsB, 1);
    }
    public WndWithCanScrollButton(ArrayList<? extends canScrollButton> buttons, int buttonsPerRow, ArrayList<Button> buttonsB, int buttonsBPerRow){
        this(null, null, null, buttons, buttonsPerRow, buttonsB, buttonsBPerRow);
    }
    public WndWithCanScrollButton(Image image, String titleText, String message, ArrayList<? extends canScrollButton> buttons, int buttonsPerRow, ArrayList<Button> buttonsB, int buttonsBPerRow){
        super();
        int textWidth = PixelScene.landscape() ? 144 : 120;

        final int GAP = 2;
        float pos = GAP;
        if (titleText != null) {
            Component title;
            if (image == null) {
                RenderedTextBlock tfTitle = PixelScene.renderTextBlock(titleText, 9);
                tfTitle.setHightlighting(true);
                tfTitle.hardlight(TITLE_COLOR);
                tfTitle.setPos(GAP, pos);
                tfTitle.maxWidth(textWidth - GAP * 2);
                title = tfTitle;
            }
            else {
                IconTitle tfTitle = new IconTitle(image, titleText);
                tfTitle.setRect(0, pos, textWidth - GAP * 2, 0);
                title = tfTitle;
            }

            add(title);
            pos = title.bottom() + 2*GAP;
        }
        if (message != null) {
            RenderedTextBlock m = PixelScene.renderTextBlock( 6 );
            m.text(message, textWidth);
            m.setPos( 0, pos );
            add( m );
            pos = m.bottom() + 2*GAP;
        }
        final int btnHeight = 16;
        int HEIGHT = 144;
        int lastBtnRow = 0;
        lastBtnRow += (int) Math.ceil(buttons.size() / (float) buttonsPerRow);
        lastBtnRow += (int) Math.ceil(buttonsB.size() / (float) buttonsBPerRow);
        HEIGHT = Math.min(HEIGHT, lastBtnRow * (btnHeight + GAP) + (int) pos);
        resize(120, HEIGHT);
        ScrollPane list = new ScrollPane(new Component()) {

            @Override
            public void onClick(float x, float y) {
                int max_size = buttons.size();
                for (int i = 0; i < max_size; ++i) {
                    if (buttons.get(i).onClick(x, y))
                        break;
                }
            }
        };
        add(list);
        Component content = list.content();
        int width = 120 / buttonsPerRow - (buttonsPerRow - 1) * GAP;
        int col = 0;
        int row = 0;
        for (canScrollButton btn : buttons){

            btn.setRect(col * width, (btnHeight + GAP)*row, width, btnHeight);
            PixelScene.align((Button) btn);
            col++;
            if (col == buttonsPerRow) {
                col = 0;
                row++;
            }
            content.add((Button) btn);
        }
        if (!buttons.isEmpty())
            content.setSize(120, ((Button) buttons.get(buttons.size()-1)).bottom());
        else
            content.setSize(120, 0);
        list.setSize( list.width(), list.height() );
        list.setRect(0, (int) pos, 120, HEIGHT - Math.round(buttonsB.size() / (float) buttonsBPerRow) * (btnHeight + GAP));
        list.scrollTo(0, 0);
        width = 120 / buttonsBPerRow - (buttonsBPerRow - 1) * GAP;
        if (col != 0){
            col = 0;
            row++;
        }
        for (Button btn : buttonsB){

            btn.setRect(col * width, (btnHeight + GAP)*row, width, btnHeight);
            PixelScene.align(btn);
            col++;
            if (col == buttonsPerRow) {
                col = 0;
                row++;
            }
            content.add(btn);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
