package com.xz.scorep.executor.pss.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

/**
 * @author by fengye on 2017/5/24.
 */
public class PaintUtils {
    static final Logger LOG = LoggerFactory.getLogger(PaintUtils.class);

    public static final String PNG = "png";
    public static final String JPG = "jpg";

    public static final String SCREEN_SHOT_SUFFIX_PNG = ".png";
    public static final String SCREEN_SHOT_SUFFIX_JPG = ".jpg";

    /**
     * 导入网络图片到缓冲区
     *
     * @param imgName URL地址
     * @return 图片缓存对象
     */
    public static BufferedImage loadImageUrl(String imgName) throws Exception {
        URL url = new URL(imgName);
        return ImageIO.read(url);
    }

    /**
     * 生成新图片到本地
     *
     * @param newImage 本地图片生成路径
     * @param img      图片缓存
     * @param suffix   扩展名
     */
    public static void writeImageLocal(String newImage, BufferedImage img, String suffix) throws Exception {
        if (newImage != null && img != null) {
            File outputFile = new File(newImage);
            ImageIO.write(img, suffix, outputFile);
        } else {
            LOG.error("需指定图片和生成路径！");
        }
    }

    /**
     * 修改图片，返回修改后的图片缓冲区（单行文本）
     *
     * @param img       图片缓存对象
     * @param content   内容
     * @param font      字体
     * @param positionX 宽度坐标
     * @param positionY 高度坐标
     * @return 修改后的图片缓存对象
     */
    public static BufferedImage modifyImage(BufferedImage img, String content, Font font, float positionX, float positionY) {
        Graphics2D g;
        try {
            int w = img.getWidth();
            int h = img.getHeight();

            //起始位置
            float x;//宽度
            float y;//高度

            //计算画笔起始位置
            if (positionX > w || positionY > h) {
                LOG.info("试卷扫描截图出现异常，坐标位置大于图片识别区域！");
                return img;
            } else {
                x = positionX;//宽度
                y = positionY + font.getSize() + 2;//高度
            }

            Color color = new Color(255, 0, 0);

            g = img.createGraphics();
            g.setColor(color);//字体颜色
            g.setFont(font);

            if (content != null) {
                g.drawString(new String(content.getBytes(), "UTF-8"), x, y);
            }
            g.dispose();
        } catch (Exception e) {
            LOG.error("修改图片失败！");
        }

        return img;
    }
}
