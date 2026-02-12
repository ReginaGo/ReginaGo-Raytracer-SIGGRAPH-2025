package lights;

import BVH.BoundingBox;
import core.Intersection;
import core.Ray;
import core.Vector3D;
import objects.Object3D;

import java.awt.*;

public class SpotLight extends Light {
    private Vector3D position;
    private Vector3D direction;
    private double cutoffAngle; // en grados

    public SpotLight(Vector3D position, Vector3D direction, Color color, double intensity, double cutoffAngleDegrees) {
        super(Vector3D.ZERO(), color, intensity);
        this.position = position;
        this.direction = Vector3D.normalize(direction);
        this.cutoffAngle = Math.toRadians(cutoffAngleDegrees); // lo convertimos a radianes
    }

    @Override
    public double getNDotL(Intersection intersection) {
        Vector3D L = Vector3D.normalize(Vector3D.substract(position, intersection.getPosition()));
        Vector3D toHit = Vector3D.normalize(Vector3D.substract(intersection.getPosition(), position));
        double cosAngle = Vector3D.dotProduct(toHit, direction); // ya normalizados

        // Compara con coseno del cutoff (más eficiente)
        if (cosAngle < Math.cos(cutoffAngle)) return 0.0;

        return Math.max(Vector3D.dotProduct(intersection.getNormal(), L), 0.0);
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        return null; // no se intersecta
    }

    @Override
    public Vector3D getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public Vector3D getDirection() {
        return direction;
    }

    public void setDirection(Vector3D direction) {
        this.direction = Vector3D.normalize(direction);
    }

    public double getCutoffAngle() {
        return Math.toDegrees(cutoffAngle);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(position, position); // sin volumen
    }
}
