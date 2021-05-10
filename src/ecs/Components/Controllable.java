package ecs.Components;

import ecs.Component;
import util.Container;

public class Controllable extends Component {
    private Container controllable;
    private int lastID;

    public Controllable(){
        controllable = new Container();
        lastID = 1;
    }

    public int getControllerID(int index){
        return (int)controllable.get(index);
    }

    public void setControllerID(int index, int cameraID){
        this.controllable.set(index, cameraID);
    }

    public Controllable add(){
        super.setLastWriteIndex(this.controllable.add(lastID));
        lastID++;
        return this;
    }
}
