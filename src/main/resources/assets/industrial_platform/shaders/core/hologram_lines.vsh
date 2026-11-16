#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:fog.glsl>
#include <minecraft:globals.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in vec2 UV3;
layout(location = 4) in float LineWidth;

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
layout(location = 2) out vec4 vertexColor;

void main() {
    // UV0/UV3 承载对端坐标和角点；0/1 为起点，2/3 为终点。
    vec3 otherPosition = vec3(UV0, UV3.x);
    bool atStart = UV3.y < 2.0;
    vec3 startPosition = atStart ? Position : otherPosition;
    vec3 endPosition = atStart ? otherPosition : Position;
    vec4 linePosStart = ProjMat * ModelViewMat * vec4(startPosition, 1.0);
    vec4 linePosEnd = ProjMat * ModelViewMat * vec4(endPosition, 1.0);
    vec4 vertexPosition = atStart ? linePosStart : linePosEnd;

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = Color;

    if (any(lessThanEqual(ScreenSize, vec2(0.0)))) {
        gl_Position = vertexPosition;
        return;
    }

    vec2 screenDirection = (linePosEnd.xy * linePosStart.w - linePosStart.xy * linePosEnd.w) * ScreenSize;
    float directionLengthSquared = dot(screenDirection, screenDirection);
    if (!(directionLengthSquared > 0.0) || isinf(directionLengthSquared)) {
        gl_Position = vertexPosition;
        return;
    }

    vec2 lineScreenDirection = screenDirection * inversesqrt(directionLengthSquared);
    vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * LineWidth / ScreenSize;
    float side = UV3.y == 0.0 || UV3.y == 3.0 ? 1.0 : -1.0;
    gl_Position = vec4(vertexPosition.xy + lineOffset * (side * vertexPosition.w), vertexPosition.zw);
}
