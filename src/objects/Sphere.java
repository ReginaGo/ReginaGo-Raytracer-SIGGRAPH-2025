package objects;

import BVH.BoundingBox;
import Materials.Material;
import Materials.UVMapper;
import core.Intersection;
import core.Ray;
import core.Vector3D;

import java.awt.*;

public class Sphere extends Object3D implements UVMapper {
    private double radius;

    public Sphere(Vector3D position, double radius, Color color, Material material) {
        super(position, color);
        setRadius(radius);
        setMaterial(material);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        final double EPSILON = 1e-6;

        Vector3D L = Vector3D.substract(getPosition(), ray.getOrigin());
        double tca = Vector3D.dotProduct(L, ray.getDirection());
        double d2 = Vector3D.dotProduct(L, L) - tca * tca;

        double radius2 = radius * radius;
        if (d2 > radius2) return null;

        double thc = Math.sqrt(radius2 - d2);
        double t0 = tca - thc;
        double t1 = tca + thc;

        double t = (t0 > EPSILON) ? t0 : (t1 > EPSILON ? t1 : -1);
        if (t < 0) return null;

        Vector3D hit = Vector3D.add(ray.getOrigin(), Vector3D.scalarMultiplication(ray.getDirection(), t));
        Vector3D normal = Vector3D.normalize(Vector3D.substract(hit, getPosition()));

        return new Intersection(hit, t, normal, this);
    }

    @Override
    public BoundingBox getBoundingBox() {
        Vector3D pos = getPosition();
        double r = getRadius();
        Vector3D min = new Vector3D(pos.getX() - r, pos.getY() - r, pos.getZ() - r);
        Vector3D max = new Vector3D(pos.getX() + r, pos.getY() + r, pos.getZ() + r);
        return new BoundingBox(min, max);
    }

    @Override
    public double[] getUV(Vector3D point) {
        Vector3D center = getPosition();
        Vector3D p = Vector3D.normalize(Vector3D.substract(point, center));

        double u = 0.5 + (Math.atan2(p.getZ(), p.getX()) / (2 * Math.PI));
        double v = 0.5 - (Math.asin(p.getY()) / Math.PI);

        return new double[]{u, v};
    }
}
