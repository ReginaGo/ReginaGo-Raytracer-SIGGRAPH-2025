package lights;

import BVH.BoundingBox;
import core.Intersection;
import core.Ray;
import core.Vector3D;

import java.awt.*;

public class PointLight extends Light {
    private Vector3D position;

    public PointLight(Vector3D position, Color color, double intensity) {
        super(Vector3D.ZERO(), color, intensity);
        setPosition(position);
    }

    @Override
    public double getNDotL(Intersection intersection) {
        Vector3D lightDir = Vector3D.normalize(Vector3D.substract(position, intersection.getPosition()));
        return Math.max(Vector3D.dotProduct(intersection.getNormal(), lightDir), 0.0);
    }

    @Override
    public Vector3D getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        return null; // PointLight is not intersectable
    }

    @Override
    public BoundingBox getBoundingBox() {
        // Lights have no volume — we return a degenerate box at the light's position
        return new BoundingBox(position, position);
    }
}
