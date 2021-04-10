package ua.kpi.fpm.pzks.maokg;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import jdk.jfr.TransitionTo;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class Main extends JFrame implements ActionListener, KeyListener {
    private final static String crabLocation = "crab.obj";
    private final static String backgroundLocation = "background.png";
    private final BranchGroup root = new BranchGroup();
    private final Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
    private final TransformGroup mainTransformGroup = new TransformGroup();
    private final Transform3D transform3D = new Transform3D();
    private final Transform3D rotateTransformX = new Transform3D();
    private final Transform3D rotateTransformY = new Transform3D();
    private final Transform3D rotateTransformZ = new Transform3D();
    private final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    private SimpleUniverse universe;
    private Scene scene;
    private Background background;
    private Timer timer;

    private int maxCoordinatsCounter = 300;

    private PathInterpolator pathInterpolator = new PathInterpolator(
            new PathInterpolator.Coordinats3D[] {
                    new PathInterpolator.Coordinats3D(3f, 0f, 0f, 1f, 30f, 0f, 0f),
                    new PathInterpolator.Coordinats3D(5f, 0f, 0f, 1f, 0f, 0f, 0f),
                    new PathInterpolator.Coordinats3D(2f, 1f, 0f, 1f, -30f, 0f, 0f),
                    new PathInterpolator.Coordinats3D(-1f, 1f, 0f, 1f, 0f, 0f, 0f),
            }
            ,
            maxCoordinatsCounter
    );

    private PathInterpolator.Coordinats3D[] interpolatedPath = pathInterpolator.getInterpolatedPath();
    private int coordinatsCounter = 0;

    private float currentX = 3,
                currentY = 0,
                currentZ = 0,
                currentScale = 1,
                currentRotateX = 0,
                currentRotateY = 0,
                currentRotateZ = 0;

    public static void main(String[] args) {
        try {
            Main window = new Main();
            window.setVisible(true);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public Main() throws IOException {
        init();
        addBackground();
        addLight();
        setInitialViewAngle();
        setInitialLocation();
        compile();
    }

    private void compile() {
        root.compile();
        universe.addBranchGraph(root);
    }

    private void setInitialLocation() {
        transform3D.setTranslation(new Vector3f(currentX, currentY, currentZ));
        transform3D.setScale(currentScale);
        mainTransformGroup.setTransform(transform3D);
    }

    private void init() throws IOException {
        setTitle("Андрій Коваль КП-83 ЛАБ 5");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas.setDoubleBufferEnable(true);
        canvas.addKeyListener(this);
        getContentPane().add(canvas, BorderLayout.CENTER);

        universe = new SimpleUniverse(canvas);
        universe.getViewingPlatform().setNominalViewingTransform();

        scene = getSceneFromFile();

        mainTransformGroup.addChild(scene.getSceneGroup());
        mainTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        root.addChild(mainTransformGroup);

        timer = new Timer(10, this);
        timer.start();
    }

    private void addLight() {
        DirectionalLight dirLight = new DirectionalLight(
                new Color3f(Color.WHITE),
                new Vector3f(4.0f, -7.0f, -12.0f)
        );

        dirLight.setInfluencingBounds(new BoundingSphere(new Point3d(), 1000));
        root.addChild(dirLight);

        AmbientLight ambientLight = new AmbientLight(new Color3f(Color.WHITE));
        DirectionalLight directionalLight = new DirectionalLight(
                new Color3f(Color.BLACK),
                new Vector3f(-1F, -1F, -1F)
        );
        BoundingSphere influenceRegion = new BoundingSphere(new Point3d(), 1000);
        ambientLight.setInfluencingBounds(influenceRegion);
        directionalLight.setInfluencingBounds(influenceRegion);
        root.addChild(ambientLight);
        root.addChild(directionalLight);
    }

    private TextureLoader getTextureLoader(String path) throws IOException {
        URL textureResource = currentClassLoader.getResource(path);
        if (textureResource == null) {
            throw new IOException("Couldn't find texture: " + path);
        }
        return new TextureLoader(textureResource.getPath(), canvas);
    }

    private void addBackground() throws IOException {
        background = new Background(getTextureLoader(backgroundLocation).getImage());
        background.setImageScaleMode(Background.SCALE_FIT_MAX);
        background.setApplicationBounds(new BoundingSphere(new Point3d(),1000));
        background.setCapability(Background.ALLOW_IMAGE_WRITE);
        root.addChild(background);
    }

    private void setInitialViewAngle() {
        ViewingPlatform vp = universe.getViewingPlatform();
        Transform3D transform = new Transform3D();
        transform.lookAt(
                new Point3d(-4, 0, 0),
                new Point3d(-1, 0, 0),
                new Vector3d(0, 1, 0)
        );
        transform.invert();
        vp.getViewPlatformTransform().setTransform(transform);
    }

    private Scene getSceneFromFile() throws IOException {
        ObjectFile file = new ObjectFile(ObjectFile.RESIZE);
        file.setFlags(ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
        InputStream inputStream = currentClassLoader.getResourceAsStream(crabLocation);
        if (inputStream == null) {
            throw new IOException("Resource " + crabLocation + " not found");
        }
        return file.load(new BufferedReader(new InputStreamReader(inputStream)));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        float diff = (e.isShiftDown() ? -1 : 1) * 0.05f;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_X: {
                currentX += diff;
            } break;
            case KeyEvent.VK_Y: {
                currentY += diff;
            } break;
            case KeyEvent.VK_Z: {
                currentZ += diff;
            } break;
            case KeyEvent.VK_S: {
                currentScale += diff;
            } break;
            case KeyEvent.VK_UP: {
                currentRotateX += diff;
                rotateTransformX.rotX(diff);
                transform3D.mul(rotateTransformX);
            } break;
            case KeyEvent.VK_DOWN: {
                currentRotateY += diff;
                rotateTransformY.rotY(diff);
                transform3D.mul(rotateTransformY);
            } break;
            case KeyEvent.VK_LEFT: {
                currentRotateZ += diff;
                rotateTransformZ.rotZ(diff);
                transform3D.mul(rotateTransformZ);
            } break;
        }

        String debugMessage = String.format(
                "\nX: %f, Y: %f, Z: %f, Scale: %f\n" +
                "RotX: %f, RotY: %f, RotZ: %f",
                currentX, currentY, currentZ, currentScale, currentRotateX, currentRotateY, currentRotateZ
        );
        System.out.println(debugMessage);
        // todo move to seprate func
        transform3D.setTranslation(new Vector3f(currentX, currentY, currentZ));
        transform3D.setScale(currentScale);
        mainTransformGroup.setTransform(transform3D);
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (coordinatsCounter >= interpolatedPath.length) {
            coordinatsCounter = 0;
        }
        PathInterpolator.Coordinats3D currentCoords = interpolatedPath[coordinatsCounter];
        PathInterpolator.Coordinats3D prevCoords = coordinatsCounter == 0
                ? interpolatedPath[interpolatedPath.length - 1]
                : interpolatedPath[coordinatsCounter - 1];
        coordinatsCounter += 1;
//        String debugMessage = String.format(
//                "\nX: %f, Y: %f, Z: %f, Scale: %f\n" +
//                        "RotX: %f, RotY: %f, RotZ: %f",
//                currentCoords.currentX,
//                currentCoords.currentY,
//                currentCoords.currentZ,
//                currentCoords.currentScale,
//                currentCoords.currentRotateX,
//                currentCoords.currentRotateY,
//                currentCoords.currentRotateZ
//        );
//        System.out.println(debugMessage);

//        System.out.println(currentCoords.currentRotateX - prevCoords.currentRotateX);
////        System.out.println(prevCoords.currentRotateX);
//
//        rotateTransformX.rotX(currentCoords.currentRotateX - prevCoords.currentRotateX);
//        transform3D.mul(rotateTransformX);
//        rotateTransformY.rotY(currentCoords.currentRotateY - prevCoords.currentRotateY);
//        transform3D.mul(rotateTransformY);
//        rotateTransformZ.rotZ(currentCoords.currentRotateZ - prevCoords.currentRotateZ);
//        transform3D.mul(rotateTransformZ);
//
//        transform3D.setTranslation(new Vector3f(currentCoords.currentX, currentCoords.currentY, currentCoords.currentZ));
//        transform3D.setScale(currentCoords.currentScale);
//        mainTransformGroup.setTransform(transform3D);
    }
}
