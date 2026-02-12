package objects;

import Materials.Material;
import Materials.UVMapper;
import core.Intersection;
import core.Ray;
import core.Vector3D;
import BVH.BoundingBox;

import java.awt.*;


public class Triangle extends Object3D implements IIntersectable, UVMapper {
    public static final double EPSILON = 0.0000000000001;
    private Vector3D[] vertices;
    private Vector3D[] normals;
    private double[][] uv; // tres pares {u, v}

    public void setUV(double[][] uv) {
        this.uv = uv;
    }

    public double[][] getUV() {
        return uv;
    }


    private int smoothingGroup = -1; // -1 = without smoothing


    public void setSmoothingGroup(int group) {
        this.smoothingGroup = group;
    }


    public int getSmoothingGroup() {
        return smoothingGroup;
    }


    // Usado por OBJReader para triángulos con normales
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D[] normals) {
        super(Vector3D.ZERO(), Color.WHITE);
        setVertices(v0, v1, v2);
        setNormals(normals);
    }

    // Usado por código que no tiene normales (flat shading)
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2) {
        super(Vector3D.ZERO(), Color.WHITE);
        setVertices(v0, v1, v2);
        setNormals(null);
    }

    // Constructor completo con material, posición y color (opcional)
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D[] normals, Vector3D position, Color color, Material material) {
        super(position, color);
        setVertices(v0, v1, v2);
        setNormals(normals);
        setMaterial(material);
    }

    // Flat shading con posición y color (opcional)
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D position, Color color) {
        super(position, color);
        setVertices(v0, v1, v2);
        setNormals(null);
    }


    public Vector3D[] getVertices() {
        return vertices;
    }

    private void setVertices(Vector3D[] vertices) {
        this.vertices = vertices;
    }

    public void setVertices(Vector3D v0, Vector3D v1, Vector3D v2) {
        setVertices(new Vector3D[]{v0, v1, v2});
    }

    public Vector3D getNormal(Vector3D point){
        Vector3D normal = Vector3D.ZERO();
        Vector3D[] normals = getNormals();

        if(normals == null){
            Vector3D[] vertices = getVertices();
            Vector3D v = Vector3D.substract(vertices[1], vertices[0]);
            Vector3D w = Vector3D.substract(vertices[0], vertices[2]);
            normal = Vector3D.normalize(Vector3D.crossProduct(v, w));
            return normal;
        }

        Vector3D[] vertices = getVertices();
        Vector3D a = vertices[0];
        Vector3D b = vertices[1];
        Vector3D c = vertices[2];

        // Coordenadas baricéntricas
        Vector3D v0 = Vector3D.substract(b, a);
        Vector3D v1 = Vector3D.substract(c, a);
        Vector3D v2 = Vector3D.substract(point, a);

        double d00 = Vector3D.dotProduct(v0, v0);
        double d01 = Vector3D.dotProduct(v0, v1);
        double d11 = Vector3D.dotProduct(v1, v1);
        double d20 = Vector3D.dotProduct(v2, v0);
        double d21 = Vector3D.dotProduct(v2, v1);

        double denom = d00 * d11 - d01 * d01;
        double beta = (d11 * d20 - d01 * d21) / denom;
        double gamma = (d00 * d21 - d01 * d20) / denom;
        double alpha = 1.0 - beta - gamma;

        // Interpolación
        Vector3D n0 = normals[0];
        Vector3D n1 = normals[1];
        Vector3D n2 = normals[2];

        Vector3D interpolatedNormal = Vector3D.add(
                Vector3D.add(Vector3D.scalarMultiplication(n0, alpha), Vector3D.scalarMultiplication(n1, beta)),
                Vector3D.scalarMultiplication(n2, gamma)
        );

        //System.out.println(interpolatedNormal);

        return Vector3D.normalize(interpolatedNormal);

    }

    public Vector3D[] getNormals() {
        return normals;
    }

    private void setNormals(Vector3D[] normals) {
        this.normals = normals;
    } //setting the array or normals

    @Override
    public Intersection getIntersection(Ray ray) {


        final double EPSILON = 1e-8;

        Vector3D[] vert = getVertices();
        Vector3D edge1  = Vector3D.substract(vert[1], vert[0]);
        Vector3D edge2  = Vector3D.substract(vert[2], vert[0]);

        Vector3D pvec = Vector3D.crossProduct(ray.getDirection(), edge2);
        double det = Vector3D.dotProduct(edge1, pvec);

        // Si el determinante es ~0 el rayo es paralelo al triángulo ─> no hay intersección
        if (Math.abs(det) < EPSILON) return null;

        double invDet = 1.0 / det;

        Vector3D tvec = Vector3D.substract(ray.getOrigin(), vert[0]);
        double u = Vector3D.dotProduct(tvec, pvec) * invDet;
        if (u < 0.0 || u > 1.0) return null;

        Vector3D qvec = Vector3D.crossProduct(tvec, edge1);
        double v = Vector3D.dotProduct(ray.getDirection(), qvec) * invDet;
        if (v < 0.0 || (u + v) > 1.0) return null;

        double t = Vector3D.dotProduct(edge2, qvec) * invDet;
        if (t < EPSILON) return null;          // El triángulo está detrás de la cámara

        Vector3D hitPos = Vector3D.add(ray.getOrigin(),
                Vector3D.scalarMultiplication(ray.getDirection(), t));
        Vector3D hitNormal = getNormal(hitPos);

        return new Intersection(hitPos, t, hitNormal, this); //changed object: null to this
    }

    @Override
    public BoundingBox getBoundingBox() {
        Vector3D[] v = this.getVertices();
        Vector3D min = Vector3D.min(Vector3D.min(v[0], v[1]), v[2]);
        Vector3D max = Vector3D.max(Vector3D.max(v[0], v[1]), v[2]);
        return new BoundingBox(min, max);
    }

    @Override
    public double[] getUV(Vector3D point) {
        if (uv == null) return new double[]{0, 0};

        // Barycentric interpolation
        Vector3D[] verts = getVertices();

        Vector3D v0 = Vector3D.substract(verts[1], verts[0]);
        Vector3D v1 = Vector3D.substract(verts[2], verts[0]);
        Vector3D v2 = Vector3D.substract(point, verts[0]);

        double d00 = Vector3D.dotProduct(v0, v0);
        double d01 = Vector3D.dotProduct(v0, v1);
        double d11 = Vector3D.dotProduct(v1, v1);
        double d20 = Vector3D.dotProduct(v2, v0);
        double d21 = Vector3D.dotProduct(v2, v1);

        double denom = d00 * d11 - d01 * d01;
        double beta = (d11 * d20 - d01 * d21) / denom;
        double gamma = (d00 * d21 - d01 * d20) / denom;
        double alpha = 1.0 - beta - gamma;

        double u = alpha * uv[0][0] + beta * uv[1][0] + gamma * uv[2][0];
        double v = alpha * uv[0][1] + beta * uv[1][1] + gamma * uv[2][1];

        return new double[]{u, v};
    }

}
