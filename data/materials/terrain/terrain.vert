uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat3 g_NormalMatrix;
uniform mat4 g_ViewMatrix;

uniform vec4 g_LightColor;
uniform vec4 g_LightPosition;

attribute vec3 inPosition;
attribute vec3 inNormal;
attribute vec2 inTexCoord;
attribute vec4 inTangent;

varying vec3 vNormal;
varying vec2 texCoord;
varying vec3 vPosition;
varying vec4 vLightDir;

varying vec4 DiffuseSum;


void main()
{

    vec4 pos = vec4(inPosition, 1.0);
    gl_Position = g_WorldViewProjectionMatrix * pos;
    texCoord = inTexCoord;

    vPosition = (g_WorldViewMatrix * pos).xyz;
    vNormal  = normalize(g_NormalMatrix * inNormal);

    vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz, g_LightColor.w));
    wvLightPos.w = g_LightPosition.w;

    float posLight = step(0.5, g_LightColor.w);
    vec3 tempVec = wvLightPos.xyz * sign(posLight - 0.5) - (vPosition * posLight);
    vLightDir = vec4(normalize(tempVec), 1.0);

    DiffuseSum  = g_LightColor;

}