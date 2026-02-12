package BVH;

import core.Intersection;
import core.Ray;
import core.Vector3D;
import objects.Object3D;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class BVHNode extends Object3D {
    private static final int MAX_OBJECTS_PER_LEAF = 2;

    private BoundingBox bounds;
    private BVHNode left;
    private BVHNode right;
    private List<Object3D> objects; // solo en nodos hoja

    public BVHNode(List<Object3D> objs) {
        super(Vector3D.ZERO(), Color.BLACK); // No se usan en nodos internos
        build(objs);
    }

    private void build(List<Object3D> objs) {
        this.bounds = computeBounds(objs);

        if (objs.size() <= MAX_OBJECTS_PER_LEAF) {
            this.objects = objs;
            return;
        }

        // Elegir eje con mayor extensión
        Vector3D extent = bounds.max.subtract(bounds.min);
        int axis = (extent.getX() > extent.getY() && extent.getX() > extent.getZ()) ? 0 :
                (extent.getY() > extent.getZ()) ? 1 : 2;

        objs.sort(Comparator.comparingDouble(o -> o.getBoundingBox().getCentroid().get(axis)));

        int mid = objs.size() / 2;
        this.left = new BVHNode(objs.subList(0, mid));
        this.right = new BVHNode(objs.subList(mid, objs.size()));
    }

    private BoundingBox computeBounds(List<Object3D> objs) {
        BoundingBox box = objs.get(0).getBoundingBox();
        for (int i = 1; i < objs.size(); i++) {
            box = BoundingBox.union(box, objs.get(i).getBoundingBox());
        }
        return box;
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        return intersect(ray, 0.001, Double.POSITIVE_INFINITY);
    }

    public Intersection intersect(Ray ray, double tMin, double tMax) {
        if (!bounds.intersect(ray, tMin, tMax)) return null;

        if (objects != null) {
            Intersection closest = null;
            double closestT = tMax;

            for (Object3D obj : objects) {
                Intersection hit = obj.getIntersection(ray);
                if (hit != null && hit.getDistance() < closestT) {
                    closest = hit;
                    closestT = hit.getDistance();
                }
            }
            return closest;
        }

        Intersection hitLeft = left.intersect(ray, tMin, tMax);
        Intersection hitRight = right.intersect(ray, tMin, (hitLeft != null) ? hitLeft.getDistance() : tMax);

        if (hitLeft != null && hitRight != null)
            return hitLeft.getDistance() < hitRight.getDistance() ? hitLeft : hitRight;
        else
            return (hitLeft != null) ? hitLeft : hitRight;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bounds;
    }
}
