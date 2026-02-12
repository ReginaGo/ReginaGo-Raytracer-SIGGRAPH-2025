package lights;

import BVH.BoundingBox;
import core.Intersection;
import core.Ray;
import core.Vector3D;

import java.awt.*;

public class DirectionalLight extends Light{
    public Vector3D direction;

    public DirectionalLight(Vector3D direction, Color color, double intensity) {
        super(Vector3D.ZERO(), color, intensity);
        setDirection(direction);
    }

    @Override
    public double getNDotL(Intersection intersection) {
        return Math.max(Vector3D.dotProduct(intersection.getNormal(), Vector3D.scalarMultiplication(getDirection(), -1.0)), 0.0);
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        return null;
    }

    public Vector3D getDirection() {
        return direction;
    }

    public void setDirection(Vector3D direction) {
        this.direction = Vector3D.normalize(direction);
    }

    @Override
    public BoundingBox getBoundingBox() {
        // Directional light doesn't have volume nor position
        Vector3D dummy = new Vector3D(0, 0, 0);
        return new BoundingBox(dummy, dummy);
    }
}

