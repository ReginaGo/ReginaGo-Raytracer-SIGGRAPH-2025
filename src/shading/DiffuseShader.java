package shading;

import objects.Camera;
import core.Intersection;
import core.Vector3D;
import lights.Light;
import objects.Object3D;

import java.awt.Color;
import java.util.List;

public class DiffuseShader implements Shader {


    @Override
    public Color shade(List<Object3D> objects, Intersection hit, List<Light> lights, Camera camera) {

        Color objColor  = hit.getObject().getColor();
        Color pixel     = Color.BLACK;
        double EPS = 1e-4; //bias

        for (Light light : lights) {
            double nDotL      = light.getNDotL(hit);
            Color  lightColor = light.getColor();
            double intensity  = light.getIntensity() * nDotL;

            double[] lc = { lightColor.getRed()/255.0,
                    lightColor.getGreen()/255.0,
                    lightColor.getBlue()/255.0 };

            double[] oc = { objColor.getRed()/255.0,
                    objColor.getGreen()/255.0,
                    objColor.getBlue()/255.0 };

            for (int k = 0; k < 3; k++) {
                oc[k] *= intensity * lc[k];
            }

            Color diffuse = new Color(
                    (float) clamp(oc[0]), (float) clamp(oc[1]), (float) clamp(oc[2]));

            pixel = add(pixel, diffuse);
        }
        return pixel;
    }

    private static double clamp(double v) { return Math.max(0.0, Math.min(1.0, v)); }

    private static Color add(Color a, Color b){
        return new Color(
                (float) clamp(a.getRed()/255.0 + b.getRed()/255.0),
                (float) clamp(a.getGreen()/255.0 + b.getGreen()/255.0),
                (float) clamp(a.getBlue()/255.0 + b.getBlue()/255.0));
    }
}
