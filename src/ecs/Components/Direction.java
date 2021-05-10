package ecs.Components;

import ecs.Component;
import util.Container;

public class Direction extends Component {
    private Container<Float>[] direction = new Container[3];

    public Direction(){
        direction[0] = new Container(Float.class);
        direction[1] = new Container(Float.class);
        direction[2] = new Container(Float.class);
    }

    public float[] getDirection(int index){
        float[] direction = new float[3];

        direction[0] = this.direction[0].get(index);
        direction[1] = this.direction[1].get(index);
        direction[2] = this.direction[2].get(index);

        return direction;
    }

    public float getXDir(int index){
        return this.direction[0].get(index);
    }

    public float getYDir(int index){
        return this.direction[1].get(index);
    }

    public float getZDir(int index){
        return this.direction[2].get(index);
    }

    public Container[] getDirections(){
        return direction;
    }

    public Container getXDir(){
        return direction[0];
    }

    public Container getYDir(){
        return direction[1];
    }

    public Container getZDir(){
        return direction[2];
    }

    public void setDirection(float[] direction, int index){
        this.direction[0].set(index, direction[0]);
        this.direction[1].set(index, direction[1]);
        this.direction[2].set(index, direction[2]);
    }

    public Direction add(float[] direction){
        //System.out.println("Direction added: (" + direction[0] + ", " + direction[1] + ")");
        int index = this.direction[0].add(direction[0]);
        this.direction[1].add(direction[1]);
        this.direction[2].add(direction[2]);
        //System.out.println("----------------------------" + index);
        super.setLastWriteIndex(index);
        return this;
    }

    public void setXDir(int index, float xDir){
        direction[0].set(index,xDir);
    }

    public void setYDir(int index, float yDir){
        direction[1].set(index,yDir);
    }

    public void setZYDir(int index, float zDir){
        direction[2].set(index,zDir);
    }
}
