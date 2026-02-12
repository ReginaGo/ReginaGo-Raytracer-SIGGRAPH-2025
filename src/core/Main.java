package core;

import BVH.BVHNode;
import Materials.Material;
import Materials.Texture;
import lights.AreaLight;
import lights.DirectionalLight;
import lights.PointLight;
import lights.SpotLight;
import objects.*;
import shading.BlinnPhongShader;
import shading.DiffuseShader;
import shading.Shader;
import core.Raytracer;
import tools.OBJReader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Main {

        public static void main (String[]args) throws IOException {
            System.out.println(new Date());

        Scene scene01 = new Scene();
        Shader shader = new BlinnPhongShader();   // for simpler shading Change shader type to diffuse (optional)
        Raytracer rt  = new Raytracer(shader);


            //----------------MATERIALS---------------------------

            //basic
            Material metal   = new Material(0.05, 0.45, 1.0, 128);  // high brightness
            Material plastic = new Material(0.15, 0.75, 0.25, 12); // blow brightness

            //Advanced
            Material glass = new Material(0.1, 0.2, 0.3, 64);
            glass.setTransparency(1.0);
            glass.setRefractiveIndex(1.5);
            glass.setReflectivity(0.1);

            Material mirror = new Material(0.1, 0.2, 0.5, 64);
            mirror.setReflectivity(1.0); //full mirror effect

            Material water = new Material(0.05, 0.2, 0.1, 32);
            water.setTransparency(0.9);
            water.setRefractiveIndex(1.33);
            water.setReflectivity(0.05);

            Material diamond = new Material(0.2, 0.3, 0.9, 128);
            diamond.setTransparency(1.0);
            diamond.setRefractiveIndex(2.4);
            diamond.setReflectivity(0.5);

            Material foggy = new Material(0.05, 0.1, 0.2, 16);
            foggy.setTransparency(0.5);
            foggy.setRefractiveIndex(1.2);
            foggy.setReflectivity(0.2);

            Material puddleMaterial = new Material(0.0, 0.05, 0.6, 64);
            puddleMaterial.setTransparency(0.85);         // Más transparente
            puddleMaterial.setReflectivity(0.2);          // Refleja poco
            puddleMaterial.setRefractiveIndex(1.33);

            Texture tex = new Texture("textures/brik.jpg");
            Material brick = new Material(0.1, 0.5, 0.2, 32);
            brick.setTexture(tex);

            Texture pisoTex = new Texture("textures/floor.jpg");
            Material pisoMat = new Material(0.1, 0.6, 0.2, 32);
            pisoMat.setTexture(pisoTex);

            Texture grassTex = new Texture("textures/grass2.png");
            Material grassMaterial = new Material(0.1, 0.6, 0.2, 32);
            grassMaterial.setTexture(grassTex);

            Material wallMaterial = new Material(0.01, 0.6, 0.0, 0); // puedes ajustar

            Texture grassTexture = new Texture("obj/grass/10450_Rectangular_Grass_Patch_v1_Diffuse.jpg");
            Material grassMat = new Material(0.1, 0.6, 0.2, 32);
            grassMat.setTexture(grassTexture);

            Texture faceTexture = new Texture("obj/face/source/face/unnamed.jpg");
            Material faceMat = new Material(0.1, 0.6, 0.2, 32);
            faceMat.setTexture(faceTexture);

            Texture marbleTex = new Texture("textures/marble.jpg");
            Material marble = new Material(0.1, 0.6, 0.4, 64); // ambiente, difuso, especular, brillo
            marble.setTexture(marbleTex);

    //CAMERA_________________________________________________________________
//change to 4096 x 2160
        scene01.setCamera(new Camera(new Vector3D(0, 0, -4), 60, 60, 4096, 2160, 0.6, 50.0));


    //LIGHTS_________________________________________________________________
        //scene01.addLight(new DirectionalLight(new Vector3D(0.0,0.0,1.0), Color.yellow, 1.1));
       // scene01.addLight(new PointLight(new Vector3D(-5,0.0,0.0), Color.WHITE, 1.1));
        // scene01.addLight(new PointLight(new Vector3D(1,3,1), Color.WHITE, 1.1));
        //SpotLight foco = new SpotLight(new Vector3D(0, 0, 0), new Vector3D(0, -1, 0), Color.RED, 1.0, 10.0);
        //HOW TO USE: postion, downwards position, color,intensity, aperture angle

//            AreaLight bushLight = new AreaLight(
//                    new Vector3D(-0.4, 0.2, -3),       // Centro de la luz: justo arriba del bush
//                    new Vector3D(0.3, 0, 0),           // Ancho (horizontal)
//                    new Vector3D(0, 0, 0.3),           // Alto (profundidad)
//                    4, 4,                              // Resolución: más suave con más subdivisiones
//                    new Color(255, 255, 200),          // Color cálido (amarillo tenue)
//                    1.5                                // Intensidad fuerte para destacar
//            );
//            scene01.addLight(bushLight);
//HOW TO USE: center, width, height, emitters a x a, color, intenisty
            SpotLight bushSpot = new SpotLight(
                    new Vector3D(-0.4, 1.0, -3),       // Posición de la luz (arriba del bush)
                    new Vector3D(0, -1, 0),            // Dirección: apunta hacia abajo
                    new Color(255, 255, 180),          // Luz cálida (como foco de galería)
                    1.5,                               // Intensidad buena
                    20.0                               // Ángulo estrecho (más cerrado = más enfocado)
            );
            scene01.addLight(bushSpot);


            SpotLight dramaticSideLight = new SpotLight(
                    new Vector3D(-5, 2.5, 0),          // posición (fuera de cámara izquierda)
                    new Vector3D(1, -0.5, 1),          // dirección diagonal hacia la estatua
                    new Color(255, 210, 160),          // luz cálida
                    1.5,                               // intensidad fuerte
                    15.0                               // ángulo de apertura estrecho
            );
            scene01.addLight(dramaticSideLight);



            //scene01.addLight(foco);

//            scene01.addLight(new SpotLight(
//                    new Vector3D(3, 3, 3),
//                    new Vector3D(-1, -1, -1),
//                    new Color(255, 200, 200),
//                    1.0,
//                    20
//            ));

//            scene01.addLight(new SpotLight(
//                    new Vector3D(-3, 3, 3),
//                    new Vector3D(1, -1, -1),
//                    new Color(200, 200, 255),
//                    1.0,
//                    20
//            ));

            //SCENERY
            Material skyMat = new Material(0.0, 0.0, 0.0, 1); // sin iluminación
            Texture skyTex = new Texture("textures/sky.jpg");
            skyMat.setTexture(skyTex);

            Sphere sky = new Sphere(new Vector3D(0, 0, 0), 999, Color.WHITE, skyMat);
            scene01.addObject(sky);


            scene01.addObject(new Sphere(new Vector3D(0, 1, 4), 0.1, new Color(255, 255, 255) ,glass));
            scene01.addObject(new Sphere(new Vector3D(1, 0, 3), 0.05, Color.BLUE, glass));
            scene01.addObject(new Sphere(new Vector3D(-0.45, 0.8, 5), 0.05, Color.BLUE, glass));
            scene01.addObject(new Sphere(new Vector3D(-0.5, 0.7, 1), 0.05, Color.BLUE, glass));
            scene01.addObject(new Sphere(new Vector3D(0.5, 0.5, 2), 0.06, Color.BLUE, glass));
            scene01.addObject(new Sphere(new Vector3D(-1, 0, 1), 0.05, Color.BLUE, glass));
            scene01.addObject(new Sphere(new Vector3D(-1.2, 0.45, 2.5), 0.05, Color.BLUE, glass));




//            Plane floor = new Plane(new Vector3D(0, -1, 0),new Vector3D(0, 1, 0) , Color.WHITE, grassMaterial);
//            scene01.addObject(floor);


           BVHNode grassModel = OBJReader.getModel3D("obj/grass/10450_Rectangular_Grass_Patch_v1_iterations-2.obj", new Vector3D(0, -2, 4), Color.WHITE, grassMat, 0.07, new Vector3D(-90, 0, 0), false);
           scene01.addObject(grassModel);


            BVHNode bush01 = OBJReader.getModel3D(
                    "obj/BS09_NeriumOleander_OBJ/OBJ_BS09_NeriumOleander_3.obj",
                    new Vector3D(-3, -1, 5),
                    Color.WHITE,
                    null,
                    0.006,
                    new Vector3D(0, 0, 0),false
            );

            BVHNode bush06 = OBJReader.getModel3D(
                    "obj/BS09_NeriumOleander_OBJ/OBJ_BS09_NeriumOleander_3.obj",
                    new Vector3D(3, -1, 4),
                    Color.WHITE,
                    null,
                    0.006,
                    new Vector3D(0, 0, 0),false
            );
            BVHNode bush02 = OBJReader.getModel3D(
                    "obj/BS09_NeriumOleander_OBJ/OBJ_BS09_NeriumOleander_2.obj",
                    new Vector3D(-3, -1, 4),
                    Color.WHITE,
                    null,
                    0.006,
                    new Vector3D(0, 0, 0),false
            );

            BVHNode bush03 = OBJReader.getModel3D(
                    "obj/BS09_NeriumOleander_OBJ/OBJ_BS09_NeriumOleander_2.obj",
                    new Vector3D(-0.4, -0.5, -3),
                    Color.WHITE,
                    null,
                    0.002,
                    new Vector3D(0, 0, 0),false
            );
            BVHNode bush04 = OBJReader.getModel3D(
                    "obj/BS09_NeriumOleander_OBJ/OBJ_BS09_NeriumOleander_2.obj",
                    new Vector3D(0.4, -0.5, -3),
                    Color.WHITE,
                    null,
                    0.002,
                    new Vector3D(0, 0, 0),false
            );
            BVHNode bush05 = OBJReader.getModel3D(
                    "obj/BS09_NeriumOleander_OBJ/OBJ_BS09_NeriumOleander_2.obj",
                    new Vector3D(3, -1, 5),
                    Color.WHITE,
                    null,
                    0.006,
                    new Vector3D(0, 0, 0),false
            );

            BVHNode pillar = OBJReader.getModel3D(
                    "obj/wk3_doric_pillar_final(2).obj",
                    new Vector3D(2.5, -1.2, 5),
                    Color.WHITE,
                    marble,
                    0.05,
                    new Vector3D(0, 0, 0),true
            );

            scene01.addObject(pillar);
            BVHNode pillar2 = OBJReader.getModel3D(
                    "obj/wk3_doric_pillar_final(2).obj",
                    new Vector3D(-2.5, -1.2, 4),
                    Color.WHITE,
                    marble,
                    0.04,
                    new Vector3D(0, 0, 0),true
            );



//            BVHNode face = OBJReader.getModel3D(
//                    "obj/face/source/face/face.obj",
//                    new Vector3D(-2, 2.2, 5),
//                    Color.WHITE,
//                    faceMat,
//                    1,
//                    new Vector3D(0, 110, 0),true
//            );
//            scene01.addObject(face);
//
//            BVHNode face2 = OBJReader.getModel3D(
//                    "obj/face/source/face/face.obj",
//                    new Vector3D(2, 2.2, 5),
//                    Color.WHITE,
//                    faceMat,
//                    1,
//                    new Vector3D(0, -150, 0),true
//            );
//            scene01.addObject(face2);

            scene01.addObject(pillar2);
            BVHNode statue = OBJReader.getModel3D(

                    "obj/tritonen-und-najadenbrunnen/source/Brunnen_C/Brunnen_C.obj",
                    new Vector3D(-0.35, -1.4, 4.5),
                    Color.WHITE,
                   null,
                    0.25,
                    new Vector3D(-90, 90, 0),false
            );

            BVHNode puddle = OBJReader.getModel3D(
                    "obj/Puddle/Puddle.obj",
                    new Vector3D(0.1, -1.36, 4.1),
                    new Color(80, 150, 255) ,
                    water,
                    0.01,
                    new Vector3D(180, 0, 3)
                    ,true
            );
            scene01.addObject(puddle);

//            BVHNode wall = OBJReader.getModel3D("obj/wall-08/source/obj/wall-08.obj", new Vector3D(5, -1.5, 13), Color.WHITE, wallMaterial, 0.5, new Vector3D(0, -180, 0),false
//            );
            scene01.addObject(bush03);
            scene01.addObject(bush06);
            scene01.addObject(bush04); //front covering
            scene01.addObject(bush05); // front covering
           scene01.addObject(bush01); //red left
            scene01.addObject(bush02); //pink left
            //scene01.addObject(wall); //wall
            scene01.addObject(statue);



            BufferedImage image = rt.raytrace(scene01);
        File outputImage = new File("image.png");
        try {
            ImageIO.write(image, "png", outputImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(new Date());
    }
}

