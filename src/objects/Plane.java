package objects;

import BVH.BoundingBox;
import Materials.UVMapper;
import core.*;
import Materials.Material;

import java.awt.*;

public class Plane extends Object3D implements UVMapper {


    private Vector3D normal;

    public Plane(Vector3D point, Vector3D normal, Color color) {
        super(point, color);
        this.normal = Vector3D.normalize(normal);
    }
    public Plane(Vector3D point, Vector3D normal, Color color, Material material) {
        super(point, color);
        this.normal = Vector3D.normalize(normal);
        setMaterial(material);
    }


    public Vector3D getNormal() { return normal; }

    @Override
    public Intersection getIntersection(Ray ray) {
        double denom = Vector3D.dotProduct(normal, ray.getDirection());

        // Si es casi cero, el rayo es paralelo al plano
        if (Math.abs(denom) < 1e-8) return null;

        Vector3D p0l0 = Vector3D.substract(getPosition(), ray.getOrigin());
        double t = Vector3D.dotProduct(p0l0, normal) / denom;

        if (t < 1e-4) return null;            // detrás de la cámara o demasiado cerca

        Vector3D hitPos = Vector3D.add(
                ray.getOrigin(),
                Vector3D.scalarMultiplication(ray.getDirection(), t)
        );

        return new Intersection(hitPos, t, normal, this);
    }

    @Override
    public BoundingBox getBoundingBox() {
        throw new UnsupportedOperationException("Plane is infinite — does not have a bounding box.");
    }
    @Override
    public double[] getUV(Vector3D point) {
        double scale = 1.0; // Repite la textura cada 1/4 unidad (ajusta a gusto)

        double u = (point.getX() * scale) % 1;
        double v = (point.getZ() * scale) % 1;

        if (u < 0) u += 1;
        if (v < 0) v += 1;

        return new double[]{u, v};
    }
}
