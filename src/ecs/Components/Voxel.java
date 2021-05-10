package ecs.Components;

import ecs.Component;
import graphic.Mesh;
import util.VTree;

public class Voxel extends Component {
    private VTree voxel;
    private Mesh voxelMesh;
    public boolean initialized;
    public boolean updateRequired;
    public int size;

    public Voxel(){
        voxel = new VTree();
        initialized = false;
        updateRequired = true;
    }

    public VTree getVoxel(){
        return voxel;
    }

    public Mesh getVoxelMesh(){ return voxelMesh; }

    public void setVoxel(VTree voxelTree){
        this.voxel=voxelTree;
    }

    public void setVoxelMesh(Mesh voxelMesh){
        this.voxelMesh = voxelMesh;
    }

    public Voxel add(VTree voxelTree){
        int index = 0;
        voxel = voxelTree;
        super.setLastWriteIndex(index);
        return this;
    }
}
