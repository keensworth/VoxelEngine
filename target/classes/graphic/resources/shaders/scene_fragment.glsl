#version 330

in vec3 surfacePos;
in vec3 outNormal;
in vec3 outOffset;
in vec3 outColor;
in float outSpecular;
in float outDiffuse;

out vec4 fragColor;

uniform vec3 cameraPos;
uniform vec3[15] pointLights;
uniform vec3[15] spotLights;

vec3 CalculateLight(vec3 lightPos, bool isPointLight){
    vec3 lightDir = normalize(lightPos-(surfacePos+outOffset));
    float distanceToLight = distance(lightPos, (surfacePos+outOffset));

    vec3 norm = outNormal;
    float diff = max(dot(norm, lightDir), 0.0)*outDiffuse*12;
    vec3 diffuse = (diff * vec3(1,1,1)) / (distanceToLight*2+0.001);

    return (diffuse) * outColor;
}

void main() {
    float gamma = 1.6;
    float ambientStrength = 0.2f;
    vec3 result = vec3(0,0,0);

    for (int i = 0; i < pointLights.length; i++){
        vec3 lightPos = pointLights[i];
        if(lightPos != vec3(0,0,0)){
            result += CalculateLight(lightPos, true);
        }
    }

    for (int i = 0; i < spotLights.length; i++){
        vec3 lightPos = spotLights[i];
        if(lightPos != vec3(0,0,0)){
            result += CalculateLight(lightPos, false);
        }
    }

    result += ambientStrength*outColor;

    result = pow(result, vec3(1 / gamma));
    fragColor = vec4(result,1f);
    //fragColor = vec4(1,0,1,1);
}