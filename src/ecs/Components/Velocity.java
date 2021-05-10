package ecs.Components;

import ecs.Component;
import util.Container;

public class Velocity extends Component {
    private Container<Float>[] velocity = new Container[3];

    public Velocity(){
        velocity[0] = new Container(Float.class);
        velocity[1] = new Container(Float.class);
        velocity[2] = new Container(Float.class);
    }

    public float[] getVelocity(int index){
        float[] velocity = new float[3];

        velocity[0] = this.velocity[0].get(index);
        velocity[1] = this.velocity[1].get(index);
        velocity[2] = this.velocity[2].get(index);

        return velocity;
    }

    public float getXVel(int index){
        return this.velocity[0].get(index);
    }

    public float getYVel(int index){
        return this.velocity[1].get(index);
    }

    public float getZVel(int index){
        return this.velocity[2].get(index);
    }

    public Container[] getVelocities(){
        return velocity;
    }

    public Container getXVels(){
        return velocity[0];
    }

    public Container getYVels(){
        return velocity[1];
    }

    public Container getZVels(){
        return velocity[2];
    }

    public void setVelocity(float[] velocity, int index){
        this.velocity[0].set(index, velocity[0]);
        this.velocity[1].set(index, velocity[1]);
        this.velocity[2].set(index, velocity[2]);
    }

    public Velocity add(float[] velocity){
        //System.out.println("Velocity added: (" + velocity[0] + ", " + velocity[1] + ")");
        int index = this.velocity[0].add(velocity[0]);
        this.velocity[1].add(velocity[1]);
        this.velocity[2].add(velocity[2]);
        super.setLastWriteIndex(index);
        return this;
    }

    public void setXVel(int index, float xVel){
        velocity[0].set(index,xVel);
    }

    public void setYVel(int index, float yVel){
        velocity[1].set(index,yVel);
    }

    public void setZVel(int index, float zVel){
        velocity[2].set(index,zVel);
    }
}
