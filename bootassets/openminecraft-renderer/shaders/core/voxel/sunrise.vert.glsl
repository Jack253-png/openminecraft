#version 330 core
#extension GL_ARB_separate_shader_objects : enable

#include "basics/vertexgen.glsl"

#include "basics/structs/camera.glsl"
uniform SunRiseData
{
    vec4 color;
    float sunAngle;
    float range;
    float height;
}
sun;
layout(location = 0) out vec4 discColor;

#define PI 3.1415926535897

void main()
{
    vec4 actualPos;
    int id = vertexgen_id();
    if (id % 3 == 0)
    {
        actualPos = vec4(0.0, 0.0, 0.0, 1.0);
        discColor = sun.color;
    }
    else if (id % 3 == 1)
    {
        actualPos = vec4(sin(PI / 4 * (id / 3)), 0.0, cos(PI / 4 * (id / 3)), 1.0);
        discColor = sun.color * vec4(vec3(1.0), 0.0);
    }
    else
    {
        actualPos = vec4(sin(PI / 4 * (id / 3 + 1)), 0.0, cos(PI / 4 * (id / 3 + 1)), 1.0);
        discColor = sun.color * vec4(vec3(1.0), 0.0);
    }

    actualPos *= sun.range;
    actualPos.y = sun.height;
    float ca = cos(sun.sunAngle);
    float sa = sin(sun.sunAngle);
    mat3 rotX = mat3(1.0, 0.0, 0.0, 0.0, ca, -sa, 0.0, sa, ca);
    actualPos.xyz = rotX * actualPos.xyz;
    gl_Position = camera.viewProj * actualPos;
    gl_Position.z = 0;
}
