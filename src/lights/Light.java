package lights;

import core.*;
import objects.Object3D;

import java.awt.*;

public abstract class Light extends Object3D {
    private double intensity;

    public Light(Vector3D position, Color color, double intensity) {
        super(position, color);
        setIntensity(intensity);
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public abstract double getNDotL(Intersection intersection);
}
