package ecs.Systems;

import ecs.Entity;
import ecs.System;
import ecs.Components.*;
import org.joml.Vector3f;
import util.ComponentMask;
import util.Container;
import util.ETree.EntNode;

public class MovementSys extends System {
    private Position position;
    private Velocity velocity;
    private Rotation rotation;
    private Interact interact;
    private Entity camera;

    public MovementSys() {
        super(Position.class, Velocity.class);
    }

    @Override
    public Class update(float dt, EntNode entityTree, ComponentMask componentMask, boolean entityChange) {
        java.lang.System.out.println("Updating MovementSys");
        Entity[] entities = getEntities(entityTree);
        camera = getEntities(entityTree, new Class[]{Camera.class})[0];

        int[] positionIndices = getComponentIndices(Position.class, entities, componentMask);
        int[] velocityIndices = getComponentIndices(Velocity.class, entities, componentMask);

        position = (Position) componentMask.getComponent(Position.class);
        velocity = (Velocity) componentMask.getComponent(Velocity.class);
        rotation = (Rotation) componentMask.getComponent(Rotation.class);
        interact = (Interact) componentMask.getComponent(Interact.class);

        updateCameraPos(dt);
        //moveLight(entityTree, dt);
        return null;
    }

    private void updateCameraPos(float dt){
        int gravity = 0;
        Vector3f cameraVelocity = new Vector3f(velocity.getVelocity(velocity.getEntityIndex(camera.getEntityId())));
        Vector3f cameraPos = new Vector3f(position.getPosition(position.getEntityIndex(camera.getEntityId())));

        cameraPos.add(cameraVelocity);

        velocity.setVelocity(new float[]{0,0,0*cameraVelocity.z-gravity*dt},velocity.getEntityIndex(camera.getEntityId()));
        position.setPosition(new float[]{cameraPos.x, cameraPos.y, cameraPos.z}, position.getEntityIndex(camera.getEntityId()));
    }

    private void moveLight(EntNode entityTree, float dt){
        Entity[] entities = getEntities(entityTree, new Class[]{Light.class});
        for (Entity entity : entities){
            Vector3f pos = new Vector3f(position.getPosition(position.getEntityIndex(entity.getEntityId())));
            pos.y += 64*dt;
            position.setPosition(new float[]{pos.x, pos.y, pos.z}, position.getEntityIndex(entity.getEntityId()));
        }
    }

    @Override
    public void exit() {

    }
}
