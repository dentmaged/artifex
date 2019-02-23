#version 330

in vec2 tc;

FS_OUT(colour)

tex depthMap;
tex shadowMap;
tex exposure;

uniform mat4 toShadowMapSpace;
uniform mat4 inverseProjectionMatrix;

uniform vec3 lightDirection;
uniform vec4 lightPosition;
uniform vec3 lightAttenuation;
uniform vec3 lightColour;
uniform vec2 lightCutoff;

const float PI = 3.14159265358979;
const float G_SCATTERING = 0.0924;
const float steps = 16;

const float ditherPattern[16] = float[](
	0.0f, 0.5f, 0.125f, 0.625f,
	0.75f, 0.22f, 0.875f, 0.375f,
	0.1875f, 0.6875f, 0.0625f, 0.5625,
	0.9375f, 0.4375f, 0.8125f, 0.3125
);

#include "util.glsl"

void main(void) {
	vec3 viewPosition = getPosition(tc);

	float distance = length(viewPosition);
	if (distance > 1000)
		discard;

	vec3 unitDirection = viewPosition / distance;

	float stepLength = distance / steps;
	vec3 step = unitDirection * stepLength;

	vec3 position = vec3(step * ditherPattern[int(16 * fract(sin(dot(vec4(floor(viewPosition * 100), 0), vec4(12.9898, 78.233, 45.164, 94.673))) * 43758.5453)) % 16]);
	vec3 fog = vec3(0.0);
	for (int i = 0; i < steps; i++) {
		if (lightPosition.w == 1) {
			vec4 shadowCoords = toShadowMapSpace * vec4(position, 1.0);
			if (shadowCoords.z < texture2D(shadowMap, shadowCoords.xy).x) {
				float result = 1.0 - G_SCATTERING * G_SCATTERING;
				result /= (4.0 * PI * pow(1.0 + G_SCATTERING * G_SCATTERING - (2.0 * G_SCATTERING) * dot(unitDirection, lightDirection), 1.5));

				fog += result * lightColour * 0.25;
			}
		} else if (lightPosition.w == 2) {
			vec3 L = lightPosition.xyz - position;
			float currentDistance = length(L);
			L /= currentDistance;

			float intensity = clamp((dot(L, normalize(-lightDirection)) - lightCutoff.y) / (lightCutoff.x - lightCutoff.y), 0, 1);
			float attenuation = 1.0 / (lightAttenuation.x + (lightAttenuation.y * currentDistance) + (lightAttenuation.z * currentDistance * currentDistance));

			float result = 1.0 - G_SCATTERING * G_SCATTERING;
			result /= (4.0 * PI * pow(1.0 + G_SCATTERING * G_SCATTERING - (2.0 * G_SCATTERING) * dot(unitDirection, lightDirection), 1.5));

			fog += result * intensity * attenuation * lightColour;
		}

		position += step;
	}

	fog /= steps;
	out_colour = vec4(texture2D(exposure, vec2(0.5)).x * fog, 1.0);
}
