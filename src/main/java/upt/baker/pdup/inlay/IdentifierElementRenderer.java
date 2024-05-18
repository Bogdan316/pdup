package upt.baker.pdup.inlay;

import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.FontInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.GraphicsUtil;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

public class IdentifierElementRenderer implements EditorCustomElementRenderer {
    private static final int HORIZONTAL_MARGIN = 3;
    private static final int HORIZONTAL_PADDING = 5;
    private static final int VERTICAL_PADDING = 3;
    private static final int ARC_RADIUS = 8;

    private static final JBColor BG_COLOR = new JBColor(0x333333, 0x333333);

    private static final JBColor FONT_COLOR = new JBColor(0x787878, 0x787878);
    private final String body;

    public IdentifierElementRenderer(String body) {
        this.body = body;
    }

    private static FontInfo getFontInfo(@NotNull Editor editor) {
        var colorsScheme = editor.getColorsScheme();
        var fontPreferences = colorsScheme.getFontPreferences();
        return new FontInfo(
                fontPreferences.getFontFamily(),
                fontPreferences.getSize(fontPreferences.getFontFamily()) * 0.85f,
                Font.ITALIC,
                fontPreferences.useLigatures(),
                FontInfo.getFontRenderContext(editor.getContentComponent())
        );
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        var fontInfo = getFontInfo(inlay.getEditor());
        return fontInfo.fontMetrics().stringWidth(body) + 2 * (HORIZONTAL_PADDING + HORIZONTAL_MARGIN);
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
        GraphicsUtil.setupRoundedBorderAntialiasing(g);
        g.setColor(BG_COLOR);
        g.fillRoundRect(
                targetRegion.x + HORIZONTAL_MARGIN,
                targetRegion.y + VERTICAL_PADDING,
                targetRegion.width - 2 * HORIZONTAL_MARGIN,
                targetRegion.height - 2 * VERTICAL_PADDING,
                ARC_RADIUS,
                ARC_RADIUS
        );
        var fontInfo = getFontInfo(inlay.getEditor());
        g.setFont(fontInfo.getFont());
        g.setColor(FONT_COLOR);
        g.drawString(body, targetRegion.x + HORIZONTAL_PADDING + HORIZONTAL_MARGIN, targetRegion.y + fontInfo.fontMetrics().getAscent() + 2);
    }
}
