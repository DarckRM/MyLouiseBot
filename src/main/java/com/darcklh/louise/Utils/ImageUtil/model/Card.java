package com.darcklh.louise.Utils.ImageUtil.model;

import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

@Data
public class Card extends Element {
    private String title;
    private int radius;
    private String background;
    private Border border;
    private int paddingX = 10;
    private int paddingY = 10;
    private ArrayList<Element> contents = new ArrayList<>();

    @Override
    public void draw(Graphics2D g) {
        g.setFont(getFont());
        g.setColor(getColor());

        if(isShadow()) {
            shadow(g);
        }

        g.fillRoundRect(getX(), getY(), getWidth(), getHeight(), getRadius(), getRadius());

        if (getTitle() != null) {
            g.setColor(Color.WHITE);
            g.drawString(getTitle(), getX() + getPaddingX(), getY() + g.getFont().getSize() + getPaddingY());
        }

        // 绘制子元素
        if (!contents.isEmpty()) {
            // 重新计算绘制高度和宽度
            setX(getX() + getPaddingX());
            setY(getY() + g.getFont().getSize() + getPaddingY());
            for (Element content : contents) {
                content.setX(getX());
                content.setY(getY());
                content.draw(g);
            }
        }

    }

    @Override
    public void shadow(Graphics2D g2d) {
        // 需要给阴影留出空间 创建一个比 Card 大一点的 BufferedImage
        BufferedImage shadowArea = new BufferedImage(getWidth() + 20, getHeight() + 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D area = shadowArea.createGraphics();
        Color color = getColor().darker();
        area.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        area.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_DEFAULT);
        int x = 10, y = 10, a = 200, w = getWidth(), h =getHeight();
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        for (int i = 0; i <= 5; i++) {
            color = new Color(r, g, b, a);
            area.setColor(color);
            area.drawRoundRect(x, y, w, h, getRadius(), getRadius());
            x--;
            y--;
            w += 2;
            h += 2;
            a -= 40;
        }
        Image image = Image.build().buffered(shadowArea).blur(8);

        g2d.drawImage(image.getImage(), getX() - 10, getY() - 10, null);
    }

    public Card addText(Text text) {
        return this;
    }
}
