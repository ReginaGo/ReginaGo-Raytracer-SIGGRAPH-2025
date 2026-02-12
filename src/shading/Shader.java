package shading;

import objects.Camera;
import core.Intersection;
import lights.Light;
import objects.Object3D;

import java.awt.Color;
import java.util.List;

public interface Shader {
    Color shade(List<Object3D> objects, Intersection hit, List<Light> lights, Camera camera);
}
