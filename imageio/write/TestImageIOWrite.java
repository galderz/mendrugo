import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TestImageIOWrite {

    static Color[] colors = new Color[] {
        Color.white, Color.red, Color.green,
        Color.blue, Color.black };

    static final int dx = 50;
    static final int h = 100;

    public static void main(String[] args) throws Exception {
        TestImageIOWrite iIo = new TestImageIOWrite();
        iIo.test();
    }

    private void test() throws IOException {
        BufferedImage src = createTestImage();

        String[] fileFormats = new String[] { /*"jpg",*/ "tiff", "bmp", "gif", "png", "wbmp" };
        for (int i = 0; i < fileFormats.length; i++) {
            String fName = "test." + fileFormats[i];
            File f = new File(fName);

            System.out.println("Writing image: " + fName);
            if (!ImageIO.write(src, fileFormats[i].toUpperCase(), f)) {
                throw new RuntimeException("Failed to write test image.");
            }
        }
        System.out.println("Test passed");
    }

    private static BufferedImage createTestImage() {
        BufferedImage img = new BufferedImage(dx * colors.length, h, TYPE_BYTE_BINARY);

        Graphics2D g = img.createGraphics();
        for (int i = 0; i < colors.length; i++) {
            g.setColor(colors[i]);
            g.fillRect(i * dx, 0, dx, h);
        }
        g.dispose();

        return img;
    }

}
