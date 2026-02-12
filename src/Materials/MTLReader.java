package Materials;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class MTLReader {

    public static Map<String, Material> loadMTL(String path) {
        Map<String, Material> materials = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            String currentName = null;
            Material currentMaterial = null;

            File mtlFile = new File(path);
            File mtlParent = mtlFile.getParentFile();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("newmtl ")) {
                    if (currentName != null && currentMaterial != null) {
                        materials.put(currentName, currentMaterial);
                    }
                    currentName = line.substring(7).trim();
                    currentMaterial = new Material(0.1, 0.6, 0.3, 32);
                }

                // Leer textura difusa
                else if (line.startsWith("map_Kd ")) {
                    String textureName = line.substring(7).trim();
                    File textureFile = new File(mtlParent, textureName);
                    try {
                        BufferedImage img = ImageIO.read(textureFile);
                        Texture tex = new Texture(img);
                        if (currentMaterial != null) {
                            currentMaterial.setTexture(tex);
                        }
                        else{
                            System.out.println("Error: texture file does not exist");
                        }
                    } catch (IOException e) {
                        System.err.println("Could not load texture: " + textureFile.getAbsolutePath());
                    }
                }

                // Opcional: lectura de reflectividad, transparencia, etc. (puedes expandir si gustas)
            }

            if (currentName != null && currentMaterial != null) {
                materials.put(currentName, currentMaterial);
            }

        } catch (IOException e) {
            System.err.println("Failed to load .mtl: " + e.getMessage());
        }

        return materials;
    }
}
