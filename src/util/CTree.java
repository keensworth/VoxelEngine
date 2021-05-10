package util;

import graphic.Mesh;

public class CTree {
    private CTree parent;
    private short rootParentHeight;  // <PPPPPP,RRRRRR,HHHH>   Parent index, root index, height/depth (0 = Chunk level)
    private long childMask;          // <00010010.....10010>   (1 indicates valid/alive child node)
    private CTree[] children;
    private Mesh[] chunks;            // Chunk data (specular, diffuse ,R, G, B)
    // <SSSS,DDDD,RRRRRRRR,GGGGGGGG,BBBBBBBB>

    public CTree(){
        this(4); //default of 4
    }

    public CTree(int height){
        this.parent = null;

        if (height > 0){
            this.chunks = null;
            children = new CTree[64];
        } else {
            this.chunks = new Mesh[64];
            children = null;
        }

        this.childMask = 0;
        updateMask();

        rootParentHeight = (byte)(((0b000000)<<10)|((0b000000)<<4)|(height&0b1111));
    }

    public CTree(CTree parent, Mesh[] chunks, int parentIndex, int rootIndex, int height){
        this.parent = parent;
        if (chunks == null) {
            this.chunks = new Mesh[64];
        } else {
            this.chunks = chunks;
        }
        children = null;


        this.childMask = 0;
        updateMask();

        rootParentHeight = (byte)((((rootIndex)&0b111111)<<10)|(((parentIndex)&0b111111)<<4)|(height&0b1111));
    }

    public CTree(CTree parent, CTree[] children, int parentIndex, int rootIndex, int height){
        this.parent = parent;
        this.children = children;
        chunks = null;

        this.childMask = 0;
        updateMask();

        rootParentHeight = (byte)((((rootIndex)&0b111111)<<10)|(((parentIndex)&0b111111)<<4)|(height&0b1111));
    }

    public void setParent(CTree parent){
        this.parent = parent;
    }

    public CTree getParent(){
        return parent;
    }

    public void setChunks(Mesh[] chunks){
        this.chunks = chunks;
    }

    public Mesh[] getChunks(){
        return chunks;
    }

    public Mesh[] getChunks(int x, int y, int z){
        int height = this.getHeight();
        return getChunksAux(x, y, z, this, height);
    }

    private Mesh[] getChunksAux(int x, int y, int z, CTree CTree, int height){
        byte xPos = (byte) ((x>>>(2*height))&0b11);
        byte yPos = (byte) ((y>>>(2*height))&0b11);
        byte zPos = (byte) ((z>>>(2*height))&0b11);
        byte index = (byte) (xPos + 4*yPos + 16*zPos);

        if (!CTree.childExists(index)){
            return null;
        }
        if (height>0){
            return getChunksAux(x, y, z, CTree.getChild(index), height-1);
        } else {
            return CTree.getChunks();
        }
    }

    public void setChunk(int chunk, Mesh data){
        chunks[chunk] = data;
    }

    public Mesh getChunkData(int Chunk){
        return chunks[Chunk];
    }

    public Mesh getChunkData(int x, int y, int z){
        int height = this.getHeight();
        return getChunkDataAux(x, y, z, this, height);
    }

    private Mesh getChunkDataAux(int x, int y, int z, CTree CTree, int height){
        byte xPos = (byte) ((x>>>(2*height))&0b11);
        byte yPos = (byte) ((y>>>(2*height))&0b11);
        byte zPos = (byte) ((z>>>(2*height))&0b11);
        byte index = (byte) (xPos + 4*yPos + 16*zPos);

        if (!CTree.childExists(index)){
            return null;
        }
        if (height>0){
            return getChunkDataAux(x, y, z, CTree.getChild(index), height-1);
        } else {
            return CTree.getChunkData(index);
        }
    }

    public void addChunk(int x, int y, int z, Mesh data){
        if (data != null) {
            int height = this.getHeight();
            addChunkAux(x, y, z, data, this, height);
        }
    }

    private void addChunkAux(int x, int y, int z, Mesh data, CTree CTree, int height){
        byte xPos = (byte) ((x>>>(2*height))&0b11);
        byte yPos = (byte) ((y>>>(2*height))&0b11);
        byte zPos = (byte) ((z>>>(2*height))&0b11);
        byte index = (byte) (xPos + 4*yPos + 16*zPos);

        if (!CTree.childExists(index) && height > 0){
            CTree.newChild(index);
            CTree.updateMask();
        }

        if(height>0){
            addChunkAux(x, y, z, data, CTree.getChild(index), height-1);
        } else {
            CTree.setChunk(index, data);
            CTree.updateMask();
        }
    }

    public void addChunks(int[] x, int[] y, int[] z, Mesh[] data){
        int height = this.getHeight();
        for (int i = 0; i < x.length; i++){
            addChunkAux(x[i], y[i], z[i], data[i], this, height);
        }
    }

    public void setChildren(CTree[] children){
        this.children = children;
    }

    public CTree[] getChildren(){
        return children;
    }

    public void setChild(byte index, CTree data){
        children[index] = data;
    }

    public CTree getChild(byte index){
        return children[index];
    }

    public void newChild(byte index){
        if (this.getHeight()>1) {
            children[index] = new CTree(this, new CTree[64], this.getRootIndex(), index, this.getHeight() - 1);
        } else if (this.getHeight()==1){
            Mesh[] empty = new Mesh[64];
            children[index] = new CTree(this, empty, this.getRootIndex(), index, 0);
        }
    }

    public void updateMask(){
        long tempMask = 0;
        if (chunks != null){
            long scan = 1;
            for (byte i = 0; i < 64; i++){
                if (chunks[i] != null){
                    tempMask |= scan;
                }
                scan = scan<<1;
            }
        } else if (children != null){
            long scan = 1;
            for (byte i = 0; i < 64; i++){
                if (children[i] != null){
                    tempMask |= scan;
                }
                scan = scan<<1;
            }
        }
        childMask = tempMask;
    }

    public boolean childExists(byte index){
        return (((childMask>>>index)&0b1) == 1);
    }

    public void setChildAlive(byte index){
        long temp = 1;
        childMask |= (temp<<index);
    }

    public void setChildDead(byte index){
        long temp = 1;
        childMask &= ~(temp<<index);
    }

    public int getRootIndex(){
        return (rootParentHeight>>>4)&0b111111;
    }

    public int getParentIndex(){
        return (rootParentHeight>>>10)&0b111111;
    }

    public int getHeight(){
        return (rootParentHeight)&0b1111;
    }

    public int reverse(int i) {
        // HD, Figure 7-1
        i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
        i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;
        i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;
        i = (i << 24) | ((i & 0xff00) << 8) |
                ((i >>> 8) & 0xff00) | (i >>> 24);
        return i;
    }

    public int removeZeros(int i){
        byte temp = 0b1;
        while(temp!=0){
            temp = (byte) (temp>>>1);
            i = i >>>1;
        }
        return i;
    }

    public static int getARGB(int data) {
        return ((data&0x00FFFFFF)|0xFF000000);
    }

    public static int getSpec(int data){
        return (data>>>28);
    }

    public static int getDiff(int data){
        return (data>>>24)&0xF;
    }

    //---------------------------------------------Chunk RELEVANT METHODS-----------------------------------------------
    /*

    public byte getEdgeMask(byte Chunk){
        return ((Chunk[index]>>>24)&0b11111111);
    }
    
    public byte getX2Edge(byte Chunk){
        return (Chunk[index]&0b11111111);
    }

    public byte getY2Edge(byte Chunk){
        return ((Chunk[index]>>>8)&0b11111111);
    }

    public byte getZ2Edge(byte Chunk){
        return ((Chunk[index]>>>16)&0b11111111);
    }
    
    public byte getX1Edge(byte Chunk){
        
    }

    public byte getY1Edge(byte Chunk){

    }

    public byte getZ1Edge(byte Chunk){

    }

    public byte getX2Chunk(byte Chunk){
        return getChunk(Chunk, (byte) 1);
    }

    public byte getY2Chunk(byte Chunk){
        return getChunk(Chunk, (byte) 4);
    }

    public byte getZ2Chunk(byte Chunk){
        return getChunk(Chunk, (byte) 16);
    }

    public byte getX1Chunk(byte Chunk){
        return getChunk(Chunk, (byte) -1);
    }

    public byte getY1Chunk(byte Chunk){
        return getChunk(Chunk, (byte) -4);
    }

    public byte getZ1Chunk(byte Chunk){
        return getChunk(Chunk, (byte) -16);
    }

    public byte getX2ChunkData(byte Chunk){
        return getChunk(Chunk, (byte) 1);
    }

    public byte getY2ChunkData(byte Chunk){
        return getChunk(Chunk, (byte) 4);
    }

    public byte getZ2ChunkData(byte Chunk){
        return getChunk(Chunk, (byte) 16);
    }

    public byte getX1ChunkData(byte Chunk){
        return getChunk(Chunk, (byte) -1);
    }

    public byte getY1ChunkData(byte Chunk){
        return getChunk(Chunk, (byte) -4);
    }

    public byte getZ1ChunkData(byte Chunk){
        return getChunk(Chunk, (byte) -16);
    }

    public byte getChunk(byte Chunk, byte indexShift){
        if ((Chunk + indexShift)<0){

        } else if (Chunk + indexShift > 64){

        } else {
            return
        }
    }

    public byte getChunkData(byte Chunk, byte indexShift){
        if ((Chunk + indexShift)<0){

        } else if (Chunk + indexShift > 64){

        } else {
            return
        }
    }

     */
}
