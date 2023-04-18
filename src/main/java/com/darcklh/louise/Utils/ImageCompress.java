package com.darcklh.louise.Utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

/**
 * @author DarckLH
 * @date 2022/4/22 18:56
 * @Description
 */
@Slf4j
public class ImageCompress {


    public static void compress(BufferedImage image, String filePath, String filename) throws IOException {

        //判断目录是否存在
        File folder = new File(filePath);

        if (!folder.exists() && !folder.isDirectory()) {
            log.info("创建了图片索引库缓存文件夹" + filePath);
            folder.mkdirs();
        }

        File compressedImageFile = new File(filePath + filename);
        OutputStream os = new FileOutputStream(compressedImageFile);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");

        ImageWriter writer = writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        //压缩图片的质量,数值越小压缩率越高,图片质量越低
        param.setCompressionQuality(0.2f);
        writer.write(null, new IIOImage(image, null, null), param);

        os.close();
        ios.close();
        writer.dispose();
    }

    public static void resize(String src, String filename) throws IOException {

        BufferedImage img = null;
        BufferedImage tempPNG = null;

        img = ImageIO.read(new File(src + "/" + filename));
        tempPNG = resizeImage(img, img.getWidth() / 2, img.getHeight() / 2);

        //裁剪不压缩,存储
        // ImageIO.write(tempPNG, "png", new File(dest));
        //压缩后输出
        compress(tempPNG, src.replaceAll("images", "images_index") + "/", filename);
        log.info("图片 " + filename + " 压缩完成");
    }

    public static BufferedImage resizeImage(final Image image, int width, int height) {

        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //创建graph2D,用来将image写入bufferedImage
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        //below three lines are for RenderingHints for better image quality at cost of higher processing time
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }

}
