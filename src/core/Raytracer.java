package core;


import Materials.Texture;
import lights.DirectionalLight;
import lights.Light;
import lights.PointLight;
import objects.*;
import shading.Shader;
import tools.OBJReader;


import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Raytracer {


    private final Shader shader; //CHANGES!!!!


    public Raytracer(Shader shader) {
        this.shader = shader; //CHANGESSS!!!! DELETE
    }

    public  BufferedImage raytrace(Scene scene) {
        double aperture = 0.01;         // tamaño de apertura (cuánto desenfoque)
        double focalDistance = Vector3D.magnitude(new Vector3D(1, -1, 1).subtract(new Vector3D(0, 0, -4)));// distancia que debe quedar enfocada
        int dofSamples = 16;            // rayos por píxel (entre 8 y 64 está bien)
        boolean enableDOF = false;       // para activarlo o desactivarlo fácilmente

        Camera mainCamera = scene.getCamera();
        Texture skyTex = new Texture("textures/sky.jpg");
        double[] nearFarPlanes = mainCamera.getNearFarPlanes();

        BufferedImage image = new BufferedImage(
                mainCamera.getResolutionWidth(),
                mainCamera.getResolutionHeight(),
                BufferedImage.TYPE_INT_RGB);

        List<Object3D> objects = scene.getObjects();
        List<Light> lights = scene.getLights();
        Vector3D[][] posRaytrace = mainCamera.calculatePositionsToRay();
       // Vector3D pos = mainCamera.getPosition(); //CHANGE!!!
        Vector3D CamPos = mainCamera.getPosition();
        double cameraZ = CamPos.getZ(); //every CamPos used to be pos

        for (int i = 0; i < posRaytrace.length; i++) {
            for (int j = 0; j < posRaytrace[i].length; j++) {

                Color accumulatedColor = new Color(0, 0, 0);

                for (int s = 0; s < (enableDOF ? dofSamples : 1); s++) {
                    // Dirección del rayo base (desde CamPos al píxel)
                    double x = posRaytrace[i][j].getX() + CamPos.getX();
                    double y = posRaytrace[i][j].getY() + CamPos.getY();
                    double z = posRaytrace[i][j].getZ() + CamPos.getZ();
                    Vector3D pixelPoint = new Vector3D(x, y, z);

                    Vector3D rayOrigin = CamPos;
                    if (enableDOF) {
                        rayOrigin = getApertureSample(CamPos, aperture);
                    }

                    Vector3D focalPoint = pixelPoint;
                    if (enableDOF) {
                        Vector3D dirToPixel = Vector3D.normalize(pixelPoint.subtract(rayOrigin)); // ← nota que usamos rayOrigin aquí
                        focalPoint = rayOrigin.add(Vector3D.scalarMultiplication(dirToPixel, focalDistance));

                    }

                    Vector3D finalDir = Vector3D.normalize(focalPoint.subtract(rayOrigin));
                    Ray ray = new Ray(rayOrigin, finalDir);

                    Intersection hit = raycast(ray, objects, null,
                            new double[]{cameraZ + nearFarPlanes[0], cameraZ + nearFarPlanes[1]});

                    Color sampleColor;

                    if (hit != null) {
                        sampleColor = shader.shade(objects, hit, lights, mainCamera);

                        if (sampleColor == null) {
                            Vector3D dir = Vector3D.normalize(ray.getDirection());
                            double u = 0.5 + (Math.atan2(dir.getZ(), dir.getX()) / (2 * Math.PI));
                            double v = 0.5 - (Math.asin(dir.getY()) / Math.PI);
                            sampleColor = skyTex.getColor(u, v);
                        }
                    } else {
                        Vector3D dir = Vector3D.normalize(ray.getDirection());
                        double u = 0.5 + (Math.atan2(dir.getZ(), dir.getX()) / (2 * Math.PI));
                        double v = 0.5 - (Math.asin(dir.getY()) / Math.PI);
                        sampleColor = skyTex.getColor(u, v);
                    }

                    accumulatedColor = addColors(accumulatedColor, sampleColor);
                }

                Color finalColor = divideColor(accumulatedColor, enableDOF ? dofSamples : 1);
                image.setRGB(i, j, finalColor.getRGB());
            }
        }
        return image;
    }

    public static Vector3D getApertureSample(Vector3D center, double radius) {
        double r = radius * Math.sqrt(Math.random());
        double theta = 2 * Math.PI * Math.random();
        double dx = r * Math.cos(theta);
        double dy = r * Math.sin(theta);
        return new Vector3D(center.getX() + dx, center.getY() + dy, center.getZ());
    }

    public static Color addColors(Color c1, Color c2) {
        int r = c1.getRed() + c2.getRed();
        int g = c1.getGreen() + c2.getGreen();
        int b = c1.getBlue() + c2.getBlue();
        return new Color(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }

    public static Color divideColor(Color c, int samples) {
        return new Color(c.getRed() / samples, c.getGreen() / samples, c.getBlue() / samples);
    }


    public static Intersection raycast(Ray ray, List<Object3D> objects, Object3D caster, double[] clippingPlanes) {
        Intersection closestIntersection = null;

        for (int i = 0; i < objects.size(); i++) {
            Object3D currObj = objects.get(i);
            if (caster == null || !currObj.equals(caster)) {
                Intersection intersection = currObj.getIntersection(ray);
                if (intersection != null) {
                    double distance = intersection.getDistance();
                    double intersectionZ = intersection.getPosition().getZ();

                    if (distance >= 0 &&
                            (closestIntersection == null || distance < closestIntersection.getDistance()) &&
                            (clippingPlanes == null || (intersectionZ >= clippingPlanes[0] && intersectionZ <= clippingPlanes[1]))) {
                        closestIntersection = intersection;
                        closestIntersection.setRay(ray);
                    }
                }
            }
        }

        return closestIntersection;
    }

}
