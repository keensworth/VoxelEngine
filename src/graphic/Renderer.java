package graphic;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import ecs.Component;
import ecs.Components.*;
import ecs.ECS;
import ecs.Entity;
import org.joml.*;
import org.joml.Math;
import util.CTree;
import util.ComponentMask;
import util.Container;
import util.ETree.EntNode;
import util.VTree;

import javax.imageio.ImageIO;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL32.*;


public class Renderer {

    private int HEIGHT;
    private int WIDTH;

    //Frustrum
    private final float FOV = (float) Math.toRadians(75.0f);
    private final float Z_NEAR = 1f;
    private final float Z_FAR = 1500.f;
    private float aspectRatio;

    //Lights
    private static final int MAX_POINT_LIGHTS = 15;
    private static final int MAX_SPOT_LIGHTS = 15;

    //GLFW window
    private Window window;

    //Shaders
    private ShaderProgram sceneShaderProgram;

    //Transform matrices
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;

    //Camera
    private float[] cameraPos;
    private float[] cameraDir;

    private int sum = 0;

    public Renderer(){

    }

    public void init(Window window) {
        this.window = window;
        glClearColor(0.8f,1.0f,1.0f,0.0f);

        WIDTH = window.getWidth();
        HEIGHT = window.getHeight();

        aspectRatio = (float) WIDTH / HEIGHT;
        projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio, Z_NEAR, Z_FAR);

        setupObjects();
        setupSceneShader();
    }

    public void renderVoxels(EntNode entities, ComponentMask components){
        Voxel voxel = (Voxel) components.getComponent(Voxel.class);
        Chunk chunk = (Chunk) components.getComponent(Chunk.class);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
        glEnable(GL_MULTISAMPLE);

        long start = java.lang.System.nanoTime();
        //bind shader
        sceneShaderProgram.bind();

        //set uniforms
        sceneShaderProgram.setUniform("modelMatrix", new Matrix4f().identity());
        sceneShaderProgram.setUniform("viewMatrix", viewMatrix);
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        setLightUniforms(entities, components);

        //frustrum cull chunks
        CTree chunks = cullChunks(chunk.getChunk());

        //render chunks
        renderChunks(chunks, 0, 0, 0);

        //unbind shader
        sceneShaderProgram.unbind();
        long end = java.lang.System.nanoTime();
        java.lang.System.out.println(" > > " + (end-start)/1000000f + "ms to complete render call");
    }

    private void renderChunks(CTree cTree, int x, int y, int z){
        int height = cTree.getHeight();
        if (height==0){
            Mesh[] chunks = cTree.getChunks();
            for (int i = 0; i < chunks.length; i++){
                if (cTree.childExists((byte)i)){
                    Mesh mesh = chunks[i];
                    mesh.renderInstanced();
                    //System.out.println(cTree.getSize());
                }
            }
        } else {
            CTree[] children = cTree.getChildren();
            for (int i = 0; i < children.length; i++){
                if (cTree.childExists((byte) i)){
                    int newX = x + (i%4) * (4<<(2*(height-1)));
                    int newY = y + ((i%16)/4) * (4<<(2*(height-1)));
                    int newZ = z + (i/16) * (4<<(2*(height-1)));
                    renderChunks(children[i], newX, newY, newZ);
                }
            }
        }
    }

    private CTree cullChunksRays(CTree cTree){
        CTree culledChunks = new CTree(cTree.getHeight());

        Vector3f camPos = new Vector3f(cameraPos);
        Vector3f fwd = getFwd();
        //System.out.println("FWD:" + fwd);
        Vector3f right = getRight();
        //System.out.println("RGT:" + right);
        Vector3f up = getUp(fwd, right);

        float h = Math.tan(FOV/2f);
        float w = aspectRatio*h;
        //System.out.println(h + "---------------" + w);
        Vector3f p = new Vector3f(camPos).add(new Vector3f(fwd));
        Vector3f f00 = new Vector3f(p).add((new Vector3f(new Vector3f(right).mul(-w)).add(new Vector3f(up).mul(h))));
        for (int x = 0; x < WIDTH/32; x++){
            float xRatio = (32*x)/(float)WIDTH;
            float xAmount = xRatio*2*w;
            for (int y = 0; y < HEIGHT/32; y++){
                float yRatio = (32*y)/(float)HEIGHT;
                float yAmount = yRatio*2*h;
                Vector3f unit = (new Vector3f(f00).add((new Vector3f(right).mul(xAmount)).add(new Vector3f(up).mul(yAmount*-1)))).sub(camPos);
                for (int d = 0; d < 1000/32f; d++){
                    Vector3f pos = new Vector3f(camPos).add(new Vector3f(unit).mul(32*(d-1)));
                    int xPos = (int) pos.x;
                    int yPos = (int) pos.y;
                    int zPos = (int) pos.z;
                    xPos = (xPos/64);
                    yPos = (yPos/64);
                    zPos = (zPos/64);

                    Mesh mesh = cTree.getChunkData(xPos, yPos, zPos);

                    if (mesh != null){
                        culledChunks.addChunk(xPos, yPos, zPos, mesh);
                        break;
                    }
                }
            }
        }
        return culledChunks;
    }

    private CTree cullChunks(CTree cTree){
        CTree culledChunks = new CTree(cTree.getHeight());
        int voxelCount = 0;

        Vector3f camPos = new Vector3f(cameraPos);
        Vector3f fwd = getFwd();
        //System.out.println("FWD:" + fwd);
        Vector3f right = getRight();
        //System.out.println("RGT:" + right);
        Vector3f up = getUp(fwd, right);

        float h = Math.tan(FOV/2f);
        float w = aspectRatio*h;
        //System.out.println(h + "---------------" + w);

        for (int d = 0; d < 32; d++){
            Vector3f p = new Vector3f(camPos).add(new Vector3f(fwd).mul(32*(d-2)));
            //System.out.println("P:" + p);
            Vector3f f00 = new Vector3f(p).add((new Vector3f(new Vector3f(right).mul(-w*d*32)).add(new Vector3f(up).mul(h*d*32))));
            Vector3f f10 = new Vector3f(p).add((new Vector3f(new Vector3f(right).mul(w*d*32)).add(new Vector3f(up).mul(h*d*32))));
            Vector3f f01 = new Vector3f(p).add((new Vector3f(new Vector3f(right).mul(-w*d*32)).add(new Vector3f(up).mul(-h*d*32))));

            //System.out.println("F00" + f00);

            Vector3f f00f10 = (new Vector3f(f10).sub(f00));
            Vector3f f00f01 = (new Vector3f(f01).sub(f00));

            float xSteps = Math.ceil((f00f10.length()/32));
            float ySteps = Math.ceil((f00f01.length()/32));

            f00f10.normalize();
            f00f01.normalize();

            for (int x = 0; x < xSteps; x++){
                for (int y = 0; y < ySteps; y++){
                    Vector3f pos = new Vector3f(f00).add((new Vector3f(f00f10).mul(32*x)).add(new Vector3f(f00f01).mul(32*y)));
                    int xPos = (int) pos.x;
                    int yPos = (int) pos.y;
                    int zPos = (int) pos.z;
                    xPos = (xPos/64);
                    yPos = (yPos/64);
                    zPos = (zPos/64);

                    Mesh mesh = cTree.getChunkData(xPos, yPos, zPos);

                    if (mesh != null){
                        if(culledChunks.getChunkData(xPos, yPos, zPos) == null) {
                            culledChunks.addChunk(xPos, yPos, zPos, mesh);
                            voxelCount += mesh.getInstanceCount();
                            if (voxelCount > 1750000)
                                return culledChunks;
                        }
                    }
                }
            }
        }
        return culledChunks;
    }

    private void setLightUniforms(EntNode entities, ComponentMask components){
        Container<Entity> lights = entities.getEntities(components.getFromClasses(Light.class));
        //System.out.println(Integer.toBinaryString(components.getFromClasses(Light.class)));
        Position position = (Position) components.getComponent(Position.class);
        Light light = (Light) components.getComponent(Light.class);
        
        //set light uniforms
        int pointLightIndex = 0;
        int spotLightIndex = 0;
        for (int i = 0; i < lights.getSize(); i++){
            int entity = lights.get(i).getEntityId();
            
            float[] pos = position.getPosition(position.getEntityIndex(entity));
            int lightType = light.getLight(light.getEntityIndex(entity));

            if (lightType == 0){
                sceneShaderProgram.setUniform("pointLights["+pointLightIndex+"]", new Vector3f(pos));
                pointLightIndex++;
            } else {
                sceneShaderProgram.setUniform("spotLights["+spotLightIndex+"]", new Vector3f(pos));
                spotLightIndex++;
            }
        }

        //reset the rest of the light uniforms
        for (int i = pointLightIndex; i < MAX_POINT_LIGHTS; i++){
            sceneShaderProgram.setUniform("pointLights["+i+"]", new Vector3f(0,0,0));
        }
        for (int i = spotLightIndex; i < MAX_SPOT_LIGHTS; i++){
            sceneShaderProgram.setUniform("spotLights["+i+"]", new Vector3f(0,0,0));
        }

    }

    private void setupObjects(){
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_TEXTURE_2D);
        glActiveTexture(GL_TEXTURE0);
        
        cameraPos = new float[3];
        cameraDir = new float[3];
    }
    
    private void setupSceneShader(){
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(loadResource("resources/shaders/scene_vertex.glsl"));
        sceneShaderProgram.createFragmentShader(loadResource("resources/shaders/scene_fragment.glsl"));
        sceneShaderProgram.link();

        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("modelMatrix");
        sceneShaderProgram.createUniform("viewMatrix");
        sceneShaderProgram.createUniform("spotLights", MAX_SPOT_LIGHTS);
        sceneShaderProgram.createUniform("pointLights", MAX_POINT_LIGHTS);
        //sceneShaderProgram.createUniform("cameraPos");
    }

    private void updateViewMatrix(float[] cameraPos, float[] cameraDir){
        viewMatrix = new Matrix4f().identity();
        viewMatrix
                .rotate(cameraDir[0], new Vector3f(1, 0, 0))
                .rotate(cameraDir[1], new Vector3f(0, 1, 0))
                .rotate(cameraDir[2], new Vector3f(0, 0, 1));
        viewMatrix.translate(-cameraPos[0], -cameraPos[1], -cameraPos[2]);

    }

    public void prepare(Window window, float[] cameraPos, float[] cameraDir, float[] cam2Pos, float[] cam2Dir) {
        this.cameraPos = cameraPos;
        this.cameraDir = cameraDir;
        System.out.println(cameraDir[0] + ", " + cameraDir[1] + ", " + cameraDir[2]);

        if (cam2Pos!=null)
            updateViewMatrix(cam2Pos, cam2Dir);
        else
            updateViewMatrix(cameraPos, cameraDir);

        clear();

        glViewport(0, 0, window.getWidth(), window.getHeight());
    }

    private Vector3f getFwd(){
        return new Vector3f(
                (float)Math.sin(cameraDir[2])*Math.sin(cameraDir[0])*-1,
                (float)Math.cos(cameraDir[2])*Math.sin(cameraDir[0])*-1,
                (float)-Math.cos(cameraDir[0])
        ).normalize();
    }

    private Vector3f getUp(Vector3f fwd, Vector3f right){
        return (new Vector3f(right).cross(fwd)).normalize();
    }

    private Vector3f getRight(){
        return new Vector3f(
                (float)Math.sin(cameraDir[2] + Math.PI/2),
                (float)Math.cos(cameraDir[2] + Math.PI/2),
                0f
        ).normalize();
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private void clearBuffers(int... buffers){
        for (int buffer : buffers){
            glBindFramebuffer(GL_FRAMEBUFFER, buffer);
            clear();
        }
    }

    public static String loadResource(String fileName) {
        String result = "";
        try (InputStream in = Renderer.class.getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        } catch (IOException e){
            System.out.println("Could not load resource: " + fileName);
        }
        return result;
    }

    public void cleanup() {
        
    }
}