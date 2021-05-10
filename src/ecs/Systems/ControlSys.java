package ecs.Systems;

import ecs.Components.*;
import ecs.Entity;
import ecs.System;
import graphic.Mesh;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.system.CallbackI;
import util.*;
import util.ETree.EntNode;

import java.util.ArrayList;

public class ControlSys extends System {
    Position position;
    Rotation rotation;
    Velocity velocity;
    Radius radius;
    Input input;
    Controllable controllable;
    Voxel voxel;
    Chunk chunk;

    Entity camera;

    private static float turnSpeed = 10;

    public ControlSys() {
        super(Input.class, Velocity.class, Position.class);
    }

    @Override
    public Class update(float dt, EntNode entityTree, ComponentMask componentMask, boolean entityChange) {
        java.lang.System.out.println("Updating ControlSys");
        updateValues(entityTree, componentMask);

        updateLookRotation(dt);
        updatePlayerVelocity(dt);
        updateMouseClick(dt);

        return null;
    }

    private void updateMouseClick(float dt){
        VTree vTree = voxel.getVoxel();
        CTree cTree = chunk.getChunk();

        if (input.isClicked()) {

            float[] camPos = position.getPosition(position.getEntityIndex(camera.getEntityId()));
            float[] camRot = rotation.getRotation(rotation.getEntityIndex(camera.getEntityId()));
            Vector3f fwd = getFwd(camRot);
            //System.out.println("FWD:" + fwd);
            Vector3f right = getRight(camRot);
            //System.out.println("RGT:" + right);
            Vector3f up = getUp(fwd, right);

            float h = Math.tan(Math.toRadians(75f) / 2f);
            float w = (16 / 9f) * h;
            //System.out.println(h + "---------------" + w);
            Vector3f p = new Vector3f(camPos).add(new Vector3f(fwd));
            for (int d = 0; d < 1000; d++){
                p.add(new Vector3f(fwd).mul(d*0.1f));
                int data = vTree.getVoxelData((int)p.x,(int)p.y,(int)p.z);
                if (data != 0 && data != -1){
                    destroyVoxels((int)p.x,(int)p.y,(int)p.z, vTree);
                    break;
                }
            }
        }
    }

    private void destroyVoxels(int x, int y, int z, VTree vTree){
        java.lang.System.out.println(x + ", " + y + ", " + z + "=========================================");
        CTree totalChunk = chunk.getChunk();
        for (int xNew = x-2; xNew < x+2; xNew++){
            for (int yNew = y-2; yNew < y+2; yNew++){
                for (int zNew = z-2; zNew < z+2; zNew++){
                    vTree.addVoxel(xNew, yNew, zNew, 0);
                }
            }
        }
        VTreeEditor editor = new VTreeEditor();
        editor.allVoxels = vTree;
        ArrayList<VTree> trees = new ArrayList<>(8);
        boolean[] chunkify = new boolean[8];
        for (int i = 0; i < 8; i++){
            chunkify[i] = true;
        }
        trees.add(vTree.getChildAtHeight(3, x-2, y-2, z-2));
        VTree sum1 = vTree.getChildAtHeight(3, x-2, y-2, z+2);
        if (trees.contains(sum1));
            chunkify[1] = false;
        trees.add(sum1);

        VTree sum2 = vTree.getChildAtHeight(3, x-2, y+2, z-2);
        if (trees.contains(sum2))
            chunkify[2] = false;
        trees.add(sum2);

        VTree sum3 = vTree.getChildAtHeight(3, x-2, y+2, z+2);
        if (trees.contains(sum3))
            chunkify[3] = false;
        trees.add(sum3);

        VTree sum4 = vTree.getChildAtHeight(3, x+2, y-2, z-2);
        if (trees.contains(sum4))
            chunkify[4] = false;
        trees.add(sum4);

        VTree sum5 = vTree.getChildAtHeight(3, x+2, y-2, z+2);
        if (trees.contains(sum5))
            chunkify[5] = false;
        trees.add(sum5);

        VTree sum6 = vTree.getChildAtHeight(3, x+2, y+2, z-2);
        if (trees.contains(sum6))
            chunkify[6] = false;
        trees.add(sum6);

        VTree sum7 = vTree.getChildAtHeight(3, x+2, y+2, z+2);
        if (trees.contains(sum7))
            chunkify[7] = false;
        trees.add(sum7);

        Mesh chunk1 = editor.parseToChunk(trees.get(0), (x-2)-(x-2)%64, (y-2)-(y-2)%64, (z-2)-(z-2)%64);
        if (chunk1 != null) {
            totalChunk.addChunk(((x - 2) / 64), (y - 2) / 64, (z - 2) / 64, chunk1);
        }
        if (chunkify[1]) {
            Mesh chunk2 = editor.parseToChunk(trees.get(1), (x - 2) - (x - 2) % 64, (y - 2) - (y - 2) % 64, (z + 2) - (z + 2) % 64);
            if (chunk2 != null) {
                totalChunk.addChunk((x - 2) / 64, (y - 2) / 64, (z + 2) / 64, chunk2);
            }
        }
        if (chunkify[2]) {
            Mesh chunk3 = editor.parseToChunk(trees.get(2), (x - 2) - (x - 2) % 64, (y + 2) - (y + 2) % 64, (z - 2) - (z - 2) % 64);
            if (chunk3 != null) {
                totalChunk.addChunk((x - 2) / 64, (y + 2) / 64, (z - 2) / 64, chunk3);
            }
        }
        if (chunkify[3]) {
            Mesh chunk4 = editor.parseToChunk(trees.get(3), (x - 2) - (x - 2) % 64, (y + 2) - (y + 2) % 64, (z + 2) - (z + 2) % 64);
            if (chunk4 != null) {
                totalChunk.addChunk((x - 2) / 64, (y + 2) / 64, (z + 2) / 64, chunk4);
            }
        }
        if (chunkify[4]) {
            Mesh chunk5 = editor.parseToChunk(trees.get(4), (x + 2) - (x + 2) % 64, (y - 2) - (y - 2) % 64, (z - 2) - (z - 2) % 64);
            if (chunk5 != null) {
                totalChunk.addChunk((x + 2) / 64, (y - 2) / 64, (z - 2) / 64, chunk5);
            }
        }
        if (chunkify[5]) {
            Mesh chunk6 = editor.parseToChunk(trees.get(5), (x + 2) - (x + 2) % 64, (y - 2) - (y - 2) % 64, (z + 2) - (z + 2) % 64);
            if (chunk6 != null) {
                totalChunk.addChunk((x + 2) / 64, (y - 2) / 64, (z + 2) / 64, chunk6);
            }
        }
        if (chunkify[6]) {
            Mesh chunk7 = editor.parseToChunk(trees.get(6), (x + 2) - (x + 2) % 64, (y + 2) - (y + 2) % 64, (z - 2) - (z - 2) % 64);
            if (chunk7 != null) {
                totalChunk.addChunk((x + 2) / 64, (y + 2) / 64, (z - 2) / 64, chunk7);
            }
        }
        if (chunkify[7]) {
            Mesh chunk8 = editor.parseToChunk(trees.get(7), (x + 2) - (x + 2) % 64, (y + 2) - (y + 2) % 64, (z + 2) - (z + 2) % 64);
            if (chunk8 != null) {
                totalChunk.addChunk((x + 2) / 64, (y + 2) / 64, (z + 2) / 64, chunk8);
            }
        }
        chunk.setChunk(totalChunk);
    }

    private void updateLookRotation(float dt){
        Vector3f lookDir = new Vector3f(rotation.getRotation(rotation.getEntityIndex(camera.getEntityId())));

        Vector2f cursorPos = new Vector2f(input.getMousePos()[0], input.getMousePos()[1]);
        Vector2f dCursor = cursorPos.sub(input.getPrevMousePos()[0], input.getPrevMousePos()[1]);
        float sensitivity = 0.05f;

        if (dCursor.x != 0 || dCursor.y != 0) {
            lookDir.x -= dCursor.y*dt*sensitivity;
            lookDir.x = lookDir.x < -3.0415f ? -3.0415f : lookDir.x > -0.1f ? -0.1f : lookDir.x;
            lookDir.z += (dCursor.x*dt*sensitivity) % (2 * 3.1415f);
        }
        rotation.setRotation(new float[]{lookDir.x,lookDir.y,lookDir.z}, rotation.getEntityIndex(camera.getEntityId()));
        input.setPrevMousePos(input.getMousePos());
    }


    private void updatePlayerVelocity(float dt){
        Vector3f playerVelocity = new Vector3f(velocity.getVelocity(velocity.getEntityIndex(camera.getEntityId())));
        Vector3f lookDir = new Vector3f(rotation.getRotation(rotation.getEntityIndex(camera.getEntityId())));

        Vector3f camFwd = getFwd(new float[]{lookDir.x,lookDir.y,lookDir.z});

        boolean[] keysPressed = input.getPressedKeys();
        float speedFactor = keysPressed[0] ? 15f : 1;
        float walkSpeed = 4;

        if (keysPressed[2]){ //A
            playerVelocity.x += -Math.cos(lookDir.z)*walkSpeed*dt*speedFactor;
            playerVelocity.y += Math.sin(lookDir.z)*walkSpeed*dt*speedFactor;
        }
        if (keysPressed[3]){ //D
            playerVelocity.x += Math.cos(lookDir.z)*walkSpeed*dt*speedFactor;
            playerVelocity.y += -Math.sin(lookDir.z)*walkSpeed*dt*speedFactor;
        }
        if (keysPressed[4]){ //W
            playerVelocity.x += Math.sin(lookDir.z)*walkSpeed*dt*speedFactor*-Math.sin(lookDir.x);
            playerVelocity.y += Math.cos(lookDir.z)*walkSpeed*dt*speedFactor*-Math.sin(lookDir.x);
            playerVelocity.z += camFwd.z*walkSpeed*dt*speedFactor;
        }
        if (keysPressed[5]){ //S
            playerVelocity.x += -Math.sin(lookDir.z)*walkSpeed*dt*speedFactor*-Math.sin(lookDir.x);
            playerVelocity.y += -Math.cos(lookDir.z)*walkSpeed*dt*speedFactor*-Math.sin(lookDir.x);
            playerVelocity.z += -camFwd.z*walkSpeed*dt*speedFactor;
        }
        if (keysPressed[1] && playerVelocity.z == 0) //SPACE
            playerVelocity.z += 512*dt;

        if (((keysPressed[2]&&keysPressed[4])||(keysPressed[2]&&keysPressed[5])||(keysPressed[5]&&keysPressed[3])||(keysPressed[3]&&keysPressed[4])) && !(((keysPressed[4]&&keysPressed[5]))||((keysPressed[2]&&keysPressed[3])))){
            playerVelocity.x /= Math.sqrt(2);
            playerVelocity.y /= Math.sqrt(2);
        }

        velocity.setVelocity(new float[]{playerVelocity.x,playerVelocity.y,playerVelocity.z}, velocity.getEntityIndex(camera.getEntityId()));
    }

    private Vector3f getFwd(float[] cameraDir){
        return new Vector3f(
                (float) Math.sin(cameraDir[2])*Math.sin(cameraDir[0])*-1,
                (float)Math.cos(cameraDir[2])*Math.sin(cameraDir[0])*-1,
                (float)-Math.cos(cameraDir[0])
        ).normalize();
    }

    private Vector3f getUp(Vector3f fwd, Vector3f right){
        return (new Vector3f(right).cross(fwd)).normalize();
    }

    private Vector3f getRight(float[] cameraDir){
        return new Vector3f(
                (float)Math.sin(cameraDir[2] + Math.PI/2),
                (float)Math.cos(cameraDir[2] + Math.PI/2),
                0f
        ).normalize();
    }


    private void updateValues(EntNode entityTree, ComponentMask componentMask){
        position = (Position) componentMask.getComponent(Position.class);
        rotation = (Rotation) componentMask.getComponent(Rotation.class);
        velocity = (Velocity) componentMask.getComponent(Velocity.class);
        radius = (Radius) componentMask.getComponent(Radius.class);
        input = (Input) componentMask.getComponent(Input.class);
        controllable = (Controllable) componentMask.getComponent(Controllable.class);
        voxel = (Voxel) componentMask.getComponent(Voxel.class);
        chunk = (Chunk) componentMask.getComponent(Chunk.class);

        camera = getEntities(entityTree, new Class[]{Camera.class})[0];
    }

    @Override
    public void exit() {

    }
}
