package objects;

import core.Intersection;
import core.Ray;

public interface IIntersectable {
    public abstract Intersection getIntersection(Ray ray);
}
