package lights;

import BVH.BoundingBox;
import core.Intersection;
import core.Ray;
import core.Vector3D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AreaLight extends Light {
    private Vector3D center;
    private Vector3D uVec, vVec;
    private int samplesU, samplesV;
    private List<PointLight> emitters = new ArrayList<>(); // inicializar aquí directamente

    public AreaLight(Vector3D center, Vector3D uVec, Vector3D vVec, int samplesU, int samplesV, Color color, double intensity) {
        // No usar setPosition aún, así evitamos llamadas prematuras
        super(Vector3D.ZERO(), color, intensity);
        this.center = center;
        this.uVec = uVec;
        this.vVec = vVec;
        this.samplesU = samplesU;
        this.samplesV = samplesV;
        generateEmitters();
    }

    private void generateEmitters() {
        emitters.clear();
        for (int i = 0; i < samplesU; i++) {
            for (int j = 0; j < samplesV; j++) {
                double su = ((i + 0.5) / samplesU - 0.5);
                double sv = ((j + 0.5) / samplesV - 0.5);
                Vector3D offset = Vector3D.add(
                        Vector3D.scalarMultiplication(uVec, su),
                        Vector3D.scalarMultiplication(vVec, sv)
                );
                Vector3D pos = Vector3D.add(center, offset);
                emitters.add(new PointLight(pos, getColor(), getIntensity() / (samplesU * samplesV)));
            }
        }
    }

    @Override
    public double getNDotL(Intersection intersection) {
        double sum = 0;
        for (PointLight p : emitters) {
            sum += p.getNDotL(intersection);
        }
        return sum / emitters.size();
    }

    @Override
    public Intersection getIntersection(Ray ray) {
        return null;
    }

    @Override
    public Vector3D getPosition() {
        return center;
    }

    @Override
    public void setPosition(Vector3D position) {
        this.center = position;
        // Solo llamar esto si emitters ya está inicializado
        if (emitters != null) {
            generateEmitters();
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(center, center);
    }
}
