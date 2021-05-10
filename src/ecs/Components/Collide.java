package ecs.Components;

import ecs.Component;
import org.joml.Vector3f;
import org.lwjgl.system.CallbackI;
import util.Container;

public class Collide extends Component {
    private Container<Vector3f> dimensions;
    private Container<Vector3f> corners;

    public Collide(){
        corners = new Container<>(Vector3f.class);
        dimensions = new Container<>(Vector3f.class);
    }

    public Vector3f getDimensions(int index){
        return (Vector3f) dimensions.get(index);
    }

    public Vector3f getCorner(int index){
        return (Vector3f) corners.get(index);
    }

    public void setDimensions(int index, Vector3f dimensions){
        this.dimensions.set(index, dimensions);
    }

    public void setCorner(int index, Vector3f corner){
        this.corners.set(index, corner);
    }

    public Collide add(Vector3f corner, Vector3f dimensions){
        super.setLastWriteIndex(this.dimensions.add(dimensions));
        this.corners.add(corner);
        return this;
    }
}
