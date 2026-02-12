package objects;

import BVH.BoundingBox;
import Materials.Material;
import core.Intersection;
import core.Ray;
import core.Vector3D;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Model3D extends Object3D{
    private List<Triangle> triangles;
    public Model3D(Vector3D position, Triangle[] triangles, Color color, Material material) {
        super(position, color);
        setTriangles(triangles);
        setMaterial(material);
    }

    public List<Triangle> getTriangles() {
        return triangles;
    }

    public void setTriangles(Triangle[] triangles) {
        Vector3D position = getPosition();
        Set<Vector3D> uniqueVertices = new HashSet<>();
        for(Triangle triangle : triangles){
            uniqueVertices.addAll(Arrays.asList(triangle.getVertices()));
        }

        for(Vector3D vertex : uniqueVertices){
            vertex.setX(vertex.getX() + position.getX());
            vertex.setY(vertex.getY() + position.getY());
            vertex.setZ(vertex.getZ() + position.getZ());
        }
        this.triangles = Arrays.asList(triangles);
    }


    @Override
    public Intersection getIntersection(Ray ray) {
        double distance = -1;
        Vector3D position = Vector3D.ZERO();
        Vector3D normal = Vector3D.ZERO();

        for(Triangle triangle : getTriangles()){
            Intersection intersection = triangle.getIntersection(ray);
            if (intersection  == null) continue;
            double intersectionDistance = intersection.getDistance();
            System.out.println("[HIT] d=" + intersection.getDistance());
            //
            if(intersectionDistance > 0 && (intersectionDistance < distance || distance < 0)){
                distance = intersectionDistance;
                position = Vector3D.add(ray.getOrigin(), Vector3D.scalarMultiplication(ray.getDirection(), distance));
                normal = triangle.getNormal(position);
            }
        }

        if(distance == -1){
            return null;
        }

        return new Intersection(position, distance, normal, this);
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (triangles == null || triangles.isEmpty()) {
            return new BoundingBox(getPosition(), getPosition());
        }

        BoundingBox box = triangles.get(0).getBoundingBox();
        for (int i = 1; i < triangles.size(); i++) {
            box = BoundingBox.union(box, triangles.get(i).getBoundingBox());
        }
        return box;
    }
}
