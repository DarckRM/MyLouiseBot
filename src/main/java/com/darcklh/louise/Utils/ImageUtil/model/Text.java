package com.darcklh.louise.Utils.ImageUtil.model;

import lombok.Data;

import java.awt.*;

@Data
public class Text extends Element {
    private String text;
    private Border border;

    @Override
    public void draw(Graphics2D g) {
        g.setFont(getFont());
        g.setColor(getColor());

        if(isShadow()) {
            shadow(g);
        }
        String[] finalText = getText().split("\n");
        for (String s : finalText) {
            g.drawString(s, getX() + getPaddingX(), getY() + g.getFont().getSize() + getPaddingY());
            setY(getY() + getFont().getSize());
        }

    }
}
