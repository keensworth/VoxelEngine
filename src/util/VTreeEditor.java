package util;

import graphic.Mesh;

public class VTreeEditor {
    private  int[][] positions;
    private int[] colors;
    private int[] specular;
    private int[] diffuse;

    private Container<int[]> pos;
    private Container<Integer> col;
    private Container<Integer> spec;
    private Container<Integer> diff;

    private CTree cTree;
    public VTree allVoxels;

    private int currentSize;

    public VTreeEditor(){
        pos = new Container<>(int[].class, 64);
        col = new Container<>(Integer.class, 64);
        spec = new Container<>(Integer.class, 64);
        diff = new Container<>(Integer.class, 64);

        currentSize = 0;
    }

    public void parse(VTree vTree){
        allVoxels = vTree;
        parseToChunk(vTree, 0, 0, 0);
        formatContainers();
    }

    public Mesh parseToChunk(VTree vTree, int x, int y, int z){
        if (vTree==null)
            return null;
        int height = vTree.getHeight();
        if (height==2){
            parseAux(vTree, x, y, z);
            formatContainers();
            Mesh chunk = createChunk(currentSize);
            resetContainers();
            return chunk;
        } else {
            VTree[] children = vTree.getChildren();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    if (vTree.childExists((byte) i)) {
                        int newX = x + (i % 4) * (4 << (2 * (height - 1)));
                        int newY = y + ((i % 16) / 4) * (4 << (2 * (height - 1)));
                        int newZ = z + (i / 16) * (4 << (2 * (height - 1)));
                        parseToChunk(children[i], newX, newY, newZ);
                    }
                }
            }
        }
        return null;
    }

    private void parseAux(VTree vTree, int x, int y, int z){
        int height = vTree.getHeight();
        if (height==0){
            int[] voxels = vTree.getVoxels();
            for (int i = 0; i < voxels.length; i++){
                if (vTree.childExists((byte)i) && vTree != null) {
                    if (!voxelOccluded(allVoxels, x + i % 4, y + (i % 16) / 4, z + i / 16)){
                        int[] globalPos = new int[]{
                                x + i % 4,
                                y + (i % 16) / 4,
                                z + i / 16
                        };
                        int data = voxels[i];
                        int tempColor = VTree.getARGB(data);
                        int tempSpec = VTree.getSpec(data);
                        int tempDiff = VTree.getDiff(data);
                        pos.add(globalPos);
                        col.add(tempColor);
                        spec.add(tempSpec);
                        diff.add(tempDiff);
                        currentSize += 1;
                    }
                }
            }
        } else {
            VTree[] children = vTree.getChildren();
            for (int i = 0; i < children.length; i++){
                if (vTree.childExists((byte) i)){
                    int newX = x + (i%4) * (4<<(2*(height-1)));
                    int newY = y + ((i%16)/4) * (4<<(2*(height-1)));
                    int newZ = z + (i/16) * (4<<(2*(height-1)));
                    parseAux(children[i], newX, newY, newZ);
                }
            }
        }
    }


    private boolean voxelOccluded(VTree vTree, int x, int y, int z){
        int xP = vTree.getVoxelData(x+1,y,z);
        int xN = vTree.getVoxelData(x-1,y,z);
        int yP = vTree.getVoxelData(x,y+1,z);
        int yN = vTree.getVoxelData(x,y-1,z);
        int zP = vTree.getVoxelData(x,y,z+1);
        int zN = vTree.getVoxelData(x,y,z-1);
        if (xP == 0){
            return false;
        }
        if (xN == 0){
            return false;
        }
        if (yP == 0){
            return false;
        }
        if (yN == 0){
            return false;
        }
        if (zP == 0){
            return false;
        }
        if (zN == 0){
            return false;
        }
        return true;
    }

    //BROKEN
    private boolean voxelOccludedRecursive(VTree vTree, int i){
        boolean xPOccluded = false;
        boolean xNOccluded = false;
        boolean yPOccluded = false;
        boolean yNOccluded = false;
        boolean zPOccluded = false;
        boolean zNOccluded = false;

        //XP
        if (i%4 == 3){
            VTree neighbor = getNeighbor(vTree.getParent(), vTree.getRootIndex(), 1);
            if (neighbor != null)
                xPOccluded = neighbor.childExists((byte) (i-3));
        } else {
            xPOccluded = vTree.childExists((byte) (i+1));
        }
        //XN
        if (i%4 == 0){
            VTree neighbor = getNeighbor(vTree.getParent(), vTree.getRootIndex(), -1);
            if (neighbor != null)
                xNOccluded = neighbor.childExists((byte) (i+3));
        } else {
            xNOccluded = vTree.childExists((byte) (i-1));
        }
        //YP
        if (i%16 >= 12){
            VTree neighbor = getNeighbor(vTree.getParent(), vTree.getRootIndex(), 4);
            if (neighbor != null)
                yPOccluded = neighbor.childExists((byte) (i-12));
        } else {
            yPOccluded = vTree.childExists((byte) (i+4));
        }
        //YN
        if (i%16 <= 3){
            VTree neighbor = getNeighbor(vTree.getParent(), vTree.getRootIndex(), -4);
            if (neighbor != null)
                yNOccluded = neighbor.childExists((byte) (i+12));
        } else {
            yNOccluded = vTree.childExists((byte) (i-4));
        }
        //ZP
        if (i + 16 > 63){
            VTree neighbor = getNeighbor(vTree.getParent(), vTree.getRootIndex(), 16);
            if (neighbor != null)
                zPOccluded = neighbor.childExists((byte) (i-48));
        } else {
            zPOccluded = vTree.childExists((byte) (i+16));
        }
        //ZN
        if (i - 16 < 0){
            VTree neighbor = getNeighbor(vTree.getParent(), vTree.getRootIndex(), -16);
            if (neighbor != null)
                xPOccluded = neighbor.childExists((byte) (i+48));
        } else {
            zNOccluded = vTree.childExists((byte) (i-16));
        }

        return xPOccluded&&xNOccluded&&yPOccluded&&yNOccluded&&zPOccluded&&zNOccluded;
    }

    private VTree getNeighbor(VTree vTree, int currentIndex, int indexChange){
        boolean climbTree = false;
        int childIndex = 0;
        if (indexChange==1 && currentIndex%4==3){
            climbTree = true;
            childIndex = -3;
        } else if (indexChange==-1 && currentIndex%4 == 0){
            climbTree = true;
            childIndex = 3;
        } else if (indexChange==4 && currentIndex%16+4 > 15){
            climbTree = true;
            childIndex = -12;
        } else if (indexChange==-4 && currentIndex%16-4 < 0){
            climbTree = true;
            childIndex = 12;
        } else if (indexChange==16 && currentIndex + 16 > 63){
            climbTree = true;
            childIndex = -48;
        } else if (indexChange==-16 && currentIndex - 16 < 0){
            climbTree = true;
            childIndex = 48;
        }
        //System.out.println(currentIndex + ", " + indexChange);
        if (climbTree && vTree.getParent() != null) {
            VTree neighbor = getNeighbor(vTree.getParent(), vTree.getRootIndex(), indexChange);
            if (neighbor != null){
                return neighbor.getChild((byte) (currentIndex + childIndex));
            }
        }
        else if (currentIndex + indexChange >= 0)
            return vTree.getChild((byte) (currentIndex + indexChange));
        return null;
    }

    private void formatContainers(){
        positions = new int[pos.getSize()][3];
        for (int i = 0; i < pos.getSize(); i++){
            int[] posArr = pos.get(i);
            positions[i] = posArr;
        }

        colors = new int[col.getSize()];
        for (int i = 0; i < col.getSize(); i++){
            colors[i] = col.get(i);
        }
        specular = new int[spec.getSize()];
        for (int i = 0; i < spec.getSize(); i++){
            specular[i] = spec.get(i);
        }
        diffuse = new int[diff.getSize()];
        for (int i = 0; i < diff.getSize(); i++){
            diffuse[i] = diff.get(i);
        }
    }

    private Mesh createChunk(int size){
        //reshape positions
        float[] newPos = new float[positions.length * 3];
        for (int i = 0; i < positions.length; i++) {
            newPos[i * 3] = positions[i][0];
            newPos[i * 3 + 1] = positions[i][1];
            newPos[i * 3 + 2] = positions[i][2];
        }

        //reshape color
        float[] newCol = new float[colors.length * 3];
        for (int i = 0; i < colors.length; i++) {
            newCol[i * 3] = ((colors[i] >> 16) & 0xFF)/255f;
            newCol[i * 3 + 1] = ((colors[i] >> 8) & 0xFF)/255f;
            newCol[i * 3 + 2] = (colors[i] & 0xFF)/255f;
        }

        //recast specular
        float[] newSpec = new float[specular.length];
        for (int i = 0; i < specular.length; i++) {
            newSpec[i] = ((float) specular[i]) / 15f;
        }

        //recast diffuse
        float[] newDiff = new float[diffuse.length];
        for (int i = 0; i < diffuse.length; i++) {
            newDiff[i] = ((float) diffuse[i]) / 15f;
        }

        return initVoxelMesh(newPos, newCol, newSpec, newDiff, size);
    }

    private Mesh initVoxelMesh(float[] offsets, float[] colors, float[] spec, float[] diff, int size){
        float[] positions = new float[]{
                //bottom
                0f, 0f, 0f,
                1f, 0f, 0f,
                1f, 1f, 0f,
                0f, 1f, 0f,
                //top
                0f, 0f, 1f,
                1f, 0f, 1f,
                1f, 1f, 1f,
                0f, 1f, 1f,
                //right
                1f, 0f, 0f,
                1f, 1f, 0f,
                1f, 1f, 1f,
                1f, 0f, 1f,
                //left
                0f, 0f, 0f,
                0f, 1f, 0f,
                0f, 1f, 1f,
                0f, 0f, 1f,
                //front
                0f, 0f, 0f,
                1f, 0f, 0f,
                1f, 0f, 1f,
                0f, 0f, 1f,
                //back
                0f, 1f, 0f,
                1f, 1f, 0f,
                1f, 1f, 1f,
                0f, 1f, 1f
        };
        float[] normals = new float[]{
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,

                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,

                1, 0, 0,
                1, 0, 0,
                1, 0, 0,
                1, 0, 0,

                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,

                0, -1, 0,
                0, -1, 0,
                0, -1, 0,
                0, -1, 0,

                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0
        };
        int[] indices = new int[]{
                0,3,2,2,1,0,
                4,5,6,6,7,4,
                8,9,10,10,11,8,
                12,15,14,14,13,12,
                16,17,18,18,19,16,
                20,23,22,22,21,20
        };
        return new Mesh(positions, indices, normals, offsets, colors, spec, diff, size);
    }

    private void resetContainers(){
        pos = new Container<>(int[].class, 64);
        col = new Container<>(Integer.class, 64);
        spec = new Container<>(Integer.class, 64);
        diff = new Container<>(Integer.class, 64);

        positions = null;
        colors = null;
        specular = null;
        diffuse = null;

        currentSize = 0;
    }

    public int[][] getPositions(){
        return positions;
    }

    public int[] getColors(){
        return colors;
    }

    public int[] getSpecular(){
        return specular;
    }

    public int[] getDiffuse(){
        return diffuse;
    }

    public CTree getCTree(){
        return cTree;
    }

    public void delete(){
        positions = null;
        colors = null;
        specular = null;
        diffuse = null;
    }
}
