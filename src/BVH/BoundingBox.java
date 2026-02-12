package BVH;

import core.Ray;
import core.Vector3D;

public class BoundingBox {
    public Vector3D min;
    public Vector3D max;
    public static final double EPSILON = 1e-8;

    public BoundingBox(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }

    public static BoundingBox union(BoundingBox a, BoundingBox b) {
        return new BoundingBox(
                Vector3D.min(a.min, b.min),
                Vector3D.max(a.max, b.max)
        );
    }

    public Vector3D getCentroid() {
        return min.add(max).divide(2.0);
    }

    public boolean intersect(Ray ray, double tMin, double tMax) {
        for (int i = 0; i < 3; i++) {
            double origin = ray.getOrigin().get(i);
            double dir = ray.getDirection().get(i);

            if (Math.abs(dir) < EPSILON) {
                if (origin < min.get(i) || origin > max.get(i)) return false;
            } else {
                double invD = 1.0 / dir;
                double t0 = (min.get(i) - origin) * invD;
                double t1 = (max.get(i) - origin) * invD;

                if (invD < 0.0) {
                    double tmp = t0;
                    t0 = t1;
                    t1 = tmp;
                }

                tMin = Math.max(tMin, t0);
                tMax = Math.min(tMax, t1);

                if (tMax <= tMin) return false;
            }
        }
        return true;
    }
}
