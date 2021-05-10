package ecs.Systems;

import ecs.Components.*;
import ecs.Entity;
import ecs.System;
import org.joml.Vector3f;
import util.ComponentMask;
import util.Container;
import util.ETree.CollisionNode;
import util.ETree.EntNode;
import util.Geometry;

public class CollisionSys extends System {
    private ComponentMask components;
    private Position position;
    private Velocity velocity;
    private Collide collide;
    private Rotation rotation;
    private Interact interact;

    Entity camera;

    public CollisionSys() {
        super(Position.class, Radius.class);
    }

    @Override
    public Class update(float dt, EntNode entityTree, ComponentMask componentMask, boolean indexChange) {
        java.lang.System.out.println("Updating CollisionSys");

        updateValues(dt, entityTree, componentMask, indexChange);

        checkCollisions(entityTree);

        updateHoldable(entityTree);

        return null;
    }

    private void checkCollisions(EntNode entityTree){
        // camera position
        Vector3f camPos = new Vector3f(position.getPosition(position.getEntityIndex(camera.getEntityId())));
        Vector3f camVel = new Vector3f(velocity.getVelocity(velocity.getEntityIndex(camera.getEntityId())));
        float playerRadius = 8;
        float playerHeight = 0;

        //ground
        if (camPos.z-playerHeight < 0){
            position.setPosition(new float[]{camPos.x, camPos.y, playerHeight}, position.getEntityIndex(camera.getEntityId()));
            velocity.setVelocity(new float[]{camVel.x, camVel.y, 0}, velocity.getEntityIndex(camera.getEntityId()));
        }

        //get collidable objects
        Entity[] entities = getEntities(entityTree, new Class[]{Collide.class});

        for (Entity entity : entities){
            Vector3f pos = new Vector3f(position.getPosition(position.getEntityIndex(entity.getEntityId())));
            Vector3f corner = collide.getCorner(collide.getEntityIndex(entity.getEntityId()));
            corner = new Vector3f(corner).add(pos);
            Vector3f dimensions = collide.getDimensions(collide.getEntityIndex(entity.getEntityId()));

            if (cubeSphereIntersect(corner, dimensions, camPos, playerRadius)){
                adjustPlayerPosition(corner, dimensions, camPos, playerRadius);
                break;
            }
        }
    }

    private boolean cubeSphereIntersect(Vector3f corner, Vector3f dimensions, Vector3f camPos, float radius){
        Vector3f corner2 = new Vector3f(corner).add(dimensions);

        float camX = camPos.x;
        float camY = camPos.y;
        float camZ = camPos.z;

        float distanceSqrd = radius * radius;
        if (camPos.x < corner.x)
            distanceSqrd -= squared(camX-corner.x);
        else if (camPos.x > corner2.x)
            distanceSqrd -= squared(camX-corner2.x);
        if (camPos.y < corner.y)
            distanceSqrd -= squared(camY-corner.y);
        else if (camPos.y > corner2.y)
            distanceSqrd -= squared(camY-corner2.y);
        if (camPos.z < corner.z)
            distanceSqrd -= squared(camZ-corner.z);
        else if (camPos.z > corner2.z)
            distanceSqrd -= squared(camZ-corner2.z);
        return distanceSqrd > 0;
    }

    private void adjustPlayerPosition(Vector3f corner, Vector3f dimensions, Vector3f camPos, float radius){
        Vector3f corner2 = new Vector3f(corner).add(dimensions);
        float camX = camPos.x;
        float camY = camPos.y;
        float camZ = camPos.z;

        float c1x = corner.x;
        float c1y = corner.y;
        float c1z = corner.z;

        float c2x = corner2.x;
        float c2y = corner2.y;
        float c2z = corner2.z;

        if (Math.abs(camX-c1x)+Math.abs(camX-c2x) > dimensions.x){
            if (camX < c1x){
                camPos.x = c1x-radius-0.001f;
            } else if (camPos.x > corner2.x) {
                camPos.x = c2x+radius+0.001f;
            }
        } else if (Math.abs(camY-c1y)+Math.abs(camY-c2y) > dimensions.y){
            if (camY < c1y){
                camPos.y = c1y-radius-0.001f;
            } else if (camPos.y > corner2.y) {
                camPos.y = c2y+radius+0.001f;
            }
        } else if (Math.abs(camZ-c1z)+Math.abs(camZ-c2z) > dimensions.z){
            if (camZ < c1z){
                camPos.z = c1z-radius-0.001f;
            } else if (camPos.z > corner2.z) {
                camPos.z = c2z+radius+0.001f;
            }
        }
        position.setPosition(new float[]{camPos.x, camPos.y, camPos.z}, position.getEntityIndex(camera.getEntityId()));
    }

    private float squared(float f){
        return f*f;
    }

    private void updateHoldable(EntNode entityTree){
        Entity[] entities = getEntities(entityTree, new Class[]{Interact.class});
        int holdingID = interact.getHoldingID();

        for (Entity entity : entities){
            int interactID = interact.getInteractID(interact.getEntityIndex(entity.getEntityId()));
            if (interactID==holdingID){
                updateHoldablePosition(entity);
                break;
            }
        }

    }

    private void updateHoldablePosition(Entity entity){
        Vector3f camRot = new Vector3f(rotation.getRotation(rotation.getEntityIndex(camera.getEntityId())));
        Vector3f camPos = new Vector3f(position.getPosition(position.getEntityIndex(camera.getEntityId())));

        position.setPosition(new float[]{camPos.x+(float) (Math.sin(camRot.z+0.4)*24), camPos.y+ (float) (Math.cos(camRot.z+0.4)*24), camPos.z-16}, position.getEntityIndex(entity.getEntityId()));
        rotation.setRotation(new float[]{0,0,-camRot.z}, rotation.getEntityIndex(entity.getEntityId()));
    }

    private void updateValues(float dt, EntNode entityTree, ComponentMask componentMask, boolean indexChange){
        components = componentMask;
        int worldWidth = this.getECS().width;
        int worldHeight = this.getECS().height;

        position = (Position) componentMask.getComponent(Position.class);
        velocity = (Velocity) componentMask.getComponent(Velocity.class);
        collide = (Collide) componentMask.getComponent(Collide.class);
        rotation = (Rotation) componentMask.getComponent(Rotation.class);
        interact = (Interact) componentMask.getComponent(Interact.class);

        camera = getEntities(entityTree, new Class[]{Camera.class})[0];
    }

    private int getIndex(Entity[] arr, Entity entity) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == entity)
                return i;
        return -1;
    }

    @Override
    public void exit() {

    }
}
