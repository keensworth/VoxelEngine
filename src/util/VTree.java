package util;

public class VTree {
    private VTree parent;
    private short rootParentHeight;  // <PPPPPP,RRRRRR,HHHH>   Parent index, root index, height/depth (0 = voxel level)
    private long childMask;          // <00010010.....10010>   (1 indicates valid/alive child node)
    private VTree[] children;
    private int[] voxels;            // Voxel data (specular, diffuse ,R, G, B)
                                     // <SSSS,DDDD,RRRRRRRR,GGGGGGGG,BBBBBBBB>

    public VTree(){
        this(4); //default of 4
    }

    public VTree(int height){
        this.parent = null;

        if (height > 0){
            this.voxels = null;
            children = new VTree[64];
        } else {
            this.voxels = new int[64];
            children = null;
        }

        this.childMask = 0;
        updateMask();

        rootParentHeight = (byte)(((0b000000)<<10)|((0b000000)<<4)|(height&0b1111));
    }

    public VTree(VTree parent, int[] voxels, int parentIndex, int rootIndex, int height){
        this.parent = parent;
        if (voxels == null) {
            this.voxels = new int[64];
        } else {
            this.voxels = voxels;
        }
        children = null;


        this.childMask = 0;
        updateMask();

        rootParentHeight = (byte)((((rootIndex)&0b111111)<<10)|(((parentIndex)&0b111111)<<4)|(height&0b1111));
    }

    public VTree(VTree parent, VTree[] children, int parentIndex, int rootIndex, int height){
        this.parent = parent;
        this.children = children;
        voxels = null;

        this.childMask = 0;
        updateMask();

        rootParentHeight = (byte)((((rootIndex)&0b111111)<<10)|(((parentIndex)&0b111111)<<4)|(height&0b1111));
    }
    
    public void setParent(VTree parent){
        this.parent = parent;    
    }
    
    public VTree getParent(){
        return parent;
    }
    
    public void setVoxels(int[] voxels){
        this.voxels = voxels;
    }

    public int[] getVoxels(){
        return voxels;
    }

    public int[] getVoxels(int x, int y, int z){
        int height = this.getHeight();
        return getVoxelsAux(x, y, z, this, height);
    }

    private int[] getVoxelsAux(int x, int y, int z, VTree VTree, int height){
        byte xPos = (byte) ((x>>>(2*height))&0b11);
        byte yPos = (byte) ((y>>>(2*height))&0b11);
        byte zPos = (byte) ((z>>>(2*height))&0b11);
        byte index = (byte) (xPos + 4*yPos + 16*zPos);

        if (!VTree.childExists(index)){
            return null;
        }
        if (height>0){
            return getVoxelsAux(x, y, z, VTree.getChild(index), height-1);
        } else {
            return VTree.getVoxels();
        }
    }
    
    public void setVoxel(byte voxel, int data){
        voxels[voxel] = data;
    }
    
    public int getVoxelData(byte voxel){
        return voxels[voxel];
    }

    public int getVoxelData(int x, int y, int z){
        int height = this.getHeight();
        return getVoxelDataAux(x, y, z, this, height);
    }

    private int getVoxelDataAux(int x, int y, int z, VTree VTree, int height){
        byte xPos = (byte) ((x>>>(2*height))&0b11);
        byte yPos = (byte) ((y>>>(2*height))&0b11);
        byte zPos = (byte) ((z>>>(2*height))&0b11);
        byte index = (byte) (xPos + 4*yPos + 16*zPos);


        if (height>0){
            if (!VTree.childExists(index))
                return -1;
            return getVoxelDataAux(x, y, z, VTree.getChild(index), height-1);
        } else {
            if (!VTree.childExists(index))
                return 0;
            return VTree.getVoxelData(index);
        }
    }

    public void addVoxel(int x, int y, int z, int data){
        int height = this.getHeight();
        addVoxelAux(x,y,z,data,this, height);
    }

    private void addVoxelAux(int x, int y, int z, int data, VTree VTree, int height){
        byte xPos = (byte) ((x>>>(2*height))&0b11);
        byte yPos = (byte) ((y>>>(2*height))&0b11);
        byte zPos = (byte) ((z>>>(2*height))&0b11);
        byte index = (byte) (xPos + 4*yPos + 16*zPos);

        if (!VTree.childExists(index) && height > 0){
            VTree.newChild(index);
            VTree.updateMask();
        }

        if(height>0){
            addVoxelAux(x, y, z, data, VTree.getChild(index), height-1);
        } else {
            VTree.setVoxel(index, data);
            VTree.updateMask();
        }
    }

    public void addVoxels(int[] x, int[] y, int[] z, int[] data){
        int height = this.getHeight();
        for (int i = 0; i < x.length; i++){
            addVoxelAux(x[i], y[i], z[i], data[i], this, height);
        }
    }
    
    public void setChildren(VTree[] children){
        this.children = children;
    }

    public VTree[] getChildren(){
        return children;
    }
    
    public void setChild(byte index, VTree data){
        children[index] = data;
    }

    public VTree getChild(byte index){
        return children[index];
    }

    public VTree getChildAtHeight(int height, int x, int y, int z){
        return getChildAtHeightAux(this, this.getHeight(), height, x, y, z);
    }

    public VTree getChildAtHeightAux(VTree vTree, int height, int desiredHeight, int x, int y, int z){
        byte xPos = (byte) ((x>>>(2*height))&0b11);
        byte yPos = (byte) ((y>>>(2*height))&0b11);
        byte zPos = (byte) ((z>>>(2*height))&0b11);
        byte index = (byte) (xPos + 4*yPos + 16*zPos);


        if (height>desiredHeight){
            if (!vTree.childExists(index))
                return null;
            return getChildAtHeightAux(vTree.getChild(index), height-1, desiredHeight, x, y, z);
        } else {
            if (!vTree.childExists(index))
                return null;
            return vTree.getChild(index);
        }
    }

    public void newChild(byte index){
        if (this.getHeight()>1) {
            children[index] = new VTree(this, new VTree[64], this.getRootIndex(), index, this.getHeight() - 1);
        } else if (this.getHeight()==1){
            int[] empty = new int[64];
            children[index] = new VTree(this, empty, this.getRootIndex(), index, 0);
        }
    }
    
    public void updateMask(){
        long tempMask = 0;
        if (voxels != null){
            long scan = 1;
            for (byte i = 0; i < 64; i++){
                if (voxels[i] != 0){
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
    
    //---------------------------------------------VOXEL RELEVANT METHODS-----------------------------------------------
    /*

    public byte getEdgeMask(byte voxel){
        return ((voxel[index]>>>24)&0b11111111);
    }
    
    public byte getX2Edge(byte voxel){
        return (voxel[index]&0b11111111);
    }

    public byte getY2Edge(byte voxel){
        return ((voxel[index]>>>8)&0b11111111);
    }

    public byte getZ2Edge(byte voxel){
        return ((voxel[index]>>>16)&0b11111111);
    }
    
    public byte getX1Edge(byte voxel){
        
    }

    public byte getY1Edge(byte voxel){

    }

    public byte getZ1Edge(byte voxel){

    }

    public byte getX2Voxel(byte voxel){
        return getVoxel(voxel, (byte) 1);
    }

    public byte getY2Voxel(byte voxel){
        return getVoxel(voxel, (byte) 4);
    }

    public byte getZ2Voxel(byte voxel){
        return getVoxel(voxel, (byte) 16);
    }

    public byte getX1Voxel(byte voxel){
        return getVoxel(voxel, (byte) -1);
    }

    public byte getY1Voxel(byte voxel){
        return getVoxel(voxel, (byte) -4);
    }

    public byte getZ1Voxel(byte voxel){
        return getVoxel(voxel, (byte) -16);
    }

    public byte getX2VoxelData(byte voxel){
        return getVoxel(voxel, (byte) 1);
    }

    public byte getY2VoxelData(byte voxel){
        return getVoxel(voxel, (byte) 4);
    }

    public byte getZ2VoxelData(byte voxel){
        return getVoxel(voxel, (byte) 16);
    }

    public byte getX1VoxelData(byte voxel){
        return getVoxel(voxel, (byte) -1);
    }

    public byte getY1VoxelData(byte voxel){
        return getVoxel(voxel, (byte) -4);
    }

    public byte getZ1VoxelData(byte voxel){
        return getVoxel(voxel, (byte) -16);
    }

    public byte getVoxel(byte voxel, byte indexShift){
        if ((voxel + indexShift)<0){

        } else if (voxel + indexShift > 64){

        } else {
            return
        }
    }

    public byte getVoxelData(byte voxel, byte indexShift){
        if ((voxel + indexShift)<0){

        } else if (voxel + indexShift > 64){

        } else {
            return
        }
    }

     */
}
