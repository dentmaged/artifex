#version 330

in vec3 s_normal;
in vec2 tc;
in vec4 clipSpace;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(bloom)
FS_OUT(godrays)

tex albedo;
tex specular;
tex metallic;
tex roughness;
tex ao;
tex depthMap;

uniform bool usesAOMap;
uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;
uniform mat4 inverseTransformationMatrix;

#include "util.glsl"
#include "material.glsl"

void main(void) {
	vec2 ndc = (clipSpace.xy / clipSpace.w) * 0.5 + 0.5;
	vec3 viewPosition = getPosition(ndc);
	vec3 worldPosition = (inverseViewMatrix * vec4(viewPosition, 1)).xyz;
	vec3 objectPosition = (inverseTransformationMatrix * vec4(worldPosition, 1)).xyz;
	vec3 clip = 0.5 - abs(objectPosition.xyz);
	if (clip.x < 0 || clip.y < 0 || clip.z < 0)
		discard;

	vec4 diffuse = texture2D(albedo, objectPosition.xz + 0.5);
	if (diffuse.w < 0.5)
		discard;

	float metallic = texture2D(metallic, tc).r;
	float roughness = texture2D(roughness, tc).r;
	float ao = usesAOMap ? texture2D(ao, tc).r : 1;

	emitDecal(diffuse, texture2D(specular, tc).r * diffuse.xyz, metallic, roughness, ao);
}
