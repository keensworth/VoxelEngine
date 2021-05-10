
import ecs.ECS;
import ecs.Components.*;
import ecs.Events.Event;
import ecs.Events.EventManager;
import ecs.Systems.*;
import org.joml.SimplexNoise;
import util.VTree;

public class Application {
    private ECS ecs;

    //------Initialize Components------//
    private Radius radius;                            //object radius
    private Position position;                        //object position
    private Rotation rotation;                        //object rotation
    private Direction direction;
    private Scale scale;
    private Velocity velocity;                        //object velocity
    private Camera camera;                            //camera
    //private Render render;                          //current color base
    private Input input;                              //keeps track of user input
    private World world;                              //stores terrain meshes
    private Light light;
    private ModelData modelData;
    private MeshData meshData;
    private Controllable controllable;
    private Interact interact;
    private Collide collide;
    private Voxel voxel;
    private Chunk chunk;

    //------Initialize Systems---------//
    private InputSys inputSys;                        //fetch input from GLFW
    private ControlSys controlSys;                    //check for input (click, vector) and launch launcher
    private CollisionSys collisionSys;                //build collision tree
    private MovementSys movementSys;                  //update positions of moving entities
    private RenderSys renderSys;                      //color each object based on health, render to screen (paint screen)

    private final int WIDTH = 2560;
    private final int HEIGHT = 1440;
    protected final float PI = (float) 3.14159;
    protected final float camDistance = (float) ((HEIGHT/2) * Math.sqrt(3));

    public Application() {

        ecs = new ECS(WIDTH, HEIGHT);
        ecs.addEventManager(new EventManager(
                new Event("example_event1"),
                new Event("example_event2")
        ));
        ecs.addRenderer(
                renderSys    = new RenderSys(ecs.width, ecs.height)
        );
        ecs.setWindow(renderSys.getWindow());
        ecs.addComponent(
                radius       = new Radius(),
                position     = new Position(),
                rotation     = new Rotation(),
                direction    = new Direction(),
                scale        = new Scale(),
                velocity     = new Velocity(),
                camera       = new Camera(),
                input        = new Input(),
                world        = new World(),
                light        = new Light(),
                modelData    = new ModelData(),
                meshData     = new MeshData(),
                controllable = new Controllable(),
                collide      = new Collide(),
                interact     = new Interact(),
                voxel        = new Voxel(),
                chunk        = new Chunk()
        );
        ecs.addSystem(
                inputSys      = new InputSys(),
                controlSys    = new ControlSys(),
                movementSys   = new MovementSys(),
                collisionSys  = new CollisionSys()
        );

        initScene();
        run();
    }

    private void initScene(){
        SimplexNoise noise = new SimplexNoise();
        float cubeSizeXY =1001;
        float sizeZ = 400;
        VTree vTree = new VTree(8);
        for (int k = 0; k < 1; k++) {
            for (int x = 0; x < cubeSizeXY; x++) {
                for (int y = 0; y < cubeSizeXY; y++) {
                    float noiseV = ((noise.noise(x/300f,y/300f)))*200f + 200;
                    for (int z = 0; z < noiseV; z++) {
                        int data;
                        if (z > noiseV - 5) {
                            int red = 0;;
                            int green = 160;
                            int blue = 0;
                            data = (0xFF000000 | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
                        } else {
                            int red = 155;
                            int green = 155;
                            int blue = 155;
                            data = (0xFF000000 | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
                        }

                        vTree.addVoxel(x, y, z, data);
                    }
                }
            }
        }

        voxel.setVoxel(vTree);


        // init player
        ecs.createEntity(
                position.add(new float[]{0,0,24}),
                velocity.add(new float[]{0,0,0}),
                rotation.add(new float[]{-PI/2, 0, 0}),
                camera.add(),
                controllable.add()
        );

        // add lights
        ecs.createEntity(
                position.add(new float[]{290,290,290}),
                light.add(0)
        );
        ecs.createEntity(
                position.add(new float[]{295,295,295}),
                light.add(0)
        );
        ecs.createEntity(
                position.add(new float[]{290,290,290}),
                light.add(0)
        );
        for (int i = 0; i < 8; i ++){
            ecs.createEntity(
                    position.add(new float[]{i*125,i*125,i*125}),
                    light.add(0)
            );
        }
    }

    //Main loop
    private void run(){
        double start,end;
        float dt = 1/60f;

        while(true){
            start = System.nanoTime();

            update(dt);

            end = System.nanoTime();
            dt = (float) ((end-start)/1000000000f);
            System.out.println(1/dt + " FPS <<<<<<<<<<<<<<<<<<<<<<<<<");
        }
    }

    private void update(float delta){
        ecs.update(delta);
    }

    public void onExit(){
        ecs.exit();
    }

    public static void main(String[] args) {
        Application app = new Application();
    }
}
