#version 330

in vec2 tc;

FS_OUT(colour)

tex depthMap;
tex normal;
tex noise;

uniform mat4 projectionMatrix;
uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;
uniform vec3 samples[64];
uniform vec2 noiseTextureScale;

uniform int kernelSize;
uniform float radius;
uniform float bias;

#include "util.glsl"

void main(void) {
	vec3 position = getPosition(tc);
	vec3 normal = normalize(texture2D(normal, tc).xyz);
	vec3 random = normalize(texture2D(noise, tc * noiseTextureScale).xyz);

	vec3 tangent = normalize(random - normal * dot(random, normal));
	vec3 bitangent = cross(normal, tangent);
	mat3 tbn = mat3(tangent, bitangent, normal);

	float occlusion = 0;
	for (int i = 0; i < kernelSize; i++) {
		vec3 sample = tbn * samples[i];
		sample = position + sample * radius;

		vec4 offset = projectionMatrix * vec4(sample, 1);
		offset.xy /= offset.w;
		offset.xy = offset.xy * 0.5 + 0.5;

		float sampleDepth = texture2D(depthMap, offset.xy).r;
		float rangeCheck = smoothstep(0, 1, radius / abs(position.z - sampleDepth));
		occlusion += (sampleDepth >= sample.z + bias ? rangeCheck : 0);
	}

	occlusion = 1 - occlusion / kernelSize;
	out_colour = vec4(occlusion, occlusion, occlusion, 1);
}
