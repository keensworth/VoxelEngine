package ecs.Components;

import ecs.Component;
import ecs.Entity;

public class Input extends Component {
    private Entity controllable;

    private float[] mousePos;
    private float[] prevMousePos;
    private float[] clickPos;
    private boolean clicked = false;

    private boolean[] pressedKeys;

    public int clickCoolDown = 0;

    public Input(){
        mousePos = new float[2];
        prevMousePos = new float[2];
        clickPos = new float[2];
        pressedKeys = new boolean[7];
    }

    public Input setControllable(){
        //System.out.prfloatln("Input added");
        super.setLastWriteIndex(0);
        return this;
    }

    public void setControllable(Entity entity){
        this.controllable = entity;
    }

    public Entity getControllable(){
        return this.controllable;
    }

    public float[] getClickPos(){
        return clickPos;
    }

    public float[] getMousePos(){
        return mousePos;
    }

    public float[] getPrevMousePos(){
        return prevMousePos;
    }

    public void setPrevMousePos(float[] prevMousePos){
        this.prevMousePos = prevMousePos;
    }

    public boolean isClicked(){
        return clicked;
    }

    public void setClickPos(float[] clickPos){
        this.clickPos = clickPos;
    }

    public void setMousePos(float[] move){
        mousePos = move;
    }

    public void setClicked(){
        clicked = true;
    }

    public void setUnclicked(){
        clicked = false;
    }

    public void reset(){
        clicked = false;
        pressedKeys = new boolean[7];
    }

    public boolean[] getPressedKeys(){
        return pressedKeys;
    }

    public void keyStateChange(int keyIndex, boolean pressed){
        pressedKeys[keyIndex] = pressed;
    }

}
