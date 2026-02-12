package objects;


import Materials.Material;
import core.Vector3D;
import BVH.BoundingBox;
import java.awt.*;

public abstract class Object3D implements IIntersectable{
    private Color color;
    private Vector3D position;
    private Material material = Material.DEFAULT;

    public Object3D(Vector3D position, Color color) {
        setPosition(position);
        setColor(color);
    }
    public abstract BVH.BoundingBox getBoundingBox();

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Vector3D getPosition() {
        return position;
    }

    public Material getMaterial()  { return material; }

    public void setMaterial(Material m){ this.material = m; }

    public void setPosition(Vector3D position) {
        this.position = position;
    }
}
