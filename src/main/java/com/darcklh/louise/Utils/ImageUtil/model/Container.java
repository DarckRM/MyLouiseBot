package com.darcklh.louise.Utils.ImageUtil.model;

import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
@Data
public class Container {
    private int width;
    private int height;
    private int radius;
    private int restWidth;
    private int restHeight;

    private int paddingX;
    private int paddingY;
    private int startX;
    private int startY;
    private Layout containerLayout;
    private List<Element> elements = new ArrayList<>();
    private BufferedImage image;

    public static Container build(int width, int height, int radius, int paddingX, int paddingY, Layout containerLayout) {
        Container container = new Container();
        container.width = width;
        container.height = height;
        container.radius = radius;
        container.paddingX = paddingX;
        container.paddingY = paddingY;
        container.restWidth = width - paddingX * 2;
        container.restHeight = height - paddingY * 2;
        container.startX = paddingX;
        container.startY = paddingY;
        container.containerLayout = containerLayout;
        container.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return container;
    }
    public static Container build(BufferedImage image, int radius, int paddingX, int paddingY, Layout containerLayout) {
        Container container = new Container();
        container.width = image.getWidth();
        container.height = image.getHeight();
        container.radius = radius;
        container.paddingX = paddingX;
        container.paddingY = paddingY;
        container.restWidth = image.getWidth() - paddingX * 2;
        container.restHeight = image.getHeight() - paddingY * 2;
        container.startX = paddingX;
        container.startY = paddingY;
        container.containerLayout = containerLayout;
        container.image = image;
        return container;
    }
    public Container background(BufferedImage newImage) {
        image = newImage;
        width = image.getWidth();
        height = image.getHeight();
        return this;
    }
    public Container add(Element element) {
        boolean result = switch (containerLayout) {
            case AUTO -> false;
            case FLEX -> false;
            case ROW -> rowAdd(element);
        };
        if (!result)
            throw new IllegalArgumentException("添加 Element 失败");
        return this;
    }

    private boolean rowAdd(Element element) {
        // 判断 Container 是否可以放下 Element
        if (element.getWidth() <= restWidth) {
            element.setX(startX);
            element.setY(startY);
            // ROW 布局下所有 Element 作画 Y 轴坐标相同 只影响 X 轴坐标
            startX += element.getWidth() + element.getMarginX();
            restWidth -= element.getWidth() + element.getMarginX();
            elements.add(element);
            return true;
        }
        else
            throw new IllegalArgumentException("Container 无法放下 Element");
    }
    public String getBase64Url() {
        return "http://";
    }
    public BufferedImage getBufferedImage() {
        // 根据 Container 的 padding 变化定义 Element 在 Container 中的位置
        for (Element element : elements) {
            Graphics2D g2d = image.createGraphics();
            // 平滑字体
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_DEFAULT);

            element.draw(g2d);
        }
        return image;
    }

    /**
     * Container 的布局方式
     * AUTO: 自动布局
     * FLEX: 根据 Element 的宽高自动布局
     * ROW: 每个 Element 占据一行
     */
    public enum Layout {
        AUTO,
        FLEX,
        ROW
    }
}
