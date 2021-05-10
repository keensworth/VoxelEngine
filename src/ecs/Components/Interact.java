package ecs.Components;

import ecs.Component;
import util.Container;

public class Interact extends Component {
    private Container<Boolean> holdable;
    private Container<Integer> interactID;
    private boolean handsFree;
    private int holdingID;

    public Interact(){
        holdable = new Container<>(Boolean.class);
        interactID = new Container<>(Integer.class);
        handsFree = true;
        holdingID = -1;
    }

    public boolean isHoldable(int index){
        return holdable.get(index);
    }

    public void setHoldable(int index, boolean holdable){
        this.holdable.set(index, holdable);
    }

    public boolean isHandsFree(){
        return handsFree;
    }

    public void setHandsFree(boolean handsFree){
        this.handsFree = handsFree;
    }

    public int getInteractID(int index){
        return interactID.get(index);
    }

    public void setInteractID(int index, int interactID){
        this.interactID.set(index, interactID);
    }

    public int getHoldingID(){
        return holdingID;
    }

    public void setHoldingID(int holdingID){
        this.holdingID = holdingID;
    }

    public Interact add(int interactID, boolean holdable){
        super.setLastWriteIndex(this.holdable.add(holdable));
        this.interactID.add(interactID);
        return this;
    }
}
