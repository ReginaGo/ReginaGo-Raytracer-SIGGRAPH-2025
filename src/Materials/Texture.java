package Materials;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Texture {
    private BufferedImage image;

    public Texture(String path) {
        try {
            image = ImageIO.read(new File(path));
        } catch (Exception e) {
            System.err.println("Error loading texture: " + e.getMessage());
        }
    }

    public Texture(BufferedImage image) {
        this.image = image;
    }


    // u, v ∈ [0, 1]
    public Color getColor(double u, double v) {
        if (image == null) return Color.BLACK;

        int width = image.getWidth();
        int height = image.getHeight();

        int x = (int) (u * width) % width;
        int y = (int) ((1 - v) * height) % height; // image goes  from top to bottom

        x = (x < 0) ? x + width : x;
        y = (y < 0) ? y + height : y;

        return new Color(image.getRGB(x, y));
    }
}
