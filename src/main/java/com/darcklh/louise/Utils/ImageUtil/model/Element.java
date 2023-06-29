package com.darcklh.louise.Utils.ImageUtil.model;

import lombok.Data;

import java.awt.*;

@Data
public abstract class Element {
    private int x;
    private int y;
    private int width;
    private int height;
    private int marginX;
    private int marginY;
    private int paddingX;
    private int paddingY;
    private boolean display;
    private Color color;
    private Font font;
    private boolean shadow;

    public Element init() {
        return this;
    }

    public void draw(Graphics2D g) {
    }

    public void shadow(Graphics2D g) {

    }

}
