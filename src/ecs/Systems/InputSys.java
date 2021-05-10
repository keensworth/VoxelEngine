package ecs.Systems;

import ecs.Components.*;
import ecs.Entity;
import ecs.System;
import static org.lwjgl.glfw.GLFW.*;

import graphic.Window;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFW.*;
import util.ComponentMask;
import util.Container;
import util.ETree.EntNode;

public class InputSys extends System {
    private Input input;
    private Window window;
    private Position position;
    private Entity camera;
    private GLFWCursorPosCallback cursor_position_callback;
    private GLFWMouseButtonCallback mouse_button_callback;
    private GLFWKeyCallback key_press_callback;

    private boolean holding = false;
    private int holdKey = 0;


    public InputSys() {
        super(Input.class);
    }

    @Override
    public Class update(float dt, EntNode entityTree, ComponentMask componentMask, boolean entityChange) {
        java.lang.System.out.println("Updating InputSys");

        updateValues(componentMask, entityTree);

        //Check for input
        glfwPollEvents();
        updateKeyPress(-1,-1);

        return null;
    }

    private void updateCursorPos(double xpos, double ypos){
        input.setMousePos(new float[]{(float) xpos, (float) ypos});
        java.lang.System.out.println(xpos + " " + ypos);
    }

    private void updateMouseClick(int button, int action){
        if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {   //left click, set clicked
            input.setClicked();
        }
        if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) {   //left click, set clicked
            input.setUnclicked();
        }
    }

    private void updateKeyPress(int button, int action){
        boolean pressed = action != GLFW_RELEASE;
        if (button == GLFW_KEY_LEFT_SHIFT){
            input.keyStateChange(0, pressed);
        } else if (button == GLFW_KEY_SPACE){
            input.keyStateChange(1, pressed);
        } else if (button == GLFW_KEY_A){
            input.keyStateChange(2, pressed);
        } else if (button == GLFW_KEY_D){
            input.keyStateChange(3, pressed);
        } else if (button == GLFW_KEY_W){
            input.keyStateChange(4, pressed);
        } else if (button == GLFW_KEY_S){
            input.keyStateChange(5, pressed);
        }else if (button == GLFW_KEY_E){
            input.keyStateChange(6, pressed);
        }

        if (button == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {  //escape, close window
            glfwSetWindowShouldClose(window.getWindowHandle(), true);
        }
    }

    private void updateValues(ComponentMask componentMask, EntNode entityTree){
        if (window==null){
            window = ecs.getWindow();
            glfwSetCursorPosCallback(window.getWindowHandle(), cursor_position_callback = new GLFWCursorPosCallback() {
                @Override
                public void invoke(long window, double xpos, double ypos) {
                    updateCursorPos(xpos, ecs.height-ypos); //(0,0) - bottom left of window
                }
            });

            glfwSetMouseButtonCallback(window.getWindowHandle(), mouse_button_callback = new GLFWMouseButtonCallback(){
                @Override
                public void invoke(long window, int button, int action, int mods) {
                    updateMouseClick(button, action);
                }
            });

            glfwSetKeyCallback(window.getWindowHandle(), key_press_callback = new GLFWKeyCallback(){
                @Override
                public void invoke(long window, int key, int scancode, int action, int mods) {
                    updateKeyPress(key, action);
                }
            });
        }
        input = (Input) componentMask.getComponent(Input.class);
        input.clickCoolDown -= 1;
        if (input.clickCoolDown < 0)
            input.clickCoolDown = 0;
        position = (Position) componentMask.getComponent(Position.class);
        Entity[] cameras = getEntities(entityTree, new Class[]{Camera.class});
        camera = cameras[0];
    }

    @Override
    public void exit() {

    }
}
