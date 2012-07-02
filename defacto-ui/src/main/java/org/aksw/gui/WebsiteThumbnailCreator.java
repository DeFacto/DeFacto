package org.aksw.gui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 6/3/12
 * Time: 5:46 PM
 * Fetches the thumbnail of a website
 */
public class WebsiteThumbnailCreator {
    public static void main( String[] args )
    {
        BufferedImage image = null;
        try {

            URL url = new URL("https://api.url2png.com/v3/P4DE5D1C99D8EF/7bbb6e0d1b74fb0ae1d4f18b06320096/100x100/http://www.masrawy.com");
            image = ImageIO.read(url);

            ImageIO.write(image, "jpg",new File("/home/data/Desktop/out.jpg"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }

    public static String fetchWebsiteThumbnail(String websiteURL, String outputImageFilename){

        BufferedImage image = null;
        try {

            URL url = new URL("https://api.url2png.com/v3/P4DE5D1C99D8EF/7bbb6e0d1b74fb0ae1d4f18b06320096/150x150/" +
                    websiteURL);

            String currentDir = new File(".").getAbsolutePath();

            image = ImageIO.read(url);

            String outputImageFullPath = "/tmp/"
                    + outputImageFilename + ".jpg";

            ImageIO.write(image, "jpg",new File(outputImageFullPath));

            return outputImageFullPath;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

    }
}
