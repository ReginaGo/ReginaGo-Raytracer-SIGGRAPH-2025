package shading;

import Materials.Material;
import Materials.UVMapper;
import core.Ray;
import core.Raytracer;
import objects.Camera;
import core.Intersection;
import core.Vector3D;
import lights.Light;
import objects.Object3D;

import java.awt.Color;
import java.util.List;

public class BlinnPhongShader implements Shader {

    @Override
    public Color shade(List<Object3D> objects, Intersection hit, List<Light> lights, Camera camera) {
        Ray viewRay = hit.getRay();
        if (viewRay != null && viewRay.getDepth() > 5) {
            return new Color(0, 0, 0); // Evita recursión infinita
        }
        Material mat     = hit.getObject().getMaterial(); // material
        if (mat.getTexture() != null &&
                mat.getKa() == 0 && mat.getKd() == 0 && mat.getKs() == 0 &&
                hit.getObject() instanceof UVMapper) {

            double[] uv = ((UVMapper) hit.getObject()).getUV(hit.getPosition());
            return mat.getTexture().getColor(uv[0], uv[1]);
        }
        Color base;        // base color
        if (mat.getTexture() != null && hit.getObject() instanceof UVMapper) {
            double[] uv = ((UVMapper) hit.getObject()).getUV(hit.getPosition());
            base = mat.getTexture().getColor(uv[0], uv[1]);
        } else {
            base = hit.getObject().getColor();
        }
        Object3D caster  = hit.getObject();               // collisioned object
        Vector3D P       = hit.getPosition();             // collision point
        Vector3D N       = Vector3D.normalize(hit.getNormal());
        Vector3D V       = Vector3D.normalize(camera.getPosition().subtract(P)); // view direction

        // Empiezas con el término ambiente
        double r = mat.getKa() * base.getRed();   // material * base color (%red)
        double g = mat.getKa() * base.getGreen();
        double b = mat.getKa() * base.getBlue();

        double EPS = 1e-4; // bias para evitar self-shadowing
        for (Light light : lights) {
            // Vector no normalizado, magnitud/distancia entre el hit y la luz
            Vector3D Lvec      = Vector3D.substract(light.getPosition(), P);
            double lightDist   = Vector3D.magnitude(Lvec);

            // Dirección unitaria del vector
            Vector3D Ldir      = Vector3D.normalize(Lvec);

            // Shadow ray con bias
            Ray shadowRay = new Ray(
                    Vector3D.add(P, Vector3D.scalarMultiplication(N, EPS)),
                    Ldir
            );

            // Ray casting para detectar bloqueadores
            Intersection blocker = Raytracer.raycast(
                    shadowRay,
                    objects,
                    caster,    // evita self‐intersection
                    null
            );

            // Si no hay bloqueadores → se calcula difuso + especular
            if (blocker == null || blocker.getDistance() >= lightDist) {
                double nDotL = Math.max(Vector3D.dotProduct(N, Ldir), 0.0);
                double diff  = mat.getKd() * light.getIntensity() * nDotL;

                Vector3D H   = Vector3D.normalize(Vector3D.add(Ldir, V));
                double nDotH = Math.max(Vector3D.dotProduct(N, H), 0.0);
                double spec  = mat.getKs()
                        * light.getIntensity()
                        * Math.pow(nDotH, mat.getShininess());

                Color lc = light.getColor();
                r += base.getRed()   * diff + lc.getRed()   * spec;
                g += base.getGreen() * diff + lc.getGreen() * spec;
                b += base.getBlue()  * diff + lc.getBlue()  * spec;
            }
        }

        // -------------------- REFLECTION --------------------
        if (mat.getReflectivity() > 0.0) {
            Vector3D I = Vector3D.normalize(P.subtract(camera.getPosition())); // direction incedent
            Vector3D R = Vector3D.substract(I, Vector3D.scalarMultiplication(N, 2 * Vector3D.dotProduct(I, N))); // dirección reflejada

            Ray reflectionRay = new Ray(
                    Vector3D.add(P, Vector3D.scalarMultiplication(N, EPS)),
                    R
            );
            if (viewRay != null) {
                reflectionRay.setDepth(viewRay.getDepth() + 1);
            }

            Intersection reflectionHit = Raytracer.raycast(reflectionRay, objects, caster, null);
            if (reflectionHit != null) {
                Color reflectedColor = shade(objects, reflectionHit, lights, camera); // recursión

                double reflectivity = mat.getReflectivity();
                r = (1 - reflectivity) * r + reflectivity * reflectedColor.getRed();
                g = (1 - reflectivity) * g + reflectivity * reflectedColor.getGreen();
                b = (1 - reflectivity) * b + reflectivity * reflectedColor.getBlue();
            }
        }

        // -------------------- REFRACTION --------------------
        if (mat.getTransparency() > 0.0) {
            double eta = 1.0 / mat.getRefractiveIndex(); // asumimos entrando de aire (1.0) a objeto
            Vector3D I = Vector3D.normalize(P.subtract(camera.getPosition()));
            double cosi = -Vector3D.dotProduct(N, I);
            double k = 1.0 - eta * eta * (1.0 - cosi * cosi);

            if (k >= 0) { // hay transmisión
                Vector3D T = Vector3D.add(
                        Vector3D.scalarMultiplication(I, eta),
                        Vector3D.scalarMultiplication(N, eta * cosi - Math.sqrt(k))
                );

                Ray refractionRay = new Ray(
                        Vector3D.add(P, Vector3D.scalarMultiplication(N, -EPS)), // pushes inwards
                        T
                );
                if (viewRay != null) {
                    refractionRay.setDepth(viewRay.getDepth() + 1); //AVOIDS INFINITE RAY JUMPING AHHHH limit 5 :P
                }

                Intersection refractionHit = Raytracer.raycast(refractionRay, objects, caster, null);
                Color skyColor = new Color(255, 170, 200); // rosa cielo pastel, o la dominante de tu imagen
//                if (refractionHit != null) {
//                    Color refractedColor = shade(objects, refractionHit, lights, camera); // recursivo
//
//                    double transparency = mat.getTransparency();
//                    r = (1 - transparency) * r + transparency * refractedColor.getRed();
//                    g = (1 - transparency) * g + transparency * refractedColor.getGreen();
//                    b = (1 - transparency) * b + transparency * refractedColor.getBlue();
//                }
                double transparency = mat.getTransparency();
                Color refractedColor;

                if (refractionHit != null) {
                    refractedColor = shade(objects, refractionHit, lights, camera);
                } else {
                    refractedColor = skyColor; // ← 🎯 aquí va tu variable simple
                }

                r = (1 - transparency) * r + transparency * refractedColor.getRed();
                g = (1 - transparency) * g + transparency * refractedColor.getGreen();
                b = (1 - transparency) * b + transparency * refractedColor.getBlue();
            }
        }

        // ---------- Alpha Masking ----------
        if (mat.getAlphaMask() != null && hit.getObject() instanceof UVMapper) {
            double[] uv = ((UVMapper) hit.getObject()).getUV(hit.getPosition());

            int texX = (int)(uv[0] * mat.getAlphaMask().getWidth());
            int texY = (int)((1.0 - uv[1]) * mat.getAlphaMask().getHeight()); // invertido

            // Limita dentro del rango válido de la imagen
            texX = Math.max(0, Math.min(texX, mat.getAlphaMask().getWidth() - 1));
            texY = Math.max(0, Math.min(texY, mat.getAlphaMask().getHeight() - 1));

            int alpha = new Color(mat.getAlphaMask().getRGB(texX, texY)).getRed();

            if (alpha < 128) {
                return null; // Es transparente → ignora este impacto
            }


        }

        // -------------------- FOG (Bruma) --------------------
        double fogStart = 6.5;      // desde qué distancia empieza a mezclarse
        double fogEnd   = 24.0;     // a qué distancia ya es completamente niebla
        double dist     = Vector3D.magnitude(P.subtract(camera.getPosition())); // distancia del punto a la cámara

        double rawFactor = Math.max(0, Math.min(1, (fogEnd - dist) / (fogEnd - fogStart))); //1 near 0 far
        double fogFactor = Math.pow(rawFactor, 1.5);

// Color de la bruma (mismo tono que tu cielo rosado pastel)
        Color fogColor = new Color(255,240,230);

// Interpolación lineal entre color del objeto y bruma
        r = fogFactor * r + (1 - fogFactor) * fogColor.getRed();
        g = fogFactor * g + (1 - fogFactor) * fogColor.getGreen();
        b = fogFactor * b + (1 - fogFactor) * fogColor.getBlue();


        return new Color(
                (int) clamp(r),
                (int) clamp(g),
                (int) clamp(b)
        );
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(255, v));
    }
}
