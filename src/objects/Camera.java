package objects;


import java.awt.*;
import core.*;

public class Camera extends Object3D {
    //FOV[0] = Horizontal | FOV[1] = Vertical
    private double[] fieldOfView = new double[2];
    private double defaultZ = 15.0;
    private int[] resolution = new int[2];
    private double[] nearFarPlanes = new double[2];

    public Camera(Vector3D position, double fovH, double fovV,
                  int width, int height, double nearPlane, double farPlane) {
        super(position, Color.BLACK);
        setFOV(fovH, fovV);
        setResolution(width, height);
        setNearFarPlanes(new double[]{nearPlane, farPlane});
    }

    private void recomputeFovVertical() {
        double aspect = (double) getResolutionHeight() / getResolutionWidth(); // H/W
        double fovHrad = Math.toRadians(fieldOfView[0]);                       // a rad
        fieldOfView[1] = Math.toDegrees(                                       // ← en grados
                2.0 * Math.atan(Math.tan(fovHrad / 2.0) * aspect)
        );
    }

    public double[] getFieldOfView() {
        return fieldOfView;
    }

    private void setFieldOfView(double[] fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    public double getFOVHorizontal() {

        return fieldOfView[0];
    }

    public double getFOVVertical() {
        return fieldOfView[1];
    }

    public void setFOVHorizontal(double fovH) {
        fieldOfView[0] = fovH;
        recomputeFovVertical();

    }

    public void setFOVVertical(double fovV) {
        fieldOfView[1] = fovV;
    }

    public void setFOV(double fovH, double fovV) {
        setFOVHorizontal(fovH);
        setFOVVertical(fovV);
    }

    public double getDefaultZ() {
        return defaultZ;
    }

    public void setDefaultZ(double defaultZ) {
        this.defaultZ = defaultZ;
    }

    public int[] getResolution() {
        return resolution;
    }

    public void setResolutionWidth(int width) {
        resolution[0] = width;
    }

    public void setResolutionHeight(int height) {
        resolution[1] = height;
    }

    public void setResolution(int width, int height) {
        setResolutionWidth(width);
        setResolutionHeight(height);
        recomputeFovVertical();
    }

    public int getResolutionWidth() {
        return resolution[0];
    }

    public int getResolutionHeight() {
        return resolution[1];
    }

    private void setResolution(int[] resolution) {
        this.resolution = resolution;
    }

    public double[] getNearFarPlanes() {
        return nearFarPlanes;
    }

    private void setNearFarPlanes(double[] nearFarPlanes) {
        this.nearFarPlanes = nearFarPlanes;
    }


    public Vector3D[][] calculatePositionsToRay() {
        int W = getResolutionWidth();
        int H = getResolutionHeight();

        double fovHrad = Math.toRadians(getFOVHorizontal());
        double fovVrad = Math.toRadians(getFOVVertical());

        double halfW = Math.tan(fovHrad / 2.0) * defaultZ;   // ancho/2 del plano
        double halfH = Math.tan(fovVrad / 2.0) * defaultZ;   // alto/2 del plano

        Vector3D[][] positions = new Vector3D[W][H];

        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {

                // normaliza píxel a rango [-1,1] centrado
                double nx = ( (x + 0.5) / W ) * 2.0 - 1.0;
                double ny = 1.0 - ( (y + 0.5) / H ) * 2.0;

                double px = nx * halfW;
                double py = ny * halfH;
                double pz = defaultZ;

                positions[x][y] = new Vector3D(px, py, pz);
            }
        }
        return positions;
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        return new Intersection(Vector3D.ZERO(), -1, Vector3D.ZERO(), null);
    }

    @Override
    public BVH.BoundingBox getBoundingBox() {
        // Las cámaras no tienen volumen, pero devolvemos una caja sin volumen en su posición
        Vector3D pos = getPosition();
        return new BVH.BoundingBox(pos, pos);
    }
}
