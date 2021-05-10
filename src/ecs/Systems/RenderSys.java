package ecs.Systems;

import ecs.Component;
import ecs.Components.*;
import ecs.Entity;
import ecs.System;
import graphic.Renderer;
import graphic.Window;
import util.ComponentMask;
import util.Container;
import util.ETree.EntNode;
import util.VTree;
import util.VTreeParser;

public class RenderSys extends System {

    private Input input;
    private Position position;
    private Rotation rotation;
    private Radius radius;
    private World world;
    private Voxel voxel;
    private Chunk chunk;

    private Entity[] camera;

    private Window window;
    private Renderer renderer;

    public RenderSys(int width, int height){
        window = new Window("Voqcel", width, height, false);
        window.init();
        renderer = new Renderer();
        renderer.init(window);
    }

    @Override
    public Class update(float dt, EntNode entityTree, ComponentMask componentMask, boolean entityChange) {
        java.lang.System.out.println("Updating RenderSys");

        updateValues(dt, entityTree, componentMask, entityChange);

        float[] cameraPos = position.getPosition(position.getEntityIndex(camera[0].getEntityId()));
        float[] cameraDir = rotation.getRotation(rotation.getEntityIndex(camera[0].getEntityId()));

        //float[] cam2Pos = new float[]{0, 512, 512};
        //float[] cam2Dir = new float[]{(float) (-Math.PI/6), 0, (float) (Math.PI/2)};
        float[] cam2Pos = null;
        float[] cam2Dir = null;

        renderer.prepare(window, cameraPos, cameraDir, cam2Pos, cam2Dir);
        renderVoxels(entityTree, componentMask);

        window.update();
        return null;
    }

    private void renderVoxels(EntNode entities, ComponentMask components){
        if (!voxel.initialized || voxel.updateRequired) {
            long start = java.lang.System.nanoTime();
            VTree voxels = voxel.getVoxel();
            VTreeParser vTreeParser = new VTreeParser();
            vTreeParser.parse(voxels);

            chunk.setChunk(vTreeParser.getCTree());

            long end = java.lang.System.nanoTime();
            java.lang.System.out.println(" > > " + (end-start)/1000000f + "ms to parse VTree");
            voxel.initialized = true;
            voxel.updateRequired = false;
        }

        renderer.renderVoxels(entities, components);
    }

    private void updateValues(float dt, EntNode entityTree, ComponentMask componentMask, boolean entityChange){
        camera = getEntities(entityTree, new Class[]{Camera.class});

        position = (Position) componentMask.getComponent(Position.class);
        radius = (Radius) componentMask.getComponent(Radius.class);
        rotation = (Rotation) componentMask.getComponent(Rotation.class);
        input = (Input) componentMask.getComponent(Input.class);
        world = (World) componentMask.getComponent(World.class);
        voxel = (Voxel) componentMask.getComponent(Voxel.class);
        chunk = (Chunk) componentMask.getComponent(Chunk.class);
    }

    public Window getWindow(){
        return window;
    }

    @Override
    public void exit() {

    }
}

