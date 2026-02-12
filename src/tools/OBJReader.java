package tools;

import BVH.BVHNode;
import Materials.MTLReader;
import Materials.Material;
import core.Vector3D;
import objects.Model3D;
import objects.Triangle;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class OBJReader {

    public static BVHNode getModel3D(String path, Vector3D origin, Color color, double scaleFactor, Vector3D rotation,boolean forceMaterial) {
        System.out.println("[OBJReader] → 3-args  ( DEFAULT Material)");
        return getModel3D(path, origin, color, Material.DEFAULT, scaleFactor, rotation,false);
    }

    public static BVHNode getModel3D(String path, Vector3D origin, Color color, Material material, double scaleFactor, Vector3D rotation,  boolean forceMaterial) {
        System.out.println("[OBJReader] → 4-args  path=" + path +
                "  origin=" + origin +
                "  color=" + color +
                "  material=" + material);
        if (material == null) material = Material.DEFAULT;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

            List<Triangle> triangles = new ArrayList<>();
            List<Vector3D> vertices  = new ArrayList<>();
            List<Vector3D> normals   = new ArrayList<>();
            List<double[]> texCoords = new ArrayList<>();
            String line;
            int currentSmoothingGroup = -1;
            Map<String, Material> materials = new HashMap<>();
            String currentMaterialName = null;

            File objFile = new File(path);
            File parentDir = objFile.getParentFile();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("mtllib ")) {
                    String mtlFileName = line.substring(7).trim();
                    File mtlFile = new File(parentDir, mtlFileName);
                    materials = MTLReader.loadMTL(mtlFile.getAbsolutePath());
                } else if (line.startsWith("usemtl ")) {
                    currentMaterialName = line.substring(7).trim();
                }
                if (line.startsWith("s ")) {
                    if (line.trim().equals("s off") || line.trim().equals("s 0")) {
                        currentSmoothingGroup = -1;
                    } else {
                        currentSmoothingGroup = Integer.parseInt(line.trim().substring(2));
                    }
                } else if (line.startsWith("v ") || line.startsWith("vn ")) {
                    String[] vertexComponents = line.split("(\\s)+");
                    if (vertexComponents.length >= 4) {
                        double x = Double.parseDouble(vertexComponents[1]);
                        double y = Double.parseDouble(vertexComponents[2]);
                        double z = Double.parseDouble(vertexComponents[3]);
                        Vector3D vec = new Vector3D(x, y, z);
                        if (line.startsWith("v ")) {
                            vec = new Vector3D(vec.getX() * scaleFactor, vec.getY() * scaleFactor, vec.getZ() * scaleFactor);
                            vec = rotate(vec, rotation.getX(), rotation.getY(), rotation.getZ());
                            vec = Vector3D.add(vec, origin);
                            vertices.add(vec);
                        } else {
                            vec = rotate(vec, rotation.getX(), rotation.getY(), rotation.getZ());
                            normals.add(vec);
                        }
                    }
                } else if (line.startsWith("vt ")) {
                    String[] parts = line.split("\\s+");
                    double u = Double.parseDouble(parts[1]);
                    double v = Double.parseDouble(parts[2]);
                    texCoords.add(new double[]{u, v});
                } else if (line.startsWith("f ")) {
                    String[] faceComponents = line.split("(\\s)+");
                    List<Integer> faceVertex = new ArrayList<>();
                    List<Integer> faceNormals = new ArrayList<>();
                    List<Integer> faceUVs = new ArrayList<>();

//                    for (int i = 1; i < faceComponents.length; i++) {
//                        String[] infoVertex = faceComponents[i].split("/");
//                        if (infoVertex.length >= 3) {
//                            int vertexIndex = Integer.parseInt(infoVertex[0]);
//                            int texIndex = Integer.parseInt(infoVertex[1]);
//                            int normalIndex = Integer.parseInt(infoVertex[2]);
//                            faceVertex.add(vertexIndex);
//                            faceUVs.add(texIndex);
//                            faceNormals.add(normalIndex);
//                        }
//                    }

                    for (int i = 1; i < faceComponents.length; i++) {
                        String[] infoVertex = faceComponents[i].split("/");

                        // Vértices (siempre deben existir)
                        int vertexIndex = Integer.parseInt(infoVertex[0]);
                        faceVertex.add(vertexIndex);

                        // UVs
                        if (infoVertex.length > 1 && !infoVertex[1].isEmpty()) {
                            int texIndex = Integer.parseInt(infoVertex[1]);
                            faceUVs.add(texIndex);
                        } else {
                            faceUVs.add(-1); // sin UV
                        }

                        // Normales
                        if (infoVertex.length > 2 && !infoVertex[2].isEmpty()) {
                            int normalIndex = Integer.parseInt(infoVertex[2]);
                            faceNormals.add(normalIndex);
                        } else {
                            faceNormals.add(-1); // sin normal
                        }
                    }

                    if (faceVertex.size() >= 3) {
                        Vector3D v0 = vertices.get(faceVertex.get(0) - 1);
                        Vector3D v1 = vertices.get(faceVertex.get(1) - 1);
                        Vector3D v2 = vertices.get(faceVertex.get(2) - 1);

                        Vector3D n0 = (faceNormals.get(0) > 0) ? normals.get(faceNormals.get(0) - 1) : Vector3D.normalize(Vector3D.crossProduct(Vector3D.substract(v1, v0), Vector3D.substract(v2, v0)));
                        Vector3D n1 = (faceNormals.get(1) > 0) ? normals.get(faceNormals.get(1) - 1) : n0;
                        Vector3D n2 = (faceNormals.get(2) > 0) ? normals.get(faceNormals.get(2) - 1) : n0;

                        double[] uv0 = (faceUVs.get(0) > 0) ? texCoords.get(faceUVs.get(0) - 1) : new double[]{0.0, 0.0};
                        double[] uv1 = (faceUVs.get(1) > 0) ? texCoords.get(faceUVs.get(1) - 1) : new double[]{0.0, 0.0};
                        double[] uv2 = (faceUVs.get(2) > 0) ? texCoords.get(faceUVs.get(2) - 1) : new double[]{0.0, 0.0};
                        //Material usedMat = materials.getOrDefault(currentMaterialName, material);
                        Material usedMat = (forceMaterial || !materials.containsKey(currentMaterialName))
                                ? material
                                : materials.get(currentMaterialName);
                        Triangle triangle1 = new Triangle(v0, v1, v2, new Vector3D[]{n0, n1, n2});
                        triangle1.setSmoothingGroup(currentSmoothingGroup);
                        triangle1.setMaterial(usedMat);
                        triangle1.setColor(color);
                        triangle1.setUV(new double[][]{uv0, uv1, uv2});
                        triangles.add(triangle1);

                        if (faceVertex.size() == 4) {
                            Vector3D v3 = vertices.get(faceVertex.get(3) - 1);
                            Vector3D n3 = (faceNormals.get(3) > 0) ? normals.get(faceNormals.get(3) - 1) : n0;
                            double[] uv3 = (faceUVs.get(3) > 0) ? texCoords.get(faceUVs.get(3) - 1) : new double[]{0.0, 0.0};

                            Triangle triangle2 = new Triangle(v0, v2, v3, new Vector3D[]{n0, n2, n3});
                            triangle2.setSmoothingGroup(currentSmoothingGroup);
                            triangle2.setMaterial(usedMat);
                            triangle2.setColor(color);
                            triangle2.setUV(new double[][]{uv0, uv2, uv3});
                            triangles.add(triangle2);
                        }
                    }

                }
            }
            reader.close();

            System.out.println("[OBJReader] Loaded Triangles: " + triangles.size());

            List<objects.Object3D> objects = new ArrayList<>();
            for (Triangle t : triangles) objects.add(t);
            return new BVHNode(objects);

        } catch (IOException e) {
            System.err.println("OBJReader error: " + e);
            return null;
        }
    }

    public static Vector3D rotate(Vector3D point, double rotationX, double rotationY, double rotationZ) {
        rotationX = Math.toRadians(rotationX);
        rotationY = Math.toRadians(rotationY);
        rotationZ = Math.toRadians(rotationZ);

        double x = point.getX(), y = point.getY(), z = point.getZ();

        double y1 = y * Math.cos(rotationX) - z * Math.sin(rotationX);
        double z1 = y * Math.sin(rotationX) + z * Math.cos(rotationX);
        y = y1;
        z = z1;

        double x2 = x * Math.cos(rotationY) + z * Math.sin(rotationY);
        double z2 = -x * Math.sin(rotationY) + z * Math.cos(rotationY);
        x = x2;
        z = z2;

        double x3 = x * Math.cos(rotationZ) - y * Math.sin(rotationZ);
        double y3 = x * Math.sin(rotationZ) + y * Math.cos(rotationZ);
        x = x3;
        y = y3;

        return new Vector3D(x, y, z);
    }
}
