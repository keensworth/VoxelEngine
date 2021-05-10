#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;
layout (location=2) in vec3 offset;
layout (location=3) in vec3 color;
layout (location=4) in float specular;
layout (location=5) in float diffuse;

out vec3 surfacePos;
out vec3 outNormal;
out vec3 outOffset;
out vec3 outColor;
out float outSpecular;
out float outDiffuse;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

void main()
{
    mat4 tempModelMatrix = modelMatrix + mat4(vec4(0.0f), vec4(0.0f), vec4(0.0f), vec4(offset, 0));
    gl_Position = projectionMatrix * viewMatrix * tempModelMatrix * vec4(position, 1.0);
    surfacePos = vec3(modelMatrix * vec4(position,1.0f));
    outNormal = normal;
    outOffset = offset;
    outColor = color;
    outSpecular = specular;
    outDiffuse = diffuse;
}