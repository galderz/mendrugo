import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;

public class Example
{
    static final WritableRaster RASTER;

    static
    {
        RASTER = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, 1, 1, 3, new Point(0, 0));
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println(RASTER);
    }
}
