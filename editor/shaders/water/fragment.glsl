#version 330

in vec3 viewPosition;
in vec4 clipSpace;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(albedo)

tex scene;
tex depthMap;
tex dudv;
tex normal;

uniform mat4 inverseViewMatrix;
uniform mat4 normalMatrix;

uniform vec3 colour;
uniform float moveFactor;
uniform float clarity;

uniform float near;
uniform float far;

const float waveStrength = 0.04;

#include "shadows.glsl"
#include "lighting.glsl"
#include "material.glsl"

void main(void) {
	vec2 ndc = (clipSpace.xy / clipSpace.w) * 0.5 + 0.5;
	float floorDistance = 2.0 * near * far / (far + near - (2.0 * texture2D(depthMap, ndc).x - 1.0) * (far - near));
	float surfaceDistance = 2.0 * near * far / (far + near - (2.0 * gl_FragCoord.z - 1) * (far - near));
	float waterDepth = floorDistance - surfaceDistance;
	float depthReduction = clamp(waterDepth * clarity, 0.3, 1.0);

	vec2 distortedTexCoords = texture2D(dudv, vec2(tc.x + moveFactor, tc.y)).xy * 0.1;
	distortedTexCoords = tc + vec2(distortedTexCoords.x, distortedTexCoords.y + moveFactor);
	vec2 totalDistortion = (texture2D(dudv, distortedTexCoords).xy * 2.0 - 1.0) * waveStrength * depthReduction;

	vec4 normalMapColour = texture2D(normal, distortedTexCoords);
	vec3 localNormal = vec3(normalMapColour.r * 2.0 - 1.0, normalMapColour.b * 3.0, normalMapColour.g * 2.0 - 1.0);
	vec3 s_normal = normalize(mat3(normalMatrix) * localNormal);

	emit(vec4(colour, depthReduction), s_normal, 0.0, 0.0, 0.1, 1.0);
}
