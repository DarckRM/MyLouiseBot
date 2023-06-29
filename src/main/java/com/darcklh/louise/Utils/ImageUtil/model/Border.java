package com.darcklh.louise.Utils.ImageUtil.model;

import lombok.Data;

import java.awt.*;

@Data
public class Border {
    private int width;
    private Color color;
    private Type type;

    public enum Type {
        SOLID,
        DOTTED,
        DASHED,

    }
}
