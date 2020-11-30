import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

public class TestImageIORead {

    private void readImage() throws IOException {
        String[] fileFormats = new String[] { /*"jpg",*/ "tiff", "bmp", "gif", "png", "wbmp" };
        for (String ext: fileFormats) {
            String fileName = "test." + ext;
            File file = new File("../write/" + fileName);
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            ImageReader reader = ImageIO.getImageReaders(iis).next();
            reader.setInput(iis);

            Iterator<ImageTypeSpecifier> type = reader.getImageTypes(0);
            System.out.println("Read file: " + fileName + " of type: " + type);
        }
    }

    public static void main(String[] args) throws Exception {
        TestImageIORead ioRead = new TestImageIORead();
        ioRead.readImage();
        System.out.println("Test passed!");
    }

}
