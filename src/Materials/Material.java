package Materials;

import java.awt.image.BufferedImage;

public class Material {
    //COMPONENTS
    private final double Ka;        // ambience
    private final double Kd;        // diffuse
    private final double Ks;        // specular
    private final double shininess; // brightness
    private  double reflectivity = 0.0; // 0 = nothing, 1 = mirror
    private  double transparency = 0.0;
    private  double refractiveIndex = 1.0;
    private Texture texture;
    private BufferedImage alphaMask;

    public BufferedImage getAlphaMask() {
        return alphaMask;
    }

    public void setAlphaMask(BufferedImage alphaMask) {
        this.alphaMask = alphaMask;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setReflectivity(double reflectivity) {
        this.reflectivity = reflectivity;
    }

    public void setTransparency(double transparency) {
        this.transparency = transparency;
    }

    public void setRefractiveIndex(double refractiveIndex) {
        this.refractiveIndex = refractiveIndex;
    }

    public double getReflectivity() {
        return reflectivity;
    }

    public double getTransparency() {
        return transparency;
    }

    public double getRefractiveIndex() {
        return refractiveIndex;
    }

    public Material(double Ka, double Kd, double Ks, double shininess) {
        this.Ka = Ka;
        this.Kd = Kd;
        this.Ks = Ks;
        this.shininess = shininess;
    }

    //for complex obj with alpha masking values

    public Material(double Ka, double Kd, double Ks, double shininess,
                    Texture texture, BufferedImage alphaMask) {
        this.Ka = Ka;
        this.Kd = Kd;
        this.Ks = Ks;
        this.shininess = shininess;
        this.texture = texture;
        this.alphaMask = alphaMask;
    }

    /* getters */
    public double getKa()        { return Ka; }
    public double getKd()        { return Kd; }
    public double getKs()        { return Ks; }
    public double getShininess() { return shininess; }

    /* Material “default” */
    public static Material DEFAULT = new Material(0.10, 0.65, 0.35, 32);
}
