package ecs.Components;

import ecs.Component;
import graphic.Mesh;
import util.CTree;

public class Chunk extends Component {
    private CTree chunk;
    private Mesh chunkMesh;
    public boolean initialized;
    public boolean updateRequired;
    public int size;

    public Chunk(){
        chunk = new CTree();
        initialized = false;
        updateRequired = true;
    }

    public CTree getChunk(){
        return chunk;
    }

    public Mesh getChunkMesh(int x, int y, int z){ return chunk.getChunkData(x, y, z); }

    public void setChunk(CTree chunkTree){
        this.chunk=chunkTree;
    }

    public void setChunkMesh(Mesh chunkMesh){
        this.chunkMesh = chunkMesh;
    }

    public Chunk add(CTree chunkTree){
        int index = 0;
        chunk = chunkTree;
        super.setLastWriteIndex(index);
        return this;
    }
}
